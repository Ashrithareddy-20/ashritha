package com.planora.service;

import com.planora.dto.VendorRecommendationDto;
import com.planora.entity.Event;
import com.planora.entity.Role;
import com.planora.entity.User;
import com.planora.entity.Vendor;
import com.planora.exception.ResourceNotFoundException;
import com.planora.repository.EventRepository;
import com.planora.repository.VendorRepository;
import com.planora.security.UserPrincipal;
import com.planora.service.impl.VendorRecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorRecommendationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VendorRepository vendorRepository;

    private VendorRecommendationService recommendationService;

    private UserPrincipal ownerPrincipal;
    private UserPrincipal otherPrincipal;
    private UserPrincipal adminPrincipal;
    private User ownerUser;
    private Event weddingEvent;

    @BeforeEach
    void setUp() {
        recommendationService = new VendorRecommendationServiceImpl(eventRepository, vendorRepository);

        ownerPrincipal = new UserPrincipal(
                1L, "owner@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        otherPrincipal = new UserPrincipal(
                2L, "other@planora.com", "hash", Role.USER,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        adminPrincipal = new UserPrincipal(
                99L, "admin@planora.com", "hash", Role.ADMIN,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        ownerUser = new User(1L, "Owner", "owner@planora.com", "hash", Role.USER);

        weddingEvent = new Event(
                10L, "Grand Wedding", "Wedding", "Hyderabad",
                LocalDate.of(2026, 12, 1), 300, 50000.0
        );
        weddingEvent.setCreatedBy(ownerUser);
    }

    @Test
    void getRecommendations_WhenEventNotFound_ShouldThrowResourceNotFoundException() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> recommendationService.getRecommendationsForEvent(999L, ownerPrincipal));
    }

    @Test
    void getRecommendations_WhenOtherUser_ShouldThrowAccessDeniedException() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(weddingEvent));

        assertThrows(AccessDeniedException.class,
                () -> recommendationService.getRecommendationsForEvent(10L, otherPrincipal));
    }

    @Test
    void getRecommendations_WhenAdmin_ShouldAllowAccess() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(weddingEvent));
        when(vendorRepository.findAll()).thenReturn(Collections.emptyList());

        List<VendorRecommendationDto> result = recommendationService.getRecommendationsForEvent(10L, adminPrincipal);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRecommendations_BudgetFiltering_ShouldExcludeVendorsExceedingBudget() {
        Vendor affordable = new Vendor(1L, "Budget Caterer", "Catering", "Hyderabad", "b@cat.com", 30000.0, 4.2);
        Vendor tooExpensive = new Vendor(2L, "Luxury Caterer", "Catering", "Hyderabad", "l@cat.com", 60000.0, 4.9);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(weddingEvent));
        when(vendorRepository.findAll()).thenReturn(List.of(affordable, tooExpensive));

        List<VendorRecommendationDto> recs = recommendationService.getRecommendationsForEvent(10L, ownerPrincipal);

        assertEquals(1, recs.size());
        assertEquals("Budget Caterer", recs.get(0).getVendorName());
        assertEquals(30000.0, recs.get(0).getPrice());
    }

    @Test
    void getRecommendations_LocationMatching_ShouldScoreHigherForMatchingLocation() {
        Vendor local = new Vendor(1L, "Local Decor", "Decor", "Hyderabad", "local@decor.com", 25000.0, 4.0);
        Vendor distant = new Vendor(2L, "Distant Decor", "Decor", "Mumbai", "distant@decor.com", 25000.0, 4.0);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(weddingEvent));
        when(vendorRepository.findAll()).thenReturn(List.of(distant, local));

        List<VendorRecommendationDto> recs = recommendationService.getRecommendationsForEvent(10L, ownerPrincipal);

        assertEquals(2, recs.size());
        assertEquals("Local Decor", recs.get(0).getVendorName());
        assertTrue(recs.get(0).getMatchScore() > recs.get(1).getMatchScore());
        assertTrue(recs.get(0).getMatchReason().contains("Matches event location"));
    }

    @Test
    void getRecommendations_CategoryMatching_ShouldScoreHigherForMatchingCategory() {
        Vendor matchingCat = new Vendor(1L, "Wedding Photography", "Photography", "Delhi", "photo@w.com", 25000.0, 4.0);
        Vendor unrelatedCat = new Vendor(2L, "Industrial Tech", "Tech Equipment", "Delhi", "tech@ind.com", 25000.0, 4.0);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(weddingEvent));
        when(vendorRepository.findAll()).thenReturn(List.of(unrelatedCat, matchingCat));

        List<VendorRecommendationDto> recs = recommendationService.getRecommendationsForEvent(10L, ownerPrincipal);

        assertEquals(2, recs.size());
        assertEquals("Wedding Photography", recs.get(0).getVendorName());
        assertTrue(recs.get(0).getMatchScore() > recs.get(1).getMatchScore());
        assertTrue(recs.get(0).getMatchReason().contains("Recommended category"));
    }

    @Test
    void getRecommendations_RatingRanking_ShouldRankHigherRatingAboveLowerRatingWhenOtherFactorsEqual() {
        Vendor fiveStar = new Vendor(1L, "Top Florist", "Floral Decor", "Hyderabad", "top@florist.com", 20000.0, 5.0);
        Vendor threeStar = new Vendor(2L, "Average Florist", "Floral Decor", "Hyderabad", "avg@florist.com", 20000.0, 3.0);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(weddingEvent));
        when(vendorRepository.findAll()).thenReturn(List.of(threeStar, fiveStar));

        List<VendorRecommendationDto> recs = recommendationService.getRecommendationsForEvent(10L, ownerPrincipal);

        assertEquals(2, recs.size());
        assertEquals("Top Florist", recs.get(0).getVendorName());
        assertTrue(recs.get(0).getMatchScore() > recs.get(1).getMatchScore());
    }

    @Test
    void getRecommendations_CombinedCriteria_BestMatchRanksFirst() {
        Vendor idealVendor = new Vendor(1L, "Ideal Wedding Caterer", "Catering", "Hyderabad", "ideal@cater.com", 35000.0, 4.8);
        Vendor goodVendor = new Vendor(2L, "Out-of-Town Caterer", "Catering", "Bangalore", "out@cater.com", 35000.0, 4.8);
        Vendor poorVendor = new Vendor(3L, "Random Vendor", "Hardware", "Pune", "rand@hw.com", 45000.0, 3.0);
        Vendor overBudget = new Vendor(4L, "Ultra Luxury Venue", "Venue", "Hyderabad", "ultra@venue.com", 90000.0, 5.0);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(weddingEvent));
        when(vendorRepository.findAll()).thenReturn(List.of(poorVendor, goodVendor, idealVendor, overBudget));

        List<VendorRecommendationDto> recs = recommendationService.getRecommendationsForEvent(10L, ownerPrincipal);

        assertEquals(3, recs.size());
        assertEquals("Ideal Wedding Caterer", recs.get(0).getVendorName());
        assertEquals("Out-of-Town Caterer", recs.get(1).getVendorName());
        assertEquals("Random Vendor", recs.get(2).getVendorName());
    }

    @Test
    void getRecommendations_WhenNoVendorsAvailable_ShouldReturnEmptyList() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(weddingEvent));
        when(vendorRepository.findAll()).thenReturn(Collections.emptyList());

        List<VendorRecommendationDto> recs = recommendationService.getRecommendationsForEvent(10L, ownerPrincipal);

        assertNotNull(recs);
        assertTrue(recs.isEmpty());
    }
}
