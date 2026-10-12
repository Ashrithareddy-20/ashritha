package com.planora.controller;

import com.planora.dto.LoginRequestDto;
import com.planora.dto.LoginResponseDto;
import com.planora.dto.RegisterRequestDto;
import com.planora.dto.RegisterResponseDto;
import com.planora.entity.Role;
import com.planora.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void register_ShouldReturn201AndResponseDto() {
        RegisterRequestDto requestDto = new RegisterRequestDto(
                "Diana Prince", "diana@example.com", "Secure@999"
        );
        RegisterResponseDto responseDto = new RegisterResponseDto(
                1L, "Diana Prince", "diana@example.com", Role.USER
        );
        when(authService.register(any(RegisterRequestDto.class))).thenReturn(responseDto);

        ResponseEntity<RegisterResponseDto> response = authController.register(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Diana Prince", response.getBody().getFullName());
        assertEquals("diana@example.com", response.getBody().getEmail());
        assertEquals(Role.USER, response.getBody().getRole());
        verify(authService).register(requestDto);
    }

    @Test
    void login_ShouldReturn200AndTokenResponse() {
        LoginRequestDto requestDto = new LoginRequestDto("diana@example.com", "Secure@999");
        LoginResponseDto responseDto = new LoginResponseDto(
                "sample.jwt.token", 1L, "Diana Prince", "diana@example.com", Role.USER
        );
        when(authService.login(any(LoginRequestDto.class))).thenReturn(responseDto);

        ResponseEntity<LoginResponseDto> response = authController.login(requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("sample.jwt.token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getType());
        assertEquals("diana@example.com", response.getBody().getEmail());
        verify(authService).login(requestDto);
    }

    @Test
    void getCurrentUser_ShouldReturn200AndUserDto() {
        com.planora.security.UserPrincipal principal = new com.planora.security.UserPrincipal(
                1L, "diana@example.com", "hash", Role.USER,
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
        );
        RegisterResponseDto responseDto = new RegisterResponseDto(
                1L, "Diana Prince", "diana@example.com", Role.USER
        );
        when(authService.getCurrentUser(principal)).thenReturn(responseDto);

        ResponseEntity<RegisterResponseDto> response = authController.getCurrentUser(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Diana Prince", response.getBody().getFullName());
        assertEquals("diana@example.com", response.getBody().getEmail());
        assertEquals(Role.USER, response.getBody().getRole());
        verify(authService).getCurrentUser(principal);
    }
}
