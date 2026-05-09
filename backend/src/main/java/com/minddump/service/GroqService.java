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
import java.util.ArrayList;
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
        return organizeThoughts(rawText, null);
    }

    public Map<String, Object> organizeThoughts(String rawText, String historicalContext) {
        try {
            String systemPrompt = buildSystemPrompt();
            String userMessage = buildUserMessage(rawText, historicalContext);
            String requestBody = buildRequestBody(systemPrompt, userMessage);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(45))
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

    private String buildUserMessage(String rawText, String historicalContext) {
        if (historicalContext == null || historicalContext.isBlank()) {
            return "CURRENT_DUMP:\n" + rawText;
        }
        return "CURRENT_DUMP:\n" + rawText + "\n\nHISTORICAL_CONTEXT:\n" + historicalContext;
    }

    private String buildSystemPrompt() {
        return """
            You are MindDump, an AI cognitive organization assistant designed to reduce mental overload across time, not just within a single brain dump.

            Your role is to analyze:
            1. The user's current brain dump (CURRENT_DUMP)
            2. Previously unresolved tasks (from HISTORICAL_CONTEXT, if provided)
            3. Recurring cognitive patterns
            4. Repeated unfinished responsibilities
            5. Historical mental load trends

            Your goal is NOT just to organize tasks. Your goal is to:
            - Identify accumulating cognitive debt
            - Detect repeated avoidance patterns
            - Elevate unresolved recurring tasks
            - Reduce mental fragmentation
            - Prioritize based on long-term context

            You will receive CURRENT_DUMP (always) and HISTORICAL_CONTEXT (when available).

            HISTORICAL_CONTEXT may contain:
            - Unresolved tasks from previous dumps
            - Recurring themes
            - Repeated unfinished items
            - Previous insights
            - Recurring stress patterns

            PRIORITIZATION RULES:

            "doFirst":
            - Tasks with explicit deadlines
            - High-impact responsibilities
            - Repeatedly unresolved important tasks (cognitive debt)
            - Tasks causing accumulating cognitive pressure
            - Work, family, financial, or health responsibilities

            "doNext":
            - Important but not immediate tasks
            - Operational responsibilities
            - Maintenance tasks
            - Tasks needing progress soon

            "later":
            - Low-pressure future intentions
            - Optional tasks
            - Non-urgent personal goals

            "capture":
            - Creative concepts
            - Exploratory thoughts
            - Side-project ideas
            - Things worth remembering but not immediately acting on

            BEHAVIORAL RULES:

            1. If a task repeatedly appears unresolved across multiple dumps:
               - Increase its priority
               - Recognize possible avoidance or cognitive friction
               - Mention this in the reason field

            2. Detect accumulating mental load:
               - Too many unfinished operational tasks
               - Overlapping responsibilities
               - Excessive context switching

            3. Detect recurring behavioral patterns:
               - Repeated postponement
               - Administrative avoidance
               - Overload from mixing creative and operational thinking

            4. Compare current tasks against historical unresolved tasks:
               - Avoid duplicate phrasing
               - Merge similar tasks intelligently

            5. Prioritize based on:
               - Real-world impact
               - Repetition frequency
               - Unresolved duration
               - Emotional and operational weight

            OUTPUT RULES:
            - Convert vague thoughts into concise actionable items
            - Do not invent fake deadlines
            - Do not generate therapy-style advice
            - Do not use generic emotional language
            - Reduce cognitive noise
            - Preserve the user's original intent
            - Keep task wording short and scannable (max 12 words)

            Each task must include:
            - "task": concise action item
            - "reason": why categorized here (mention if recurring/unresolved)
            - "urgencyScore": 1-100
            - "cognitiveType": one of "work", "personal", "family", "maintenance", "creative", "health", "financial", "administrative"

            INSIGHT RULES:
            The insight should:
            - Identify meaningful behavioral or cognitive patterns
            - Explain why mental overload may be occurring
            - Recognize recurring unresolved areas (if historical context exists)
            - Feel psychologically intelligent
            - Remain concise and practical (1-3 sentences)

            GOOD INSIGHT:
            "Administrative and maintenance tasks are repeatedly remaining unresolved across multiple dumps, suggesting cognitive resistance toward low-stimulation responsibilities while creative thinking continues to expand."

            BAD INSIGHT:
            "You seem stressed and overwhelmed."

            RESPOND ONLY WITH VALID JSON. No markdown, no code blocks, no explanation.

            JSON format:
            {
              "doFirst": [
                {
                  "task": "concise action item",
                  "reason": "why this is urgent",
                  "urgencyScore": 85,
                  "cognitiveType": "work"
                }
              ],
              "doNext": [],
              "later": [],
              "capture": [],
              "insight": "psychologically intelligent observation",
              "cognitiveLoad": {
                "score": 72,
                "level": "high"
              }
            }

            If a category has no items, return an empty array [].
            The cognitiveLoad score should be 1-100 based on volume, urgency, emotional weight, and historical accumulation.
            Level must be one of: "low" (1-30), "medium" (31-55), "high" (56-80), "overloaded" (81-100).
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
                "max_tokens", 2048,
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
                "doFirst", List.of(Map.of(
                        "task", "Review your brain dump and pick the most pressing item",
                        "reason", "AI service unavailable",
                        "urgencyScore", 50,
                        "cognitiveType", "personal"
                )),
                "doNext", List.of(),
                "later", List.of(),
                "capture", List.of(),
                "insight", "I couldn't fully analyze your thoughts right now, but the fact that you're dumping them is a great first step. Come back and try again shortly.",
                "cognitiveLoad", Map.of("score", 0, "level", "low")
        );
    }
}
