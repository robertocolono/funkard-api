package com.funkard.service;

import com.funkard.model.CookieConsentLog;
import com.funkard.repository.CookieConsentLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 📋 Service per gestione audit log consenso cookie
 * 
 * Funzionalità:
 * - Logging automatico di tutte le azioni di consenso
 * - Tracciabilità completa per audit GDPR
 * - Export log per utenti e admin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CookieConsentLogService {
    
    private final CookieConsentLogRepository logRepository;
    
    /**
     * Valori ammessi per action
     */
    public static final String ACTION_ACCEPTED = "ACCEPTED";
    public static final String ACTION_REJECTED = "REJECTED";
    public static final String ACTION_UPDATED = "UPDATED";
    public static final String ACTION_REVOKED = "REVOKED";
    
    /**
     * 📋 Registra una modifica al consenso cookie
     * 
     * @param userId ID utente
     * @param action Azione: ACCEPTED, REJECTED, UPDATED, REVOKED
     * @param oldPreferences Preferenze precedenti (JSON o null)
     * @param newPreferences Nuove preferenze (JSON)
     * @param ipAddress IP address (opzionale)
     * @param userAgent User Agent (opzionale)
     * @return CookieConsentLog salvato
     */
    @Transactional
    public CookieConsentLog logConsentChange(
            Long userId,
            String action,
            String oldPreferences,
            String newPreferences,
            String ipAddress,
            String userAgent) {
        
        // Validazione action
        if (!isValidAction(action)) {
            throw new IllegalArgumentException("Azione non valida: " + action + 
                ". Valori ammessi: ACCEPTED, REJECTED, UPDATED, REVOKED");
        }
        
        log.info("📋 Registrazione audit log: userId={}, action={}", userId, action);
        
        CookieConsentLog logEntry = new CookieConsentLog();
        logEntry.setUserId(userId);
        logEntry.setAction(action);
        logEntry.setOldPreferences(oldPreferences);
        logEntry.setNewPreferences(newPreferences != null ? newPreferences : "{}");
        logEntry.setIpAddress(ipAddress); // Opzionale, può essere null
        logEntry.setUserAgent(userAgent); // Opzionale, può essere null
        
        CookieConsentLog saved = logRepository.save(logEntry);
        log.debug("✅ Audit log creato: id={}", saved.getId());
        
        return saved;
    }
    
    /**
     * 🔍 Ottieni tutti i log per un utente (ordinati per data, più recenti prima)
     * @param userId ID utente
     * @return Lista log
     */
    public List<CookieConsentLog> getLogsByUserId(Long userId) {
        return logRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 🔍 Ottieni ultimo log per un utente
     * @param userId ID utente
     * @return Ultimo log o null
     */
    public CookieConsentLog getLastLogByUserId(Long userId) {
        return logRepository.findMostRecentByUserId(userId).orElse(null);
    }
    
    /**
     * 🗑️ Elimina tutti i log per un utente (GDPR: diritto alla cancellazione)
     * @param userId ID utente
     */
    @Transactional
    public void deleteLogsByUserId(Long userId) {
        log.info("🗑️ Eliminazione audit log per utente: {}", userId);
        logRepository.deleteByUserId(userId);
    }
    
    /**
     * ✅ Verifica se un'azione è valida
     * @param action Azione da validare
     * @return true se valida
     */
    private boolean isValidAction(String action) {
        return action != null && (
            ACTION_ACCEPTED.equals(action) ||
            ACTION_REJECTED.equals(action) ||
            ACTION_UPDATED.equals(action) ||
            ACTION_REVOKED.equals(action)
        );
    }
}

