package com.planora.service;

import com.planora.dto.BookingRequestDto;
import com.planora.dto.BookingResponseDto;
import com.planora.dto.BookingStatusUpdateDto;
import com.planora.security.UserPrincipal;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(Long eventId, BookingRequestDto requestDto, UserPrincipal principal);

    List<BookingResponseDto> getBookingsByEvent(Long eventId, UserPrincipal principal);

    BookingResponseDto getBookingById(Long bookingId, UserPrincipal principal);

    BookingResponseDto updateBookingStatus(Long bookingId, BookingStatusUpdateDto statusDto, UserPrincipal principal);

    void deleteBooking(Long bookingId, UserPrincipal principal);
}
