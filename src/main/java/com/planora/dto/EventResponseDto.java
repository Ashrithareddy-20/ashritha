package com.planora.dto;

import java.time.LocalDate;

public class EventResponseDto {

    private Long id;
    private String eventName;
    private String eventType;
    private String location;
    private LocalDate eventDate;
    private Integer guestCount;
    private Double budget;
    private Long createdByUserId;

    public EventResponseDto() {
    }

    public EventResponseDto(Long id, String eventName, String eventType, String location, LocalDate eventDate, Integer guestCount, Double budget) {
        this.id = id;
        this.eventName = eventName;
        this.eventType = eventType;
        this.location = location;
        this.eventDate = eventDate;
        this.guestCount = guestCount;
        this.budget = budget;
    }

    public EventResponseDto(Long id, String eventName, String eventType, String location, LocalDate eventDate, Integer guestCount, Double budget, Long createdByUserId) {
        this.id = id;
        this.eventName = eventName;
        this.eventType = eventType;
        this.location = location;
        this.eventDate = eventDate;
        this.guestCount = guestCount;
        this.budget = budget;
        this.createdByUserId = createdByUserId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
