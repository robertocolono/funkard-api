package com.funkard.admin.controller;

import com.funkard.admin.dto.NotificationDTO;
import com.funkard.admin.service.AdminNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = {"https://admin.funkard.com", "https://funkard.vercel.app"})
public class AdminNotificationController {
    
    private final AdminNotificationService service;
    
    @Value("${admin.token}")
    private String adminToken;
    
    public AdminNotificationController(AdminNotificationService service) {
        this.service = service;
    }
    
    // 🔹 Lista tutte le notifiche
    @GetMapping
    public ResponseEntity<?> getAllNotifications(@RequestHeader("Authorization") String authorization) {
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        List<NotificationDTO> notifications = service.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    // 🔹 Lista notifiche non lette
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestHeader("Authorization") String authorization) {
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        List<NotificationDTO> notifications = service.getUnreadNotifications();
        return ResponseEntity.ok(notifications);
    }
    
    // 🔹 Aggiungi nuova notifica
    @PostMapping("/add")
    public ResponseEntity<?> addNotification(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authorization) {
        
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        String type = request.get("type");
        String title = request.get("title");
        String message = request.get("message");
        String priority = request.getOrDefault("priority", "normal");
        
        NotificationDTO notification = service.addNotification(type, title, message, priority);
        return ResponseEntity.ok(notification);
    }
    
    // 🔹 Marca come letta
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization) {
        
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        service.markAsRead(id);
        return ResponseEntity.ok(Map.of("status", "marked_as_read"));
    }
    
    // 🔹 Elimina notifica
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authorization) {
        
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        service.deleteNotification(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
    
    // 🔹 Conta notifiche non lette
    @GetMapping("/count")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("Authorization") String authorization) {
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        long count = service.getUnreadCount();
        return ResponseEntity.ok(Map.of("unread_count", count));
    }
}