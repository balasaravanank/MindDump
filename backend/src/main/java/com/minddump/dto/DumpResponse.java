package com.minddump.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DumpResponse {
    private Long id;
    private String rawText;
    private List<Map<String, Object>> doFirst;
    private List<Map<String, Object>> doNext;
    private List<Map<String, Object>> later;
    private List<Map<String, Object>> capture;
    private String insight;
    private Map<String, Object> cognitiveLoad;
    private List<String> completedItems;
    private LocalDateTime createdAt;
}
