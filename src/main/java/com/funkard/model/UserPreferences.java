package com.funkard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 🍪 Modello per preferenze utente e gestione cookie (GDPR compliance)
 * 
 * Gestisce:
 * - Preferenze cookie (accettazione e dettagli)
 * - Timestamp accettazione per audit GDPR
 * - Preferenze JSON per granularità
 */
@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 🔗 Relazione OneToOne con User
     * Ogni utente ha una sola entry di preferenze
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    /**
     * 🍪 GDPR Compliance: Accettazione cookie
     * true = utente ha accettato i cookie
     * false/null = non accettato
     */
    @Column(name = "cookies_accepted")
    private Boolean cookiesAccepted = false;
    
    /**
     * 🍪 GDPR Compliance: Preferenze cookie dettagliate (JSON)
     * 
     * Formato esempio:
     * {
     *   "necessary": true,
     *   "analytics": false,
     *   "marketing": false,
     *   "functional": true
     * }
     */
    @Column(name = "cookies_preferences", columnDefinition = "TEXT")
    private String cookiesPreferences;
    
    /**
     * 🔒 GDPR Compliance: Timestamp accettazione cookie
     * Tracciabilità completa per audit
     */
    @Column(name = "cookies_accepted_at")
    private LocalDateTime cookiesAcceptedAt;
    
    /**
     * 📅 Timestamp creazione preferenze
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * 📅 Timestamp ultimo aggiornamento
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 🔄 Pre-update hook per aggiornare updatedAt
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

