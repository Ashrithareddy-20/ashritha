package com.planora.service;

import com.planora.dto.EventRequestDto;
import com.planora.dto.EventResponseDto;
import com.planora.entity.Event;
import com.planora.entity.Role;
import com.planora.entity.User;
import com.planora.exception.ResourceNotFoundException;
import com.planora.repository.EventRepository;
import com.planora.repository.UserRepository;
import com.planora.security.UserPrincipal;
import com.planora.service.impl.EventServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    private EventService eventService;

    private UserPrincipal ownerPrincipal;
    private UserPrincipal otherPrincipal;
    private UserPrincipal adminPrincipal;
    private User ownerUser;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository, userRepository);

        ownerPrincipal = new UserPrincipal(
                1L, "owner@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        otherPrincipal = new UserPrincipal(
                2L, "other@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        adminPrincipal = new UserPrincipal(
                3L, "admin@planora.com", "hash", Role.ADMIN,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        ownerUser = new User(1L, "Owner User", "owner@planora.com", "hash", Role.USER);
    }

    @Test
    void getAllEvents_ShouldReturnMappedDtos() {
        Event event = new Event(
                1L, "Corporate Meet", "Corporate", "Boardroom",
                LocalDate.of(2026, 12, 10), 25, 2000.0
        );
        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<EventResponseDto> result = eventService.getAllEvents();

        assertEquals(1, result.size());
        assertEquals("Corporate Meet", result.get(0).getEventName());
        verify(eventRepository).findAll();
    }

    @Test
    void getEventById_WhenExists_ShouldReturnDto() {
        Event event = new Event(
                1L, "Corporate Meet", "Corporate", "Boardroom",
                LocalDate.of(2026, 12, 10), 25, 2000.0
        );
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventResponseDto result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Corporate Meet", result.getEventName());
        verify(eventRepository).findById(1L);
    }

    @Test
    void getEventById_WhenNotFound_ShouldThrowException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> eventService.getEventById(99L)
        );

        assertTrue(exception.getMessage().contains("Event not found with id: 99"));
        verify(eventRepository).findById(99L);
    }

    @Test
    void createEvent_ShouldSaveAndSetCreatedBy() {
        EventRequestDto requestDto = new EventRequestDto(
                "Charity Ball", "Fundraiser", "Grand Hall",
                LocalDate.of(2026, 11, 20), 100, 15000.0
        );

        Event savedEvent = new Event(
                1L, "Charity Ball", "Fundraiser", "Grand Hall",
                LocalDate.of(2026, 11, 20), 100, 15000.0
        );
        savedEvent.setCreatedBy(ownerUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        EventResponseDto result = eventService.createEvent(requestDto, ownerPrincipal);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Charity Ball", result.getEventName());
        assertEquals(1L, result.getCreatedByUserId());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_WhenOwner_ShouldUpdate() {
        Event existingEvent = new Event(
                1L, "Old Title", "Party", "Room A",
                LocalDate.of(2026, 11, 20), 50, 5000.0
        );
        existingEvent.setCreatedBy(ownerUser);

        EventRequestDto updateDto = new EventRequestDto(
                "New Title", "Conference", "Room B",
                LocalDate.of(2026, 12, 1), 100, 10000.0
        );
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponseDto result = eventService.updateEvent(1L, updateDto, ownerPrincipal);

        assertNotNull(result);
        assertEquals("New Title", result.getEventName());
        verify(eventRepository).findById(1L);
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void updateEvent_WhenOtherUser_ShouldThrowAccessDeniedException() {
        Event existingEvent = new Event(
                1L, "Old Title", "Party", "Room A",
                LocalDate.of(2026, 11, 20), 50, 5000.0
        );
        existingEvent.setCreatedBy(ownerUser);

        EventRequestDto updateDto = new EventRequestDto(
                "Hacked Title", "Conference", "Room B",
                LocalDate.of(2026, 12, 1), 100, 10000.0
        );
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

        assertThrows(AccessDeniedException.class, () -> eventService.updateEvent(1L, updateDto, otherPrincipal));
    }

    @Test
    void updateEvent_WhenAdmin_ShouldAllowUpdate() {
        Event existingEvent = new Event(
                1L, "Old Title", "Party", "Room A",
                LocalDate.of(2026, 11, 20), 50, 5000.0
        );
        existingEvent.setCreatedBy(ownerUser);

        EventRequestDto updateDto = new EventRequestDto(
                "Admin Override", "Conference", "Room B",
                LocalDate.of(2026, 12, 1), 100, 10000.0
        );
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponseDto result = eventService.updateEvent(1L, updateDto, adminPrincipal);

        assertNotNull(result);
        assertEquals("Admin Override", result.getEventName());
    }

    @Test
    void updateEvent_WhenNotFound_ShouldThrowException() {
        EventRequestDto updateDto = new EventRequestDto(
                "New Title", "Conference", "Room B",
                LocalDate.of(2026, 12, 1), 100, 10000.0
        );
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(99L, updateDto, ownerPrincipal));
        verify(eventRepository).findById(99L);
    }

    @Test
    void deleteEvent_WhenOwner_ShouldDelete() {
        Event existingEvent = new Event(
                1L, "To Delete", "Party", "Room A",
                LocalDate.of(2026, 11, 20), 50, 5000.0
        );
        existingEvent.setCreatedBy(ownerUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

        eventService.deleteEvent(1L, ownerPrincipal);

        verify(eventRepository).findById(1L);
        verify(eventRepository).delete(existingEvent);
    }

    @Test
    void deleteEvent_WhenOtherUser_ShouldThrowAccessDeniedException() {
        Event existingEvent = new Event(
                1L, "To Delete", "Party", "Room A",
                LocalDate.of(2026, 11, 20), 50, 5000.0
        );
        existingEvent.setCreatedBy(ownerUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));

        assertThrows(AccessDeniedException.class, () -> eventService.deleteEvent(1L, otherPrincipal));
    }

    @Test
    void deleteEvent_WhenNotFound_ShouldThrowException() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.deleteEvent(99L, ownerPrincipal));
        verify(eventRepository).findById(99L);
    }

    @Test
    void getMyEvents_WhenUserHasEvents_ShouldReturnOnlyUserEvents() {
        Event event = new Event(
                1L, "Personal Meetup", "Meetup", "Cafe",
                LocalDate.of(2026, 12, 10), 10, 500.0
        );
        event.setCreatedBy(ownerUser);
        when(eventRepository.findByCreatedBy_Id(1L)).thenReturn(List.of(event));

        List<EventResponseDto> result = eventService.getMyEvents(ownerPrincipal);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Personal Meetup", result.get(0).getEventName());
        assertEquals(1L, result.get(0).getCreatedByUserId());
        verify(eventRepository).findByCreatedBy_Id(1L);
    }

    @Test
    void getMyEvents_WhenUserHasNoEvents_ShouldReturnEmptyList() {
        when(eventRepository.findByCreatedBy_Id(1L)).thenReturn(List.of());

        List<EventResponseDto> result = eventService.getMyEvents(ownerPrincipal);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventRepository).findByCreatedBy_Id(1L);
    }
}
