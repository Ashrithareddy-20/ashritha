package com.planora.service;

import com.planora.dto.VendorResponseDto;
import com.planora.entity.Vendor;
import com.planora.repository.VendorRepository;
import com.planora.service.impl.VendorServiceImpl;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorFilterLogicTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private Root<Vendor> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<String> categoryPath;

    @Mock
    private Path<String> locationPath;

    @Mock
    private Path<Double> pricePath;

    @Mock
    private Path<Double> ratingPath;

    @Mock
    private Predicate predicate;

    private VendorService vendorService;

    @BeforeEach
    void setUp() {
        vendorService = new VendorServiceImpl(vendorRepository);
    }

    @Test
    void searchVendors_CategoryFilter_ShouldCreateCaseInsensitivePredicate() {
        when(root.<String>get("serviceCategory")).thenReturn(categoryPath);
        when(cb.lower(categoryPath)).thenReturn(categoryPath);
        when(cb.equal(categoryPath, "catering")).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        ArgumentCaptor<Specification<Vendor>> captor = ArgumentCaptor.forClass(Specification.class);
        when(vendorRepository.findAll(captor.capture())).thenReturn(Collections.emptyList());

        List<VendorResponseDto> result = vendorService.searchVendors("Catering", null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        Specification<Vendor> spec = captor.getValue();
        Predicate resPredicate = spec.toPredicate(root, query, cb);
        assertNotNull(resPredicate);
        verify(cb).equal(categoryPath, "catering");
    }

    @Test
    void searchVendors_LocationFilter_ShouldCreateCaseInsensitivePredicate() {
        when(root.<String>get("location")).thenReturn(locationPath);
        when(cb.lower(locationPath)).thenReturn(locationPath);
        when(cb.equal(locationPath, "hyderabad")).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        ArgumentCaptor<Specification<Vendor>> captor = ArgumentCaptor.forClass(Specification.class);
        when(vendorRepository.findAll(captor.capture())).thenReturn(Collections.emptyList());

        List<VendorResponseDto> result = vendorService.searchVendors(null, "Hyderabad", null, null);

        assertNotNull(result);
        Specification<Vendor> spec = captor.getValue();
        spec.toPredicate(root, query, cb);
        verify(cb).equal(locationPath, "hyderabad");
    }

    @Test
    void searchVendors_MaxPriceFilter_ShouldCreateLessThanOrEqualToPredicate() {
        when(root.<Double>get("price")).thenReturn(pricePath);
        when(cb.lessThanOrEqualTo(pricePath, 25000.0)).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        ArgumentCaptor<Specification<Vendor>> captor = ArgumentCaptor.forClass(Specification.class);
        when(vendorRepository.findAll(captor.capture())).thenReturn(Collections.emptyList());

        List<VendorResponseDto> result = vendorService.searchVendors(null, null, 25000.0, null);

        assertNotNull(result);
        Specification<Vendor> spec = captor.getValue();
        spec.toPredicate(root, query, cb);
        verify(cb).lessThanOrEqualTo(pricePath, 25000.0);
    }

    @Test
    void searchVendors_MinRatingFilter_ShouldCreateGreaterThanOrEqualToPredicate() {
        when(root.<Double>get("rating")).thenReturn(ratingPath);
        when(cb.greaterThanOrEqualTo(ratingPath, 4.5)).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        ArgumentCaptor<Specification<Vendor>> captor = ArgumentCaptor.forClass(Specification.class);
        when(vendorRepository.findAll(captor.capture())).thenReturn(Collections.emptyList());

        List<VendorResponseDto> result = vendorService.searchVendors(null, null, null, 4.5);

        assertNotNull(result);
        Specification<Vendor> spec = captor.getValue();
        spec.toPredicate(root, query, cb);
        verify(cb).greaterThanOrEqualTo(ratingPath, 4.5);
    }

    @Test
    void searchVendors_CombinedFilters_ShouldCombineAllWithAnd() {
        when(root.<String>get("serviceCategory")).thenReturn(categoryPath);
        when(cb.lower(categoryPath)).thenReturn(categoryPath);
        when(cb.equal(categoryPath, "catering")).thenReturn(predicate);

        when(root.<String>get("location")).thenReturn(locationPath);
        when(cb.lower(locationPath)).thenReturn(locationPath);
        when(cb.equal(locationPath, "hyderabad")).thenReturn(predicate);

        when(root.<Double>get("price")).thenReturn(pricePath);
        when(cb.lessThanOrEqualTo(pricePath, 20000.0)).thenReturn(predicate);

        when(root.<Double>get("rating")).thenReturn(ratingPath);
        when(cb.greaterThanOrEqualTo(ratingPath, 4.0)).thenReturn(predicate);

        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Vendor vendor = new Vendor(1L, "Hyderabad Royal Catering", "Catering", "Hyderabad", "info@royal.com", 18000.0, 4.5);
        ArgumentCaptor<Specification<Vendor>> captor = ArgumentCaptor.forClass(Specification.class);
        when(vendorRepository.findAll(captor.capture())).thenReturn(List.of(vendor));

        List<VendorResponseDto> result = vendorService.searchVendors("Catering", "Hyderabad", 20000.0, 4.0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hyderabad Royal Catering", result.get(0).getVendorName());

        Specification<Vendor> spec = captor.getValue();
        spec.toPredicate(root, query, cb);
        verify(cb).equal(categoryPath, "catering");
        verify(cb).equal(locationPath, "hyderabad");
        verify(cb).lessThanOrEqualTo(pricePath, 20000.0);
        verify(cb).greaterThanOrEqualTo(ratingPath, 4.0);
    }

    @Test
    void searchVendors_WhenNoMatch_ShouldReturnEmptyList() {
        ArgumentCaptor<Specification<Vendor>> captor = ArgumentCaptor.forClass(Specification.class);
        when(vendorRepository.findAll(captor.capture())).thenReturn(Collections.emptyList());

        List<VendorResponseDto> result = vendorService.searchVendors("NonExistent", "Nowhere", 1.0, 5.0);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
