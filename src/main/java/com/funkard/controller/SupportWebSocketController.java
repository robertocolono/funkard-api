package com.funkard.controller;

import com.funkard.admin.dto.SupportMessageDTO;
import com.funkard.admin.model.SupportMessage;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.service.SupportTicketService;
import com.funkard.admin.repository.SupportMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.UUID;

@Controller
public class SupportWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SupportTicketService ticketService;

    @Autowired
    private SupportMessageRepository messageRepository;

    // 📩 Riceve messaggio e lo inoltra in tempo reale
    @MessageMapping("/support/{ticketId}/send")
    public void handleMessage(@DestinationVariable UUID ticketId, SupportMessageDTO payload) {
        try {
            SupportTicket ticket = ticketService.findById(ticketId);
            if (ticket == null) {
                System.err.println("❌ Ticket non trovato: " + ticketId);
                return;
            }

            // Salva messaggio su DB
            SupportMessage msg = new SupportMessage();
            msg.setTicket(ticket);
            msg.setSender(payload.getSender());
            msg.setMessage(payload.getContent());
            msg.setCreatedAt(OffsetDateTime.now());
            messageRepository.save(msg);

            // Rispedisci il messaggio a tutti gli iscritti su quel ticket
            SupportMessageDTO broadcast = new SupportMessageDTO(msg);
            messagingTemplate.convertAndSend("/topic/support/" + ticketId, broadcast);
            
            System.out.println("✅ Messaggio WebSocket inviato per ticket: " + ticketId);
            
        } catch (Exception e) {
            System.err.println("❌ Errore WebSocket per ticket " + ticketId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
