package com.funkard.admin.system;

import com.funkard.admin.log.AdminActionLogRepository;
import com.funkard.admin.service.SystemCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 🔧 Controller per operazioni di manutenzione sistema
 * Endpoint protetti con Bearer token FUNKARD_CRON_SECRET o JWT con ruolo ADMIN
 */
@RestController
@RequestMapping("/api/admin/maintenance")
@RequiredArgsConstructor
@Slf4j
public class MaintenanceController {

    private final AdminActionLogRepository logRepository;
    private final SystemCleanupService cleanupService;

    @PostMapping("/cleanup-logs")
    public ResponseEntity<String> cleanupLogs(
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
            
            log.info("🧹 Funkard Maintenance Logs Cleanup — deleted {} old entries (older than {}) [cron]", deleted, cutoff);
            
            String result = deleted > 0 ? "success" : "no_entries";
            cleanupService.saveCleanupResult(result, deleted, "Maintenance logs cleanup");
            
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
        
        log.info("🧹 Funkard Maintenance Logs Cleanup — deleted {} old entries (older than {})", deleted, cutoff);
        
        String result = deleted > 0 ? "success" : "no_entries";
        cleanupService.saveCleanupResult(result, deleted, "Maintenance logs cleanup");
        
        return ResponseEntity.ok("🧹 Deleted " + deleted + " old admin logs (older than 2 months)");
    }
}

