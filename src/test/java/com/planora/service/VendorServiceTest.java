package com.planora.service;

import com.planora.dto.VendorRequestDto;
import com.planora.dto.VendorResponseDto;
import com.planora.entity.Vendor;
import com.planora.exception.ResourceNotFoundException;
import com.planora.repository.VendorRepository;
import com.planora.service.impl.VendorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    private VendorService vendorService;

    @BeforeEach
    void setUp() {
        vendorService = new VendorServiceImpl(vendorRepository);
    }

    @Test
    void getAllVendors_ShouldReturnList() {
        Vendor vendor = new Vendor(1L, "Lumiere Photography", "Photography", "Hyderabad", "lumiere@photos.com", 25000.0, 4.9);
        when(vendorRepository.findAll()).thenReturn(List.of(vendor));

        List<VendorResponseDto> result = vendorService.getAllVendors();

        assertEquals(1, result.size());
        assertEquals("Lumiere Photography", result.get(0).getVendorName());
        verify(vendorRepository).findAll();
    }

    @Test
    void getVendorById_WhenFound_ShouldReturnDto() {
        Vendor vendor = new Vendor(1L, "Lumiere Photography", "Photography", "Hyderabad", "lumiere@photos.com", 25000.0, 4.9);
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));

        VendorResponseDto result = vendorService.getVendorById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Lumiere Photography", result.getVendorName());
        verify(vendorRepository).findById(1L);
    }

    @Test
    void getVendorById_WhenNotFound_ShouldThrowException() {
        when(vendorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vendorService.getVendorById(99L));
        verify(vendorRepository).findById(99L);
    }

    @Test
    void createVendor_ShouldSaveAndReturnDto() {
        VendorRequestDto requestDto = new VendorRequestDto("DJ Pulse", "Music", "Mumbai", "info@djpulse.com", 12000.0, 4.7);
        Vendor savedVendor = new Vendor(1L, "DJ Pulse", "Music", "Mumbai", "info@djpulse.com", 12000.0, 4.7);
        when(vendorRepository.save(any(Vendor.class))).thenReturn(savedVendor);

        VendorResponseDto result = vendorService.createVendor(requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DJ Pulse", result.getVendorName());
        verify(vendorRepository).save(any(Vendor.class));
    }

    @Test
    void updateVendor_WhenExists_ShouldUpdateAndReturnDto() {
        Vendor existingVendor = new Vendor(1L, "Old DJ", "Music", "Delhi", "old@dj.com", 8000.0, 4.0);
        VendorRequestDto updateDto = new VendorRequestDto("DJ Sonic", "Music & Sound", "Delhi NCR", "sonic@dj.com", 15000.0, 4.8);
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(existingVendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendorResponseDto result = vendorService.updateVendor(1L, updateDto);

        assertNotNull(result);
        assertEquals("DJ Sonic", result.getVendorName());
        assertEquals("Music & Sound", result.getServiceCategory());
        assertEquals("Delhi NCR", result.getLocation());
        assertEquals("sonic@dj.com", result.getContactEmail());
        assertEquals(15000.0, result.getPrice());
        assertEquals(4.8, result.getRating());
        verify(vendorRepository).findById(1L);
        verify(vendorRepository).save(existingVendor);
    }

    @Test
    void updateVendor_WhenNotFound_ShouldThrowException() {
        VendorRequestDto updateDto = new VendorRequestDto("DJ Sonic", "Music", "Delhi", "sonic@dj.com", 15000.0, 4.8);
        when(vendorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vendorService.updateVendor(99L, updateDto));
        verify(vendorRepository).findById(99L);
    }

    @Test
    void deleteVendor_WhenExists_ShouldDelete() {
        Vendor existingVendor = new Vendor(1L, "To Delete", "Decor", "Goa", "decor@goa.com", 5000.0, 3.5);
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(existingVendor));

        vendorService.deleteVendor(1L);

        verify(vendorRepository).findById(1L);
        verify(vendorRepository).delete(existingVendor);
    }

    @Test
    void deleteVendor_WhenNotFound_ShouldThrowException() {
        when(vendorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vendorService.deleteVendor(99L));
        verify(vendorRepository).findById(99L);
    }

    @Test
    void searchVendors_ShouldQueryRepositoryWithSpecification() {
        Vendor vendor = new Vendor(1L, "Royal Catering", "Catering", "Hyderabad", "info@royal.com", 15000.0, 4.8);
        when(vendorRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(vendor));

        List<VendorResponseDto> result = vendorService.searchVendors("Catering", "Hyderabad", 20000.0, 4.0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Royal Catering", result.get(0).getVendorName());
        assertEquals("Catering", result.get(0).getServiceCategory());
        assertEquals("Hyderabad", result.get(0).getLocation());
        verify(vendorRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class));
    }
}
