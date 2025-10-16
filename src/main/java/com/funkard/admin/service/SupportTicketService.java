package com.funkard.admin.service;

import com.funkard.admin.dto.SupportTicketDTO;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.repository.SupportTicketRepository;
import com.funkard.service.EmailService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SupportTicketService {
    
    private final SupportTicketRepository repository;
    private final EmailService emailService;
    
    public SupportTicketService(SupportTicketRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }
    
    // 🔹 Lista tutti i ticket
    public List<SupportTicketDTO> getAllTickets() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(SupportTicketDTO::fromEntity)
                .toList();
    }
    
    // 🔹 Lista ticket attivi
    public List<SupportTicketDTO> getActiveTickets() {
        return repository.findActiveTickets()
                .stream()
                .map(SupportTicketDTO::fromEntity)
                .toList();
    }
    
    // 🔹 Lista ticket per status
    public List<SupportTicketDTO> getTicketsByStatus(String status) {
        return repository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(SupportTicketDTO::fromEntity)
                .toList();
    }
    
    // 🔹 Lista ticket per priorità
    public List<SupportTicketDTO> getTicketsByPriority(String priority) {
        return repository.findByPriorityOrderByCreatedAtDesc(priority)
                .stream()
                .map(SupportTicketDTO::fromEntity)
                .toList();
    }
    
    // 🔹 Lista ticket per categoria
    public List<SupportTicketDTO> getTicketsByCategory(String category) {
        return repository.findByCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(SupportTicketDTO::fromEntity)
                .toList();
    }
    
    // 🔹 Rispondi a ticket
    public void replyToTicket(UUID ticketId, String adminResponse) {
        SupportTicket ticket = repository.findById(ticketId).orElseThrow();
        ticket.setAdminResponse(adminResponse);
        ticket.setStatus("resolved");
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setResolvedAt(LocalDateTime.now());
        repository.save(ticket);
        
        // Invia email di risposta all'utente
        try {
            String subject = "Risposta al tuo ticket di supporto: " + ticket.getSubject();
            emailService.sendSimple(ticket.getUserEmail(), subject, adminResponse);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Errore invio email: " + e.getMessage());
        }
    }
    
    // 🔹 Chiudi ticket
    public void closeTicket(UUID ticketId) {
        SupportTicket ticket = repository.findById(ticketId).orElseThrow();
        ticket.setStatus("closed");
        ticket.setUpdatedAt(LocalDateTime.now());
        repository.save(ticket);
    }
    
    // 🔹 Aggiorna priorità
    public void updatePriority(UUID ticketId, String priority) {
        SupportTicket ticket = repository.findById(ticketId).orElseThrow();
        ticket.setPriority(priority);
        ticket.setUpdatedAt(LocalDateTime.now());
        repository.save(ticket);
    }
    
    // 🔹 Statistiche
    public long getOpenTicketsCount() {
        return repository.countByStatus("open");
    }
    
    public long getInProgressTicketsCount() {
        return repository.countByStatus("in_progress");
    }
    
    public long getResolvedTicketsCount() {
        return repository.countByStatus("resolved");
    }
    
    public long getTicketsCountByPriority(String priority) {
        return repository.countByPriority(priority);
    }
}
