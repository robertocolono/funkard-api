package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import com.funkard.admin.service.AdminNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        return repo.findByReadFalseOrderByCreatedAtDesc();
    }

    @GetMapping("/archived")
    public List<AdminNotification> getArchived() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable UUID id) {
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
        List<AdminNotification> result = repo.filter(type, severity, resolved);

        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        return ResponseEntity.ok(result);
    }
}