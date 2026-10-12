package com.planora.service;

import com.planora.dto.VendorRecommendationDto;
import com.planora.security.UserPrincipal;

import java.util.List;

public interface VendorRecommendationService {

    List<VendorRecommendationDto> getRecommendationsForEvent(Long eventId, UserPrincipal principal);
}
