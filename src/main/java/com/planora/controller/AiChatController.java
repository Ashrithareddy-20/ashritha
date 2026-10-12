package com.planora.controller;

import com.planora.dto.AiChatRequestDto;
import com.planora.dto.AiChatResponseDto;
import com.planora.security.UserPrincipal;
import com.planora.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Assistant", description = "AI-powered event planning assistance and advice")
public class AiChatController {

    private final AiAssistantService aiAssistantService;

    public AiChatController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Chat with Planora AI", description = "Provides intelligent recommendations, budget breakdowns, and advice grounded in event data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI assistance response generated"),
            @ApiResponse(responseCode = "400", description = "Invalid request or blank message"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token")
    })
    public ResponseEntity<AiChatResponseDto> chat(
            @Valid @RequestBody AiChatRequestDto requestDto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AiChatResponseDto responseDto = aiAssistantService.chat(requestDto, principal);
        return ResponseEntity.ok(responseDto);
    }
}
