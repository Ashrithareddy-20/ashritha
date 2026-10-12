package com.planora.service;

import com.planora.dto.AiChatRequestDto;
import com.planora.dto.AiChatResponseDto;
import com.planora.entity.Event;
import com.planora.entity.Role;
import com.planora.entity.User;
import com.planora.entity.Vendor;
import com.planora.repository.EventRepository;
import com.planora.repository.VendorRepository;
import com.planora.security.UserPrincipal;
import com.planora.service.impl.AiAssistantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VendorRepository vendorRepository;

    private UserPrincipal testPrincipal;
    private User testUser;

    @BeforeEach
    void setUp() {
        testPrincipal = new UserPrincipal(
                1L, "user@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        testUser = new User(1L, "Test User", "user@planora.com", "hash", Role.USER);
    }

    @Test
    void chat_WhenApiKeyMissing_ShouldReturnHelpfulConfigurationMessage() {
        AiAssistantServiceImpl service = new AiAssistantServiceImpl(
                Optional.of(chatModel), eventRepository, vendorRepository
        );
        ReflectionTestUtils.setField(service, "apiKey", "");

        AiChatRequestDto requestDto = new AiChatRequestDto("How do I plan a wedding?");
        AiChatResponseDto response = service.chat(requestDto, testPrincipal);

        assertNotNull(response);
        assertTrue(response.getAnswer().contains("OpenAI API key is not configured"));
        verifyNoInteractions(chatModel);
    }

    @Test
    void chat_WhenChatModelNotPresent_ShouldReturnHelpfulConfigurationMessage() {
        AiAssistantServiceImpl service = new AiAssistantServiceImpl(
                Optional.empty(), eventRepository, vendorRepository
        );
        ReflectionTestUtils.setField(service, "apiKey", "test-key");

        AiChatRequestDto requestDto = new AiChatRequestDto("How do I plan a wedding?");
        AiChatResponseDto response = service.chat(requestDto, testPrincipal);

        assertNotNull(response);
        assertTrue(response.getAnswer().contains("OpenAI API key is not configured"));
    }

    @Test
    void chat_WhenChatModelCallSucceeds_ShouldReturnGeneratedContent() {
        AiAssistantServiceImpl service = new AiAssistantServiceImpl(
                Optional.of(chatModel), eventRepository, vendorRepository
        );
        ReflectionTestUtils.setField(service, "apiKey", "sk-test-key-12345");

        Event event = new Event(1L, "Tech Summit", "Conference", "Hyderabad", LocalDate.of(2026, 11, 20), 100, 50000.0);
        event.setCreatedBy(testUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        Vendor vendor = new Vendor(10L, "Star Catering", "Catering", "Hyderabad", "star@mail.com", 20000.0, 4.8);
        when(vendorRepository.findAll()).thenReturn(List.of(vendor));

        Generation generation = new Generation(new AssistantMessage("Planora suggests booking Star Catering for your Tech Summit."));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        AiChatRequestDto requestDto = new AiChatRequestDto("Suggest vendors for my event", 1L);
        AiChatResponseDto response = service.chat(requestDto, testPrincipal);

        assertNotNull(response);
        assertEquals("Planora suggests booking Star Catering for your Tech Summit.", response.getAnswer());
    }

    @Test
    void chat_WhenChatModelThrowsException_ShouldHandleGracefullyWithoutCrashing() {
        AiAssistantServiceImpl service = new AiAssistantServiceImpl(
                Optional.of(chatModel), eventRepository, vendorRepository
        );
        ReflectionTestUtils.setField(service, "apiKey", "sk-test-key-12345");

        when(eventRepository.findByCreatedBy_Id(1L)).thenReturn(Collections.emptyList());
        when(vendorRepository.findAll()).thenReturn(Collections.emptyList());
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("Connection timeout"));

        AiChatRequestDto requestDto = new AiChatRequestDto("What are good venues?");
        AiChatResponseDto response = service.chat(requestDto, testPrincipal);

        assertNotNull(response);
        assertTrue(response.getAnswer().contains("AI service is currently unavailable"));
    }
}
