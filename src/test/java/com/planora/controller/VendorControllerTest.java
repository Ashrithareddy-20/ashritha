package com.planora.controller;

import com.planora.dto.VendorRequestDto;
import com.planora.dto.VendorResponseDto;
import com.planora.service.VendorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorControllerTest {

    @Mock
    private VendorService vendorService;

    private VendorController vendorController;

    @BeforeEach
    void setUp() {
        vendorController = new VendorController(vendorService);
    }

    @Test
    void getAllVendors_ShouldReturn200AndList() {
        VendorResponseDto vendorDto = new VendorResponseDto(
                1L, "Elegant Florals", "Floral Decor", "Mumbai", "contact@florals.com", 18000.0, 4.6
        );
        when(vendorService.getAllVendors()).thenReturn(List.of(vendorDto));

        ResponseEntity<List<VendorResponseDto>> response = vendorController.getAllVendors(null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Elegant Florals", response.getBody().get(0).getVendorName());
        verify(vendorService).getAllVendors();
    }

    @Test
    void getAllVendors_WithFilters_ShouldCallSearchVendors() {
        VendorResponseDto vendorDto = new VendorResponseDto(
                1L, "Elegant Florals", "Catering", "Hyderabad", "contact@florals.com", 18000.0, 4.6
        );
        when(vendorService.searchVendors("Catering", "Hyderabad", 20000.0, 4.0))
                .thenReturn(List.of(vendorDto));

        ResponseEntity<List<VendorResponseDto>> response =
                vendorController.getAllVendors("Catering", "Hyderabad", 20000.0, 4.0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Elegant Florals", response.getBody().get(0).getVendorName());
        verify(vendorService).searchVendors("Catering", "Hyderabad", 20000.0, 4.0);
    }

    @Test
    void getVendorById_ShouldReturn200AndVendor() {
        VendorResponseDto vendorDto = new VendorResponseDto(
                1L, "Grand Catering", "Catering", "Chennai", "info@grandcatering.com", 40000.0, 4.9
        );
        when(vendorService.getVendorById(1L)).thenReturn(vendorDto);

        ResponseEntity<VendorResponseDto> response = vendorController.getVendorById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Grand Catering", response.getBody().getVendorName());
        verify(vendorService).getVendorById(1L);
    }

    @Test
    void createVendor_ShouldReturn201AndCreatedVendor() {
        VendorRequestDto requestDto = new VendorRequestDto(
                "Pixel Magic Films", "Videography", "Pune", "pixel@magic.com", 35000.0, 4.7
        );
        VendorResponseDto responseDto = new VendorResponseDto(
                1L, "Pixel Magic Films", "Videography", "Pune", "pixel@magic.com", 35000.0, 4.7
        );
        when(vendorService.createVendor(any(VendorRequestDto.class))).thenReturn(responseDto);

        ResponseEntity<VendorResponseDto> response = vendorController.createVendor(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Pixel Magic Films", response.getBody().getVendorName());
        verify(vendorService).createVendor(requestDto);
    }

    @Test
    void updateVendor_ShouldReturn200AndUpdatedVendor() {
        VendorRequestDto requestDto = new VendorRequestDto(
                "Pixel Magic Films Studio", "Videography & Drone", "Pune", "info@pixelmagic.com", 45000.0, 4.9
        );
        VendorResponseDto responseDto = new VendorResponseDto(
                1L, "Pixel Magic Films Studio", "Videography & Drone", "Pune", "info@pixelmagic.com", 45000.0, 4.9
        );
        when(vendorService.updateVendor(1L, requestDto)).thenReturn(responseDto);

        ResponseEntity<VendorResponseDto> response = vendorController.updateVendor(1L, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Pixel Magic Films Studio", response.getBody().getVendorName());
        verify(vendorService).updateVendor(1L, requestDto);
    }

    @Test
    void deleteVendor_ShouldReturn204NoContent() {
        ResponseEntity<Void> response = vendorController.deleteVendor(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(vendorService).deleteVendor(1L);
    }
}
