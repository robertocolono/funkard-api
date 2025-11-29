package com.funkard.admin.log;

import com.funkard.admin.system.SystemMaintenanceController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@Slf4j
public class AdminActionLogController {

    private final AdminActionLogRepository logRepository;
    private final SystemMaintenanceController systemController;

    @GetMapping("/{type}/{id}")
    public ResponseEntity<List<AdminActionLog>> getHistory(
            @PathVariable String type, @PathVariable Long id) {
        List<AdminActionLog> logs = logRepository.findByTargetIdAndTargetTypeOrderByCreatedAtAsc(id, type.toUpperCase());
        return ResponseEntity.ok(logs);
    }

    // 🧹 cleanup automatico per log vecchi di 2 mesi
    @RequestMapping(
        value = "/cleanup",
        method = {RequestMethod.DELETE, RequestMethod.POST}
    )
    public ResponseEntity<String> cleanupOldLogs(
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
            LocalDateTime cutoff = LocalDateTime.now().minusMonths(2);
            int deleted = logRepository.deleteOlderThan(cutoff);

            // 🔎 Log su Render console
            if (deleted > 0) {
                log.info("🧹 Funkard Admin Logs Cleanup — deleted {} old entries (older than {}) [cron]", deleted, cutoff);
            } else {
                log.info("✅ Funkard Admin Logs Cleanup — no old entries to delete (checked up to {}) [cron]", cutoff);
            }

            // 📊 Aggiorna status del cleanup
            String result = deleted > 0 ? "success" : "no_entries";
            systemController.updateCleanupStatus(new SystemMaintenanceController.CleanupStatus(result, deleted, LocalDateTime.now()), authHeader);

            return ResponseEntity.ok("🧹 Deleted " + deleted + " old admin logs (older than 2 months)");
        }
        
        // 🔐 Richiede autenticazione ADMIN per utenti normali
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() ||
            auth.getAuthorities().stream().noneMatch(a -> 
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            throw new RuntimeException("Access Denied");
        }
        
        LocalDateTime cutoff = LocalDateTime.now().minusMonths(2);
        int deleted = logRepository.deleteOlderThan(cutoff);

        // 🔎 Log su Render console
        if (deleted > 0) {
            log.info("🧹 Funkard Admin Logs Cleanup — deleted {} old entries (older than {})", deleted, cutoff);
        } else {
            log.info("✅ Funkard Admin Logs Cleanup — no old entries to delete (checked up to {})", cutoff);
        }

        // 📊 Aggiorna status del cleanup
        String result = deleted > 0 ? "success" : "no_entries";
        systemController.updateCleanupStatus(new SystemMaintenanceController.CleanupStatus(result, deleted, LocalDateTime.now()), authHeader);

        return ResponseEntity.ok("🧹 Deleted " + deleted + " old admin logs (older than 2 months)");
    }
}
