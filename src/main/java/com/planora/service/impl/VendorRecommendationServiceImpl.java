package com.planora.service.impl;

import com.planora.dto.VendorRecommendationDto;
import com.planora.entity.Event;
import com.planora.entity.Role;
import com.planora.entity.Vendor;
import com.planora.exception.ResourceNotFoundException;
import com.planora.repository.EventRepository;
import com.planora.repository.VendorRepository;
import com.planora.security.UserPrincipal;
import com.planora.service.VendorRecommendationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VendorRecommendationServiceImpl implements VendorRecommendationService {

    private final EventRepository eventRepository;
    private final VendorRepository vendorRepository;

    private static final Map<String, Set<String>> EVENT_TYPE_CATEGORIES = new HashMap<>();

    static {
        EVENT_TYPE_CATEGORIES.put("wedding", Set.of("catering", "photography", "videography", "decor", "floral decor", "music", "venue", "makeup"));
        EVENT_TYPE_CATEGORIES.put("birthday", Set.of("catering", "decor", "music", "cake", "entertainment", "photography", "venue"));
        EVENT_TYPE_CATEGORIES.put("conference", Set.of("catering", "audio-visual", "videography", "venue", "tech", "photography"));
        EVENT_TYPE_CATEGORIES.put("corporate", Set.of("catering", "audio-visual", "videography", "venue", "music", "photography"));
        EVENT_TYPE_CATEGORIES.put("anniversary", Set.of("catering", "decor", "photography", "music", "floral decor", "venue"));
        EVENT_TYPE_CATEGORIES.put("party", Set.of("catering", "decor", "music", "photography", "venue", "entertainment"));
    }

    public VendorRecommendationServiceImpl(EventRepository eventRepository, VendorRepository vendorRepository) {
        this.eventRepository = eventRepository;
        this.vendorRepository = vendorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorRecommendationDto> getRecommendationsForEvent(Long eventId, UserPrincipal principal) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        validateEventOwnershipOrAdmin(event, principal);

        List<Vendor> allVendors = vendorRepository.findAll();
        if (allVendors.isEmpty()) {
            return Collections.emptyList();
        }

        List<VendorRecommendationDto> eligible = new ArrayList<>();

        Double eventBudget = event.getBudget();
        String eventLocation = event.getLocation() != null ? event.getLocation().trim().toLowerCase() : "";
        String eventType = event.getEventType() != null ? event.getEventType().trim().toLowerCase() : "";

        Set<String> preferredCategories = EVENT_TYPE_CATEGORIES.getOrDefault(eventType, Collections.emptySet());

        for (Vendor v : allVendors) {
            Double price = v.getPrice() != null ? v.getPrice() : 0.0;

            // Strict budget rule: vendor price must not exceed event budget
            if (eventBudget != null && price > eventBudget) {
                continue;
            }

            double score = 0.0;
            List<String> reasons = new ArrayList<>();

            // 1. Rating contribution (0 - 40 points, normalized: rating / 5.0 * 40)
            Double rating = v.getRating() != null ? v.getRating() : 0.0;
            double ratingScore = Math.min(5.0, Math.max(0.0, rating)) / 5.0 * 40.0;
            score += ratingScore;
            if (rating >= 4.0) {
                reasons.add("High rating (" + rating + "★)");
            }

            // 2. Location match (0 or 30 points)
            String vendorLocation = v.getLocation() != null ? v.getLocation().trim().toLowerCase() : "";
            if (!eventLocation.isEmpty() && !vendorLocation.isEmpty() && eventLocation.equalsIgnoreCase(vendorLocation)) {
                score += 30.0;
                reasons.add("Matches event location (" + v.getLocation() + ")");
            }

            // 3. Category match (0 or 20 points)
            String vendorCategory = v.getServiceCategory() != null ? v.getServiceCategory().trim().toLowerCase() : "";
            boolean categoryMatched = false;
            if (!vendorCategory.isEmpty()) {
                if (preferredCategories.contains(vendorCategory) || (!eventType.isEmpty() && vendorCategory.contains(eventType))) {
                    categoryMatched = true;
                }
            }
            if (categoryMatched) {
                score += 20.0;
                reasons.add("Recommended category for " + event.getEventType());
            }

            // 4. Budget fit (0 - 10 points)
            if (eventBudget != null && eventBudget > 0) {
                double budgetRatio = 1.0 - (price / eventBudget);
                double budgetScore = Math.max(0.0, Math.min(1.0, budgetRatio)) * 10.0;
                score += budgetScore;
                reasons.add("Within budget (" + String.format("%.0f%%", (price / eventBudget) * 100) + " of budget)");
            }

            double roundedScore = Math.round(score * 10.0) / 10.0;
            String reasonText = String.join(", ", reasons);

            eligible.add(new VendorRecommendationDto(
                    v.getId(),
                    v.getVendorName(),
                    v.getServiceCategory(),
                    v.getLocation(),
                    v.getContactEmail(),
                    v.getPrice(),
                    v.getRating(),
                    roundedScore,
                    reasonText
            ));
        }

        // Rank by matchScore descending; tie-break by rating descending, then price ascending
        eligible.sort(Comparator
                .comparing(VendorRecommendationDto::getMatchScore, Comparator.reverseOrder())
                .thenComparing(VendorRecommendationDto::getRating, Comparator.reverseOrder())
                .thenComparing(VendorRecommendationDto::getPrice)
        );

        return eligible;
    }

    private void validateEventOwnershipOrAdmin(Event event, UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to access event recommendations");
        }
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        if (event.getCreatedBy() != null && !event.getCreatedBy().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You are not authorized to view recommendations for this event");
        }
    }
}
