package com.funkard.controller;

import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 🎯 Controller per gestione assegnazioni ticket con sistema basato sui ruoli
 */
@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://funkard-admin.vercel.app", "https://funkard.vercel.app", "http://localhost:3000"})
public class AdminTicketAssignmentController {

    private final SupportTicketService ticketService;

    /**
     * 🎯 Assegna ticket a support specifico
     * POST /api/admin/tickets/{id}/assign
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> assignTicket(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        try {
            String supportEmail = request.get("supportEmail");
            if (supportEmail == null || supportEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "supportEmail è obbligatorio"));
            }

            SupportTicket ticket = ticketService.assignTicket(id, supportEmail);
            
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
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'assegnazione: " + e.getMessage()));
        }
    }

    /**
     * 🔓 Rilascia ticket assegnato
     * POST /api/admin/tickets/{id}/release
     */
    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> releaseTicket(@PathVariable UUID id) {
        try {
            SupportTicket ticket = ticketService.releaseTicket(id);
            
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
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il rilascio: " + e.getMessage()));
        }
    }

    /**
     * 🎯 Assegna ticket con controllo ruoli
     * POST /api/admin/tickets/{id}/assign-with-role
     */
    @PostMapping("/{id}/assign-with-role")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> assignTicketWithRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        try {
            String supportEmail = request.get("supportEmail");
            String userRole = request.get("userRole");
            String userId = request.get("userId");
            
            if (supportEmail == null || supportEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "supportEmail è obbligatorio"));
            }

            // Simula un User object per il controllo ruoli
            // In un'implementazione reale, questo verrebbe dal JWT token
            com.funkard.model.User user = new com.funkard.model.User();
            user.setId(Long.parseLong(userId != null ? userId : "1"));
            user.setRole(userRole != null ? userRole : "support");

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
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'assegnazione: " + e.getMessage()));
        }
    }

    /**
     * 🔓 Rilascia ticket con controllo ruoli
     * POST /api/admin/tickets/{id}/release-with-role
     */
    @PostMapping("/{id}/release-with-role")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> releaseTicketWithRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        try {
            String userRole = request.get("userRole");
            String userId = request.get("userId");
            
            // Simula un User object per il controllo ruoli
            com.funkard.model.User user = new com.funkard.model.User();
            user.setId(Long.parseLong(userId != null ? userId : "1"));
            user.setRole(userRole != null ? userRole : "support");

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
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il rilascio: " + e.getMessage()));
        }
    }

    /**
     * 📊 Statistiche assegnazioni
     * GET /api/admin/tickets/assignment-stats
     */
    @GetMapping("/assignment-stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAssignmentStats() {
        try {
            long totalTickets = ticketService.countAll();
            long assignedTickets = ticketService.countAssignedTickets();
            long unassignedTickets = totalTickets - assignedTickets;
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", Map.of(
                    "totalTickets", totalTickets,
                    "assignedTickets", assignedTickets,
                    "unassignedTickets", unassignedTickets,
                    "assignmentRate", totalTickets > 0 ? (double) assignedTickets / totalTickets : 0.0
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il recupero delle statistiche: " + e.getMessage()));
        }
    }
}
