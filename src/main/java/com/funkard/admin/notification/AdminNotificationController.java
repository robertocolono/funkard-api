package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/unresolved")
    public ResponseEntity<List<AdminNotification>> getUnresolved() {
        return ResponseEntity.ok(service.getUnresolved());
    }

    @GetMapping("/reference/{referenceType}/{referenceId}")
    public ResponseEntity<List<AdminNotification>> getByReference(
            @PathVariable String referenceType, 
            @PathVariable Long referenceId) {
        return ResponseEntity.ok(service.getByReference(referenceType, referenceId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<AdminNotification>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(service.getByType(type));
    }

    @PostMapping
    public ResponseEntity<AdminNotification> create(@RequestBody AdminNotification notification) {
        return ResponseEntity.ok(service.create(notification));
    }

    @PostMapping("/with-reference")
    public ResponseEntity<AdminNotification> createWithReference(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String message = (String) body.get("message");
        String type = (String) body.get("type");
        String referenceType = (String) body.get("referenceType");
        Long referenceId = Long.valueOf(body.get("referenceId").toString());
        
        return ResponseEntity.ok(service.createWithReference(title, message, type, referenceType, referenceId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        service.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Void> markAsResolved(@PathVariable Long id) {
        service.markAsResolved(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archiveNotification(@PathVariable Long id) {
        service.archiveNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resolve-and-archive")
    public ResponseEntity<Void> resolveAndArchive(@PathVariable Long id) {
        service.resolveAndArchive(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = Map.of(
            "unread", service.countUnread(),
            "unresolved", service.countUnresolved(),
            "info", service.countByType("INFO"),
            "warning", service.countByType("WARNING"),
            "error", service.countByType("ERROR"),
            "support", service.countByType("SUPPORT")
        );
        return ResponseEntity.ok(stats);
    }

    // 📁 Endpoint archivio
    @GetMapping("/archive")
    public ResponseEntity<List<AdminNotificationArchive>> getArchive() {
        return ResponseEntity.ok(service.getArchive());
    }

    @GetMapping("/archive/type/{type}")
    public ResponseEntity<List<AdminNotificationArchive>> getArchiveByType(@PathVariable String type) {
        return ResponseEntity.ok(service.getArchiveByType(type));
    }

    @GetMapping("/archive/reference/{referenceType}/{referenceId}")
    public ResponseEntity<List<AdminNotificationArchive>> getArchiveByReference(
            @PathVariable String referenceType, 
            @PathVariable Long referenceId) {
        return ResponseEntity.ok(service.getArchiveByReference(referenceType, referenceId));
    }

    @GetMapping("/archive/recent")
    public ResponseEntity<List<AdminNotificationArchive>> getRecentArchive(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(service.getRecentArchive(days));
    }
}
