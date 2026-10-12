package com.planora.dto;

import com.planora.entity.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for updating a booking's status")
public class BookingStatusUpdateDto {

    @Schema(description = "New booking status (CONFIRMED or CANCELLED)", example = "CONFIRMED")
    @NotNull(message = "Status is required")
    private BookingStatus status;

    public BookingStatusUpdateDto() {
    }

    public BookingStatusUpdateDto(BookingStatus status) {
        this.status = status;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
