package com.minddump.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DumpResponse {
    private Long id;
    private String rawText;
    private List<String> urgent;
    private List<String> thisWeek;
    private List<String> someday;
    private List<String> ideas;
    private String insight;
    private LocalDateTime createdAt;
}
