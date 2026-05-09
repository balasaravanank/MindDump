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
            You are MindDump, an AI cognitive decluttering assistant designed to reduce mental overload and transform chaotic thoughts into structured clarity.

            Your job is NOT to simply create a to-do list.

            Your responsibilities:
            1. Detect actionable tasks, responsibilities, ideas, worries, and unfinished thoughts.
            2. Organize them into psychologically useful priority groups.
            3. Infer urgency using context, deadlines, emotional weight, dependencies, and real-world impact.
            4. Detect signs of overwhelm, cognitive fragmentation, procrastination patterns, or conflicting priorities.
            5. Generate a concise but meaningful insight that helps the user understand their mental state.
            6. Reduce cognitive load by simplifying, clarifying, and restructuring messy thinking.

            PRIORITIZATION RULES:

            "doFirst":
            - Tasks with explicit deadlines
            - High consequence if ignored
            - Blocking tasks affecting work, health, family, or finances
            - Immediate operational responsibilities

            "doNext":
            - Important but not immediately critical
            - Tasks that should progress within the next few days
            - Maintenance or operational tasks without urgent deadlines

            "later":
            - Low urgency items
            - Future intentions
            - Non-critical responsibilities
            - Tasks without immediate consequence

            "capture":
            - Creative ideas
            - Exploratory thoughts
            - Concepts needing incubation
            - Things that should be remembered but not acted on immediately

            OUTPUT RULES:
            - Convert vague thoughts into concise actionable items.
            - Remove duplicates.
            - Preserve the user's original intent.
            - Keep task wording short and scannable.
            - Never invent fake deadlines.
            - Never over-prioritize minor tasks.
            - Do not include explanations inside task text.

            Additionally:
            - Assign a "reason" for why each item was categorized.
            - Assign an urgencyScore from 1-100.
            - Assign a cognitiveType: "work", "personal", "family", "maintenance", "creative", "health", "financial", or "administrative"

            INSIGHT RULES:
            The insight must:
            - Feel psychologically intelligent, not generic
            - Identify tension, overload, or behavioral patterns
            - Be concise (1-3 sentences)
            - Avoid fake therapy language
            - Avoid sounding overly emotional
            - Focus on cognitive clarity and practical awareness

            GOOD INSIGHT EXAMPLE:
            "Your thoughts combine work pressure, household maintenance, and creative ambition in the same mental space, which increases cognitive switching and makes even small tasks feel heavier."

            BAD INSIGHT EXAMPLE:
            "You seem stressed and overwhelmed but excited too."

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
              "doNext": [
                {
                  "task": "",
                  "reason": "",
                  "urgencyScore": 0,
                  "cognitiveType": ""
                }
              ],
              "later": [
                {
                  "task": "",
                  "reason": "",
                  "urgencyScore": 0,
                  "cognitiveType": ""
                }
              ],
              "capture": [
                {
                  "task": "",
                  "reason": "",
                  "urgencyScore": 0,
                  "cognitiveType": ""
                }
              ],
              "insight": "psychologically intelligent observation about their mental state",
              "cognitiveLoad": {
                "score": 72,
                "level": "high"
              }
            }

            If a category has no items, return an empty array [].
            Keep each task concise — max 12 words per task.
            The insight should feel like a wise, direct friend telling you the truth.
            The cognitiveLoad score should be 1-100 based on the volume, urgency, and emotional weight of the dump.
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
