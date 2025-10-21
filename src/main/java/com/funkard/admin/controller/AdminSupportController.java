package com.funkard.admin.controller;

import com.funkard.admin.dto.SupportStatsDTO;
import com.funkard.admin.dto.TicketDTO;
import com.funkard.admin.service.AdminSupportService;
import com.funkard.admin.service.SupportService;
import com.funkard.admin.service.SupportTicketService;
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

    private final SupportService service;
    private final AdminSupportService adminSupportService;
    private final SupportTicketService ticketService;
    
    @Value("${admin.token}")
    private String adminToken;

    public AdminSupportController(SupportService service, AdminSupportService adminSupportService, SupportTicketService ticketService) {
        this.service = service;
        this.adminSupportService = adminSupportService;
        this.ticketService = ticketService;
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets(@RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.getAllTickets());
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable UUID id, @RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            // Recupera ticket con messaggi e dettagli
            TicketDTO ticket = service.getTicketById(id);
            if (ticket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket non trovato");
            }
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore durante il caricamento del ticket: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSupportStats(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        if (token == null || !token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }

        List<SupportStatsDTO> stats = adminSupportService.getStatsLast30Days();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/reply/{id}")
    public ResponseEntity<?> replyToTicket(
            @PathVariable UUID id, 
            @RequestBody Map<String, String> body,
            @RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        service.replyToTicket(id, body.get("reply"));
        
        // 🔔 Notifica real-time: ticket aggiornato
        var ticket = ticketService.findById(id);
        ticketService.broadcastTicketUpdate(ticket);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/close/{id}")
    public ResponseEntity<?> closeTicket(
            @PathVariable UUID id,
            @RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // 🔔 Usa il nuovo metodo con broadcast automatico
        ticketService.updateTicketStatus(id, "closed");
        
        return ResponseEntity.ok().build();
    }
}
