package com.funkard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID; // unused but requested
import java.time.Instant; // unused but requested

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    private String password;  // hashed password
    
    @Column(unique = true)
    private String handle;
    private String nome;
    private String paese;
    private String tipoUtente; // PRIVATO o BUSINESS

    private String indirizzo;
    private String citta;
    private String cap;
    private String telefono;
    private String metodoPagamento;

    private Boolean accettaTermini; // ⚠️ Legacy - mantenuto per retrocompatibilità
    
    // 🔒 GDPR Compliance: Timestamp accettazione Termini e Privacy Policy
    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;
    
    @Column(name = "privacy_accepted_at")
    private LocalDateTime privacyAcceptedAt;
    
    private Boolean verified = false;
    private Boolean flagged = false;

    // Campi esistenti per compatibilità
    private String username;
    private String avatarUrl;
    private String role = "USER";
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Campi profilo aggiuntivi per compatibilità con UserService
    @Column(name = "language", length = 5)
    private String language = "en";
    
    private String theme;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // 🔹 Campo valuta preferita
    @Column(name = "preferred_currency", nullable = false, length = 3)
    private String preferredCurrency = "EUR";
    
    // 🗑️ GDPR Compliance: Flag cancellazione account in corso
    @Column(name = "deletion_pending", nullable = false)
    private Boolean deletionPending = false;
    
    /**
     * 📅 Data richiesta cancellazione (per tracciabilità)
     */
    @Column(name = "deletion_requested_at")
    private LocalDateTime deletionRequestedAt;
    
    /**
     * 🌍 Descrizione profilo venditore originale (testo scritto dall'utente)
     * Salvata nella lingua originale per traduzione on-demand
     * Massimo 500 caratteri
     */
    @Column(name = "description_original", columnDefinition = "TEXT")
    @Size(max = 500, message = "La bio del venditore non può superare 500 caratteri")
    private String descriptionOriginal;
    
    /**
     * 🌍 Lingua originale della descrizione profilo (codice ISO 639-1, es. "it", "en", "es")
     */
    @Column(name = "description_language", length = 5)
    private String descriptionLanguage;

    // Metodi di compatibilità per DTO/Service
    public String getName() { return this.nome; }
    public void setName(String name) { this.nome = name; }
}