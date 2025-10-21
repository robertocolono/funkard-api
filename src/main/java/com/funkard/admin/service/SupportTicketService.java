package com.funkard.admin.service;

import com.funkard.admin.dto.SupportMessageDTO;
import com.funkard.admin.dto.TicketDTO;
import com.funkard.admin.model.SupportMessage;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.repository.SupportMessageRepository;
import com.funkard.admin.repository.SupportTicketRepository;
import com.funkard.controller.AdminSupportStreamController;
import com.funkard.controller.SupportSseController;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository repo;
    private final SupportMessageRepository messageRepository;
    private final AdminNotificationService notifications;
    private final SimpMessagingTemplate messagingTemplate;
    private final SupportMessageService messageService;
    private final AdminSupportStreamController streamController;

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

        // 📡 SSE Real-time notification
        Map<String, Object> eventData = Map.of(
                "type", "NEW_TICKET",
                "id", savedTicket.getId(),
                "subject", savedTicket.getSubject(),
                "email", savedTicket.getUserEmail(),
                "status", savedTicket.getStatus(),
                "createdAt", savedTicket.getCreatedAt(),
                "priority", savedTicket.getPriority()
        );
        streamController.sendEvent("ticket-update", eventData);

        // 📡 Notifica SSE all'utente finale per conferma creazione
        SupportSseController.notifyTicketCreated(
            email,
            savedTicket.getId().toString(),
            subject
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

    // 🧠 Broadcast "intelligente" con notifiche selettive
    public void broadcastTicketUpdate(SupportTicket ticket, String eventType, boolean notifyUser) {
        try {
            Map<String, Object> update = new HashMap<>();
            update.put("event", eventType);
            update.put("ticketId", ticket.getId());
            update.put("subject", ticket.getSubject());
            update.put("status", ticket.getStatus());
            update.put("updatedAt", LocalDateTime.now());

            // 👨‍💻 Sempre invia update al pannello admin
            messagingTemplate.convertAndSend("/topic/admin/support", update);
            System.out.println("✅ WebSocket: Admin notificato per evento " + eventType);

            // 👤 Notifica utente solo se richiesto
            if (notifyUser) {
                messagingTemplate.convertAndSend("/topic/support/" + ticket.getId(), update);
                System.out.println("✅ WebSocket: Utente notificato per evento " + eventType);
            } else {
                System.out.println("🔒 WebSocket: Utente NON notificato per evento " + eventType);
            }
        } catch (Exception e) {
            System.err.println("❌ Errore WebSocket broadcast intelligente: " + e.getMessage());
        }
    }

    // 🔔 Metodo per aggiornare status con broadcast
    public SupportTicket updateTicketStatus(UUID id, String status) {
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
        
        return savedTicket;
    }

    // 💬 Aggiungi risposta admin al ticket
    @Transactional
    public SupportMessage addAdminReply(UUID ticketId, String sender, String content) {
        // Utilizza SupportMessageService per gestire automaticamente hasNewMessages
        SupportMessage savedMessage = messageService.addMessage(ticketId, content, sender);

        // 🔔 Notifica anche il messaggio in real-time
        try {
            messagingTemplate.convertAndSend("/topic/support/" + ticketId, new SupportMessageDTO(savedMessage));
            System.out.println("✅ WebSocket: Messaggio admin inviato per ticket " + ticketId);
        } catch (Exception e) {
            System.err.println("❌ Errore WebSocket messaggio admin: " + e.getMessage());
        }

        // 📡 Notifica SSE all'utente finale
        SupportTicket ticket = repo.findById(ticketId).orElse(null);
        if (ticket != null && ticket.getUserEmail() != null) {
            String messagePreview = content.length() > 60 ? content.substring(0, 60) + "..." : content;
            SupportSseController.notifyNewReply(
                ticket.getUserEmail(),
                ticketId.toString(),
                sender,
                messagePreview
            );
        }

        return savedMessage;
    }

    // 🎯 Risolve un ticket (notifica l'utente)
    public SupportTicket resolveTicket(UUID id) {
        SupportTicket ticket = updateTicketStatus(id, "resolved");
        
        // ✅ Notifica all'utente che è stato risolto
        broadcastTicketUpdate(ticket, "RESOLVED", true);
        
        // 📡 Notifica SSE all'utente finale
        if (ticket.getUserEmail() != null) {
            SupportSseController.notifyTicketResolved(
                ticket.getUserEmail(),
                ticket.getId().toString(),
                "resolved"
            );
        }
        
        return ticket;
    }

    // 🔒 Chiude un ticket (solo admin)
    public SupportTicket closeTicket(UUID id) {
        SupportTicket ticket = updateTicketStatus(id, "closed");
        
        // 🔒 Solo admin: non inviamo broadcast all'utente
        broadcastTicketUpdate(ticket, "CLOSED", false);
        
        // 📡 Notifica SSE all'utente finale
        if (ticket.getUserEmail() != null) {
            SupportSseController.notifyTicketClosed(
                ticket.getUserEmail(),
                ticket.getId().toString()
            );
        }
        
        return ticket;
    }

    // 🔄 Riapre un ticket risolto (solo admin)
    public SupportTicket reopenTicket(UUID id) {
        SupportTicket ticket = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket non trovato"));

        if (!ticket.getStatus().equalsIgnoreCase("resolved")) {
            throw new IllegalStateException("Solo i ticket risolti possono essere riaperti.");
        }

        ticket.setStatus("open");
        ticket.setUpdatedAt(LocalDateTime.now());
        SupportTicket savedTicket = repo.save(ticket);

        // 🔄 Notifica admin che il ticket è stato riaperto
        broadcastTicketUpdate(savedTicket, "REOPENED", false);

        return savedTicket;
    }

    // 👨‍💻 Marca i messaggi come letti dall'admin
    @Transactional
    public void markMessagesAsRead(UUID ticketId) {
        messageService.markAsReadByAdmin(ticketId);
    }

    // 📊 Conta ticket con nuovi messaggi
    public long countTicketsWithNewMessages() {
        return messageService.countTicketsWithNewMessages();
    }

    // 👨‍💻 Assegna ticket a un support
    @Transactional
    public SupportTicket assignTicket(UUID ticketId, String supportEmail) {
        SupportTicket ticket = repo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trovato"));

        if (ticket.isLocked()) {
            throw new IllegalStateException("Ticket già assegnato a: " + ticket.getAssignedTo());
        }

        ticket.setAssignedTo(supportEmail);
        ticket.setLocked(true);
        ticket.setStatus("in_progress");
        ticket.setUpdatedAt(LocalDateTime.now());
        
        SupportTicket savedTicket = repo.save(ticket);

        // 🔔 Notifica assegnazione
        notifications.createAdminNotification(
                "Ticket assegnato",
                "Ticket: " + ticket.getSubject() + " assegnato a " + supportEmail,
                "normal",
                "ticket_assignment"
        );

        // 🔔 Broadcast real-time
        broadcastTicketUpdate(savedTicket, "ASSIGNED", false);

        // 📡 SSE Real-time notification per assegnazione
        Map<String, Object> eventData = Map.of(
                "type", "TICKET_ASSIGNED",
                "id", savedTicket.getId(),
                "subject", savedTicket.getSubject(),
                "assignedTo", supportEmail,
                "status", savedTicket.getStatus(),
                "locked", savedTicket.isLocked()
        );
        streamController.sendEvent("ticket-update", eventData);

        return savedTicket;
    }

    // 🔓 Rilascia ticket (unlock)
    @Transactional
    public SupportTicket releaseTicket(UUID ticketId) {
        SupportTicket ticket = repo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trovato"));

        if (!ticket.isLocked()) {
            throw new IllegalStateException("Ticket non è attualmente assegnato");
        }

        ticket.setAssignedTo(null);
        ticket.setLocked(false);
        ticket.setStatus("open");
        ticket.setUpdatedAt(LocalDateTime.now());
        
        SupportTicket savedTicket = repo.save(ticket);

        // 🔔 Broadcast real-time
        broadcastTicketUpdate(savedTicket, "RELEASED", false);

        return savedTicket;
    }

    // 📋 Lista ticket assegnati a un support
    public List<SupportTicket> getTicketsAssignedTo(String supportEmail) {
        return repo.findAll().stream()
                .filter(t -> supportEmail.equals(t.getAssignedTo()))
                .sorted(Comparator.comparing(SupportTicket::getCreatedAt).reversed())
                .toList();
    }

    // 📊 Conta ticket assegnati
    public long countAssignedTickets() {
        return repo.findAll().stream()
                .filter(SupportTicket::isLocked)
                .count();
    }

    /**
     * 🎯 Assegna ticket con controllo ruoli avanzato
     */
    @Transactional
    public SupportTicket assignTicketWithRole(UUID ticketId, com.funkard.model.User user) {
        SupportTicket ticket = repo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trovato"));

        // Verifica permessi ruolo
        if (!canUserAssignTicket(user, ticket)) {
            throw new IllegalStateException("Utente non autorizzato ad assegnare questo ticket");
        }

        // Se già assegnato e non è lui → blocco
        if (ticket.isLocked() && !isUserAssignedToTicket(ticket, user)) {
            throw new IllegalStateException("Ticket già assegnato a un altro support");
        }

        // Assegna ticket
        ticket.setAssignedTo(user.getEmail());
        ticket.setAssignedToUser(user);
        ticket.setLocked(true);
        ticket.setStatus("in_progress");
        ticket.setUpdatedAt(LocalDateTime.now());

        SupportTicket savedTicket = repo.save(ticket);

        // 🔔 Notifica admin
        notifications.createAdminNotification(
                "Ticket assegnato",
                "Ticket: " + ticket.getSubject() + " assegnato a " + user.getEmail(),
                "normal",
                "ticket_assignment"
        );

        // 📡 SSE Real-time notification
        Map<String, Object> eventData = Map.of(
                "type", "TICKET_ASSIGNED",
                "id", savedTicket.getId(),
                "subject", savedTicket.getSubject(),
                "assignedTo", user.getEmail(),
                "assignedToUser", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                ),
                "status", savedTicket.getStatus(),
                "locked", savedTicket.isLocked()
        );
        streamController.sendEvent("ticket-update", eventData);

        return savedTicket;
    }

    /**
     * 🔓 Rilascia ticket con controllo ruoli
     */
    @Transactional
    public SupportTicket unassignTicketWithRole(UUID ticketId, com.funkard.model.User user) {
        SupportTicket ticket = repo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trovato"));

        // Verifica permessi per sbloccare
        if (!canUserUnassignTicket(user, ticket)) {
            throw new IllegalStateException("Non autorizzato a sbloccare questo ticket");
        }

        // Rilascia ticket
        ticket.setAssignedTo(null);
        ticket.setAssignedToUser(null);
        ticket.setLocked(false);
        ticket.setStatus("open");
        ticket.setUpdatedAt(LocalDateTime.now());

        SupportTicket savedTicket = repo.save(ticket);

        // 📡 SSE Real-time notification
        Map<String, Object> eventData = Map.of(
                "type", "TICKET_UNASSIGNED",
                "id", savedTicket.getId(),
                "subject", savedTicket.getSubject(),
                "status", savedTicket.getStatus(),
                "locked", savedTicket.isLocked()
        );
        streamController.sendEvent("ticket-update", eventData);

        return savedTicket;
    }

    /**
     * 🔐 Verifica se l'utente può assegnare il ticket
     */
    private boolean canUserAssignTicket(com.funkard.model.User user, SupportTicket ticket) {
        String userRole = user.getRole();
        
        // SUPER_ADMIN può assegnare qualsiasi ticket
        if ("SUPER_ADMIN".equals(userRole)) {
            return true;
        }
        
        // ADMIN può assegnare ticket non chiusi
        if ("ADMIN".equals(userRole)) {
            return !"closed".equals(ticket.getStatus());
        }
        
        // SUPPORT può assegnare solo ticket aperti
        if ("SUPPORT".equals(userRole)) {
            return "open".equals(ticket.getStatus()) || "reopened".equals(ticket.getStatus());
        }
        
        return false;
    }

    /**
     * 🔐 Verifica se l'utente può sbloccare il ticket
     */
    private boolean canUserUnassignTicket(com.funkard.model.User user, SupportTicket ticket) {
        String userRole = user.getRole();
        
        // SUPER_ADMIN può sbloccare qualsiasi ticket
        if ("SUPER_ADMIN".equals(userRole)) {
            return true;
        }
        
        // ADMIN può sbloccare qualsiasi ticket
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        
        // SUPPORT può sbloccare solo i propri ticket
        if ("SUPPORT".equals(userRole)) {
            return isUserAssignedToTicket(ticket, user);
        }
        
        return false;
    }

    /**
     * 🔍 Verifica se l'utente è assegnato al ticket
     */
    private boolean isUserAssignedToTicket(SupportTicket ticket, com.funkard.model.User user) {
        return ticket.getAssignedToUser() != null && 
               ticket.getAssignedToUser().getId().equals(user.getId());
    }
}
