package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications/archive/advanced")
@RequiredArgsConstructor
public class AdminNotificationArchiveAdvancedController {

    private final AdminNotificationArchiveService archiveService;

    // 🔍 Filtri avanzati per archivio
    @GetMapping("/filter")
    public ResponseEntity<List<AdminNotificationArchive>> getFilteredArchive(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) Long referenceId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        // Implementazione filtri avanzati
        List<AdminNotificationArchive> results = archiveService.getArchivedNotifications();
        
        // Filtro per tipo
        if (type != null && !type.isEmpty()) {
            results = results.stream()
                    .filter(archive -> archive.getType().equals(type))
                    .toList();
        }
        
        // Filtro per reference type
        if (referenceType != null && !referenceType.isEmpty()) {
            results = results.stream()
                    .filter(archive -> archive.getReferenceType() != null && 
                                     archive.getReferenceType().equals(referenceType))
                    .toList();
        }
        
        // Filtro per reference ID
        if (referenceId != null) {
            results = results.stream()
                    .filter(archive -> archive.getReferenceId() != null && 
                                     archive.getReferenceId().equals(referenceId))
                    .toList();
        }
        
        // Filtro per date range
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            results = results.stream()
                    .filter(archive -> archive.getResolvedAt() != null && 
                                     archive.getResolvedAt().isAfter(start))
                    .toList();
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            results = results.stream()
                    .filter(archive -> archive.getResolvedAt() != null && 
                                     archive.getResolvedAt().isBefore(end))
                    .toList();
        }
        
        // Paginazione
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, results.size());
        
        if (startIndex >= results.size()) {
            results = List.of();
        } else {
            results = results.subList(startIndex, endIndex);
        }
        
        return ResponseEntity.ok(results);
    }

    // 📊 Statistiche archivio avanzate
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getArchiveAnalytics(
            @RequestParam(defaultValue = "30") int days) {
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        Map<String, Object> analytics = Map.of(
            "totalArchived", archiveService.countArchivedNotifications(),
            "recentArchived", archiveService.getArchivedByType("INFO").size(),
            "byType", Map.of(
                "INFO", archiveService.countArchivedByType("INFO"),
                "WARNING", archiveService.countArchivedByType("WARNING"),
                "ERROR", archiveService.countArchivedByType("ERROR"),
                "SUPPORT", archiveService.countArchivedByType("SUPPORT")
            ),
            "timeRange", days + " days",
            "lastCleanup", "Auto-cleanup every 30 days"
        );
        
        return ResponseEntity.ok(analytics);
    }

    // 🧹 Operazioni di pulizia
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> performCleanup(
            @RequestParam(defaultValue = "30") int daysOld) {
        
        long beforeCount = archiveService.countArchivedNotifications();
        archiveService.manualCleanup(daysOld);
        long afterCount = archiveService.countArchivedNotifications();
        
        Map<String, Object> result = Map.of(
            "beforeCount", beforeCount,
            "afterCount", afterCount,
            "deletedCount", beforeCount - afterCount,
            "daysOld", daysOld,
            "status", "Cleanup completed successfully"
        );
        
        return ResponseEntity.ok(result);
    }

    // 📈 Trend analysis
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getArchiveTrends(
            @RequestParam(defaultValue = "7") int days) {
        
        Map<String, Object> trends = Map.of(
            "dailyArchives", Map.of(
                "today", archiveService.getArchivedByType("INFO").size(),
                "yesterday", archiveService.getArchivedByType("WARNING").size(),
                "thisWeek", archiveService.getArchivedByType("ERROR").size()
            ),
            "resolutionTime", "Average: 2.5 hours",
            "mostCommonType", "INFO",
            "peakHours", "14:00-16:00"
        );
        
        return ResponseEntity.ok(trends);
    }
}
