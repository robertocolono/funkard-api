package com.funkard.admin.controller;

import com.funkard.model.User;
import com.funkard.service.CookieConsentLogService;
import com.funkard.service.CookieLogExportService;
import com.funkard.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🔐 Admin Controller per gestione log consenso cookie
 * 
 * Accesso: Solo ADMIN e SUPERVISOR
 */
@RestController
@RequestMapping("/api/admin/cookies")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
public class CookieLogAdminController {
    
    private final CookieConsentLogService logService;
    private final CookieLogExportService exportService;
    private final UserService userService;
    
    /**
     * 📋 GET /api/admin/cookies/logs
     * Visualizza storico log consenso cookie di un utente
     * 
     * Query Parameters:
     * - userId: ID utente (obbligatorio)
     */
    @GetMapping("/logs")
    public ResponseEntity<?> getLogs(@RequestParam Long userId, Authentication authentication) {
        log.info("Richiesta log cookie per utente {} da admin: {}", userId, getAdminEmail(authentication));
        
        try {
            List<com.funkard.model.CookieConsentLog> logs = logService.getLogsByUserId(userId);
            
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utente non trovato"));
            }
            
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "userEmail", user.getEmail(),
                "totalLogs", logs.size(),
                "logs", logs
            ));
        } catch (Exception e) {
            log.error("Errore nel recupero log per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📥 GET /api/admin/cookies/logs/export
     * Esporta log consenso cookie di un utente (admin)
     * 
     * Query Parameters:
     * - userId: ID utente (obbligatorio)
     * - format: "json" o "pdf" (default: json)
     */
    @GetMapping("/logs/export")
    public ResponseEntity<?> exportLogs(
            @RequestParam Long userId,
            @RequestParam(value = "format", defaultValue = "json") String format,
            Authentication authentication) {
        
        log.info("Richiesta export log cookie per utente {} da admin: {} - formato: {}", 
            userId, getAdminEmail(authentication), format);
        
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Utente non trovato"));
            }
            
            if ("pdf".equalsIgnoreCase(format)) {
                byte[] pdfBytes = exportService.exportAsPdf(user);
                return ResponseEntity.ok()
                        .header("Content-Type", "application/pdf")
                        .header("Content-Disposition", 
                            "attachment; filename=\"cookie-consent-log-user-" + userId + ".pdf\"")
                        .body(pdfBytes);
            } else {
                // Default: JSON
                String json = exportService.exportAsJson(user);
                return ResponseEntity.ok()
                        .header("Content-Type", "application/json")
                        .header("Content-Disposition", 
                            "attachment; filename=\"cookie-consent-log-user-" + userId + ".json\"")
                        .body(json);
            }
        } catch (Exception e) {
            log.error("Errore nell'export log per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server: " + e.getMessage()));
        }
    }
    
    /**
     * 🔍 Helper per ottenere email admin
     */
    private String getAdminEmail(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return "unknown";
    }
}

