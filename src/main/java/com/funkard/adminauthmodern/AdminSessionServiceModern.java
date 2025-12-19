package com.funkard.adminauthmodern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔐 Service moderno per gestione sessioni admin (database-backed)
 * Durata sessione: 4 ore
 * Cleanup automatico ogni 2 ore
 * Usa Instant (UTC) per eliminare problemi di timezone
 */
@Service
public class AdminSessionServiceModern {

    private static final Logger logger = LoggerFactory.getLogger(AdminSessionServiceModern.class);
    
    // Durata sessione: 4 ore
    private static final long SESSION_DURATION_HOURS = 4;
    
    private final AdminSessionRepository sessionRepository;
    
    public AdminSessionServiceModern(AdminSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    
    /**
     * ✅ Crea una nuova sessione per un admin
     * @param adminId ID dell'admin
     * @return SessionId univoco
     */
    @Transactional
    public String createSession(UUID adminId) {
        String sessionId = generateSessionId();
        Instant expiresAt = Instant.now().plusSeconds(SESSION_DURATION_HOURS * 3600);
        
        AdminSession session = new AdminSession(sessionId, adminId, expiresAt);
        sessionRepository.save(session);
        
        logger.info("✅ Sessione admin moderna creata: sessionId={}, adminId={}, expiresAt={} (UTC)", 
            sessionId.substring(0, 8) + "...", adminId, expiresAt);
        
        return sessionId;
    }
    
    /**
     * 🔍 Valida una sessione e restituisce l'ID dell'admin se valida
     * Usa Instant (UTC) per confronto timezone-safe
     * @param sessionId ID della sessione
     * @return Optional con adminId se sessione valida, empty altrimenti
     */
    @Transactional(readOnly = true)
    public Optional<UUID> validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        Optional<AdminSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
        
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }
        
        AdminSession session = sessionOpt.get();
        
        // Verifica scadenza usando Instant (UTC) per eliminare ambiguità timezone
        Instant now = Instant.now();
        Instant expiresAt = session.getExpiresAt();
        boolean isExpired = now.isAfter(expiresAt);
        
        // 🔍 LOGGING TEMPORANEO PER DEBUG (da rimuovere dopo verifica)
        Duration timeUntilExpiry = Duration.between(now, expiresAt);
        long secondsUntilExpiry = timeUntilExpiry.getSeconds();
        
        logger.warn("🔍 [DEBUG TIMEZONE] Validazione sessione:");
        logger.warn("  - sessionId: {}", sessionId.substring(0, 8) + "...");
        logger.warn("  - now (Instant UTC): {}", now);
        logger.warn("  - expiresAt (dal DB, UTC): {}", expiresAt);
        logger.warn("  - isAfter (scaduta): {}", isExpired);
        logger.warn("  - differenza: {} secondi ({} minuti)", 
            secondsUntilExpiry, secondsUntilExpiry / 60);
        
        if (isExpired) {
            // Sessione scaduta, rimuovila
            sessionRepository.deleteBySessionId(sessionId);
            logger.warn("⏰ Sessione scaduta rimossa: sessionId={}, now={} (UTC), expiresAt={} (UTC)", 
                sessionId.substring(0, 8) + "...", now, expiresAt);
            return Optional.empty();
        }
        
        logger.warn("✅ Sessione valida: sessionId={}, now={} (UTC), expiresAt={} (UTC), rimanenti {} secondi", 
            sessionId.substring(0, 8) + "...", now, expiresAt, secondsUntilExpiry);
        
        return Optional.of(session.getAdminId());
    }
    
    /**
     * 🚫 Invalida una sessione (logout)
     * @param sessionId ID della sessione
     */
    @Transactional
    public void invalidateSession(String sessionId) {
        if (sessionId != null) {
            int deleted = sessionRepository.deleteBySessionId(sessionId);
            if (deleted > 0) {
                logger.info("🚫 Sessione invalidata: {}", sessionId.substring(0, 8) + "...");
            }
        }
    }
    
    /**
     * 🧹 Invalida tutte le sessioni di un admin (utile per logout forzato o cambio password)
     * @param adminId ID dell'admin
     */
    @Transactional
    public void invalidateAllSessionsForAdmin(UUID adminId) {
        int deleted = sessionRepository.deleteByAdminId(adminId);
        if (deleted > 0) {
            logger.info("🚫 Invalidate {} sessioni per admin: {}", deleted, adminId);
        }
    }
    
    /**
     * 🧹 Cleanup automatico sessioni scadute (ogni 2 ore)
     * Usa Instant (UTC) per confronto timezone-safe
     */
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // Ogni 2 ore (7200000 ms)
    @Transactional
    public void cleanupExpiredSessions() {
        Instant now = Instant.now();
        int deleted = sessionRepository.deleteExpiredSessions(now);
        
        if (deleted > 0) {
            logger.info("🧹 Cleanup sessioni moderne: rimosse {} sessioni scadute (now={} UTC)", deleted, now);
        }
        
        logger.debug("📊 Sessioni moderne attive: {}", sessionRepository.count());
    }
    
    /**
     * 🔑 Genera un sessionId univoco (UUID v4 senza trattini)
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

