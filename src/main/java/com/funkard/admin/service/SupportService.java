package com.funkard.admin.service;

import com.funkard.admin.dto.TicketDTO;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.repository.SupportTicketRepository;
import com.funkard.service.EmailService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class SupportService {
    private final SupportTicketRepository repo;
    private final EmailService emailService;

    public SupportService(SupportTicketRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    public List<TicketDTO> getAllTickets() {
        return repo.findAll().stream()
                .map(TicketDTO::fromEntity)
                .toList();
    }

    public TicketDTO getTicketById(UUID id) {
        SupportTicket ticket = repo.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
        return TicketDTO.fromEntity(ticket);
    }

    public void replyToTicket(UUID id, String reply) {
        var t = repo.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
        try {
            emailService.sendSimple(t.getUserId(), "Risposta supporto Funkard", reply);
            t.setStatus("answered");
            repo.save(t);
        } catch (Exception e) {
            System.err.println("Errore invio email per ticket " + id + ": " + e.getMessage());
            throw new RuntimeException("Errore durante l'invio della risposta", e);
        }
    }

    public void closeTicket(UUID id) {
        var t = repo.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
        t.setStatus("closed");
        repo.save(t);
    }
}
