package com.planora.controller;

import com.planora.dto.BookingRequestDto;
import com.planora.dto.BookingResponseDto;
import com.planora.dto.BookingStatusUpdateDto;
import com.planora.security.UserPrincipal;
import com.planora.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Bookings", description = "Endpoints for booking vendors for events")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/api/events/{eventId}/bookings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a booking", description = "Books a vendor for a specific event owned by the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden if not owner of the event"),
            @ApiResponse(responseCode = "404", description = "Event or Vendor not found"),
            @ApiResponse(responseCode = "409", description = "Vendor already booked for this event")
    })
    public ResponseEntity<BookingResponseDto> createBooking(
            @PathVariable Long eventId,
            @Valid @RequestBody BookingRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        BookingResponseDto response = bookingService.createBooking(eventId, requestDto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/events/{eventId}/bookings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List bookings for an event", description = "Retrieves all bookings made for the specified event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of bookings"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden if not owner of the event"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<List<BookingResponseDto>> getBookingsByEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<BookingResponseDto> bookings = bookingService.getBookingsByEvent(eventId, principal);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/api/bookings/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get booking by ID", description = "Retrieves details of a specific booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking details"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden if not owner of the event"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingResponseDto> getBookingById(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        BookingResponseDto response = bookingService.getBookingById(bookingId, principal);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/bookings/{bookingId}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update booking status", description = "Updates status of a booking (e.g. PENDING, CONFIRMED, CANCELLED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden if not owner of the event"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingResponseDto> updateBookingStatus(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingStatusUpdateDto statusDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        BookingResponseDto response = bookingService.updateBookingStatus(bookingId, statusDto, principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/bookings/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a booking", description = "Deletes or cancels a booking")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Booking deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden if not owner of the event"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        bookingService.deleteBooking(bookingId, principal);
        return ResponseEntity.noContent().build();
    }
}
