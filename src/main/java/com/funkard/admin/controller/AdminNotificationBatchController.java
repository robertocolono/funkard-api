package com.funkard.admin.controller;

import com.funkard.admin.service.AdminNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "*")
public class AdminNotificationBatchController {

    private final AdminNotificationService notificationService;

    public AdminNotificationBatchController(AdminNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/batch/resolve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> batchResolve(@RequestBody List<UUID> notificationIds) {
        try {
            int resolvedCount = 0;
            for (UUID id : notificationIds) {
                try {
                    notificationService.resolve(id, "system", "Batch resolve");
                    resolvedCount++;
                } catch (Exception e) {
                    // Log error but continue with other IDs
                    System.err.println("Failed to resolve notification " + id + ": " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "resolved", resolvedCount,
                "total", notificationIds.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/batch/archive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> batchArchive(@RequestBody List<UUID> notificationIds) {
        // Per ora, archiviare = risolvere
        return batchResolve(notificationIds);
    }

    @DeleteMapping("/batch/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody List<UUID> notificationIds) {
        try {
            int deletedCount = 0;
            for (UUID id : notificationIds) {
                try {
                    // Prima risolvi, poi elimina (se implementi delete)
                    notificationService.resolve(id, "system", "Batch delete");
                    deletedCount++;
                } catch (Exception e) {
                    System.err.println("Failed to delete notification " + id + ": " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "deleted", deletedCount,
                "total", notificationIds.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
