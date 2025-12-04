package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AdminNotification>> getArchivedNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<AdminNotification> archived = notificationRepository
                .findByReadTrueAndCreatedAtAfterOrderByCreatedAtDesc(cutoff);
        return ResponseEntity.ok(archived);
    }

    /**
     * Elimina definitivamente una notifica archiviata.
     * Solo le notifiche con read_status = true possono essere eliminate.
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteArchivedNotification(@PathVariable UUID id) {
        Optional<AdminNotification> notifOpt = notificationRepository.findById(id);

        if (notifOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdminNotification notif = notifOpt.get();

        if (!notif.isRead()) {
            return ResponseEntity.badRequest().body("Puoi eliminare solo notifiche archiviate");
        }

        notificationRepository.deleteById(id);
        return ResponseEntity.ok("Notifica archiviata eliminata definitivamente");
    }
}
