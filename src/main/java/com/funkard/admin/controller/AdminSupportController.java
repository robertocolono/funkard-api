package com.funkard.admin.controller;

import com.funkard.admin.dto.TicketDTO;
import com.funkard.admin.service.SupportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/support")
@CrossOrigin(origins = {"https://funkard.vercel.app"})
public class AdminSupportController {

    private final SupportService service;
    
    @Value("${admin.token}")
    private String adminToken;

    public AdminSupportController(SupportService service) {
        this.service = service;
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets(@RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.getAllTickets());
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
        return ResponseEntity.ok().build();
    }

    @PostMapping("/close/{id}")
    public ResponseEntity<?> closeTicket(
            @PathVariable UUID id,
            @RequestHeader("X-Admin-Token") String token) {
        if (!token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        service.closeTicket(id);
        return ResponseEntity.ok().build();
    }
}
