package com.planora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for interacting with Planora AI planning assistant")
public class AiChatRequestDto {

    @Schema(description = "User query or message for event planning assistance", example = "Help me choose catering vendors and plan budget breakdown for 200 guests.")
    @NotBlank(message = "Message cannot be blank")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    @Schema(description = "Optional ID of an event to ground the conversation context in", example = "1")
    private Long eventId;

    public AiChatRequestDto() {
    }

    public AiChatRequestDto(String message) {
        this.message = message;
    }

    public AiChatRequestDto(String message, Long eventId) {
        this.message = message;
        this.eventId = eventId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
