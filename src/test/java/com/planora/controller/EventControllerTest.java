package com.planora.controller;

import com.planora.dto.EventRequestDto;
import com.planora.dto.EventResponseDto;
import com.planora.entity.Role;
import com.planora.security.UserPrincipal;
import com.planora.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private com.planora.service.VendorRecommendationService vendorRecommendationService;

    private EventController eventController;

    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        eventController = new EventController(eventService, vendorRecommendationService);
        testPrincipal = new UserPrincipal(
                1L, "user@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void getAllEvents_ShouldReturn200AndEventList() {
        EventResponseDto eventDto = new EventResponseDto(
                1L, "Annual Gala", "Conference", "Convention Center",
                LocalDate.of(2026, 12, 1), 250, 45000.0
        );
        when(eventService.getAllEvents()).thenReturn(List.of(eventDto));

        ResponseEntity<List<EventResponseDto>> response = eventController.getAllEvents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Annual Gala", response.getBody().get(0).getEventName());
        verify(eventService).getAllEvents();
    }

    @Test
    void getEventById_ShouldReturn200AndEvent() {
        EventResponseDto eventDto = new EventResponseDto(
                1L, "Birthday Bash", "Birthday", "Grand Hall",
                LocalDate.of(2026, 11, 15), 50, 5000.0
        );
        when(eventService.getEventById(1L)).thenReturn(eventDto);

        ResponseEntity<EventResponseDto> response = eventController.getEventById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Birthday Bash", response.getBody().getEventName());
        verify(eventService).getEventById(1L);
    }

    @Test
    void createEvent_ShouldReturn201AndCreatedEvent() {
        EventRequestDto requestDto = new EventRequestDto(
                "Wedding Celebration", "Wedding", "Seaside Resort",
                LocalDate.of(2026, 10, 30), 200, 30000.0
        );
        EventResponseDto responseDto = new EventResponseDto(
                1L, "Wedding Celebration", "Wedding", "Seaside Resort",
                LocalDate.of(2026, 10, 30), 200, 30000.0, 1L
        );
        when(eventService.createEvent(any(EventRequestDto.class), any())).thenReturn(responseDto);

        ResponseEntity<EventResponseDto> response = eventController.createEvent(requestDto, testPrincipal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Wedding Celebration", response.getBody().getEventName());
        verify(eventService).createEvent(requestDto, testPrincipal);
    }

    @Test
    void updateEvent_ShouldReturn200AndUpdatedEvent() {
        EventRequestDto requestDto = new EventRequestDto(
                "Updated Gala", "Conference", "Grand Ballroom",
                LocalDate.of(2026, 12, 5), 300, 55000.0
        );
        EventResponseDto responseDto = new EventResponseDto(
                1L, "Updated Gala", "Conference", "Grand Ballroom",
                LocalDate.of(2026, 12, 5), 300, 55000.0, 1L
        );
        when(eventService.updateEvent(eq(1L), any(EventRequestDto.class), any())).thenReturn(responseDto);

        ResponseEntity<EventResponseDto> response = eventController.updateEvent(1L, requestDto, testPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Gala", response.getBody().getEventName());
        verify(eventService).updateEvent(1L, requestDto, testPrincipal);
    }

    @Test
    void deleteEvent_ShouldReturn204NoContent() {
        ResponseEntity<Void> response = eventController.deleteEvent(1L, testPrincipal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(eventService).deleteEvent(1L, testPrincipal);
    }

    @Test
    void getMyEvents_ShouldReturn200AndEventList() {
        EventResponseDto eventDto = new EventResponseDto(
                1L, "My Personal Event", "Party", "Beach House",
                LocalDate.of(2026, 12, 1), 30, 2000.0, 1L
        );
        when(eventService.getMyEvents(testPrincipal)).thenReturn(List.of(eventDto));

        ResponseEntity<List<EventResponseDto>> response = eventController.getMyEvents(testPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("My Personal Event", response.getBody().get(0).getEventName());
        verify(eventService).getMyEvents(testPrincipal);
    }

    @Test
    void getRecommendations_ShouldReturn200AndRecommendations() {
        com.planora.dto.VendorRecommendationDto recDto = new com.planora.dto.VendorRecommendationDto(
                20L, "Royal Catering", "Catering", "Hyderabad", "info@royal.com", 25000.0, 4.8, 85.0, "High rating"
        );
        when(vendorRecommendationService.getRecommendationsForEvent(10L, testPrincipal))
                .thenReturn(List.of(recDto));

        ResponseEntity<List<com.planora.dto.VendorRecommendationDto>> response =
                eventController.getRecommendations(10L, testPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Royal Catering", response.getBody().get(0).getVendorName());
        assertEquals(85.0, response.getBody().get(0).getMatchScore());
        verify(vendorRecommendationService).getRecommendationsForEvent(10L, testPrincipal);
    }
}
