package com.planora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating or updating a vendor")
public class VendorRequestDto {

    @Schema(description = "Name of the vendor company or professional", example = "Royal Banquet & Catering")
    @NotBlank(message = "Vendor name is required")
    @Size(min = 2, max = 100, message = "Vendor name must be between 2 and 100 characters")
    private String vendorName;

    @Schema(description = "Category of service offered", example = "Catering")
    @NotBlank(message = "Service category is required")
    @Size(min = 2, max = 50, message = "Service category must be between 2 and 50 characters")
    private String serviceCategory;

    @Schema(description = "Location / city where the vendor operates", example = "Hyderabad")
    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 150, message = "Location must be between 2 and 150 characters")
    private String location;

    @Schema(description = "Contact email of the vendor", example = "info@royalbanquet.com")
    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;

    @Schema(description = "Base pricing quote in USD", example = "15000.00")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private Double price;

    @Schema(description = "Customer rating between 0.0 and 5.0", example = "4.8")
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rating cannot exceed 5.0")
    private Double rating;

    public VendorRequestDto() {
    }

    public VendorRequestDto(String vendorName, String serviceCategory, String location, String contactEmail, Double price, Double rating) {
        this.vendorName = vendorName;
        this.serviceCategory = serviceCategory;
        this.location = location;
        this.contactEmail = contactEmail;
        this.price = price;
        this.rating = rating;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
