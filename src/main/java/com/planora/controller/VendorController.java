package com.planora.controller;

import com.planora.dto.VendorRequestDto;
import com.planora.dto.VendorResponseDto;
import com.planora.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@Tag(name = "Vendors", description = "Endpoints for vendor search, filtering, and administration")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    @Operation(summary = "Get or search vendors", description = "Public endpoint to retrieve vendors with optional filters (category, location, price, rating)")
    @ApiResponse(responseCode = "200", description = "List of vendors matching criteria")
    public ResponseEntity<List<VendorResponseDto>> getAllVendors(
            @Parameter(description = "Filter by service category (case-insensitive)") @RequestParam(required = false) String serviceCategory,
            @Parameter(description = "Filter by location (case-insensitive)") @RequestParam(required = false) String location,
            @Parameter(description = "Filter by maximum price (price <= maxPrice)") @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Filter by minimum rating (rating >= minRating)") @RequestParam(required = false) Double minRating
    ) {
        if (serviceCategory == null && location == null && maxPrice == null && minRating == null) {
            List<VendorResponseDto> vendors = vendorService.getAllVendors();
            return ResponseEntity.ok(vendors);
        }
        List<VendorResponseDto> vendors = vendorService.searchVendors(serviceCategory, location, maxPrice, minRating);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vendor by ID", description = "Public endpoint retrieving details of a single vendor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vendor details found"),
            @ApiResponse(responseCode = "404", description = "Vendor not found")
    })
    public ResponseEntity<VendorResponseDto> getVendorById(@PathVariable Long id) {
        VendorResponseDto vendor = vendorService.getVendorById(id);
        return ResponseEntity.ok(vendor);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new vendor", description = "Creates a vendor. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vendor successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation failed for vendor request"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden (requires ADMIN role)")
    })
    public ResponseEntity<VendorResponseDto> createVendor(@Valid @RequestBody VendorRequestDto requestDto) {
        VendorResponseDto createdVendor = vendorService.createVendor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVendor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a vendor", description = "Updates an existing vendor. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vendor successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden (requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Vendor not found")
    })
    public ResponseEntity<VendorResponseDto> updateVendor(@PathVariable Long id, @Valid @RequestBody VendorRequestDto requestDto) {
        VendorResponseDto updatedVendor = vendorService.updateVendor(id, requestDto);
        return ResponseEntity.ok(updatedVendor);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a vendor", description = "Deletes an existing vendor. Requires ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vendor successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT Bearer token"),
            @ApiResponse(responseCode = "403", description = "Forbidden (requires ADMIN role)"),
            @ApiResponse(responseCode = "404", description = "Vendor not found")
    })
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }
}
