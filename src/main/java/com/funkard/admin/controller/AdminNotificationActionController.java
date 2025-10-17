package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = {
    "https://funkard-admin.vercel.app",
    "http://localhost:3000"
}, allowCredentials = "true")
public class AdminNotificationActionController {

    private final AdminNotificationRepository notificationRepository;

    public AdminNotificationActionController(AdminNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PatchMapping("/archive/{id}")
    public ResponseEntity<?> archiveNotification(@PathVariable UUID id) {
        Optional<AdminNotification> optNotif = notificationRepository.findById(id);

        if (optNotif.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdminNotification notif = optNotif.get();
        notif.setRead(true);
        notif.setReadAt(LocalDateTime.now());
        notif.setArchivedAt(LocalDateTime.now());

        notificationRepository.save(notif);

        return ResponseEntity.ok("Notifica archiviata con successo");
    }
}
