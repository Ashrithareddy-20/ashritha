package com.planora.controller;

import com.planora.dto.BookingRequestDto;
import com.planora.dto.BookingResponseDto;
import com.planora.dto.BookingStatusUpdateDto;
import com.planora.entity.BookingStatus;
import com.planora.entity.Role;
import com.planora.security.UserPrincipal;
import com.planora.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    private BookingController bookingController;

    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        bookingController = new BookingController(bookingService);
        testPrincipal = new UserPrincipal(
                1L, "user@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void createBooking_ShouldReturn201() {
        BookingRequestDto requestDto = new BookingRequestDto(20L, "Sample note");
        BookingResponseDto responseDto = new BookingResponseDto(
                100L, 10L, "Event A", 20L, "Vendor B", "Catering", 20000.0,
                BookingStatus.PENDING, "Sample note", LocalDateTime.now(), null
        );

        when(bookingService.createBooking(eq(10L), any(BookingRequestDto.class), eq(testPrincipal)))
                .thenReturn(responseDto);

        ResponseEntity<BookingResponseDto> response = bookingController.createBooking(10L, requestDto, testPrincipal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getId());
        assertEquals(BookingStatus.PENDING, response.getBody().getStatus());
        verify(bookingService).createBooking(10L, requestDto, testPrincipal);
    }

    @Test
    void getBookingsByEvent_ShouldReturn200() {
        BookingResponseDto responseDto = new BookingResponseDto(
                100L, 10L, "Event A", 20L, "Vendor B", "Catering", 20000.0,
                BookingStatus.PENDING, "Sample note", LocalDateTime.now(), null
        );

        when(bookingService.getBookingsByEvent(10L, testPrincipal))
                .thenReturn(List.of(responseDto));

        ResponseEntity<List<BookingResponseDto>> response = bookingController.getBookingsByEvent(10L, testPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(bookingService).getBookingsByEvent(10L, testPrincipal);
    }

    @Test
    void getBookingById_ShouldReturn200() {
        BookingResponseDto responseDto = new BookingResponseDto(
                100L, 10L, "Event A", 20L, "Vendor B", "Catering", 20000.0,
                BookingStatus.CONFIRMED, "Sample note", LocalDateTime.now(), null
        );

        when(bookingService.getBookingById(100L, testPrincipal)).thenReturn(responseDto);

        ResponseEntity<BookingResponseDto> response = bookingController.getBookingById(100L, testPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getId());
        verify(bookingService).getBookingById(100L, testPrincipal);
    }

    @Test
    void updateBookingStatus_ShouldReturn200() {
        BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(BookingStatus.CONFIRMED);
        BookingResponseDto responseDto = new BookingResponseDto(
                100L, 10L, "Event A", 20L, "Vendor B", "Catering", 20000.0,
                BookingStatus.CONFIRMED, "Sample note", LocalDateTime.now(), LocalDateTime.now()
        );

        when(bookingService.updateBookingStatus(100L, updateDto, testPrincipal)).thenReturn(responseDto);

        ResponseEntity<BookingResponseDto> response = bookingController.updateBookingStatus(100L, updateDto, testPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BookingStatus.CONFIRMED, response.getBody().getStatus());
        verify(bookingService).updateBookingStatus(100L, updateDto, testPrincipal);
    }

    @Test
    void deleteBooking_ShouldReturn204() {
        ResponseEntity<Void> response = bookingController.deleteBooking(100L, testPrincipal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(bookingService).deleteBooking(100L, testPrincipal);
    }
}
