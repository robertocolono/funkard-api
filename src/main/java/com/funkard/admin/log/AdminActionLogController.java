package com.funkard.admin.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@Slf4j
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

        // 🔎 Log su Render console
        if (deleted > 0) {
            log.info("🧹 Funkard Admin Logs Cleanup — deleted {} old entries (older than {})", deleted, cutoff);
        } else {
            log.info("✅ Funkard Admin Logs Cleanup — no old entries to delete (checked up to {})", cutoff);
        }

        return ResponseEntity.ok("🧹 Deleted " + deleted + " old admin logs (older than 2 months)");
    }
}
