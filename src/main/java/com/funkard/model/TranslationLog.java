package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 📜 Modello per log traduzioni
 * 
 * Traccia tutte le traduzioni effettuate per audit,
 * privacy e debugging.
 */
@Entity
@Table(name = "translation_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, columnDefinition = "text")
    private String sourceText;
    
    @Column(columnDefinition = "text")
    private String translatedText;
    
    @Column(nullable = false, length = 5)
    private String sourceLanguage; // ISO 639-1
    
    @Column(nullable = false, length = 5)
    private String targetLanguage; // ISO 639-1
    
    @Column(name = "translation_provider", length = 50)
    private String translationProvider; // "deepl", "google", "internal"
    
    @Column(nullable = false)
    private Boolean success = true;
    
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Riferimenti opzionali per tracciabilità
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "message_type", length = 50)
    private String messageType; // "chat", "support", "email"
    
    @Column(name = "message_id")
    private UUID messageId; // ID del messaggio tradotto
}

