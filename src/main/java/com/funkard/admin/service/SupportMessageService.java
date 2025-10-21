package com.funkard.admin.service;

import com.funkard.admin.model.SupportMessage;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.repository.SupportMessageRepository;
import com.funkard.admin.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupportMessageService {

    private final SupportTicketRepository ticketRepo;
    private final SupportMessageRepository messageRepo;
    private final AdminNotificationService notifications;

    @Transactional
    public SupportMessage addMessage(UUID ticketId, String message, String sender) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        
        SupportMessage msg = new SupportMessage();
        msg.setTicket(ticket);
        msg.setMessage(message);
        msg.setSender(sender);
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

            // 📡 SSE Real-time notification per nuovo messaggio utente
            Map<String, Object> eventData = Map.of(
                    "type", "NEW_MESSAGE",
                    "ticketId", ticket.getId(),
                    "subject", ticket.getSubject(),
                    "email", ticket.getUserEmail(),
                    "sender", sender,
                    "message", message,
                    "hasNewMessages", true,
                    "createdAt", savedMessage.getCreatedAt()
            );
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
}
