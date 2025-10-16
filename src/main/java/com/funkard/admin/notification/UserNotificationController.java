package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService service;

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserNotification>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    @GetMapping("/{userId}/unread")
    public ResponseEntity<List<UserNotification>> getUnreadByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.getUnreadByUser(userId));
    }

    @GetMapping("/{userId}/unresolved")
    public ResponseEntity<List<UserNotification>> getUnresolvedByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.getUnresolvedByUser(userId));
    }

    @GetMapping("/reference/{referenceType}/{referenceId}")
    public ResponseEntity<List<UserNotification>> getByReference(
            @PathVariable String referenceType, 
            @PathVariable Long referenceId) {
        return ResponseEntity.ok(service.getByReference(referenceType, referenceId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<UserNotification>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(service.getByType(type));
    }

    @GetMapping("/{userId}/type/{type}")
    public ResponseEntity<List<UserNotification>> getByUserAndType(
            @PathVariable String userId, 
            @PathVariable String type) {
        return ResponseEntity.ok(service.getByUserAndType(userId, type));
    }

    @PostMapping
    public ResponseEntity<UserNotification> create(@RequestBody UserNotification notification) {
        return ResponseEntity.ok(service.create(notification));
    }

    @PostMapping("/for-user")
    public ResponseEntity<UserNotification> createForUser(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        String title = (String) body.get("title");
        String message = (String) body.get("message");
        String type = (String) body.get("type");
        String referenceType = (String) body.get("referenceType");
        Long referenceId = Long.valueOf(body.get("referenceId").toString());
        
        return ResponseEntity.ok(service.createForUser(userId, title, message, type, referenceType, referenceId));
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

    @PatchMapping("/{userId}/mark-all-read")
    public ResponseEntity<Void> markAllAsReadForUser(@PathVariable String userId) {
        service.markAllAsReadForUser(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getStatsByUser(@PathVariable String userId) {
        Map<String, Object> stats = Map.of(
            "unread", service.countUnreadByUser(userId),
            "unresolved", service.countUnresolvedByUser(userId),
            "info", service.countByType("INFO"),
            "warning", service.countByType("WARNING"),
            "error", service.countByType("ERROR"),
            "success", service.countByType("SUCCESS")
        );
        return ResponseEntity.ok(stats);
    }
}
