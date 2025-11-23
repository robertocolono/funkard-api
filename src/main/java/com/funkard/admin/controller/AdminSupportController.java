package com.funkard.admin.controller;

import com.funkard.admin.dto.SupportMessageDTO;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 🎫 Controller per gestione supporto admin
 * Richiede autenticazione JWT con ruolo ADMIN o SUPER_ADMIN
 */
@RestController
@RequestMapping("/api/admin/support")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "https://admin.funkard.com", "http://localhost:3000", "http://localhost:3002"})
public class AdminSupportController {

    private final SupportTicketService supportTicketService;

    // 📋 Lista tutti i ticket (con paginazione)
    @GetMapping("/tickets")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<SupportTicket>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        log.info("📋 Richiesta lista ticket (page={}, size={}, sort={})", page, size, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<SupportTicket> tickets = supportTicketService.findAll(pageable);
        
        log.info("✅ Restituiti {} ticket (totale: {})", tickets.getNumberOfElements(), tickets.getTotalElements());
        return ResponseEntity.ok(tickets);
    }

    // 📈 Statistiche ultime 30 giornate
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getStats() {
        log.info("📈 Richiesta statistiche supporto");
        return ResponseEntity.ok(supportTicketService.getStatsLast30Days());
    }

    // 💬 Rispondi a un ticket specifico
    @PostMapping("/reply/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> replyToTicket(
            @PathVariable UUID id,
            @RequestBody SupportMessageDTO payload) {
        log.info("💬 Risposta a ticket {}", id);

        try {
            var reply = supportTicketService.addAdminReply(id, payload.getSender(), payload.getContent());
            supportTicketService.broadcastTicketUpdate(reply.getTicket(), "NEW_MESSAGE", true);
            
            log.info("✅ Risposta inviata con successo per ticket {}", id);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            log.error("❌ Errore durante l'invio della risposta per ticket {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'invio della risposta: " + e.getMessage()));
        }
    }

    // 🎯 Risolvi ticket
    @PostMapping("/resolve/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> resolveTicket(@PathVariable UUID id) {
        log.info("🎯 Risoluzione ticket {}", id);

        try {
            SupportTicket ticket = supportTicketService.resolveTicket(id);
            log.info("✅ Ticket {} risolto con successo", id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            log.error("❌ Errore durante la risoluzione del ticket {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la risoluzione del ticket: " + e.getMessage()));
        }
    }

    // 🏁 Chiudi ticket
    @PostMapping("/close/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> closeTicket(@PathVariable UUID id) {
        log.info("🏁 Chiusura ticket {}", id);

        try {
            SupportTicket ticket = supportTicketService.closeTicket(id);
            log.info("✅ Ticket {} chiuso con successo", id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            log.error("❌ Errore durante la chiusura del ticket {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la chiusura del ticket: " + e.getMessage()));
        }
    }

    // 🔄 Riapri ticket
    @PostMapping("/reopen/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> reopenTicket(@PathVariable UUID id) {
        log.info("🔄 Riapertura ticket {}", id);

        try {
            SupportTicket ticket = supportTicketService.reopenTicket(id);
            log.info("✅ Ticket {} riaperto con successo", id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            log.error("❌ Errore durante la riapertura del ticket {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la riapertura del ticket: " + e.getMessage()));
        }
    }

    // 👨‍💻 Marca messaggi come letti
    @PostMapping("/{id}/mark-read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable UUID id) {
        log.info("👨‍💻 Marcatura messaggi come letti per ticket {}", id);

        try {
            supportTicketService.markMessagesAsRead(id);
            log.info("✅ Messaggi marcati come letti per ticket {}", id);
            return ResponseEntity.ok(Map.of("message", "Messaggi marcati come letti"));
        } catch (Exception e) {
            log.error("❌ Errore durante il marking dei messaggi per ticket {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il marking dei messaggi: " + e.getMessage()));
        }
    }

    // 📊 Conta ticket con nuovi messaggi
    @GetMapping("/new-messages-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Long>> getNewMessagesCount() {
        log.info("📊 Richiesta conteggio ticket con nuovi messaggi");
        
        long count = supportTicketService.countTicketsWithNewMessages();
        log.info("✅ Ticket con nuovi messaggi: {}", count);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // 👨‍💻 Assegna ticket a un support
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> assignTicket(
            @PathVariable UUID id,
            @RequestParam String supportEmail) {
        log.info("👨‍💻 Assegnazione ticket {} a {}", id, supportEmail);

        try {
            SupportTicket ticket = supportTicketService.assignTicket(id, supportEmail);
            log.info("✅ Ticket {} assegnato a {}", id, supportEmail);
            return ResponseEntity.ok(ticket);
        } catch (IllegalStateException e) {
            log.warn("⚠️ Errore durante l'assegnazione del ticket {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Errore durante l'assegnazione del ticket {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'assegnazione: " + e.getMessage()));
        }
    }

    // 🔓 Rilascia ticket (unlock)
    @PostMapping("/{id}/release")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> releaseTicket(@PathVariable UUID id) {
        log.info("🔓 Rilascio ticket {}", id);

        try {
            SupportTicket ticket = supportTicketService.releaseTicket(id);
            log.info("✅ Ticket {} rilasciato con successo", id);
            return ResponseEntity.ok(ticket);
        } catch (IllegalStateException e) {
            log.warn("⚠️ Errore durante il rilascio del ticket {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Errore durante il rilascio del ticket {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il rilascio: " + e.getMessage()));
        }
    }

    // 📋 Lista ticket assegnati a un support (con paginazione)
    @GetMapping("/assigned/{supportEmail}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<SupportTicket>> getAssignedTickets(
            @PathVariable String supportEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        log.info("📋 Richiesta ticket assegnati a {} (page={}, size={})", supportEmail, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<SupportTicket> tickets = supportTicketService.findByAssignedTo(supportEmail, pageable);
        
        log.info("✅ Restituiti {} ticket assegnati a {} (totale: {})", 
            tickets.getNumberOfElements(), supportEmail, tickets.getTotalElements());
        return ResponseEntity.ok(tickets);
    }

    // 📊 Conta ticket assegnati
    @GetMapping("/assigned-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Long>> getAssignedCount() {
        log.info("📊 Richiesta conteggio ticket assegnati");
        
        long count = supportTicketService.countAssignedTickets();
        log.info("✅ Ticket assegnati: {}", count);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
