package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService service;

    @GetMapping
    public ResponseEntity<List<AdminNotification>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<AdminNotification>> getUnread() {
        return ResponseEntity.ok(service.getUnread());
    }

    @PostMapping
    public ResponseEntity<AdminNotification> create(@RequestBody AdminNotification notification) {
        return ResponseEntity.ok(service.create(notification));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        service.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
