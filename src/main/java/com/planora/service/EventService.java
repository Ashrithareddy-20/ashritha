package com.planora.service;

import com.planora.dto.EventRequestDto;
import com.planora.dto.EventResponseDto;
import com.planora.security.UserPrincipal;

import java.util.List;

public interface EventService {

    List<EventResponseDto> getAllEvents();

    EventResponseDto getEventById(Long id);

    EventResponseDto createEvent(EventRequestDto requestDto, UserPrincipal principal);

    EventResponseDto updateEvent(Long id, EventRequestDto requestDto, UserPrincipal principal);

    void deleteEvent(Long id, UserPrincipal principal);

    List<EventResponseDto> getMyEvents(UserPrincipal principal);
}
