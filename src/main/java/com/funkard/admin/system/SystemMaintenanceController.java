package com.funkard.admin.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@Slf4j
public class SystemMaintenanceController {

    // Valori temporanei in memoria (niente DB per ora, leggerissimo)
    private static final AtomicReference<CleanupStatus> lastCleanupStatus = new AtomicReference<>();

    @PostMapping("/cleanup/status")
    public ResponseEntity<String> updateCleanupStatus(@RequestBody CleanupStatus status) {
        status.setTimestamp(LocalDateTime.now());
        lastCleanupStatus.set(status);
        log.info("🧾 Updated Cleanup Status: {}", status);
        return ResponseEntity.ok("✅ Cleanup status updated");
    }

    @GetMapping("/cleanup/status")
    public ResponseEntity<CleanupStatus> getCleanupStatus() {
        return ResponseEntity.ok(lastCleanupStatus.get() != null
                ? lastCleanupStatus.get()
                : new CleanupStatus("unknown", 0, LocalDateTime.now()));
    }

    public record CleanupStatus(String result, int deleted, LocalDateTime timestamp) {}
}
