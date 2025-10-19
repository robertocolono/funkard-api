package com.funkard.admin.controller;

import com.funkard.admin.service.SystemCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/system/cleanup")
@RequiredArgsConstructor
public class SystemCleanupController {

    private final SystemCleanupService service;

    @PostMapping("/status")
    public ResponseEntity<?> receiveCleanupStatus(@RequestBody Map<String, Object> payload) {
        try {
            String result = (String) payload.getOrDefault("result", "unknown");
            int deleted = ((Number) payload.getOrDefault("deleted", 0)).intValue();
            String details = (String) payload.getOrDefault("details", null);

            service.saveCleanupResult(result, deleted, details);
            return ResponseEntity.ok(Map.of("status", "logged", "result", result, "deleted", deleted));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getCleanupLogs() {
        return ResponseEntity.ok(service.getAllLogs());
    }
}
