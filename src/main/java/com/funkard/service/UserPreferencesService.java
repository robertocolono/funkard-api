package com.funkard.service;

import com.funkard.dto.CookiePreferencesDTO;
import com.funkard.model.User;
import com.funkard.model.UserPreferences;
import com.funkard.repository.UserPreferencesRepository;
import com.funkard.service.CookieConsentLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * 🍪 Service per gestione preferenze utente e cookie
 * 
 * Funzionalità:
 * - Salvataggio/aggiornamento preferenze cookie
 * - Sincronizzazione con backend per utenti loggati
 * - GDPR compliance con tracciabilità
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferencesService {
    
    private final UserPreferencesRepository preferencesRepository;
    private final CookieConsentLogService consentLogService;
    private final ObjectMapper objectMapper;
    
    /**
     * 🔍 Ottieni preferenze utente
     * @param user Utente
     * @return Optional<UserPreferences>
     */
    public Optional<UserPreferences> getPreferences(User user) {
        return preferencesRepository.findByUser(user);
    }
    
    /**
     * 🔍 Ottieni preferenze come DTO
     * @param user Utente
     * @return CookiePreferencesDTO
     */
    public CookiePreferencesDTO getPreferencesDTO(User user) {
        Optional<UserPreferences> prefs = preferencesRepository.findByUser(user);
        
        if (prefs.isEmpty()) {
            // Restituisci DTO vuoto se non esistono preferenze
            return new CookiePreferencesDTO(false, null, null, null);
        }
        
        UserPreferences preferences = prefs.get();
        Map<String, Boolean> cookiesMap = null;
        
        // Parse JSON preferences se presente
        if (preferences.getCookiesPreferences() != null && !preferences.getCookiesPreferences().isEmpty()) {
            try {
                cookiesMap = objectMapper.readValue(
                    preferences.getCookiesPreferences(), 
                    Map.class
                );
            } catch (JsonProcessingException e) {
                log.warn("Errore nel parsing cookies preferences per utente {}: {}", 
                    user.getId(), e.getMessage());
            }
        }
        
        return new CookiePreferencesDTO(
            preferences.getCookiesAccepted(),
            cookiesMap,
            preferences.getCookiesAcceptedAt(),
            preferences.getUpdatedAt()
        );
    }
    
    /**
     * 💾 Salva o aggiorna preferenze cookie con audit logging
     * @param user Utente
     * @param dto DTO con preferenze
     * @param ipAddress IP address (opzionale)
     * @param userAgent User Agent (opzionale)
     * @return UserPreferences salvato
     */
    @Transactional
    public UserPreferences saveCookiePreferences(
            User user, 
            CookiePreferencesDTO dto,
            String ipAddress,
            String userAgent) {
        log.info("Salvataggio preferenze cookie per utente: {}", user.getId());
        
        // Cerca preferenze esistenti
        Optional<UserPreferences> existing = preferencesRepository.findByUser(user);
        
        // Salva preferenze precedenti per audit log
        String oldPreferencesJson = null;
        Boolean oldCookiesAccepted = null;
        if (existing.isPresent()) {
            oldPreferencesJson = existing.get().getCookiesPreferences();
            oldCookiesAccepted = existing.get().getCookiesAccepted();
        }
        
        UserPreferences preferences;
        if (existing.isPresent()) {
            preferences = existing.get();
            log.debug("Aggiornamento preferenze esistenti per utente: {}", user.getId());
        } else {
            preferences = new UserPreferences();
            preferences.setUser(user);
            preferences.setCreatedAt(LocalDateTime.now());
            log.debug("Creazione nuove preferenze per utente: {}", user.getId());
        }
        
        // Aggiorna campi cookie
        preferences.setCookiesAccepted(dto.getCookiesAccepted());
        
        // Se cookiesAccepted è true, aggiorna timestamp
        if (Boolean.TRUE.equals(dto.getCookiesAccepted())) {
            preferences.setCookiesAcceptedAt(LocalDateTime.now());
        }
        
        // Serializza preferenze dettagliate in JSON
        String newPreferencesJson = null;
        if (dto.getCookiesPreferences() != null) {
            try {
                newPreferencesJson = objectMapper.writeValueAsString(dto.getCookiesPreferences());
                preferences.setCookiesPreferences(newPreferencesJson);
            } catch (JsonProcessingException e) {
                log.error("Errore nella serializzazione cookies preferences per utente {}: {}", 
                    user.getId(), e.getMessage());
                throw new RuntimeException("Errore nel salvataggio preferenze cookie", e);
            }
        }
        
        preferences.setUpdatedAt(LocalDateTime.now());
        
        UserPreferences saved = preferencesRepository.save(preferences);
        
        // 📋 Determina azione per audit log
        String action = determineAction(oldCookiesAccepted, dto.getCookiesAccepted());
        
        // 📋 Audit Log: Registra modifica per tracciabilità GDPR
        try {
            consentLogService.logConsentChange(
                user.getId(),
                action,
                oldPreferencesJson,
                newPreferencesJson != null ? newPreferencesJson : "{}",
                ipAddress,
                userAgent
            );
            log.debug("✅ Audit log creato per utente: {} - action: {}", user.getId(), action);
        } catch (Exception e) {
            // Non bloccare il salvataggio se l'audit log fallisce
            log.error("Errore nella creazione audit log per utente {}: {}", user.getId(), e.getMessage());
        }
        
        log.info("✅ Preferenze cookie salvate per utente: {}", user.getId());
        
        return saved;
    }
    
    /**
     * 🔍 Determina l'azione basandosi sulle preferenze vecchie e nuove
     * @param oldAccepted Preferenze precedenti (null se prima volta)
     * @param newAccepted Nuove preferenze
     * @return Azione: ACCEPTED, REJECTED, UPDATED, REVOKED
     */
    private String determineAction(Boolean oldAccepted, Boolean newAccepted) {
        if (oldAccepted == null) {
            // Prima volta: ACCEPTED o REJECTED
            return Boolean.TRUE.equals(newAccepted) 
                ? CookieConsentLogService.ACTION_ACCEPTED 
                : CookieConsentLogService.ACTION_REJECTED;
        }
        
        if (Boolean.TRUE.equals(oldAccepted) && Boolean.FALSE.equals(newAccepted)) {
            // Da accettato a rifiutato: REVOKED
            return CookieConsentLogService.ACTION_REVOKED;
        }
        
        if (Boolean.FALSE.equals(oldAccepted) && Boolean.TRUE.equals(newAccepted)) {
            // Da rifiutato ad accettato: ACCEPTED
            return CookieConsentLogService.ACTION_ACCEPTED;
        }
        
        // Modifica preferenze dettagliate: UPDATED
        return CookieConsentLogService.ACTION_UPDATED;
    }
    
    /**
     * 🗑️ Elimina preferenze utente
     * @param user Utente
     */
    @Transactional
    public void deletePreferences(User user) {
        log.info("Eliminazione preferenze per utente: {}", user.getId());
        preferencesRepository.deleteByUser(user);
    }
    
    /**
     * 🔍 Verifica se utente ha accettato i cookie
     * @param user Utente
     * @return true se ha accettato, false altrimenti
     */
    public boolean hasAcceptedCookies(User user) {
        return preferencesRepository.findByUser(user)
            .map(UserPreferences::getCookiesAccepted)
            .orElse(false);
    }
}

