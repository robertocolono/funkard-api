package com.funkard.admin.controller;

import com.funkard.admin.notification.AdminNotification;
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
}