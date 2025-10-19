package com.funkard.admin.log;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminActionLogController {

    private final AdminActionLogRepository logRepository;

    @GetMapping("/{type}/{id}")
    public ResponseEntity<List<AdminActionLog>> getHistory(
            @PathVariable String type, @PathVariable Long id) {
        List<AdminActionLog> logs = logRepository.findByTargetIdAndTargetTypeOrderByCreatedAtAsc(id, type.toUpperCase());
        return ResponseEntity.ok(logs);
    }

    // 🧹 cleanup automatico per log vecchi di 2 mesi
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(2);
        int deleted = logRepository.deleteOlderThan(cutoff);
        return ResponseEntity.ok("🧹 Deleted " + deleted + " old admin logs (older than 2 months)");
    }
}
