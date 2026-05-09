package com.minddump.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minddump.dto.DumpResponse;
import com.minddump.model.Dump;
import com.minddump.repository.DumpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DumpService {

    private final DumpRepository dumpRepository;
    private final GroqService groqService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public DumpResponse createDump(String rawText) {
        Map<String, Object> organized = groqService.organizeThoughts(rawText);

        Dump dump = Dump.builder()
                .rawText(rawText)
                .doFirstJson(toJson(organized.get("doFirst")))
                .doNextJson(toJson(organized.get("doNext")))
                .laterJson(toJson(organized.get("later")))
                .captureJson(toJson(organized.get("capture")))
                .insight((String) organized.get("insight"))
                .cognitiveLoadJson(toJson(organized.get("cognitiveLoad")))
                .build();

        Dump saved = dumpRepository.save(dump);
        return toResponse(saved);
    }

    public List<DumpResponse> getAllDumps() {
        return dumpRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<DumpResponse> getDumpById(Long id) {
        return dumpRepository.findById(id).map(this::toResponse);
    }

    public long getDumpCount() {
        return dumpRepository.count();
    }

    public DumpResponse toggleItem(Long id, String item) {
        Dump dump = dumpRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dump not found"));

        List<String> completed = new ArrayList<>(stringToList(dump.getCompletedItems()));

        if (completed.contains(item)) {
            completed.remove(item);
        } else {
            completed.add(item);
        }

        dump.setCompletedItems(completed.isEmpty() ? "" : String.join("||", completed));
        Dump saved = dumpRepository.save(dump);
        return toResponse(saved);
    }

    public String getPatternInsight() {
        List<Dump> allDumps = dumpRepository.findAllByOrderByCreatedAtDesc();
        if (allDumps.size() < 5) {
            return null;
        }

        List<String> allDoFirst = allDumps.stream()
                .map(d -> extractTaskTexts(d.getDoFirstJson()))
                .flatMap(List::stream)
                .collect(Collectors.toList());

        List<String> allInsights = allDumps.stream()
                .map(Dump::getInsight)
                .filter(i -> i != null && !i.isEmpty())
                .collect(Collectors.toList());

        String patternData = "Urgent items across " + allDumps.size() + " dumps:\n" +
                String.join(", ", allDoFirst) + "\n\nInsights:\n" +
                String.join("\n", allInsights);

        Map<String, Object> result = groqService.organizeThoughts(
                "PATTERN ANALYSIS MODE: Analyze these recurring patterns from multiple brain dumps and identify the top stress/productivity patterns. " +
                "Give insight about what keeps coming up and what the user should address: " + patternData
        );

        return (String) result.get("insight");
    }

    @SuppressWarnings("unchecked")
    private List<String> extractTaskTexts(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            List<Map<String, Object>> tasks = objectMapper.readValue(json, new TypeReference<>() {});
            return tasks.stream()
                    .map(t -> (String) t.getOrDefault("task", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private DumpResponse toResponse(Dump dump) {
        return DumpResponse.builder()
                .id(dump.getId())
                .rawText(dump.getRawText())
                .doFirst(fromJsonList(dump.getDoFirstJson()))
                .doNext(fromJsonList(dump.getDoNextJson()))
                .later(fromJsonList(dump.getLaterJson()))
                .capture(fromJsonList(dump.getCaptureJson()))
                .insight(dump.getInsight())
                .cognitiveLoad(fromJsonMap(dump.getCognitiveLoadJson()))
                .completedItems(stringToList(dump.getCompletedItems()))
                .createdAt(dump.getCreatedAt())
                .build();
    }

    private String toJson(Object obj) {
        if (obj == null) return "[]";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fromJsonList(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJsonMap(String json) {
        if (json == null || json.isEmpty()) return Map.of("score", 0, "level", "low");
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of("score", 0, "level", "low");
        }
    }

    private List<String> stringToList(String str) {
        if (str == null || str.isEmpty()) return List.of();
        return Arrays.asList(str.split("\\|\\|"));
    }
}
