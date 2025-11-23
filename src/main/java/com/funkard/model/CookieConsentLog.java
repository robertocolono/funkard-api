package com.funkard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 📋 Audit Log per tracciabilità consenso cookie (GDPR compliance)
 * 
 * Registra tutte le modifiche alle preferenze cookie per audit legale.
 * Principio di minimizzazione GDPR: salva solo dati necessari.
 */
@Entity
@Table(name = "cookie_consent_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CookieConsentLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 🔗 ID utente (non relazione per minimizzazione GDPR)
     * Non salviamo dati utente non necessari
     */
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;
    
    /**
     * 🎯 Azione eseguita: ACCEPTED, REJECTED, UPDATED, REVOKED
     * Immutabile dopo la creazione
     */
    @Column(name = "action", nullable = false, length = 20, updatable = false)
    private String action;
    
    /**
     * 📋 Preferenze precedenti (JSON)
     * NULL se è la prima accettazione
     */
    @Column(name = "old_preferences", columnDefinition = "TEXT", updatable = false)
    private String oldPreferences;
    
    /**
     * 📋 Nuove preferenze (JSON)
     */
    @Column(name = "new_preferences", columnDefinition = "TEXT", nullable = false, updatable = false)
    private String newPreferences;
    
    /**
     * 🔍 IP address (opzionale, per audit avanzato)
     * NULL per principio di minimizzazione GDPR
     */
    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;
    
    /**
     * 🌐 User Agent (opzionale, per audit avanzato)
     */
    @Column(name = "user_agent", columnDefinition = "TEXT", updatable = false)
    private String userAgent;
    
    /**
     * 📅 Timestamp creazione (immutabile)
     * Generato automaticamente da Hibernate
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

