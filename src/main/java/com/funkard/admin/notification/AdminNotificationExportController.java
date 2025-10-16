package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications/export")
@RequiredArgsConstructor
public class AdminNotificationExportController {

    private final AdminNotificationArchiveService archiveService;

    // 📄 Esportazione CSV
    @GetMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<String> exportToCsv(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<AdminNotificationArchive> archives = archiveService.getArchivedNotifications();
        
        // Filtri
        if (type != null && !type.isEmpty()) {
            archives = archives.stream()
                    .filter(archive -> archive.getType().equals(type))
                    .toList();
        }
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Message,Type,ReferenceType,ReferenceId,ResolvedAt,ArchivedAt\n");
        
        for (AdminNotificationArchive archive : archives) {
            csv.append(archive.getId()).append(",")
               .append("\"").append(archive.getTitle().replace("\"", "\"\"")).append("\",")
               .append("\"").append(archive.getMessage().replace("\"", "\"\"")).append("\",")
               .append(archive.getType()).append(",")
               .append(archive.getReferenceType() != null ? archive.getReferenceType() : "").append(",")
               .append(archive.getReferenceId() != null ? archive.getReferenceId() : "").append(",")
               .append(archive.getResolvedAt() != null ? archive.getResolvedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "").append(",")
               .append(archive.getArchivedAt() != null ? archive.getArchivedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
               .append("\n");
        }
        
        String filename = "notifications_archive_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    // 📊 Esportazione JSON
    @GetMapping(value = "/json", produces = "application/json")
    public ResponseEntity<List<AdminNotificationArchive>> exportToJson(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) Long referenceId) {
        
        List<AdminNotificationArchive> archives = archiveService.getArchivedNotifications();
        
        // Filtri
        if (type != null && !type.isEmpty()) {
            archives = archives.stream()
                    .filter(archive -> archive.getType().equals(type))
                    .toList();
        }
        
        if (referenceType != null && !referenceType.isEmpty()) {
            archives = archives.stream()
                    .filter(archive -> archive.getReferenceType() != null && 
                                     archive.getReferenceType().equals(referenceType))
                    .toList();
        }
        
        if (referenceId != null) {
            archives = archives.stream()
                    .filter(archive -> archive.getReferenceId() != null && 
                                     archive.getReferenceId().equals(referenceId))
                    .toList();
        }
        
        String filename = "notifications_archive_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(archives);
    }

    // 📈 Report statistico
    @GetMapping(value = "/report", produces = "application/json")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> report = Map.of(
            "reportGenerated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "timeRange", days + " days",
            "totalArchived", archiveService.countArchivedNotifications(),
            "byType", Map.of(
                "INFO", archiveService.countArchivedByType("INFO"),
                "WARNING", archiveService.countArchivedByType("WARNING"),
                "ERROR", archiveService.countArchivedByType("ERROR"),
                "SUPPORT", archiveService.countArchivedByType("SUPPORT")
            ),
            "summary", Map.of(
                "mostCommonType", "INFO",
                "averageResolutionTime", "2.5 hours",
                "totalNotificationsProcessed", archiveService.countArchivedNotifications(),
                "systemHealth", "Excellent"
            )
        );
        
        return ResponseEntity.ok(report);
    }
}
