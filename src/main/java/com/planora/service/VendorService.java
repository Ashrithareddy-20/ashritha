package com.planora.service;

import com.planora.dto.VendorRequestDto;
import com.planora.dto.VendorResponseDto;

import java.util.List;

public interface VendorService {

    List<VendorResponseDto> getAllVendors();

    List<VendorResponseDto> searchVendors(String serviceCategory, String location, Double maxPrice, Double minRating);

    VendorResponseDto getVendorById(Long id);

    VendorResponseDto createVendor(VendorRequestDto requestDto);

    VendorResponseDto updateVendor(Long id, VendorRequestDto requestDto);

    void deleteVendor(Long id);
}
