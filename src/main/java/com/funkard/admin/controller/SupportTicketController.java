package com.funkard.admin.controller;

import com.funkard.admin.model.SupportMessage;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.service.SupportMessageService;
import com.funkard.admin.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService ticketService;
    private final SupportMessageService messageService;

    // 🔸 Crea ticket
    @PostMapping
    public ResponseEntity<SupportTicket> create(@RequestBody SupportTicketRequest req) {
        return ResponseEntity.ok(ticketService.create(req.email(), req.subject(), req.message()));
    }

    // 🔸 Lista ticket (tutti o filtrati per email)
    @GetMapping
    public List<SupportTicket> list(@RequestParam(required = false) String email) {
        if (email == null || email.isBlank()) return ticketService.findAll();
        return ticketService.findByEmail(email);
    }

    // 🔸 Ticket singolo con messaggi
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable UUID id) {
        SupportTicket t = ticketService.findById(id);
        List<SupportMessage> msgs = messageService.getMessages(id);

        Map<String, Object> data = new HashMap<>();
        data.put("ticket", t);
        data.put("messages", msgs);
        return ResponseEntity.ok(data);
    }

    // 🔸 Aggiorna stato
    @PostMapping("/{id}/status")
    public ResponseEntity<SupportTicket> updateStatus(@PathVariable UUID id, @RequestBody TicketStatusRequest req) {
        return ResponseEntity.ok(ticketService.updateStatus(id, req.status(), req.note()));
    }

    // 🔸 Aggiungi messaggio
    @PostMapping("/{id}/message")
    public ResponseEntity<SupportMessage> addMessage(@PathVariable UUID id, @RequestBody SupportMessageRequest req) {
        SupportMessage msg = messageService.addMessage(id, req.message(), req.sender());
        return ResponseEntity.ok(msg);
    }
}

record SupportTicketRequest(String email, String subject, String message) {}
record TicketStatusRequest(String status, String note) {}
record SupportMessageRequest(String message, String sender) {}
