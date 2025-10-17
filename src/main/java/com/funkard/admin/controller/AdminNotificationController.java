package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import com.funkard.admin.service.AdminNotificationService;
import com.funkard.admin.service.AdminActionLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final AdminNotificationRepository repo;
    private final AdminNotificationService service;
    private final AdminActionLogger actionLogger;

    public AdminNotificationController(AdminNotificationRepository repo, AdminNotificationService service, AdminActionLogger actionLogger) {
        this.repo = repo;
        this.service = service;
        this.actionLogger = actionLogger;
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
    public ResponseEntity<Void> resolve(@PathVariable UUID id, @RequestHeader("X-Admin-User") String adminUser) {
        service.resolve(id);
        
        // Log the action
        actionLogger.logNotificationResolved(id.hashCode(), adminUser);
        
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

    @GetMapping("/unreadCount")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = repo.countByReadFalse();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @GetMapping("/unreadLatest")
    public ResponseEntity<Map<String, Object>> getUnreadLatest() {
        List<AdminNotification> latest = repo.findTop5ByReadFalseOrderByPriorityDescCreatedAtDesc();
        long count = repo.countByReadFalse();
        return ResponseEntity.ok(Map.of(
            "unreadCount", count,
            "notifications", latest
        ));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<AdminNotification>> getAllNotifications(
            @RequestParam(value = "status", required = false) String status) {
        
        List<AdminNotification> list;
        
        if ("unread".equalsIgnoreCase(status)) {
            list = repo.findByReadFalseOrderByCreatedAtDesc();
        } else if ("read".equalsIgnoreCase(status)) {
            list = repo.findByReadTrueOrderByCreatedAtDesc();
        } else {
            list = repo.findAllByOrderByCreatedAtDesc();
        }
        
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable UUID id, @RequestHeader("X-Admin-User") String adminUser) {
        var optional = repo.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdminNotification notif = optional.get();
        notif.setRead(true);
        notif.setReadAt(LocalDateTime.now());
        notif.setReadBy(adminUser);
        repo.save(notif);

        // Log the action
        actionLogger.logNotificationRead(notif.getId().hashCode(), adminUser);

        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "readBy", adminUser,
            "readAt", notif.getReadAt()
        ));
    }
}