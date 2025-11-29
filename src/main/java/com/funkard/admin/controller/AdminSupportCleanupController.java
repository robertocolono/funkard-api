package com.funkard.admin.controller;

import com.funkard.admin.service.SupportCleanupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * 🧹 Controller per cleanup automatico dei messaggi di supporto
 * Protetto con Bearer token FUNKARD_CRON_SECRET per accesso esclusivo da Cloudflare Worker
 */
@RestController
@RequestMapping("/api/admin/support")
public class AdminSupportCleanupController {

    private final SupportCleanupService cleanupService;

    public AdminSupportCleanupController(SupportCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    /**
     * 🗑️ DELETE/POST /api/admin/support/cleanup
     * Elimina tutti i messaggi associati a ticket risolti/chiusi più vecchi di 24 ore
     * 
     * Autenticazione: Bearer FUNKARD_CRON_SECRET (Cloudflare Worker) o JWT con ruolo ADMIN
     * 
     * @param authHeader Header Authorization con Bearer token
     * @return JSON con numero di messaggi eliminati, giorni e timestamp
     */
    @RequestMapping(
        value = "/cleanup",
        method = {RequestMethod.DELETE, RequestMethod.POST}
    )
    public ResponseEntity<?> cleanupOldMessages(
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
            try {
                long deleted = cleanupService.cleanupOldMessages();
                
                return ResponseEntity.ok(Map.of(
                    "deleted", deleted,
                    "days", 1,
                    "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()
                ));
            } catch (Exception e) {
                return ResponseEntity.status(500).body(Map.of(
                    "error", "Cleanup failed",
                    "message", e.getMessage()
                ));
            }
        }
        
        // 🔐 Richiede autenticazione ADMIN per utenti normali
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() ||
            auth.getAuthorities().stream().noneMatch(a -> 
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            throw new RuntimeException("Access Denied");
        }
        
        try {
            long deleted = cleanupService.cleanupOldMessages();
            
            return ResponseEntity.ok(Map.of(
                "deleted", deleted,
                "days", 1,
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Cleanup failed",
                "message", e.getMessage()
            ));
        }
    }

}

