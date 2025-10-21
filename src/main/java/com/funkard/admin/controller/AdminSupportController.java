package com.funkard.admin.controller;

import com.funkard.admin.dto.SupportMessageDTO;
import com.funkard.admin.dto.SupportStatsDTO;
import com.funkard.admin.dto.TicketDTO;
import com.funkard.admin.model.SupportMessage;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.service.AdminSupportService;
import com.funkard.admin.service.SupportService;
import com.funkard.admin.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/support")
@CrossOrigin(origins = {"https://admin.funkard.com", "https://funkard.vercel.app"})
public class AdminSupportController {

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private AdminSupportService adminSupportService;

    @Autowired
    private SupportService service;
    
    @Value("${admin.token}")
    private String adminToken;

    // 📋 Lista tutti i ticket
    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets(@RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(supportTicketService.findAll());
    }

    // 📈 Statistiche ultime 30 giornate
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(adminSupportService.getStatsLast30Days());
    }

    // 💬 Rispondi a un ticket specifico
    @PostMapping("/reply/{id}")
    public ResponseEntity<?> replyToTicket(
            @PathVariable UUID id,
            @RequestBody SupportMessageDTO payload,
            @RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            SupportMessage reply = supportTicketService.addAdminReply(id, payload.getSender(), payload.getContent());

            // 🔔 Notifica real-time nuovo messaggio (utente + admin)
            supportTicketService.broadcastTicketUpdate(reply.getTicket(), "NEW_MESSAGE", true);

            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante l'invio della risposta: " + e.getMessage());
        }
    }

    // 🎯 Risolvi ticket (notifica l'utente)
    @PostMapping("/resolve/{id}")
    public ResponseEntity<?> resolveTicket(
            @PathVariable UUID id,
            @RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            SupportTicket ticket = supportTicketService.resolveTicket(id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la risoluzione del ticket: " + e.getMessage());
        }
    }

    // 🏁 Chiudi ticket (solo admin)
    @PostMapping("/close/{id}")
    public ResponseEntity<?> closeTicket(
            @PathVariable UUID id,
            @RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            SupportTicket ticket = supportTicketService.closeTicket(id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la chiusura del ticket: " + e.getMessage());
        }
    }

    // 🔄 Riapri ticket (solo admin)
    @PostMapping("/reopen/{id}")
    public ResponseEntity<?> reopenTicket(
            @PathVariable UUID id,
            @RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            SupportTicket ticket = supportTicketService.reopenTicket(id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante la riapertura del ticket: " + e.getMessage());
        }
    }
}
