package com.funkard.admin.controller;

import com.funkard.admin.service.AdminNotificationCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cleanup")
@RequiredArgsConstructor
public class AdminCleanupController {

    private final AdminNotificationCleanupService cleanupService;

    // 🧹 Pulizia manuale
    @PostMapping("/manual")
    public ResponseEntity<Map<String, Object>> manualCleanup(
            @RequestParam(defaultValue = "30") int daysOld) {
        
        int deletedCount = cleanupService.manualCleanup(daysOld);
        
        Map<String, Object> result = Map.of(
            "status", "success",
            "deletedCount", deletedCount,
            "daysOld", daysOld,
            "message", "Pulizia manuale completata"
        );
        
        return ResponseEntity.ok(result);
    }

    // 📊 Statistiche pulizia
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCleanupStats() {
        cleanupService.printCleanupStats();
        
        Map<String, Object> stats = Map.of(
            "status", "success",
            "message", "Statistiche stampate nei log",
            "nextCleanup", "Ogni giorno alle 03:00",
            "retentionDays", 30
        );
        
        return ResponseEntity.ok(stats);
    }

    // 🔄 Test pulizia (solo per sviluppo)
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testCleanup() {
        // Test con 1 giorno per vedere se funziona
        int deletedCount = cleanupService.manualCleanup(1);
        
        Map<String, Object> result = Map.of(
            "status", "success",
            "deletedCount", deletedCount,
            "message", "Test pulizia completato (rimossi record più vecchi di 1 giorno)"
        );
        
        return ResponseEntity.ok(result);
    }
}
