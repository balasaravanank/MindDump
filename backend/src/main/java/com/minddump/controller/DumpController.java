package com.minddump.controller;

import com.minddump.dto.DumpRequest;
import com.minddump.dto.DumpResponse;
import com.minddump.service.DumpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DumpController {

    private final DumpService dumpService;

    @PostMapping("/dump")
    public ResponseEntity<DumpResponse> createDump(@RequestBody DumpRequest request) {
        if (request.getRawText() == null || request.getRawText().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        DumpResponse response = dumpService.createDump(request.getRawText().trim());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dumps")
    public ResponseEntity<List<DumpResponse>> getAllDumps() {
        return ResponseEntity.ok(dumpService.getAllDumps());
    }

    @GetMapping("/dumps/{id}")
    public ResponseEntity<DumpResponse> getDumpById(@PathVariable Long id) {
        return dumpService.getDumpById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dumps/count")
    public ResponseEntity<Map<String, Long>> getDumpCount() {
        return ResponseEntity.ok(Map.of("count", dumpService.getDumpCount()));
    }

    @GetMapping("/pattern-insight")
    public ResponseEntity<Map<String, String>> getPatternInsight() {
        String insight = dumpService.getPatternInsight();
        if (insight == null) {
            return ResponseEntity.ok(Map.of(
                    "insight", "",
                    "message", "Need at least 5 dumps to detect patterns"
            ));
        }
        return ResponseEntity.ok(Map.of("insight", insight));
    }

    @PatchMapping("/dumps/{id}/toggle-item")
    public ResponseEntity<DumpResponse> toggleItem(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String item = body.get("item");
        if (item == null || item.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        DumpResponse response = dumpService.toggleItem(id, item.trim());
        return ResponseEntity.ok(response);
    }
}
