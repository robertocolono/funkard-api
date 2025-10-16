package com.funkard.admin.controller;

import com.funkard.admin.dto.SupportTicketDTO;
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
public class SupportTicketController {
    
    private final SupportTicketService service;
    
    @Value("${admin.token}")
    private String adminToken;
    
    public SupportTicketController(SupportTicketService service) {
        this.service = service;
    }
    
    // 🔹 Lista tutti i ticket
    @GetMapping
    public ResponseEntity<?> getAllTickets(@RequestHeader("Authorization") String authorization) {
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        List<SupportTicketDTO> tickets = service.getAllTickets();
        return ResponseEntity.ok(tickets);
    }
    
    // 🔹 Lista ticket attivi
    @GetMapping("/active")
    public ResponseEntity<?> getActiveTickets(@RequestHeader("Authorization") String authorization) {
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        List<SupportTicketDTO> tickets = service.getActiveTickets();
        return ResponseEntity.ok(tickets);
    }
    
    // 🔹 Lista ticket per status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTicketsByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String authorization) {
        
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        List<SupportTicketDTO> tickets = service.getTicketsByStatus(status);
        return ResponseEntity.ok(tickets);
    }
    
    // 🔹 Lista ticket per priorità
    @GetMapping("/priority/{priority}")
    public ResponseEntity<?> getTicketsByPriority(
            @PathVariable String priority,
            @RequestHeader("Authorization") String authorization) {
        
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        List<SupportTicketDTO> tickets = service.getTicketsByPriority(priority);
        return ResponseEntity.ok(tickets);
    }
    
    // 🔹 Lista ticket per categoria
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getTicketsByCategory(
            @PathVariable String category,
            @RequestHeader("Authorization") String authorization) {
        
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        List<SupportTicketDTO> tickets = service.getTicketsByCategory(category);
        return ResponseEntity.ok(tickets);
    }
    
    // 🔹 Rispondi a ticket
    @PostMapping("/reply")
    public ResponseEntity<?> replyToTicket(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authorization) {
        
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        UUID ticketId = UUID.fromString(request.get("ticketId"));
        String adminResponse = request.get("response");
        
        service.replyToTicket(ticketId, adminResponse);
        return ResponseEntity.ok(Map.of("status", "replied"));
    }
    
    // 🔹 Chiudi ticket
    @PostMapping("/close")
    public ResponseEntity<?> closeTicket(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authorization) {
        
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        UUID ticketId = UUID.fromString(request.get("ticketId"));
        service.closeTicket(ticketId);
        return ResponseEntity.ok(Map.of("status", "closed"));
    }
    
    // 🔹 Aggiorna priorità
    @PostMapping("/priority")
    public ResponseEntity<?> updatePriority(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authorization) {
        
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        UUID ticketId = UUID.fromString(request.get("ticketId"));
        String priority = request.get("priority");
        
        service.updatePriority(ticketId, priority);
        return ResponseEntity.ok(Map.of("status", "priority_updated"));
    }
    
    // 🔹 Statistiche supporto
    @GetMapping("/stats")
    public ResponseEntity<?> getSupportStats(@RequestHeader("Authorization") String authorization) {
        if (!authorization.equals("Bearer " + adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }
        
        Map<String, Object> stats = Map.of(
            "open_tickets", service.getOpenTicketsCount(),
            "in_progress_tickets", service.getInProgressTicketsCount(),
            "resolved_tickets", service.getResolvedTicketsCount(),
            "urgent_tickets", service.getTicketsCountByPriority("urgent"),
            "high_priority_tickets", service.getTicketsCountByPriority("high")
        );
        
        return ResponseEntity.ok(stats);
    }
}
