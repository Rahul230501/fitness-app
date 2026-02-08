package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityAiService {

    private final GeminiService geminiService;

    public void generateRecommendation(Activity activity){
        geminiService.getRecommendation(createPromptForActivity(activity));
    }

    private String createPromptForActivity(Activity activity) {
        return """
            You are an AI fitness recommendation engine used in a production-grade health application.
            
            OBJECTIVE:
            Generate accurate, personalized fitness recommendations based strictly on the provided workout data.
            
            ANALYSIS GUIDELINES:
            - Evaluate workout performance and intensity
            - Identify practical improvement areas
            - Recommend the next suitable workout
            - Provide recovery and safety guidance
            - Keep advice realistic and injury-aware
            
            STRICT OUTPUT RULES (MANDATORY):
            - Output ONLY valid JSON
            - Do NOT include markdown, explanations, or extra text
            - Do NOT change JSON structure
            - JSON must be the final output
            
            REQUIRED JSON STRUCTURE:
            {
              "recommendation": "",
              "improvements": [],
              "suggestions": [],
              "safety": []
            }
            
            Workout Data:
            Activity Type: %s
            Duration: %d minutes
            Calories Burned: %d
            Additional Metrics: %s
            
            Generate a concise but detailed analysis.
            NOW RETURN JSON ONLY:
            """
                            .formatted(
                                    activity.getType(),
                                    activity.getDuration(),
                                    activity.getCaloriesBurned(),
                                    activity.getAdditionalMetrics()
                            );
    }
}
