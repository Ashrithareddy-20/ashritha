package com.planora.controller;

import com.planora.dto.EventRequestDto;
import com.planora.dto.EventResponseDto;
import com.planora.dto.VendorRecommendationDto;
import com.planora.security.UserPrincipal;
import com.planora.service.EventService;
import com.planora.service.VendorRecommendationService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Endpoints for managing events and retrieving vendor recommendations")
public class EventController {

    private final EventService eventService;
    private final VendorRecommendationService vendorRecommendationService;

    public EventController(EventService eventService, VendorRecommendationService vendorRecommendationService) {
        this.eventService = eventService;
        this.vendorRecommendationService = vendorRecommendationService;
    }

    @GetMapping
    @Operation(summary = "Get all events", description = "Public endpoint returning all events in the system")
    @ApiResponse(responseCode = "200", description = "List of all events")
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        List<EventResponseDto> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/my-events")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user's events", description = "Returns events created by the currently authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of events created by current user"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<List<EventResponseDto>> getMyEvents(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<EventResponseDto> events = eventService.getMyEvents(principal);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}/recommendations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get vendor recommendations for an event", description = "Calculates ranked recommendations based on type, location, budget, and rating")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranked list of recommended vendors"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden if event belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<List<VendorRecommendationDto>> getRecommendations(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<VendorRecommendationDto> recommendations =
                vendorRecommendationService.getRecommendationsForEvent(eventId, principal);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Public endpoint retrieving details of a single event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event details found"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        EventResponseDto event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create an event", description = "Creates a new event associated with the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation failed for event request"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<EventResponseDto> createEvent(
            @Valid @RequestBody EventRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        EventResponseDto createdEvent = eventService.createEvent(requestDto, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@eventSecurity.canModifyEvent(#id, authentication)")
    @Operation(summary = "Update an event", description = "Updates an existing event. Only the owner or an ADMIN may update.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden if not owner or ADMIN"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        EventResponseDto updatedEvent = eventService.updateEvent(id, requestDto, principal);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@eventSecurity.canModifyEvent(#id, authentication)")
    @Operation(summary = "Delete an event", description = "Deletes an existing event. Only the owner or an ADMIN may delete.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Event successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden if not owner or ADMIN"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        eventService.deleteEvent(id, principal);
        return ResponseEntity.noContent().build();
    }
}
