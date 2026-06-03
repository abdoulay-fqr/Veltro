package com.veltro.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veltro.chatbot.dto.HistoryMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class GeminiService {

    private static final String SYSTEM_PROMPT =
            "You are Veltro Assistant, the AI helper for Veltro gym management platform. " +
            "You help members with questions about their subscription, bookings, activity, " +
            "and gym services. Be concise, friendly, and helpful. " +
            "Always respond in the same language the user writes in.";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${gemini.api.max-output-tokens:800}")
    private int maxOutputTokens;

    public GeminiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String generateContent(String memberContext, List<HistoryMessage> history, String currentMessage) {
        try {
            Map<String, Object> requestBody = buildRequestBody(memberContext, history, currentMessage);
            String url = apiUrl + "?key=" + apiKey;

            String responseJson = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            return extractText(responseJson);

        } catch (Exception e) {
            log.error("[Gemini] API call failed: {}", e.getClass().getSimpleName());
            return "I'm having trouble connecting right now. Please try again in a moment.";
        }
    }

    private Map<String, Object> buildRequestBody(String memberContext, List<HistoryMessage> history, String currentMessage) {
        String fullSystemPrompt = SYSTEM_PROMPT + "\n\n" + memberContext;

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("systemInstruction", Map.of(
                "parts", List.of(Map.of("text", fullSystemPrompt))
        ));

        List<Map<String, Object>> contents = new ArrayList<>();
        for (HistoryMessage msg : history) {
            contents.add(Map.of(
                    "role", msg.getRole(),
                    "parts", List.of(Map.of("text", msg.getContent()))
            ));
        }
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", currentMessage))
        ));

        body.put("contents", contents);
        body.put("generationConfig", Map.of(
                "maxOutputTokens", maxOutputTokens,
                "temperature", 0.7
        ));

        return body;
    }

    private String extractText(String responseJson) {
        if (responseJson == null || responseJson.isBlank()) {
            return "I didn't receive a response. Please try again.";
        }
        try {
            JsonNode root = objectMapper.readTree(responseJson);

            if (root.has("error")) {
                String errorMsg = root.at("/error/message").asText("unknown error");
                int code = root.at("/error/code").asInt(0);
                log.warn("[Gemini] API error {}: {}", code, errorMsg);
                if (code == 429) {
                    return "I'm experiencing high demand right now. Please try again in a moment.";
                }
                return "I'm unable to respond right now. Please try again later.";
            }

            String text = root.at("/candidates/0/content/parts/0/text").asText(null);
            if (text == null || text.isBlank()) {
                return "I wasn't able to generate a response. Please try rephrasing your question.";
            }
            return text;

        } catch (Exception e) {
            log.error("[Gemini] Failed to parse response: {}", e.getMessage());
            return "Something went wrong. Please try again.";
        }
    }
}
