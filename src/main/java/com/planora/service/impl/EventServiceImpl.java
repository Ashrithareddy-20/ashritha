package com.planora.service.impl;

import com.planora.dto.EventRequestDto;
import com.planora.dto.EventResponseDto;
import com.planora.entity.Event;
import com.planora.entity.Role;
import com.planora.entity.User;
import com.planora.exception.ResourceNotFoundException;
import com.planora.repository.EventRepository;
import com.planora.repository.UserRepository;
import com.planora.security.UserPrincipal;
import com.planora.service.EventService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponseDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return mapToResponseDto(event);
    }

    @Override
    public EventResponseDto createEvent(EventRequestDto requestDto, UserPrincipal principal) {
        Event event = new Event();
        event.setEventName(requestDto.getEventName());
        event.setEventType(requestDto.getEventType());
        event.setLocation(requestDto.getLocation());
        event.setEventDate(requestDto.getEventDate());
        event.setGuestCount(requestDto.getGuestCount());
        event.setBudget(requestDto.getBudget());

        if (principal != null) {
            User user = userRepository.findById(principal.getId()).orElse(null);
            event.setCreatedBy(user);
        }

        Event savedEvent = eventRepository.save(event);
        return mapToResponseDto(savedEvent);
    }

    @Override
    public EventResponseDto updateEvent(Long id, EventRequestDto requestDto, UserPrincipal principal) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        validateOwnershipOrAdmin(event, principal);

        event.setEventName(requestDto.getEventName());
        event.setEventType(requestDto.getEventType());
        event.setLocation(requestDto.getLocation());
        event.setEventDate(requestDto.getEventDate());
        event.setGuestCount(requestDto.getGuestCount());
        event.setBudget(requestDto.getBudget());

        Event updatedEvent = eventRepository.save(event);
        return mapToResponseDto(updatedEvent);
    }

    @Override
    public void deleteEvent(Long id, UserPrincipal principal) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        validateOwnershipOrAdmin(event, principal);

        eventRepository.delete(event);
    }

    @Override
    public List<EventResponseDto> getMyEvents(UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view events");
        }
        return eventRepository.findByCreatedBy_Id(principal.getId())
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private void validateOwnershipOrAdmin(Event event, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to modify event");
        }
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        if (event.getCreatedBy() != null && !event.getCreatedBy().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this event");
        }
    }

    private EventResponseDto mapToResponseDto(Event event) {
        Long createdByUserId = event.getCreatedBy() != null ? event.getCreatedBy().getId() : null;
        return new EventResponseDto(
                event.getId(),
                event.getEventName(),
                event.getEventType(),
                event.getLocation(),
                event.getEventDate(),
                event.getGuestCount(),
                event.getBudget(),
                createdByUserId
        );
    }
}
