package com.planora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request body for creating or updating an event")
public class EventRequestDto {

    @Schema(description = "Name or title of the event", example = "Tech Innovators Annual Gala")
    @NotBlank(message = "Event name is required")
    @Size(min = 2, max = 100, message = "Event name must be between 2 and 100 characters")
    private String eventName;

    @Schema(description = "Category/type of event", example = "Conference")
    @NotBlank(message = "Event type is required")
    @Size(min = 2, max = 50, message = "Event type must be between 2 and 50 characters")
    private String eventType;

    @Schema(description = "City or venue location", example = "Hyderabad")
    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 150, message = "Location must be between 2 and 150 characters")
    private String location;

    @Schema(description = "Planned date of the event", example = "2026-12-15")
    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    @Schema(description = "Expected guest attendee count", example = "250")
    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "Guest count must be at least 1")
    private Integer guestCount;

    @Schema(description = "Allocated budget in USD", example = "50000.00")
    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than zero")
    private Double budget;

    public EventRequestDto() {
    }

    public EventRequestDto(String eventName, String eventType, String location, LocalDate eventDate, Integer guestCount, Double budget) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.location = location;
        this.eventDate = eventDate;
        this.guestCount = guestCount;
        this.budget = budget;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public Integer getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(Integer guestCount) {
        this.guestCount = guestCount;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }
}
