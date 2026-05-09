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

    private static final int HISTORY_LIMIT = 10;

    @SuppressWarnings("unchecked")
    public DumpResponse createDump(String rawText) {
        String historicalContext = buildHistoricalContext();
        Map<String, Object> organized = groqService.organizeThoughts(rawText, historicalContext);

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
        if (allDumps.size() < 3) {
            return null;
        }

        String context = buildHistoricalContext();
        Map<String, Object> result = groqService.organizeThoughts(
                "PATTERN ANALYSIS MODE: Based on the historical context provided, identify the top recurring stress and productivity patterns. Focus on what keeps appearing unresolved and what the user should prioritize.",
                context
        );

        return (String) result.get("insight");
    }

    // ── Historical Context Builder ──

    private String buildHistoricalContext() {
        List<Dump> recentDumps = dumpRepository.findAllByOrderByCreatedAtDesc();
        if (recentDumps.isEmpty()) {
            return null;
        }

        int limit = Math.min(recentDumps.size(), HISTORY_LIMIT);
        List<Dump> history = recentDumps.subList(0, limit);

        StringBuilder ctx = new StringBuilder();

        // 1. Unresolved tasks (tasks not in completedItems)
        List<String> unresolvedTasks = new ArrayList<>();
        for (Dump d : history) {
            Set<String> completed = new HashSet<>(stringToList(d.getCompletedItems()));
            collectUnresolved(d.getDoFirstJson(), completed, unresolvedTasks);
            collectUnresolved(d.getDoNextJson(), completed, unresolvedTasks);
            collectUnresolved(d.getLaterJson(), completed, unresolvedTasks);
        }

        if (!unresolvedTasks.isEmpty()) {
            ctx.append("UNRESOLVED TASKS FROM PREVIOUS DUMPS:\n");
            unresolvedTasks.stream().distinct().forEach(t -> ctx.append("- ").append(t).append("\n"));
            ctx.append("\n");
        }

        // 2. Recurring themes (task texts that appear in 2+ dumps)
        Map<String, Integer> taskFrequency = new HashMap<>();
        for (Dump d : history) {
            Set<String> dumpTasks = new HashSet<>();
            dumpTasks.addAll(extractTaskTexts(d.getDoFirstJson()));
            dumpTasks.addAll(extractTaskTexts(d.getDoNextJson()));
            dumpTasks.addAll(extractTaskTexts(d.getLaterJson()));
            dumpTasks.addAll(extractTaskTexts(d.getCaptureJson()));
            for (String task : dumpTasks) {
                taskFrequency.merge(task.toLowerCase().trim(), 1, Integer::sum);
            }
        }

        List<String> recurring = taskFrequency.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> e.getKey() + " (appeared " + e.getValue() + " times)")
                .collect(Collectors.toList());

        if (!recurring.isEmpty()) {
            ctx.append("RECURRING THEMES (appeared in multiple dumps):\n");
            recurring.forEach(r -> ctx.append("- ").append(r).append("\n"));
            ctx.append("\n");
        }

        // 3. Previous insights
        List<String> insights = history.stream()
                .map(Dump::getInsight)
                .filter(i -> i != null && !i.isBlank())
                .limit(5)
                .collect(Collectors.toList());

        if (!insights.isEmpty()) {
            ctx.append("PREVIOUS INSIGHTS:\n");
            insights.forEach(i -> ctx.append("- ").append(i).append("\n"));
            ctx.append("\n");
        }

        // 4. Cognitive load trend
        List<String> loadTrend = new ArrayList<>();
        for (Dump d : history) {
            Map<String, Object> load = fromJsonMap(d.getCognitiveLoadJson());
            Object score = load.get("score");
            Object level = load.get("level");
            if (score != null && !score.equals(0)) {
                loadTrend.add(level + " (" + score + "/100)");
            }
        }

        if (!loadTrend.isEmpty()) {
            ctx.append("COGNITIVE LOAD TREND (recent to old): ");
            ctx.append(String.join(" → ", loadTrend));
            ctx.append("\n");
        }

        String result = ctx.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private void collectUnresolved(String json, Set<String> completed, List<String> output) {
        for (String task : extractTaskTexts(json)) {
            if (!completed.contains(task)) {
                output.add(task);
            }
        }
    }

    // ── JSON Helpers ──

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
