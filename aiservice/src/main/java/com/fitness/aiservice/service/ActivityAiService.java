package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityAiService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String response = geminiService.getRecommendation(createPromptForActivity(activity));
        return processAiResponse(response,activity);
    }

    private Recommendation processAiResponse(String response,Activity activity) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode textNode = jsonNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");
            String jsonContentInStringForm = textNode.asString()
                    .replace("```json\\n","")
                    .replace("\\n","")
                    .trim();
            JsonNode jsonContent = objectMapper.readTree(jsonContentInStringForm);
            JsonNode analysisNode = jsonContent.path("analysis");

            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis,analysisNode,"overall","Overall: ");
            addAnalysisSection(fullAnalysis,analysisNode,"pace","Pace: ");
            addAnalysisSection(fullAnalysis,analysisNode,"heartRate","Heart Rate: ");
            addAnalysisSection(fullAnalysis,analysisNode,"caloriesBurned","Calories Burned: ");

            List<String> improvement =  extractImprovement(jsonContent.path("improvements"));
            List<String> suggestions =  extractSuggestion(jsonContent.path("suggestions"));
            List<String> safety =  extractSafety(jsonContent.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType().toString())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvement)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDate.now())
                    .build();

        }catch (Exception e){
            e.printStackTrace();
            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType().toString())
                    .recommendation("Consistent physical activity like this supports overall fitness, endurance, and long-term health.")
                    .improvements(Collections.singletonList("Maintain consistency and gradually increase intensity to continue improving performance."))
                    .suggestions(Collections.singletonList("Include proper warm-up, cool-down, and recovery in your next workout session."))
                    .safety(List.of(
                            "Warm up before starting the workout",
                            "Maintain proper form throughout the activity",
                            "Stop immediately if pain or dizziness occurs"
                    ))
                    .createdAt(LocalDate.now())
                    .build();
        }
    }

    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safeties = new ArrayList<>();
        safetyNode.forEach(safety->{
                    safeties.add(safety.toString());
                }
        );
        return  safeties.isEmpty()?Collections.singletonList("No specific safety guide provided"):safeties;
    }

    private List<String> extractSuggestion(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        suggestionsNode.forEach(suggestion->{
                    String workout = suggestion.path("workout").toString();
                    String description = suggestion.path("description").toString();
            suggestions.add(String.format("%s: %s",workout,description));
                }
        );
        return  suggestions.isEmpty()? Collections.singletonList("No specific suggestion provided"):suggestions;
    }

    private List<String> extractImprovement(JsonNode improvementNode) {
        List<String> improvements = new ArrayList<>();
        improvementNode.forEach(improvement->{
              String area = improvement.path("area").toString();
              String detail = improvement.path("recommendation").toString();
              improvements.add(String.format("%s: %s",area,detail));
                }
                );
        return  improvements.isEmpty()? Collections.singletonList("No specific improvement provided"):improvements;
    }


    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
            if(!analysisNode.path(key).isMissingNode()){
                fullAnalysis.append(prefix)
                        .append(analysisNode.path(key).asString())
                        .append("\n\n");
            }
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
