package com.funkard.admin.service;

import com.funkard.admin.model.SupportMessage;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.repository.SupportMessageRepository;
import com.funkard.admin.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupportMessageService {

    private final SupportTicketRepository ticketRepo;
    private final SupportMessageRepository messageRepo;

    public SupportMessage addMessage(UUID ticketId, String message, String sender) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        SupportMessage msg = new SupportMessage();
        msg.setTicket(ticket);
        msg.setMessage(message);
        msg.setSender(sender);
        return messageRepo.save(msg);
    }

    public List<SupportMessage> getMessages(UUID ticketId) {
        return messageRepo.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }
}
