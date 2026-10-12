package com.planora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for booking a vendor for an event")
public class BookingRequestDto {

    @Schema(description = "ID of the vendor being booked", example = "5")
    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @Schema(description = "Special instructions or booking notes", example = "Vegetarian preferences for 50 guests")
    private String notes;

    public BookingRequestDto() {
    }

    public BookingRequestDto(Long vendorId, String notes) {
        this.vendorId = vendorId;
        this.notes = notes;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
