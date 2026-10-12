package com.planora.service;

import com.planora.dto.AiChatRequestDto;
import com.planora.dto.AiChatResponseDto;
import com.planora.security.UserPrincipal;

public interface AiAssistantService {

    AiChatResponseDto chat(AiChatRequestDto requestDto, UserPrincipal principal);
}
