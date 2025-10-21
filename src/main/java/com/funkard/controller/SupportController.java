package com.funkard.controller;

import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.service.SupportTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = {"https://funkard.vercel.app", "https://funkardnew.vercel.app", "http://localhost:3000"})
public class SupportController {

    @Autowired
    private SupportTicketService supportTicketService;

    // 🔄 Riapri ticket (endpoint pubblico per utenti)
    @PostMapping("/{id}/reopen")
    public ResponseEntity<?> reopenTicket(@PathVariable UUID id) {
        try {
            SupportTicket ticket = supportTicketService.reopenTicket(id);
            return ResponseEntity.ok(ticket);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("Errore: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Errore durante la riapertura del ticket: " + e.getMessage());
        }
    }
}
