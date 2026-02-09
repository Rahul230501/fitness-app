package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityAiService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String response = geminiService.getRecommendation(createPromptForActivity(activity));
        return processAiResponse(response);
    }

    private Recommendation processAiResponse(String response) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode textNode = jsonNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");
            String jsonContent = textNode.asString()
                    .replace("```json\\n","")
                    .replace("\\n","")
                    .trim();
            JsonNode analysisJson = objectMapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            System.out.println("Ai response :"+ jsonContent);


        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
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
            
            REQUIRED JSON FORMAT:
            {
              "analysis": {
                "overall": "",
                "pace": "",
                "heartRate": "",
                "caloriesBurned": ""
              },
              "improvements": [
                {
                  "area": "",
                  "recommendation": ""
                }
              ],
              "suggestions": [
                {
                  "workout": "",
                  "description": ""
                }
              ],
              "safety": [
                ""
              ]
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
