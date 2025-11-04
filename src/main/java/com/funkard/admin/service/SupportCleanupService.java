package com.funkard.admin.service;

import com.funkard.admin.model.SupportMessage;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.repository.SupportMessageRepository;
import com.funkard.admin.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🧹 Service per cleanup automatico dei messaggi di supporto
 * Elimina messaggi associati a ticket risolti/chiusi più vecchi di 24 ore
 */
@Service
public class SupportCleanupService {

    private final SupportTicketRepository ticketRepository;
    private final SupportMessageRepository messageRepository;

    public SupportCleanupService(SupportTicketRepository ticketRepository,
                                 SupportMessageRepository messageRepository) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Elimina tutti i messaggi associati a ticket risolti o chiusi più vecchi di 24 ore
     * @return numero totale di messaggi eliminati
     */
    @Transactional
    public long cleanupOldMessages() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<SupportTicket> oldTickets = ticketRepository.findResolvedOrClosedOlderThan(cutoff);
        
        long totalDeleted = 0;
        
        for (SupportTicket ticket : oldTickets) {
            try {
                // Conta i messaggi prima di eliminarli
                List<SupportMessage> messages = 
                    messageRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId());
                
                if (!messages.isEmpty()) {
                    messageRepository.deleteByTicketId(ticket.getId());
                    totalDeleted += messages.size();
                }
            } catch (Exception e) {
                // Log errore ma continua con gli altri ticket
                System.err.println("❌ Errore durante cleanup messaggi per ticket " + ticket.getId() + ": " + e.getMessage());
            }
        }
        
        return totalDeleted;
    }

}

