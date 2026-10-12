package com.planora.service.impl;

import com.planora.dto.BookingRequestDto;
import com.planora.dto.BookingResponseDto;
import com.planora.dto.BookingStatusUpdateDto;
import com.planora.entity.Booking;
import com.planora.entity.BookingStatus;
import com.planora.entity.Event;
import com.planora.entity.Role;
import com.planora.entity.Vendor;
import com.planora.exception.DuplicateBookingException;
import com.planora.exception.ResourceNotFoundException;
import com.planora.repository.BookingRepository;
import com.planora.repository.EventRepository;
import com.planora.repository.VendorRepository;
import com.planora.security.UserPrincipal;
import com.planora.service.BookingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final VendorRepository vendorRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              EventRepository eventRepository,
                              VendorRepository vendorRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.vendorRepository = vendorRepository;
    }

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long eventId, BookingRequestDto requestDto, UserPrincipal principal) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        validateEventOwnershipOrAdmin(event, principal);

        Vendor vendor = vendorRepository.findById(requestDto.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + requestDto.getVendorId()));

        boolean activeExists = bookingRepository.existsByEvent_IdAndVendor_IdAndStatusIn(
                eventId,
                vendor.getId(),
                Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );

        if (activeExists) {
            throw new DuplicateBookingException("An active booking for vendor '" + vendor.getVendorName() + "' already exists for this event");
        }

        Booking booking = new Booking();
        booking.setEvent(event);
        booking.setVendor(vendor);
        booking.setStatus(BookingStatus.PENDING);
        booking.setNotes(requestDto.getNotes());

        Booking saved = bookingRepository.save(booking);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByEvent(Long eventId, UserPrincipal principal) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        validateEventOwnershipOrAdmin(event, principal);

        return bookingRepository.findByEvent_Id(eventId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long bookingId, UserPrincipal principal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        validateEventOwnershipOrAdmin(booking.getEvent(), principal);

        return mapToResponseDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long bookingId, BookingStatusUpdateDto statusDto, UserPrincipal principal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        validateEventOwnershipOrAdmin(booking.getEvent(), principal);

        booking.setStatus(statusDto.getStatus());
        Booking updated = bookingRepository.save(booking);
        return mapToResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteBooking(Long bookingId, UserPrincipal principal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        validateEventOwnershipOrAdmin(booking.getEvent(), principal);

        bookingRepository.delete(booking);
    }

    private void validateEventOwnershipOrAdmin(Event event, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to access booking");
        }
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        if (event.getCreatedBy() != null && !event.getCreatedBy().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You are not authorized to access bookings for this event");
        }
    }

    private BookingResponseDto mapToResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getEvent().getEventName(),
                booking.getVendor().getId(),
                booking.getVendor().getVendorName(),
                booking.getVendor().getServiceCategory(),
                booking.getVendor().getPrice(),
                booking.getStatus(),
                booking.getNotes(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
