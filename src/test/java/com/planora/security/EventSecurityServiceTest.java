package com.planora.security;

import com.planora.entity.Event;
import com.planora.entity.Role;
import com.planora.entity.User;
import com.planora.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventSecurityServiceTest {

    @Mock
    private EventRepository eventRepository;

    private EventSecurityService eventSecurityService;

    @BeforeEach
    void setUp() {
        eventSecurityService = new EventSecurityService(eventRepository);
    }

    @Test
    void canModifyEvent_WhenAdmin_ShouldAllowEvenIfNotOwner() {
        UserPrincipal admin = new UserPrincipal(
                99L, "admin@planora.com", "pass", Role.ADMIN,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        assertTrue(eventSecurityService.canModifyEvent(1L, auth));
    }

    @Test
    void canModifyEvent_WhenOwner_ShouldAllow() {
        UserPrincipal user = new UserPrincipal(
                1L, "user@planora.com", "pass", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        User owner = new User(1L, "Owner", "user@planora.com", "pass", Role.USER);
        Event event = new Event();
        event.setId(10L);
        event.setCreatedBy(owner);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertTrue(eventSecurityService.canModifyEvent(10L, auth));
    }

    @Test
    void canModifyEvent_WhenDifferentUser_ShouldDeny() {
        UserPrincipal user = new UserPrincipal(
                2L, "other@planora.com", "pass", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        User owner = new User(1L, "Owner", "owner@planora.com", "pass", Role.USER);
        Event event = new Event();
        event.setId(10L);
        event.setCreatedBy(owner);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertFalse(eventSecurityService.canModifyEvent(10L, auth));
    }
}
