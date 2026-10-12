package com.planora.dto;

public class VendorResponseDto {

    private Long id;
    private String vendorName;
    private String serviceCategory;
    private String location;
    private String contactEmail;
    private Double price;
    private Double rating;

    public VendorResponseDto() {
    }

    public VendorResponseDto(Long id, String vendorName, String serviceCategory, String location, String contactEmail, Double price, Double rating) {
        this.id = id;
        this.vendorName = vendorName;
        this.serviceCategory = serviceCategory;
        this.location = location;
        this.contactEmail = contactEmail;
        this.price = price;
        this.rating = rating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
