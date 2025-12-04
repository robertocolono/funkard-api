package com.funkard.adminauthmodern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔐 Service moderno per gestione sessioni admin (database-backed)
 * Durata sessione: 4 ore
 * Cleanup automatico ogni 2 ore
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
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);
        
        AdminSession session = new AdminSession(sessionId, adminId, expiresAt);
        sessionRepository.save(session);
        
        logger.info("✅ Sessione admin moderna creata: sessionId={}, adminId={}, expiresAt={}", 
            sessionId.substring(0, 8) + "...", adminId, expiresAt);
        
        return sessionId;
    }
    
    /**
     * 🔍 Valida una sessione e restituisce l'ID dell'admin se valida
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
        
        // Verifica scadenza
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            // Sessione scaduta, rimuovila
            sessionRepository.deleteBySessionId(sessionId);
            logger.debug("⏰ Sessione scaduta rimossa: {}", sessionId.substring(0, 8) + "...");
            return Optional.empty();
        }
        
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
     */
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // Ogni 2 ore (7200000 ms)
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        int deleted = sessionRepository.deleteExpiredSessions(now);
        
        if (deleted > 0) {
            logger.info("🧹 Cleanup sessioni moderne: rimosse {} sessioni scadute", deleted);
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

