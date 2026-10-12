package com.planora.dto;

import java.time.LocalDateTime;

public class AiChatResponseDto {

    private String answer;
    private LocalDateTime timestamp;

    public AiChatResponseDto() {
        this.timestamp = LocalDateTime.now();
    }

    public AiChatResponseDto(String answer) {
        this.answer = answer;
        this.timestamp = LocalDateTime.now();
    }

    public AiChatResponseDto(String answer, LocalDateTime timestamp) {
        this.answer = answer;
        this.timestamp = timestamp;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
