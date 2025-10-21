package com.funkard.controller;

import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.service.SupportTicketService;
import com.funkard.admin.service.SupportMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 🎫 Controller per gestione ticket di supporto per utenti finali
 */
@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = {"https://funkard.vercel.app", "https://funkardnew.vercel.app", "http://localhost:3000"})
public class SupportController {

    @Autowired
    private SupportTicketService ticketService;
    
    @Autowired
    private SupportMessageService messageService;

    /**
     * 🎫 Crea nuovo ticket di supporto
     * POST /api/support/tickets
     */
    @PostMapping("/tickets")
    public ResponseEntity<?> createTicket(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String subject = request.get("subject");
            String message = request.get("message");
            String category = request.getOrDefault("category", "general");

            if (email == null || subject == null || message == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email, subject e message sono obbligatori"));
            }

            SupportTicket ticket = ticketService.create(email, subject, message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket creato con successo",
                "ticketId", ticket.getId(),
                "status", ticket.getStatus()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la creazione del ticket: " + e.getMessage()));
        }
    }

    /**
     * 📋 Ottieni ticket per utente
     * GET /api/support/tickets?email=user@example.com
     */
    @GetMapping("/tickets")
    public ResponseEntity<?> getUserTickets(@RequestParam String email) {
        try {
            List<SupportTicket> tickets = ticketService.findByEmail(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", tickets,
                "count", tickets.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il recupero dei ticket: " + e.getMessage()));
        }
    }

    /**
     * 📄 Ottieni dettagli ticket specifico
     * GET /api/support/tickets/{id}
     */
    @GetMapping("/tickets/{id}")
    public ResponseEntity<?> getTicket(@PathVariable UUID id) {
        try {
            SupportTicket ticket = ticketService.findById(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "ticket", ticket
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ticket non trovato: " + e.getMessage()));
        }
    }

    /**
     * 💬 Aggiungi messaggio a ticket esistente
     * POST /api/support/tickets/{id}/reply
     */
    @PostMapping("/tickets/{id}/reply")
    public ResponseEntity<?> replyToTicket(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String sender = request.getOrDefault("sender", "user");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Il messaggio non può essere vuoto"));
            }

            // Aggiungi messaggio (gestito da SupportMessageService)
            var reply = messageService.addMessage(id, message, sender);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Messaggio inviato con successo",
                "replyId", reply.getId()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'invio del messaggio: " + e.getMessage()));
        }
    }

    /**
     * 🔄 Riapri ticket risolto
     * POST /api/support/tickets/{id}/reopen
     */
    @PostMapping("/tickets/{id}/reopen")
    public ResponseEntity<?> reopenTicket(@PathVariable UUID id) {
        try {
            SupportTicket ticket = ticketService.reopenTicket(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket riaperto con successo",
                "status", ticket.getStatus()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la riapertura del ticket: " + e.getMessage()));
        }
    }

    /**
     * 📊 Statistiche ticket utente
     * GET /api/support/stats?email=user@example.com
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(@RequestParam String email) {
        try {
            List<SupportTicket> tickets = ticketService.findByEmail(email);
            
            long openTickets = tickets.stream()
                    .filter(t -> "open".equals(t.getStatus()))
                    .count();
            
            long resolvedTickets = tickets.stream()
                    .filter(t -> "resolved".equals(t.getStatus()))
                    .count();
            
            long closedTickets = tickets.stream()
                    .filter(t -> "closed".equals(t.getStatus()))
                    .count();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", Map.of(
                    "totalTickets", tickets.size(),
                    "openTickets", openTickets,
                    "resolvedTickets", resolvedTickets,
                    "closedTickets", closedTickets
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il recupero delle statistiche: " + e.getMessage()));
        }
    }
}