package com.funkard.admin.controller;

import com.funkard.admin.notification.AdminNotification;
import com.funkard.admin.notification.AdminNotification.Type;
import com.funkard.admin.notification.AdminNotification.Severity;
import com.funkard.admin.notification.AdminNotificationRepository;
import com.funkard.admin.service.AdminNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final AdminNotificationRepository repo;
    private final AdminNotificationService service;

    public AdminNotificationController(AdminNotificationRepository repo, AdminNotificationService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping("/active")
    public List<AdminNotification> getActive() {
        return repo.findByResolvedFalseOrderByCreatedAtDesc();
    }

    @GetMapping("/archived")
    public List<AdminNotification> getArchived() {
        return repo.findByResolvedTrueOrderByCreatedAtDesc();
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable Long id) {
        service.resolve(id);
        return ResponseEntity.noContent().build();
    }

    // 🔍 Filtro per tipo, gravità e stato
    @GetMapping("/filter")
    public ResponseEntity<List<AdminNotification>> filter(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(defaultValue = "50") int limit
    ) {
        Type typeEnum = null;
        Severity severityEnum = null;

        try {
            if (type != null) typeEnum = Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ignored) {}

        try {
            if (severity != null) severityEnum = Severity.valueOf(severity.toUpperCase());
        } catch (IllegalArgumentException ignored) {}

        List<AdminNotification> result = repo.filter(typeEnum, severityEnum, resolved);

        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        return ResponseEntity.ok(result);
    }
}