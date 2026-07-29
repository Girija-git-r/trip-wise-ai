package com.tripwise.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripwise.ai.dto.ai.AiItineraryResultDto;
import com.tripwise.ai.entity.Trip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generates itineraries via Google Gemini's free-tier API using structured
 * JSON output (responseSchema), so the model's reply parses directly into
 * {@link AiItineraryResultDto} without brittle free-text scraping.
 * Any failure (missing key, network error, malformed response) surfaces as
 * an empty Optional so the caller can fall back to the rule-based generator.
 */
@Service
@Slf4j
public class GeminiAiService {

    private static final List<String> ACTIVITY_CATEGORIES = List.of(
            "SIGHTSEEING", "FOOD", "EXPERIENCE", "ADVENTURE", "RELAXATION", "CULTURE", "SHOPPING", "TRANSPORT");
    private static final List<String> PACKING_CATEGORIES = List.of(
            "DOCUMENTS", "CLOTHING", "ELECTRONICS", "TOILETRIES", "HEALTH", "MISC");

    private final RestClient geminiRestClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.gemini.enabled}")
    private boolean enabled;

    @Value("${app.ai.gemini.api-key}")
    private String apiKey;

    @Value("${app.ai.gemini.model}")
    private String model;

    public GeminiAiService(RestClient geminiRestClient, ObjectMapper objectMapper) {
        this.geminiRestClient = geminiRestClient;
        this.objectMapper = objectMapper;
    }

    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    public Optional<AiItineraryResultDto> generate(Trip trip) {
        if (!isAvailable()) {
            return Optional.empty();
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", buildPrompt(trip))))),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "responseSchema", buildSchema(),
                            "temperature", 0.85
                    )
            );

            JsonNode response = geminiRestClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            String text = response
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText(null);

            if (text == null || text.isBlank()) {
                log.warn("Gemini response contained no text payload");
                return Optional.empty();
            }

            AiItineraryResultDto result = objectMapper.readValue(text, AiItineraryResultDto.class);

            if (!isValid(result, trip)) {
                log.warn("Gemini response failed validation, falling back to rule-based generator");
                return Optional.empty();
            }

            return Optional.of(result);
        } catch (Exception ex) {
            log.warn("Gemini itinerary generation failed, falling back to rule-based generator: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private boolean isValid(AiItineraryResultDto result, Trip trip) {
        if (result == null || result.days == null || result.packingList == null) return false;
        if (result.days.size() != trip.getDays()) return false;
        if (result.packingList.isEmpty()) return false;
        return result.days.stream().allMatch(d -> d.activities != null && !d.activities.isEmpty());
    }

    private String buildPrompt(Trip trip) {
        String interests = trip.getInterests() == null || trip.getInterests().isEmpty()
                ? "general sightseeing"
                : String.join(", ", trip.getInterests());

        return """
                You are an expert travel planner. Create a detailed %d-day travel itinerary and a smart \
                packing list for a trip to %s.

                Traveler details:
                - Total budget: ₹%.2f (Indian Rupees)
                - Travel type: %s
                - Interests: %s

                Requirements:
                - Provide exactly %d days, numbered 1 to %d in order.
                - Each day needs 3-5 realistic, specific activities. Use real neighborhoods, landmarks, or \
                dishes for this destination when you know them, and tailor at least one activity per day to \
                the traveler's interests.
                - Day titles should be short and descriptive (e.g. "Historic Old Town & Local Markets").
                - The packing list should have 14-20 items grouped by category, with a short practical tip \
                on at least half of the items, tailored to the destination's climate/culture and the \
                traveler's interests and budget.
                - Keep the plan realistic for the stated budget.
                """.formatted(
                trip.getDays(), trip.getDestination(),
                trip.getBudget(), trip.getTravelType(), interests,
                trip.getDays(), trip.getDays()
        );
    }

    private Map<String, Object> buildSchema() {
        Map<String, Object> activitySchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "description", Map.of("type", "STRING"),
                        "category", Map.of("type", "STRING", "enum", ACTIVITY_CATEGORIES)
                ),
                "required", List.of("description", "category")
        );

        Map<String, Object> daySchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "dayNumber", Map.of("type", "INTEGER"),
                        "title", Map.of("type", "STRING"),
                        "activities", Map.of("type", "ARRAY", "items", activitySchema)
                ),
                "required", List.of("dayNumber", "title", "activities")
        );

        Map<String, Object> packingItemSchema = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "name", Map.of("type", "STRING"),
                        "category", Map.of("type", "STRING", "enum", PACKING_CATEGORIES),
                        "tip", Map.of("type", "STRING")
                ),
                "required", List.of("name", "category")
        );

        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "days", Map.of("type", "ARRAY", "items", daySchema),
                        "packingList", Map.of("type", "ARRAY", "items", packingItemSchema)
                ),
                "required", List.of("days", "packingList")
        );
    }
}
