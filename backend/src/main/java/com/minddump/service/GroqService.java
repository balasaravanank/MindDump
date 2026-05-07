package com.minddump.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GroqService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> organizeThoughts(String rawText) {
        try {
            String systemPrompt = buildSystemPrompt();
            String requestBody = buildRequestBody(systemPrompt, rawText);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Groq API error: " + response.statusCode() + " - " + response.body());
                return getFallbackResponse(rawText);
            }

            return parseGroqResponse(response.body());

        } catch (Exception e) {
            System.err.println("Error calling Groq API: " + e.getMessage());
            return getFallbackResponse(rawText);
        }
    }

    private String buildSystemPrompt() {
        return """
            You are MindDump AI — a mental clarity assistant. The user will give you a messy, unfiltered brain dump of everything on their mind. Your job is to organize it into clarity.

            RESPOND ONLY WITH VALID JSON. No markdown, no code blocks, no explanation.

            JSON format:
            {
              "urgent": ["task1", "task2"],
              "thisWeek": ["task1", "task2"],
              "someday": ["task1", "task2"],
              "ideas": ["idea1", "idea2"],
              "insight": "One honest, direct observation about the user's current mental state. Be empathetic but real. Keep it to 1-2 sentences."
            }

            Rules:
            - "urgent": Things that need attention TODAY. Use action-oriented language.
            - "thisWeek": Medium-priority items for this week. Be specific.
            - "someday": Low-urgency dreams, goals, or things to revisit later.
            - "ideas": Creative thoughts, side projects, or interesting concepts to capture.
            - "insight": Read between the lines. What is the user REALLY feeling? Are they overwhelmed? Avoiding something? Excited but scattered? Be honest, slightly direct, and helpful. This is the most important part.

            If a category has no items, return an empty array [].
            Keep each item concise — max 10 words per item.
            The insight should feel like a wise friend telling you the truth.
            """;
    }

    private String buildRequestBody(String systemPrompt, String userText) throws Exception {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userText)
                ),
                "temperature", 0.7,
                "max_tokens", 1024,
                "response_format", Map.of("type", "json_object")
        );
        return objectMapper.writeValueAsString(body);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGroqResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        return objectMapper.readValue(content, Map.class);
    }

    private Map<String, Object> getFallbackResponse(String rawText) {
        return Map.of(
                "urgent", List.of("Review your brain dump and pick the most pressing item"),
                "thisWeek", List.of("Revisit this dump when AI service is available"),
                "someday", List.of(),
                "ideas", List.of(),
                "insight", "I couldn't fully analyze your thoughts right now, but the fact that you're dumping them is a great first step. Come back and try again shortly."
        );
    }
}
