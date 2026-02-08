package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    public List<Recommendation> getRecommendationsForUser(String userId) {
         return recommendationRepository.findByUserId(userId);
    }

    public Recommendation getRecommendationForActivity(String activityId) {
         return recommendationRepository.findByActivityId(activityId)
                 .orElseThrow(()-> new RuntimeException("No recommendation found for activity id " + activityId));
    }
}
