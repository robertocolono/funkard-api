package com.funkard.admin.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "support_messages", indexes = {
    @Index(name = "idx_ticket_id", columnList = "ticket_id")
})
public class SupportMessage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false, length = 100)
    private String sender; // email utente o "admin"

    // 🌍 Campi traduzione automatica
    @Column(name = "original_language", length = 5)
    private String originalLanguage; // ISO 639-1 (es. "it", "en", "es")
    
    @Column(name = "translated_text", columnDefinition = "text")
    private String translatedText; // Testo tradotto
    
    @Column(name = "target_language", length = 5)
    private String targetLanguage; // Lingua di destinazione
    
    @Column(name = "is_translated", nullable = false)
    private Boolean isTranslated = false; // Flag traduzione

    private OffsetDateTime createdAt = OffsetDateTime.now();

    // === Costruttori ===
    public SupportMessage() {}

    public SupportMessage(SupportTicket ticket, String message, String sender) {
        this.ticket = ticket;
        this.message = message;
        this.sender = sender;
    }

    // === Getters & Setters ===
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public SupportTicket getTicket() { return ticket; }
    public void setTicket(SupportTicket ticket) { this.ticket = ticket; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    // Getters & Setters per traduzione
    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public Boolean getIsTranslated() { return isTranslated; }
    public void setIsTranslated(Boolean isTranslated) { this.isTranslated = isTranslated; }
}
