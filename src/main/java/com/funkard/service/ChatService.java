package com.funkard.service;

import com.funkard.dto.ChatMessageDTO;
import com.funkard.model.ChatMessage;
import com.funkard.model.User;
import com.funkard.repository.ChatMessageRepository;
import com.funkard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 💬 Servizio per chat tra utenti
 * 
 * Gestisce invio messaggi con traduzione automatica
 * quando mittente e destinatario hanno lingue diverse.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final UnifiedTranslationService unifiedTranslationService;
    
    /**
     * 💬 Invia messaggio tra utenti con traduzione automatica
     */
    @Transactional
    public ChatMessageDTO sendMessage(Long senderId, Long recipientId, String text) {
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("Mittente non trovato"));
        
        User recipient = userRepository.findById(recipientId)
            .orElseThrow(() -> new IllegalArgumentException("Destinatario non trovato"));
        
        // 🌍 Rileva lingue
        String senderLanguage = sender.getLanguage() != null ? sender.getLanguage() : "en";
        String recipientLanguage = recipient.getLanguage() != null ? recipient.getLanguage() : "en";
        
        // Traduci se necessario
        String translatedText = null;
        boolean isTranslated = false;
        
        if (!senderLanguage.equalsIgnoreCase(recipientLanguage)) {
            try {
                // 🌍 Usa UnifiedTranslationService (GPT-4o-mini + DeepL fallback)
                translatedText = unifiedTranslationService.translate(text, recipientLanguage);
                isTranslated = (translatedText != null && !translatedText.equals(text) && !translatedText.trim().isEmpty());
                
                if (isTranslated) {
                    log.info("✅ Messaggio tradotto: {} -> {} ({} -> {})", 
                        senderLanguage, recipientLanguage, 
                        text.length() > 50 ? text.substring(0, 50) + "..." : text, 
                        translatedText.length() > 50 ? translatedText.substring(0, 50) + "..." : translatedText);
                } else {
                    log.debug("Traduzione non necessaria o fallita, uso testo originale");
                }
            } catch (Exception e) {
                log.warn("⚠️ Errore durante traduzione messaggio chat: {}, continuo senza traduzione", e.getMessage());
                // Continua senza traduzione (translatedText rimane null, isTranslated = false)
            }
        }
        
        // Salva messaggio
        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setOriginalText(text);
        message.setOriginalLanguage(senderLanguage);
        message.setTranslatedText(translatedText);
        message.setTargetLanguage(recipientLanguage);
        message.setIsTranslated(isTranslated);
        message.setCreatedAt(LocalDateTime.now());
        
        ChatMessage saved = chatMessageRepository.save(message);
        
        return new ChatMessageDTO(saved);
    }
    
    /**
     * 📋 Recupera conversazione tra due utenti
     */
    public List<ChatMessageDTO> getConversation(Long userId1, Long userId2) {
        List<ChatMessage> messages = chatMessageRepository.findConversationBetweenUsers(userId1, userId2);
        return messages.stream()
            .map(ChatMessageDTO::new)
            .collect(Collectors.toList());
    }
    
    /**
     * 📬 Recupera messaggi non letti per un utente
     */
    public List<ChatMessageDTO> getUnreadMessages(Long userId) {
        List<ChatMessage> messages = chatMessageRepository.findUnreadMessages(userId);
        return messages.stream()
            .map(ChatMessageDTO::new)
            .collect(Collectors.toList());
    }
    
    /**
     * ✅ Marca messaggio come letto
     */
    @Transactional
    public void markAsRead(UUID messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Messaggio non trovato"));
        
        // Verifica che l'utente sia il destinatario
        if (message.getRecipient().getId().equals(userId)) {
            message.setReadAt(LocalDateTime.now());
            chatMessageRepository.save(message);
        }
    }
    
    /**
     * 📊 Conta messaggi non letti
     */
    public long countUnreadMessages(Long userId) {
        return chatMessageRepository.countUnreadMessages(userId);
    }
}

