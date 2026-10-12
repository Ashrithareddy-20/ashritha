package com.planora.service;

import com.planora.dto.LoginRequestDto;
import com.planora.dto.LoginResponseDto;
import com.planora.dto.RegisterRequestDto;
import com.planora.dto.RegisterResponseDto;
import com.planora.entity.Role;
import com.planora.entity.User;
import com.planora.exception.EmailAlreadyExistsException;
import com.planora.repository.UserRepository;
import com.planora.security.JwtUtils;
import com.planora.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtUtils);
    }

    @Test
    void register_WhenEmailUnique_ShouldSaveAndReturnDto() {
        RegisterRequestDto requestDto = new RegisterRequestDto(
                "Alice Smith", "alice@example.com", "Secret@123"
        );

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("$2a$10$hashedPassword");

        User savedUser = new User(1L, "Alice Smith", "alice@example.com", "$2a$10$hashedPassword", Role.USER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponseDto response = authService.register(requestDto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Alice Smith", response.getFullName());
        assertEquals("alice@example.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());

        verify(userRepository).existsByEmail("alice@example.com");
        verify(passwordEncoder).encode("Secret@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WhenEmailDuplicate_ShouldThrowEmailAlreadyExistsException() {
        RegisterRequestDto requestDto = new RegisterRequestDto(
                "Bob Jones", "bob@example.com", "Pass@5678"
        );
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(requestDto));

        verify(userRepository).existsByEmail("bob@example.com");
    }

    @Test
    void register_ShouldNeverStoreRawPassword() {
        RegisterRequestDto requestDto = new RegisterRequestDto(
                "Carol White", "carol@example.com", "MyPassword1"
        );
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("MyPassword1")).thenReturn("$2a$10$encodedHash");

        User savedUser = new User(2L, "Carol White", "carol@example.com", "$2a$10$encodedHash", Role.USER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(requestDto);

        verify(passwordEncoder).encode("MyPassword1");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_WhenCredentialsValid_ShouldReturnTokenAndUserDto() {
        LoginRequestDto requestDto = new LoginRequestDto("diana@planora.com", "Secret@123");
        User user = new User(5L, "Diana Prince", "diana@planora.com", "$2a$10$hashedPass", Role.USER);

        when(userRepository.findByEmail("diana@planora.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret@123", "$2a$10$hashedPass")).thenReturn(true);
        when(jwtUtils.generateToken("diana@planora.com", "USER", 5L)).thenReturn("mocked.jwt.token");

        LoginResponseDto response = authService.login(requestDto);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(5L, response.getId());
        assertEquals("Diana Prince", response.getFullName());
        assertEquals("diana@planora.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());
    }

    @Test
    void login_WhenEmailNotFound_ShouldThrowBadCredentialsException() {
        LoginRequestDto requestDto = new LoginRequestDto("unknown@planora.com", "Secret@123");
        when(userRepository.findByEmail("unknown@planora.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(requestDto));
    }

    @Test
    void login_WhenPasswordIncorrect_ShouldThrowBadCredentialsException() {
        LoginRequestDto requestDto = new LoginRequestDto("diana@planora.com", "WrongPassword");
        User user = new User(5L, "Diana Prince", "diana@planora.com", "$2a$10$hashedPass", Role.USER);

        when(userRepository.findByEmail("diana@planora.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$hashedPass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(requestDto));
    }

    @Test
    void getCurrentUser_WhenUserExists_ShouldReturnDto() {
        com.planora.security.UserPrincipal principal = new com.planora.security.UserPrincipal(
                5L, "diana@planora.com", "hash", Role.USER,
                java.util.Collections.emptyList()
        );
        User user = new User(5L, "Diana Prince", "diana@planora.com", "hash", Role.USER);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        RegisterResponseDto response = authService.getCurrentUser(principal);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("Diana Prince", response.getFullName());
        assertEquals("diana@planora.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());
    }

    @Test
    void getCurrentUser_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        com.planora.security.UserPrincipal principal = new com.planora.security.UserPrincipal(
                99L, "ghost@planora.com", "hash", Role.USER,
                java.util.Collections.emptyList()
        );
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(com.planora.exception.ResourceNotFoundException.class,
                () -> authService.getCurrentUser(principal));
    }
}
