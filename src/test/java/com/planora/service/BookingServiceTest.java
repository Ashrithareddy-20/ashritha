package com.planora.service;

import com.planora.dto.BookingRequestDto;
import com.planora.dto.BookingResponseDto;
import com.planora.dto.BookingStatusUpdateDto;
import com.planora.entity.Booking;
import com.planora.entity.BookingStatus;
import com.planora.entity.Event;
import com.planora.entity.Role;
import com.planora.entity.User;
import com.planora.entity.Vendor;
import com.planora.exception.DuplicateBookingException;
import com.planora.exception.ResourceNotFoundException;
import com.planora.repository.BookingRepository;
import com.planora.repository.EventRepository;
import com.planora.repository.VendorRepository;
import com.planora.security.UserPrincipal;
import com.planora.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VendorRepository vendorRepository;

    private BookingService bookingService;

    private UserPrincipal ownerPrincipal;
    private UserPrincipal otherPrincipal;
    private UserPrincipal adminPrincipal;

    private User ownerUser;
    private Event event;
    private Vendor vendor;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, eventRepository, vendorRepository);

        ownerPrincipal = new UserPrincipal(
                1L, "owner@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        otherPrincipal = new UserPrincipal(
                2L, "other@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        adminPrincipal = new UserPrincipal(
                99L, "admin@planora.com", "hash", Role.ADMIN,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        ownerUser = new User(1L, "Owner", "owner@planora.com", "hash", Role.USER);

        event = new Event(
                10L, "Annual Gala", "Conference", "Convention Center",
                LocalDate.of(2026, 12, 1), 250, 45000.0
        );
        event.setCreatedBy(ownerUser);

        vendor = new Vendor(
                20L, "Royal Catering", "Catering", "Hyderabad", "info@royal.com", 25000.0, 4.8
        );
    }

    @Test
    void createBooking_Success() {
        BookingRequestDto requestDto = new BookingRequestDto(20L, "Please prepare vegetarian options");
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(vendorRepository.findById(20L)).thenReturn(Optional.of(vendor));
        when(bookingRepository.existsByEvent_IdAndVendor_IdAndStatusIn(eq(10L), eq(20L), any())).thenReturn(false);

        Booking savedBooking = new Booking(100L, event, vendor, BookingStatus.PENDING, "Please prepare vegetarian options");
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponseDto response = bookingService.createBooking(10L, requestDto, ownerPrincipal);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getEventId());
        assertEquals(20L, response.getVendorId());
        assertEquals("Royal Catering", response.getVendorName());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenDuplicateActive_ShouldThrowDuplicateBookingException() {
        BookingRequestDto requestDto = new BookingRequestDto(20L, "Duplicate");
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(vendorRepository.findById(20L)).thenReturn(Optional.of(vendor));
        when(bookingRepository.existsByEvent_IdAndVendor_IdAndStatusIn(eq(10L), eq(20L), any())).thenReturn(true);

        assertThrows(DuplicateBookingException.class,
                () -> bookingService.createBooking(10L, requestDto, ownerPrincipal));
    }

    @Test
    void createBooking_WhenOtherUserAttempts_ShouldThrowAccessDeniedException() {
        BookingRequestDto requestDto = new BookingRequestDto(20L, "Unauthorized");
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(AccessDeniedException.class,
                () -> bookingService.createBooking(10L, requestDto, otherPrincipal));
    }

    @Test
    void createBooking_WhenEventNotFound_ShouldThrowResourceNotFoundException() {
        BookingRequestDto requestDto = new BookingRequestDto(20L, "Notes");
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(999L, requestDto, ownerPrincipal));
    }

    @Test
    void createBooking_WhenVendorNotFound_ShouldThrowResourceNotFoundException() {
        BookingRequestDto requestDto = new BookingRequestDto(999L, "Notes");
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(vendorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(10L, requestDto, ownerPrincipal));
    }

    @Test
    void getBookingsByEvent_Success() {
        Booking b = new Booking(100L, event, vendor, BookingStatus.PENDING, "Notes");
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(bookingRepository.findByEvent_Id(10L)).thenReturn(List.of(b));

        List<BookingResponseDto> list = bookingService.getBookingsByEvent(10L, ownerPrincipal);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(100L, list.get(0).getId());
    }

    @Test
    void getBookingsByEvent_WhenOtherUser_ShouldThrowAccessDenied() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(AccessDeniedException.class,
                () -> bookingService.getBookingsByEvent(10L, otherPrincipal));
    }

    @Test
    void getBookingById_Success() {
        Booking b = new Booking(100L, event, vendor, BookingStatus.CONFIRMED, "Notes");
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));

        BookingResponseDto response = bookingService.getBookingById(100L, ownerPrincipal);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void updateBookingStatus_Success() {
        Booking b = new Booking(100L, event, vendor, BookingStatus.PENDING, "Notes");
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDto response = bookingService.updateBookingStatus(
                100L, new BookingStatusUpdateDto(BookingStatus.CONFIRMED), ownerPrincipal
        );

        assertNotNull(response);
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        verify(bookingRepository).save(b);
    }

    @Test
    void updateBookingStatus_WhenAdmin_ShouldAllowOverride() {
        Booking b = new Booking(100L, event, vendor, BookingStatus.PENDING, "Notes");
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDto response = bookingService.updateBookingStatus(
                100L, new BookingStatusUpdateDto(BookingStatus.CONFIRMED), adminPrincipal
        );

        assertNotNull(response);
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
    }

    @Test
    void deleteBooking_Success() {
        Booking b = new Booking(100L, event, vendor, BookingStatus.PENDING, "Notes");
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));

        bookingService.deleteBooking(100L, ownerPrincipal);

        verify(bookingRepository).delete(b);
    }

    @Test
    void deleteBooking_WhenOtherUser_ShouldThrowAccessDenied() {
        Booking b = new Booking(100L, event, vendor, BookingStatus.PENDING, "Notes");
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(b));

        assertThrows(AccessDeniedException.class,
                () -> bookingService.deleteBooking(100L, otherPrincipal));
    }
}
