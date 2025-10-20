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

    @MessageMapping("/support/{ticketId}/send")
    public void handleMessage(@DestinationVariable UUID ticketId, SupportMessageDTO payload) {
        SupportTicket ticket = ticketService.findById(ticketId);
        if (ticket == null) return;

        SupportMessage msg = new SupportMessage();
        msg.setTicket(ticket);
        msg.setSender(payload.getSender());
        msg.setMessage(payload.getContent());
        msg.setCreatedAt(OffsetDateTime.now());
        messageRepository.save(msg);

        // Invia in broadcast ai client connessi
        messagingTemplate.convertAndSend("/topic/support/" + ticketId, new SupportMessageDTO(msg));
    }
}
