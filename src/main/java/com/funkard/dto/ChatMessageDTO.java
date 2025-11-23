package com.funkard.dto;

import com.funkard.model.ChatMessage;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 💬 DTO per messaggi chat tra utenti
 * 
 * Include campi traduzione automatica per frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private UUID id;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    
    // Testo messaggio
    private String originalText;
    private String translatedText;
    private String originalLanguage;
    private String targetLanguage;
    private Boolean isTranslated;
    
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    /**
     * Costruttore da entità ChatMessage
     */
    public ChatMessageDTO(ChatMessage message) {
        this.id = message.getId();
        this.senderId = message.getSender() != null ? message.getSender().getId() : null;
        this.senderName = message.getSender() != null ? 
            (message.getSender().getNome() != null ? message.getSender().getNome() : message.getSender().getEmail()) : null;
        this.recipientId = message.getRecipient() != null ? message.getRecipient().getId() : null;
        this.recipientName = message.getRecipient() != null ? 
            (message.getRecipient().getNome() != null ? message.getRecipient().getNome() : message.getRecipient().getEmail()) : null;
        
        this.originalText = message.getOriginalText();
        this.translatedText = message.getTranslatedText();
        this.originalLanguage = message.getOriginalLanguage();
        this.targetLanguage = message.getTargetLanguage();
        this.isTranslated = message.getIsTranslated() != null ? message.getIsTranslated() : false;
        
        this.createdAt = message.getCreatedAt();
        this.readAt = message.getReadAt();
    }
}

