package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import com.funkard.admin.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 🔔 Controller per notifiche admin
 * Richiede autenticazione JWT con ruolo ADMIN o SUPER_ADMIN
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "https://admin.funkard.com", "http://localhost:3000", "http://localhost:3002"})
public class AdminNotificationController {

    private final AdminNotificationService service;
    private final AdminNotificationRepository adminNotificationRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Page<AdminNotification> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        log.info("📋 Richiesta notifiche (type={}, priority={}, status={}, page={}, size={})", 
            type, priority, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        
        if (type != null || priority != null || status != null) {
            Page<AdminNotification> filtered = service.filterPaginated(type, priority, status, pageable);
            log.info("✅ Restituite {} notifiche filtrate (totale: {})", 
                filtered.getNumberOfElements(), filtered.getTotalElements());
            return filtered;
        }
        
        Page<AdminNotification> active = service.listActiveChronoPaginated(pageable);
        log.info("✅ Restituite {} notifiche attive (totale: {})", 
            active.getNumberOfElements(), active.getTotalElements());
        return active;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminNotification> get(@PathVariable UUID id) {
        log.info("📋 Richiesta notifica {}", id);
        return service.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminNotification> markRead(@PathVariable UUID id, Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        log.info("✅ Marcatura notifica {} come letta da {}", id, user);
        return ResponseEntity.ok(service.markRead(id, user));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminNotification> assign(@PathVariable UUID id, Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        log.info("👨‍💻 Assegnazione notifica {} a {}", id, user);
        return ResponseEntity.ok(service.assign(id, user));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> resolve(@PathVariable UUID id,
                                         @RequestBody(required = false) NoteReq body,
                                         Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        String note = body != null ? body.note : null;
        log.info("🎯 Risoluzione notifica {} da {}", id, user);
        service.resolve(id, user, note);
        return ResponseEntity.ok("Notification resolved successfully");
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminNotification> archive(@PathVariable UUID id,
                                                     @RequestBody(required = false) NoteReq body,
                                                     Principal principal) {
        String user = principal != null ? principal.getName() : "admin";
        String note = body != null ? body.note : null;
        log.info("📦 Archiviazione notifica {} da {}", id, user);
        return ResponseEntity.ok(service.archive(id, user, note));
    }

    @RequestMapping(
        value = "/cleanup",
        method = {RequestMethod.DELETE, RequestMethod.POST}
    )
    public ResponseEntity<CleanupRes> cleanup(
            @RequestParam(defaultValue = "30") int days,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 🔓 Verifica token cron Cloudflare (bypass per cron worker)
        String cronSecret = System.getenv("FUNKARD_CRON_SECRET");
        if (cronSecret == null || cronSecret.isBlank()) {
            cronSecret = System.getProperty("FUNKARD_CRON_SECRET", "");
        }
        cronSecret = cronSecret != null ? cronSecret.trim() : "";
        String expected = "Bearer " + cronSecret;
        
        if (authHeader != null && authHeader.equals(expected)) {
            // Bypass: cron worker autorizzato
            log.info("🧹 Cleanup notifiche archiviate più vecchie di {} giorni (cron)", days);
            LocalDateTime thresholdDate = LocalDateTime.now().minusDays(days);
            int deleted = adminNotificationRepository.deleteByArchivedTrueAndArchivedAtBefore(thresholdDate);
            log.info("✅ Eliminate {} notifiche", deleted);
            return ResponseEntity.ok(new CleanupRes(deleted, days));
        }
        
        // 🔐 Richiede autenticazione ADMIN per utenti normali
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() ||
            auth.getAuthorities().stream().noneMatch(a -> 
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            throw new RuntimeException("Access Denied");
        }
        
        log.info("🧹 Cleanup notifiche archiviate più vecchie di {} giorni", days);
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(days);
        int deleted = adminNotificationRepository.deleteByArchivedTrueAndArchivedAtBefore(thresholdDate);
        log.info("✅ Eliminate {} notifiche", deleted);
        return ResponseEntity.ok(new CleanupRes(deleted, days));
    }

    @GetMapping("/stream")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public SseEmitter streamNotifications() {
        log.info("📡 Connessione SSE notifiche");
        return service.subscribe();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = service.getUnreadCount();
        log.info("📊 Notifiche non lette: {}", count);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/unread-latest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AdminNotification>> getUnreadLatest() {
        log.info("📋 Richiesta ultime notifiche non lette");
        return ResponseEntity.ok(service.getUnreadLatest());
    }

    public record NoteReq(String note) {}
    public record CleanupRes(int deleted, int olderThanDays) {}
}