package com.funkard.adminauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🔐 Service per gestione sessioni admin in-memory
 * Durata sessione: 4 ore
 * Cleanup automatico ogni 2 ore
 */
@Service
public class AdminSessionService {

    private static final Logger logger = LoggerFactory.getLogger(AdminSessionService.class);
    
    // Durata sessione: 4 ore (in secondi)
    private static final long SESSION_DURATION_SECONDS = 4 * 60 * 60; // 14400 secondi
    
    // Struttura: sessionId -> SessionData
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    
    /**
     * ✅ Crea una nuova sessione per un admin
     * @param adminUserId ID dell'admin
     * @return SessionId univoco
     */
    public String createSession(UUID adminUserId) {
        String sessionId = generateSessionId();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(SESSION_DURATION_SECONDS);
        
        SessionData sessionData = new SessionData(adminUserId, expiresAt, LocalDateTime.now());
        sessions.put(sessionId, sessionData);
        
        logger.info("✅ Sessione admin creata: sessionId={}, adminId={}, expiresAt={}", 
            sessionId.substring(0, 8) + "...", adminUserId, expiresAt);
        
        return sessionId;
    }
    
    /**
     * 🔍 Valida una sessione e restituisce l'ID dell'admin se valida
     * @param sessionId ID della sessione
     * @return Optional con adminUserId se sessione valida, empty altrimenti
     */
    public Optional<UUID> validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }
        
        SessionData sessionData = sessions.get(sessionId);
        
        if (sessionData == null) {
            return Optional.empty();
        }
        
        // Verifica scadenza
        if (LocalDateTime.now().isAfter(sessionData.getExpiresAt())) {
            // Sessione scaduta, rimuovila
            sessions.remove(sessionId);
            logger.debug("⏰ Sessione scaduta rimossa: {}", sessionId.substring(0, 8) + "...");
            return Optional.empty();
        }
        
        return Optional.of(sessionData.getAdminUserId());
    }
    
    /**
     * 🚫 Invalida una sessione (logout)
     * @param sessionId ID della sessione
     */
    public void invalidateSession(String sessionId) {
        if (sessionId != null && sessions.remove(sessionId) != null) {
            logger.info("🚫 Sessione invalidata: {}", sessionId.substring(0, 8) + "...");
        }
    }
    
    /**
     * 🧹 Invalida tutte le sessioni di un admin (utile per logout forzato o cambio password)
     * @param adminUserId ID dell'admin
     */
    public void invalidateAllSessionsForAdmin(UUID adminUserId) {
        int removed = 0;
        for (Map.Entry<String, SessionData> entry : sessions.entrySet()) {
            if (entry.getValue().getAdminUserId().equals(adminUserId)) {
                sessions.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            logger.info("🚫 Invalidate {} sessioni per admin: {}", removed, adminUserId);
        }
    }
    
    /**
     * 🧹 Cleanup automatico sessioni scadute (ogni 2 ore)
     * Rimuove tutte le sessioni scadute dalla mappa
     */
    @Scheduled(fixedRate = 2 * 60 * 60 * 1000) // Ogni 2 ore (7200000 ms)
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        int removed = 0;
        
        sessions.entrySet().removeIf(entry -> {
            boolean expired = now.isAfter(entry.getValue().getExpiresAt());
            if (expired) {
                logger.debug("🧹 Rimossa sessione scaduta: {}", entry.getKey().substring(0, 8) + "...");
            }
            return expired;
        });
        
        if (removed > 0) {
            logger.info("🧹 Cleanup sessioni: rimosse {} sessioni scadute", removed);
        }
        
        logger.debug("📊 Sessioni attive: {}", sessions.size());
    }
    
    /**
     * 🔑 Genera un sessionId univoco (UUID v4)
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 📊 Dati di una sessione
     */
    private static class SessionData {
        private final UUID adminUserId;
        private final LocalDateTime expiresAt;
        private final LocalDateTime createdAt;
        
        public SessionData(UUID adminUserId, LocalDateTime expiresAt, LocalDateTime createdAt) {
            this.adminUserId = adminUserId;
            this.expiresAt = expiresAt;
            this.createdAt = createdAt;
        }
        
        public UUID getAdminUserId() {
            return adminUserId;
        }
        
        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }
        
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}

