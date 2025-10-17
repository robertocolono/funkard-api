package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = {
    "https://funkard-admin.vercel.app",
    "http://localhost:3000"
}, allowCredentials = "true")
public class AdminNotificationArchiveController {

    private final AdminNotificationRepository notificationRepository;

    public AdminNotificationArchiveController(AdminNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Restituisce tutte le notifiche archiviate (risolte) negli ultimi 30 giorni.
     */
    @GetMapping("/archive")
    public ResponseEntity<List<AdminNotification>> getArchivedNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<AdminNotification> archived = notificationRepository
                .findByReadTrueAndCreatedAtAfterOrderByCreatedAtDesc(cutoff);
        return ResponseEntity.ok(archived);
    }
}
