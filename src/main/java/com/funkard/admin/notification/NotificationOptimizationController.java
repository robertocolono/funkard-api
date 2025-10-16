package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications/optimization")
@RequiredArgsConstructor
public class NotificationOptimizationController {

    private final NotificationOptimizationService optimizationService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getOptimizedStats() {
        return ResponseEntity.ok(optimizationService.getOptimizedStats());
    }

    @PostMapping("/refresh-cache")
    public ResponseEntity<Map<String, String>> refreshCache() {
        optimizationService.refreshStatsCache();
        return ResponseEntity.ok(Map.of("status", "Cache refreshed successfully"));
    }

    @PostMapping("/clear-cache")
    public ResponseEntity<Map<String, String>> clearCache() {
        optimizationService.clearCache();
        return ResponseEntity.ok(Map.of("status", "Cache cleared successfully"));
    }

    @PostMapping("/batch-mark-read")
    public ResponseEntity<Map<String, String>> batchMarkAsRead(@RequestBody List<Long> notificationIds) {
        optimizationService.batchMarkAsRead(notificationIds);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "processed", String.valueOf(notificationIds.size())
        ));
    }

    @PostMapping("/batch-resolve-archive")
    public ResponseEntity<Map<String, String>> batchResolveAndArchive(@RequestBody List<Long> notificationIds) {
        optimizationService.batchResolveAndArchive(notificationIds);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "processed", String.valueOf(notificationIds.size())
        ));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        return ResponseEntity.ok(optimizationService.getDashboardData());
    }
}
