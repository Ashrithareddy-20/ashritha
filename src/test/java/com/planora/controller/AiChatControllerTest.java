package com.planora.controller;

import com.planora.dto.AiChatRequestDto;
import com.planora.dto.AiChatResponseDto;
import com.planora.entity.Role;
import com.planora.security.UserPrincipal;
import com.planora.service.AiAssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatControllerTest {

    @Mock
    private AiAssistantService aiAssistantService;

    private AiChatController aiChatController;
    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        aiChatController = new AiChatController(aiAssistantService);
        testPrincipal = new UserPrincipal(
                1L, "user@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void chat_ShouldReturn200AndAiResponse() {
        AiChatRequestDto requestDto = new AiChatRequestDto("Help me organize catering for 150 guests", 10L);
        AiChatResponseDto responseDto = new AiChatResponseDto("Here is a recommended plan for catering...");

        when(aiAssistantService.chat(any(AiChatRequestDto.class), eq(testPrincipal))).thenReturn(responseDto);

        ResponseEntity<AiChatResponseDto> response = aiChatController.chat(requestDto, testPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Here is a recommended plan for catering...", response.getBody().getAnswer());
        assertNotNull(response.getBody().getTimestamp());
        verify(aiAssistantService).chat(requestDto, testPrincipal);
    }
}
