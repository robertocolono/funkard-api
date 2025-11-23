package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 💬 Modello per messaggi chat tra utenti
 * 
 * Supporta traduzione automatica quando mittente e destinatario
 * hanno lingue diverse.
 */
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_sender", columnList = "sender_id"),
    @Index(name = "idx_chat_recipient", columnList = "recipient_id"),
    @Index(name = "idx_chat_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    
    @Column(nullable = false, columnDefinition = "text")
    private String originalText; // Testo originale del messaggio
    
    // 🌍 Campi traduzione automatica
    @Column(name = "original_language", length = 5)
    private String originalLanguage; // ISO 639-1 (es. "it", "en", "es")
    
    @Column(name = "translated_text", columnDefinition = "text")
    private String translatedText; // Testo tradotto
    
    @Column(name = "target_language", length = 5)
    private String targetLanguage; // Lingua di destinazione
    
    @Column(name = "is_translated", nullable = false)
    private Boolean isTranslated = false; // Flag traduzione
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "read_at")
    private LocalDateTime readAt; // Timestamp lettura messaggio
}

