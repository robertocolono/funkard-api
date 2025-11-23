package com.funkard.admin.dto;

import com.funkard.admin.model.SupportMessage;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SupportMessageDTO {
    private UUID id;
    private String sender;
    private String content;
    private OffsetDateTime createdAt;
    
    // 🌍 Campi traduzione
    private String originalText;
    private String translatedText;
    private String originalLanguage;
    private String targetLanguage;
    private Boolean isTranslated;

    // Constructors
    public SupportMessageDTO() {}

    public SupportMessageDTO(UUID id, String sender, String content, OffsetDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public SupportMessageDTO(SupportMessage msg) {
        this.id = msg.getId();
        this.sender = msg.getSender();
        this.content = msg.getMessage();
        this.createdAt = msg.getCreatedAt();
        
        // Campi traduzione
        this.originalText = msg.getMessage();
        this.translatedText = msg.getTranslatedText();
        this.originalLanguage = msg.getOriginalLanguage();
        this.targetLanguage = msg.getTargetLanguage();
        this.isTranslated = msg.getIsTranslated() != null ? msg.getIsTranslated() : false;
    }

    // Costruttore per WebSocket payload
    public SupportMessageDTO(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.createdAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    // Getters & Setters traduzione
    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public Boolean getIsTranslated() { return isTranslated; }
    public void setIsTranslated(Boolean isTranslated) { this.isTranslated = isTranslated; }
}
