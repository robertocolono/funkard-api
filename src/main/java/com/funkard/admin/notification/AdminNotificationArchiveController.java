package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications/archive")
@RequiredArgsConstructor
public class AdminNotificationArchiveController {

    private final AdminNotificationArchiveService archiveService;

    @GetMapping
    public ResponseEntity<List<AdminNotificationArchive>> getAllArchived() {
        return ResponseEntity.ok(archiveService.getArchivedNotifications());
    }

    @GetMapping("/reference/{referenceType}/{referenceId}")
    public ResponseEntity<List<AdminNotificationArchive>> getArchivedByReference(
            @PathVariable String referenceType, 
            @PathVariable Long referenceId) {
        return ResponseEntity.ok(archiveService.getArchivedByReference(referenceType, referenceId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<AdminNotificationArchive>> getArchivedByType(@PathVariable String type) {
        return ResponseEntity.ok(archiveService.getArchivedByType(type));
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> manualCleanup(@RequestParam(defaultValue = "30") int daysOld) {
        long beforeCount = archiveService.countArchivedNotifications();
        archiveService.manualCleanup(daysOld);
        long afterCount = archiveService.countArchivedNotifications();
        
        Map<String, Object> result = Map.of(
            "beforeCount", beforeCount,
            "afterCount", afterCount,
            "deletedCount", beforeCount - afterCount,
            "daysOld", daysOld
        );
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getArchiveStats() {
        Map<String, Object> stats = Map.of(
            "totalArchived", archiveService.countArchivedNotifications(),
            "infoArchived", archiveService.countArchivedByType("INFO"),
            "warningArchived", archiveService.countArchivedByType("WARNING"),
            "errorArchived", archiveService.countArchivedByType("ERROR"),
            "supportArchived", archiveService.countArchivedByType("SUPPORT")
        );
        return ResponseEntity.ok(stats);
    }
}
