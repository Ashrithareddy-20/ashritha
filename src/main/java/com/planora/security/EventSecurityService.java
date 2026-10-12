package com.planora.security;

import com.planora.entity.Event;
import com.planora.entity.Role;
import com.planora.repository.EventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("eventSecurity")
public class EventSecurityService {

    private final EventRepository eventRepository;

    public EventSecurityService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public boolean canAccessEvent(Long eventId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        // ADMIN can access any event
        if (principal.getRole() == Role.ADMIN) {
            return true;
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            // Let the service/controller handle ResourceNotFoundException (404)
            return true;
        }

        Event event = eventOpt.get();
        // If event has no owner (e.g. legacy/public), or if the creator matches current user
        return event.getCreatedBy() == null || event.getCreatedBy().getId().equals(principal.getId());
    }

    public boolean canModifyEvent(Long eventId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        // ADMIN can modify any event
        if (principal.getRole() == Role.ADMIN) {
            return true;
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return true;
        }

        Event event = eventOpt.get();
        // Only creator can modify their own event
        return event.getCreatedBy() == null || event.getCreatedBy().getId().equals(principal.getId());
    }
}
