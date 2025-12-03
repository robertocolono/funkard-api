package com.funkard.adminauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 🔧 Controller temporaneo per fix database
 * Endpoint di emergenza per aggiungere colonna onboarding_completed mancante
 * 
 * ⚠️ TEMPORANEO: Questo controller deve essere rimosso dopo l'uso
 * Si disabilita automaticamente dopo la prima esecuzione riuscita
 */
@RestController
@RequestMapping("/api/admin/fix")
public class AdminFixController {

    private static final Logger logger = LoggerFactory.getLogger(AdminFixController.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    // Flag per disabilitare endpoint dopo prima esecuzione
    private final AtomicBoolean alreadyExecuted = new AtomicBoolean(false);

    public AdminFixController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 🔧 GET /api/admin/fix/onboarding-column
     * Aggiunge colonna onboarding_completed se mancante
     * 
     * Autenticazione: Bearer FUNKARD_CRON_SECRET (obbligatorio)
     * 
     * Risposta:
     * - 200: { "status": "column_added" } o { "status": "already_exists" }
     * - 401: token non valido
     * - 410: endpoint già eseguito (disabilitato)
     */
    @GetMapping("/onboarding-column")
    public ResponseEntity<?> fixOnboardingColumn(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // 🔒 Verifica token cron Cloudflare (obbligatorio)
        String cronSecret = System.getenv("FUNKARD_CRON_SECRET");
        if (cronSecret == null || cronSecret.isBlank()) {
            cronSecret = System.getProperty("FUNKARD_CRON_SECRET", "");
        }
        cronSecret = cronSecret != null ? cronSecret.trim() : "";
        String expected = "Bearer " + cronSecret;
        
        if (authHeader == null || !authHeader.equals(expected)) {
            logger.warn("❌ Tentativo di accesso non autorizzato a /api/admin/fix/onboarding-column");
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Unauthorized", "message", "FUNKARD_CRON_SECRET richiesto"));
        }
        
        // 🚫 Verifica se già eseguito (disabilita dopo prima esecuzione)
        if (alreadyExecuted.get()) {
            logger.warn("⚠️ Endpoint /api/admin/fix/onboarding-column già eseguito, disabilitato");
            return ResponseEntity.status(410) // Gone
                    .body(Map.of("status", "already_executed", "message", "Endpoint già eseguito e disabilitato"));
        }
        
        try {
            // Verifica se colonna esiste già
            String checkColumnSQL = """
                SELECT EXISTS (
                    SELECT 1 
                    FROM information_schema.columns 
                    WHERE table_name = 'admin_users' 
                    AND column_name = 'onboarding_completed'
                )
                """;
            
            Boolean columnExists = jdbcTemplate.queryForObject(checkColumnSQL, Boolean.class);
            
            if (Boolean.TRUE.equals(columnExists)) {
                // Colonna già esistente
                alreadyExecuted.set(true); // Disabilita endpoint
                logger.info("✅ Colonna onboarding_completed già presente nel database");
                return ResponseEntity.ok(Map.of("status", "already_exists", "message", "Column already exists"));
            }
            
            // Aggiungi colonna
            String alterTableSQL = """
                ALTER TABLE admin_users 
                ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE
                """;
            
            jdbcTemplate.execute(alterTableSQL);
            
            // Disabilita endpoint dopo esecuzione riuscita
            alreadyExecuted.set(true);
            
            logger.info("✅ Colonna onboarding_completed aggiunta con successo");
            return ResponseEntity.ok(Map.of("status", "column_added", "message", "Column added successfully"));
            
        } catch (Exception e) {
            logger.error("❌ Errore durante l'aggiunta della colonna: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Database error", "message", e.getMessage()));
        }
    }
}

