package com.funkard.admin.controller;

import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.service.SupportTicketService;
import com.funkard.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

/**
 * 🎯 Controller per gestione assegnazioni ticket con controllo ruoli
 */
@RestController
@RequestMapping("/api/admin/tickets")
@CrossOrigin(origins = {"https://funkard-admin.vercel.app", "http://localhost:3000"})
public class TicketAssignmentController {

    @Autowired
    private SupportTicketService ticketService;

    @Value("${admin.token}")
    private String adminToken;

    /**
     * 🎯 Assegna ticket con controllo ruoli
     * POST /api/admin/tickets/{id}/assign
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignTicket(
            @PathVariable UUID id,
            @RequestHeader("X-Admin-Token") String token,
            @RequestHeader(value = "X-Admin-User", required = false) String adminUser) {
        
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // TODO: Implementare autenticazione JWT per ottenere User reale
            // Per ora usiamo un User mock basato sull'header
            User user = createMockUser(adminUser);
            
            SupportTicket ticket = ticketService.assignTicketWithRole(id, user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket assegnato con successo",
                "ticket", Map.of(
                    "id", ticket.getId(),
                    "subject", ticket.getSubject(),
                    "assignedTo", ticket.getAssignedTo(),
                    "status", ticket.getStatus(),
                    "locked", ticket.isLocked()
                )
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore durante l'assegnazione: " + e.getMessage()));
        }
    }

    /**
     * 🔓 Rilascia ticket con controllo ruoli
     * POST /api/admin/tickets/{id}/unassign
     */
    @PostMapping("/{id}/unassign")
    public ResponseEntity<?> unassignTicket(
            @PathVariable UUID id,
            @RequestHeader("X-Admin-Token") String token,
            @RequestHeader(value = "X-Admin-User", required = false) String adminUser) {
        
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // TODO: Implementare autenticazione JWT per ottenere User reale
            User user = createMockUser(adminUser);
            
            SupportTicket ticket = ticketService.unassignTicketWithRole(id, user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ticket rilasciato con successo",
                "ticket", Map.of(
                    "id", ticket.getId(),
                    "subject", ticket.getSubject(),
                    "assignedTo", ticket.getAssignedTo(),
                    "status", ticket.getStatus(),
                    "locked", ticket.isLocked()
                )
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore durante il rilascio: " + e.getMessage()));
        }
    }

    /**
     * 📊 Ottieni ticket assegnati a un utente
     * GET /api/admin/tickets/assigned/{userEmail}
     */
    @GetMapping("/assigned/{userEmail}")
    public ResponseEntity<?> getAssignedTickets(
            @PathVariable String userEmail,
            @RequestHeader("X-Admin-Token") String token) {
        
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            var tickets = ticketService.getTicketsAssignedTo(userEmail);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "tickets", tickets,
                "count", tickets.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore durante il recupero: " + e.getMessage()));
        }
    }

    /**
     * 📊 Statistiche assegnazioni
     * GET /api/admin/tickets/assignment-stats
     */
    @GetMapping("/assignment-stats")
    public ResponseEntity<?> getAssignmentStats(@RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            long assignedCount = ticketService.countAssignedTickets();
            long newMessagesCount = ticketService.countTicketsWithNewMessages();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", Map.of(
                    "assignedTickets", assignedCount,
                    "ticketsWithNewMessages", newMessagesCount,
                    "timestamp", System.currentTimeMillis()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Errore durante il recupero statistiche: " + e.getMessage()));
        }
    }

    /**
     * 🔧 Crea User mock per testing (da sostituire con autenticazione JWT)
     */
    private User createMockUser(String adminUser) {
        User user = new User();
        user.setId(1L);
        user.setEmail(adminUser != null ? adminUser : "admin@funkard.com");
        user.setRole("ADMIN"); // Default role, da modificare con autenticazione reale
        return user;
    }
}
