package com.funkard.admin.service;

import com.funkard.admin.model.SupportMessage;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.repository.SupportMessageRepository;
import com.funkard.admin.repository.SupportTicketRepository;
import com.funkard.model.User;
import com.funkard.repository.UserRepository;
import com.funkard.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportMessageService {

    private final SupportTicketRepository ticketRepo;
    private final SupportMessageRepository messageRepo;
    private final AdminNotificationService notifications;
    private final TranslationService translationService;
    private final UserRepository userRepository;

    @Transactional
    public SupportMessage addMessage(UUID ticketId, String message, String sender) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        
        // 🌍 Rileva lingue mittente e destinatario
        String senderLanguage = detectSenderLanguage(sender, ticket);
        String targetLanguage = detectTargetLanguage(sender, ticket);
        
        // Traduci se necessario
        String translatedText = null;
        boolean isTranslated = false;
        
        if (senderLanguage != null && targetLanguage != null && 
            !senderLanguage.equalsIgnoreCase(targetLanguage)) {
            try {
                Long userId = ticket.getUserId() != null ? Long.parseLong(ticket.getUserId()) : null;
                translatedText = translationService.translate(
                    message, senderLanguage, targetLanguage, 
                    userId, "support", null
                );
                isTranslated = (translatedText != null && !translatedText.equals(message));
            } catch (Exception e) {
                log.warn("⚠️ Errore durante traduzione messaggio supporto: {}", e.getMessage());
                // Continua senza traduzione
            }
        }
        
        SupportMessage msg = new SupportMessage();
        msg.setTicket(ticket);
        msg.setMessage(message);
        msg.setSender(sender);
        msg.setOriginalLanguage(senderLanguage);
        msg.setTranslatedText(translatedText);
        msg.setTargetLanguage(targetLanguage);
        msg.setIsTranslated(isTranslated);
        msg.setCreatedAt(OffsetDateTime.now());
        SupportMessage savedMessage = messageRepo.save(msg);

        boolean fromUser = !sender.equalsIgnoreCase("admin");

        // 🔔 Gestione flag hasNewMessages
        if (fromUser) {
            // 👤 Utente scrive: flag = true (admin deve vedere)
            ticket.setHasNewMessages(true);
            ticket.setUpdatedAt(java.time.LocalDateTime.now());
            ticketRepo.save(ticket);
            
            // 🔔 Notifica automatica admin
            notifications.createAdminNotification(
                    "Nuovo messaggio utente",
                    "Ticket: " + ticket.getSubject(),
                    "normal",
                    "support_message"
            );

            // 📡 SSE Real-time notification usando nuovo sistema realtime
            Map<String, Object> eventData = Map.of(
                    "ticketId", ticket.getId().toString(),
                    "subject", ticket.getSubject(),
                    "email", ticket.getUserEmail(),
                    "sender", sender,
                    "message", message,
                    "hasNewMessages", true,
                    "createdAt", savedMessage.getCreatedAt().toString()
            );
            // Notifica admin
            com.funkard.realtime.AdminStreamController.sendToRole("ADMIN", 
                com.funkard.realtime.EventType.NEW_REPLY, eventData);
            com.funkard.realtime.AdminStreamController.sendToRole("SUPER_ADMIN", 
                com.funkard.realtime.EventType.NEW_REPLY, eventData);
        } else {
            // 👨‍💻 Admin risponde: flag = false (admin ha già visto)
            ticket.setHasNewMessages(false);
            ticket.setUpdatedAt(java.time.LocalDateTime.now());
            ticketRepo.save(ticket);
        }

        return savedMessage;
    }

    public List<SupportMessage> getMessages(UUID ticketId) {
        return messageRepo.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    // 👨‍💻 Marca i messaggi come letti dall'admin
    @Transactional
    public void markAsReadByAdmin(UUID ticketId) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        
        ticket.setHasNewMessages(false);
        ticket.setUpdatedAt(java.time.LocalDateTime.now());
        ticketRepo.save(ticket);
    }

    // 📊 Conta ticket con nuovi messaggi
    public long countTicketsWithNewMessages() {
        return ticketRepo.countByHasNewMessagesTrue();
    }
    
    /**
     * 🌍 Rileva lingua del mittente
     */
    private String detectSenderLanguage(String sender, SupportTicket ticket) {
        // Se è admin, usa lingua di default o lingua admin
        if (sender != null && sender.equalsIgnoreCase("admin")) {
            // TODO: Recupera lingua admin da preferenze (default: "en")
            return "en";
        }
        
        // Se è utente, recupera da user.language
        if (ticket.getUserId() != null) {
            try {
                Long userId = Long.parseLong(ticket.getUserId());
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && user.getLanguage() != null) {
                    return user.getLanguage();
                }
            } catch (Exception e) {
                log.debug("Errore recupero lingua utente: {}", e.getMessage());
            }
        }
        
        // Fallback: usa lingua da userEmail o default
        return "en";
    }
    
    /**
     * 🌍 Rileva lingua del destinatario
     */
    private String detectTargetLanguage(String sender, SupportTicket ticket) {
        // Se mittente è admin, destinatario è utente
        if (sender != null && sender.equalsIgnoreCase("admin")) {
            return detectSenderLanguage("user", ticket);
        }
        
        // Se mittente è utente, destinatario è admin (default: "en")
        return "en";
    }
}
