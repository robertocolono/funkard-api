package com.funkard.admin.controller;

import com.funkard.admin.service.AdminNotificationCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications/cleanup")
@RequiredArgsConstructor
public class AdminNotificationCleanupController {

    private final AdminNotificationCleanupService cleanupService;

    // 🧹 Pulizia manuale
    @PostMapping("/manual")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> manualCleanup(
            @RequestParam(defaultValue = "30") int daysOld) {
        
        int deletedCount = cleanupService.manualCleanup(daysOld);
        
        Map<String, Object> result = Map.of(
            "status", "success",
            "deletedCount", deletedCount,
            "daysOld", daysOld,
            "message", "Pulizia manuale completata",
            "nextScheduledCleanup", "Ogni giorno alle 03:00"
        );
        
        return ResponseEntity.ok(result);
    }

    // 📊 Statistiche pulizia
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCleanupStats() {
        cleanupService.printCleanupStats();
        
        Map<String, Object> stats = Map.of(
            "status", "success",
            "message", "Statistiche stampate nei log del server",
            "scheduledCleanup", "Ogni giorno alle 03:00 (Europe/Rome)",
            "retentionDays", 30,
            "cleanupScope", "Notifiche risolte + archivio"
        );
        
        return ResponseEntity.ok(stats);
    }

    // 🔄 Test pulizia (solo per sviluppo)
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> testCleanup() {
        // Test con 1 giorno per vedere se funziona
        int deletedCount = cleanupService.manualCleanup(1);
        
        Map<String, Object> result = Map.of(
            "status", "success",
            "deletedCount", deletedCount,
            "message", "Test pulizia completato (rimossi record più vecchi di 1 giorno)",
            "note", "Questo è solo un test - la pulizia automatica avviene ogni giorno alle 03:00"
        );
        
        return ResponseEntity.ok(result);
    }

    // ⚙️ Info sistema pulizia
    @GetMapping("/info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCleanupInfo() {
        Map<String, Object> info = Map.of(
            "system", "Auto-cleanup notifiche Funkard",
            "schedule", "Ogni giorno alle 03:00 (Europe/Rome)",
            "retention", "30 giorni",
            "scope", Map.of(
                "resolvedNotifications", "Notifiche risolte da più di 30 giorni",
                "archivedNotifications", "Archivio più vecchio di 30 giorni"
            ),
            "features", Map.of(
                "async", "Esecuzione asincrona per non bloccare l'avvio",
                "safe", "Sicura anche se l'istanza viene riavviata",
                "renderCompatible", "Nessun cron esterno necessario",
                "manualTrigger", "Endpoint per pulizia manuale disponibile"
            ),
            "endpoints", Map.of(
                "manual", "POST /api/admin/notifications/cleanup/manual?daysOld=30",
                "test", "POST /api/admin/notifications/cleanup/test",
                "stats", "GET /api/admin/notifications/cleanup/stats",
                "info", "GET /api/admin/notifications/cleanup/info"
            )
        );
        
        return ResponseEntity.ok(info);
    }
}
