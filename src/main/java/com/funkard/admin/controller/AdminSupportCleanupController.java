package com.funkard.admin.controller;

import com.funkard.admin.service.SupportCleanupService;
import org.springframework.http.ResponseEntity;
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
     * 🗑️ DELETE /api/admin/support/cleanup
     * Elimina tutti i messaggi associati a ticket risolti/chiusi più vecchi di 24 ore
     * 
     * Autenticazione: Bearer FUNKARD_CRON_SECRET (solo Cloudflare Worker)
     * 
     * @param auth Header Authorization con Bearer token
     * @return JSON con numero di messaggi eliminati, giorni e timestamp
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanupOldMessages(@RequestHeader("Authorization") String auth) {
        // Verifica autenticazione Bearer token
        String expected = "Bearer " + System.getenv("FUNKARD_CRON_SECRET");
        if (auth == null || !expected.equals(auth)) {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Unauthorized",
                "message", "Invalid or missing Bearer token"
            ));
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

