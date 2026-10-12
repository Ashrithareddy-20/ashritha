package com.planora.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Vendor recommendation ranked by the rule-based recommendation engine")
public class VendorRecommendationDto {

    @Schema(description = "Vendor ID", example = "3")
    private Long vendorId;

    @Schema(description = "Vendor name", example = "Elite Sound & Lighting")
    private String vendorName;

    @Schema(description = "Service category", example = "Sound & Lighting")
    private String serviceCategory;

    @Schema(description = "Vendor location city", example = "Hyderabad")
    private String location;

    @Schema(description = "Contact email", example = "contact@elitesound.com")
    private String contactEmail;

    @Schema(description = "Vendor base price in USD", example = "8500.00")
    private Double price;

    @Schema(description = "Vendor average rating", example = "4.9")
    private Double rating;

    @Schema(description = "Calculated recommendation match score (0-100)", example = "92.5")
    private Double matchScore;

    @Schema(description = "Detailed justification explaining recommendation score", example = "Matches event type 'Conference' (+40 pts); in same city 'Hyderabad' (+30 pts); budget eligible (+10 pts); high rating (+12.5 pts)")
    private String matchReason;

    public VendorRecommendationDto() {
    }

    public VendorRecommendationDto(Long vendorId, String vendorName, String serviceCategory,
                                   String location, String contactEmail, Double price,
                                   Double rating, Double matchScore, String matchReason) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.serviceCategory = serviceCategory;
        this.location = location;
        this.contactEmail = contactEmail;
        this.price = price;
        this.rating = rating;
        this.matchScore = matchScore;
        this.matchReason = matchReason;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
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

    public Double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }
}
