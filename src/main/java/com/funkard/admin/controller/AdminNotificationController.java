package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.service.AdminNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final AdminNotificationService service;

    public AdminNotificationController(AdminNotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdminNotification> list(@RequestParam(required = false) String type,
                                        @RequestParam(required = false) String priority,
                                        @RequestParam(required = false) String status) {
        if (type != null || priority != null || status != null) {
            return service.filter(type, priority, status);
        }
        return service.listActiveChrono();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminNotification> get(@PathVariable UUID id) {
        return service.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<AdminNotification> markRead(@PathVariable UUID id, Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(service.markRead(id, user));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<AdminNotification> assign(@PathVariable UUID id, Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(service.assign(id, user));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<String> resolve(@PathVariable UUID id,
                                         @RequestBody(required = false) NoteReq body,
                                         Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        String note = body != null ? body.note : null;
        service.resolve(id, user, note);
        return ResponseEntity.ok("Notification resolved successfully");
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<AdminNotification> archive(@PathVariable UUID id,
                                                     @RequestBody(required = false) NoteReq body,
                                                     Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        String note = body != null ? body.note : null;
        return ResponseEntity.ok(service.archive(id, user, note));
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<CleanupRes> cleanup(@RequestParam(defaultValue = "30") int days) {
        long deleted = service.cleanupArchivedOlderThanDays(days);
        return ResponseEntity.ok(new CleanupRes(deleted, days));
    }

    @GetMapping("/stream")
    public SseEmitter streamNotifications() {
        return service.subscribe();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = service.getUnreadCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/unread-latest")
    public ResponseEntity<List<AdminNotification>> getUnreadLatest() {
        return ResponseEntity.ok(service.getUnreadLatest());
    }

    public record NoteReq(String note) {}
    public record CleanupRes(long deleted, int olderThanDays) {}
}