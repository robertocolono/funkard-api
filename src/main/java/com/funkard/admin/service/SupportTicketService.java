package com.funkard.admin.service;

import com.funkard.admin.dto.TicketDTO;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository repo;
    private final AdminNotificationService notifications;
    private final SimpMessagingTemplate messagingTemplate;

    public SupportTicket create(String email, String subject, String message) {
        SupportTicket ticket = new SupportTicket();
        ticket.setUserEmail(email);
        ticket.setSubject(subject);
        ticket.setMessage(message);
        ticket.setStatus("open");
        ticket.setPriority("normal");
        ticket.setCategory("general");
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        SupportTicket savedTicket = repo.save(ticket);

        // 🔔 Notifica admin: nuovo ticket
        notifications.createAdminNotification(
                "Nuovo ticket di supporto",
                "Da: " + email + " — " + subject,
                "high",
                "support_ticket"
        );

        return savedTicket;
    }

    public List<SupportTicket> findAll() {
        return repo.findAll();
    }

    public List<SupportTicket> findByEmail(String email) {
        return repo.findAll().stream()
                .filter(t -> t.getUserEmail().equalsIgnoreCase(email))
                .sorted(Comparator.comparing(SupportTicket::getCreatedAt).reversed())
                .toList();
    }

    public SupportTicket findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
    }

    public SupportTicket updateStatus(UUID id, String status, String note) {
        SupportTicket ticket = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket non trovato"));
        ticket.setStatus(status);
        if (note != null && !note.isBlank()) {
            ticket.setAdminResponse(note);
        }
        ticket.setUpdatedAt(LocalDateTime.now());
        if ("resolved".equals(status) || "closed".equals(status)) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        
        SupportTicket savedTicket = repo.save(ticket);
        
        // 🔔 Notifica WebSocket: aggiornamento ticket
        try {
            messagingTemplate.convertAndSend("/topic/support/" + id + "/status", Map.of(
                "ticketId", id,
                "status", status,
                "updatedAt", LocalDateTime.now().toString(),
                "message", "Ticket aggiornato: " + status
            ));
            System.out.println("✅ WebSocket: Status aggiornato per ticket " + id);
        } catch (Exception e) {
            System.err.println("❌ Errore WebSocket status update: " + e.getMessage());
        }
        
        return savedTicket;
    }

    // 🔔 Metodo per broadcast completo del ticket
    public void broadcastTicketUpdate(SupportTicket ticket) {
        try {
            TicketDTO dto = TicketDTO.fromEntity(ticket);
            messagingTemplate.convertAndSend("/topic/support/" + ticket.getId(), dto);
            System.out.println("✅ WebSocket: Ticket aggiornato broadcast per " + ticket.getId());
        } catch (Exception e) {
            System.err.println("❌ Errore WebSocket broadcast: " + e.getMessage());
        }
    }

    // 🔔 Metodo per aggiornare status con broadcast
    public void updateTicketStatus(UUID id, String status) {
        SupportTicket ticket = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket non trovato"));

        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        if ("resolved".equals(status) || "closed".equals(status)) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        
        SupportTicket savedTicket = repo.save(ticket);

        // 🔔 Notifica real-time a tutti i client collegati
        broadcastTicketUpdate(savedTicket);
    }
}
