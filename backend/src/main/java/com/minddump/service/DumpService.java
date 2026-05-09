package com.minddump.service;

import com.minddump.dto.DumpResponse;
import com.minddump.model.Dump;
import com.minddump.repository.DumpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DumpService {

    private final DumpRepository dumpRepository;
    private final GroqService groqService;

    public DumpResponse createDump(String rawText) {
        Map<String, Object> organized = groqService.organizeThoughts(rawText);

        Dump dump = Dump.builder()
                .rawText(rawText)
                .urgent(listToString(organized.get("urgent")))
                .thisWeek(listToString(organized.get("thisWeek")))
                .someday(listToString(organized.get("someday")))
                .ideas(listToString(organized.get("ideas")))
                .insight((String) organized.get("insight"))
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

        List<String> allUrgent = allDumps.stream()
                .map(Dump::getUrgent)
                .filter(u -> u != null && !u.isEmpty())
                .flatMap(u -> Arrays.stream(u.split("\\|\\|")))
                .collect(Collectors.toList());

        List<String> allInsights = allDumps.stream()
                .map(Dump::getInsight)
                .filter(i -> i != null && !i.isEmpty())
                .collect(Collectors.toList());

        String patternData = "Urgent items across " + allDumps.size() + " dumps:\n" +
                String.join(", ", allUrgent) + "\n\nInsights:\n" +
                String.join("\n", allInsights);

        Map<String, Object> result = groqService.organizeThoughts(
                "PATTERN ANALYSIS MODE: Analyze these recurring patterns from multiple brain dumps and identify the top stress/productivity patterns. " +
                "Give insight about what keeps coming up and what the user should address: " + patternData
        );

        return (String) result.get("insight");
    }

    private DumpResponse toResponse(Dump dump) {
        return DumpResponse.builder()
                .id(dump.getId())
                .rawText(dump.getRawText())
                .urgent(stringToList(dump.getUrgent()))
                .thisWeek(stringToList(dump.getThisWeek()))
                .someday(stringToList(dump.getSomeday()))
                .ideas(stringToList(dump.getIdeas()))
                .insight(dump.getInsight())
                .completedItems(stringToList(dump.getCompletedItems()))
                .createdAt(dump.getCreatedAt())
                .build();
    }

    @SuppressWarnings("unchecked")
    private String listToString(Object obj) {
        if (obj == null) return "";
        if (obj instanceof List) {
            List<String> list = (List<String>) obj;
            return String.join("||", list);
        }
        return obj.toString();
    }

    private List<String> stringToList(String str) {
        if (str == null || str.isEmpty()) return List.of();
        return Arrays.asList(str.split("\\|\\|"));
    }
}
