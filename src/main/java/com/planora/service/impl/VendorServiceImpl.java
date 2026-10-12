package com.planora.service.impl;

import com.planora.dto.VendorRequestDto;
import com.planora.dto.VendorResponseDto;
import com.planora.entity.Vendor;
import com.planora.exception.ResourceNotFoundException;
import com.planora.repository.VendorRepository;
import com.planora.service.VendorService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;

    public VendorServiceImpl(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Override
    public List<VendorResponseDto> getAllVendors() {
        return vendorRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorResponseDto> searchVendors(String serviceCategory, String location, Double maxPrice, Double minRating) {
        org.springframework.data.jpa.domain.Specification<Vendor> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (serviceCategory != null && !serviceCategory.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("serviceCategory")), serviceCategory.trim().toLowerCase()));
            }

            if (location != null && !location.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("location")), location.trim().toLowerCase()));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), minRating));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return vendorRepository.findAll(spec)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public VendorResponseDto getVendorById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return mapToResponseDto(vendor);
    }

    @Override
    public VendorResponseDto createVendor(VendorRequestDto requestDto) {
        Vendor vendor = new Vendor();
        vendor.setVendorName(requestDto.getVendorName());
        vendor.setServiceCategory(requestDto.getServiceCategory());
        vendor.setLocation(requestDto.getLocation());
        vendor.setContactEmail(requestDto.getContactEmail());
        vendor.setPrice(requestDto.getPrice());
        vendor.setRating(requestDto.getRating());

        Vendor savedVendor = vendorRepository.save(vendor);
        return mapToResponseDto(savedVendor);
    }

    @Override
    public VendorResponseDto updateVendor(Long id, VendorRequestDto requestDto) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendor.setVendorName(requestDto.getVendorName());
        vendor.setServiceCategory(requestDto.getServiceCategory());
        vendor.setLocation(requestDto.getLocation());
        vendor.setContactEmail(requestDto.getContactEmail());
        vendor.setPrice(requestDto.getPrice());
        vendor.setRating(requestDto.getRating());

        Vendor updatedVendor = vendorRepository.save(vendor);
        return mapToResponseDto(updatedVendor);
    }

    @Override
    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        vendorRepository.delete(vendor);
    }

    private VendorResponseDto mapToResponseDto(Vendor vendor) {
        return new VendorResponseDto(
                vendor.getId(),
                vendor.getVendorName(),
                vendor.getServiceCategory(),
                vendor.getLocation(),
                vendor.getContactEmail(),
                vendor.getPrice(),
                vendor.getRating()
        );
    }
}
