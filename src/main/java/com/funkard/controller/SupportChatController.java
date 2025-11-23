package com.funkard.controller;

import com.funkard.admin.dto.SupportMessageDTO;
import com.funkard.admin.model.SupportMessage;
import com.funkard.admin.model.SupportTicket;
import com.funkard.admin.service.SupportMessageService;
import com.funkard.admin.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 💬 Controller per chat di supporto tra utenti e admin
 * Endpoint: /api/support/chat/*
 * Autenticazione: JWT Bearer token
 */
@RestController
@RequestMapping("/api/support/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "https://admin.funkard.com", "http://localhost:3000", "http://localhost:3002"})
public class SupportChatController {

    private final SupportMessageService messageService;
    private final SupportTicketService ticketService;

    /**
     * 💬 Invia messaggio in una chat (utente o admin)
     * POST /api/support/chat/{ticketId}/message
     * Body: { "message": "...", "sender": "user" o "admin" }
     * Headers: Authorization: Bearer {jwt_token}
     */
    @PostMapping("/{ticketId}/message")
    public ResponseEntity<?> sendMessage(
            @PathVariable UUID ticketId,
            @RequestBody Map<String, String> request) {
        
        try {
            String message = request.get("message");
            String sender = request.get("sender");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Il messaggio non può essere vuoto"));
            }

            if (sender == null || sender.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Il campo sender è obbligatorio"));
            }

            // Verifica che il ticket esista
            SupportTicket ticket = ticketService.findById(ticketId);
            if (ticket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ticket non trovato"));
            }

            // Aggiungi messaggio (con traduzione automatica)
            log.info("💬 Invio messaggio per ticket {} da {}", ticketId, sender);
            SupportMessage savedMessage = messageService.addMessage(ticketId, message, sender);
            
            // Crea DTO con campi traduzione
            SupportMessageDTO messageDTO = new SupportMessageDTO(savedMessage);
            
            log.info("✅ Messaggio inviato con successo: {} (tradotto: {})", 
                savedMessage.getId(), savedMessage.getIsTranslated());
            
            // Response con campi traduzione
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "Messaggio inviato con successo");
            response.put("messageId", savedMessage.getId());
            response.put("ticketId", ticketId);
            response.put("createdAt", savedMessage.getCreatedAt());
            
            // Campi traduzione
            response.put("originalText", messageDTO.getOriginalText());
            response.put("translatedText", messageDTO.getTranslatedText());
            response.put("originalLanguage", messageDTO.getOriginalLanguage());
            response.put("targetLanguage", messageDTO.getTargetLanguage());
            response.put("isTranslated", messageDTO.getIsTranslated());
            
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'invio del messaggio: " + e.getMessage()));
        }
    }

    /**
     * 📋 Ottieni tutti i messaggi di una chat (con fallback polling)
     * GET /api/support/chat/{ticketId}/messages?lastMessageId={uuid}
     * 
     * Headers: Authorization: Bearer {jwt_token}
     * 
     * Se lastMessageId è fornito, restituisce solo i messaggi creati dopo quel messaggio (fallback polling).
     * Se non fornito, restituisce tutti i messaggi.
     */
    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable UUID ticketId,
            @RequestParam(required = false) String lastMessageId) {
        
        try {
            // Verifica che il ticket esista
            SupportTicket ticket = ticketService.findById(ticketId);
            if (ticket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ticket non trovato"));
            }

            List<SupportMessage> allMessages = messageService.getMessages(ticketId);
            
            // Converti a DTO con campi traduzione
            List<SupportMessageDTO> messageDTOs = allMessages.stream()
                .map(SupportMessageDTO::new)
                .toList();
            
            // Fallback polling: se lastMessageId è fornito, restituisci solo nuovi messaggi
            if (lastMessageId != null && !lastMessageId.trim().isEmpty()) {
                try {
                    UUID lastId = UUID.fromString(lastMessageId);
                    List<SupportMessageDTO> newMessages = messageDTOs.stream()
                            .filter(msg -> {
                                SupportMessage original = allMessages.stream()
                                    .filter(m -> m.getId().equals(msg.getId()))
                                    .findFirst()
                                    .orElse(null);
                                if (original == null) return false;
                                
                                SupportMessage lastMsg = allMessages.stream()
                                    .filter(m -> m.getId().equals(lastId))
                                    .findFirst()
                                    .orElse(null);
                                if (lastMsg == null) return true;
                                
                                return original.getCreatedAt().isAfter(lastMsg.getCreatedAt());
                            })
                            .toList();
                    
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "ticketId", ticketId,
                        "messages", newMessages,
                        "count", newMessages.size(),
                        "hasNewMessages", !newMessages.isEmpty()
                    ));
                } catch (IllegalArgumentException e) {
                    // lastMessageId non valido, restituisci tutti i messaggi
                    log.warn("⚠️ lastMessageId non valido: {}", lastMessageId);
                }
            }

            // Restituisci tutti i messaggi con campi traduzione
            return ResponseEntity.ok(Map.of(
                "success", true,
                "ticketId", ticketId,
                "messages", messageDTOs,
                "count", messageDTOs.size()
            ));

        } catch (Exception e) {
            log.error("❌ Errore durante il recupero dei messaggi per ticket {}: {}", ticketId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il recupero dei messaggi: " + e.getMessage()));
        }
    }

    /**
     * ✅ Marca messaggi come letti (admin)
     * POST /api/support/chat/{ticketId}/read
     * Headers: Authorization: Bearer {jwt_token}
     */
    @PostMapping("/{ticketId}/read")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> markAsRead(@PathVariable UUID ticketId) {
        
        try {

            log.info("✅ Marcatura messaggi come letti per ticket {}", ticketId);
            messageService.markAsReadByAdmin(ticketId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Messaggi marcati come letti",
                "ticketId", ticketId
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'aggiornamento: " + e.getMessage()));
        }
    }

    /**
     * 📊 Statistiche chat per ticket
     * GET /api/support/chat/{ticketId}/stats
     */
    @GetMapping("/{ticketId}/stats")
    public ResponseEntity<?> getChatStats(@PathVariable UUID ticketId) {
        try {
            SupportTicket ticket = ticketService.findById(ticketId);
            if (ticket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Ticket non trovato"));
            }

            List<SupportMessage> messages = messageService.getMessages(ticketId);
            
            long userMessages = messages.stream()
                    .filter(m -> !m.getSender().equalsIgnoreCase("admin"))
                    .count();
            
            long adminMessages = messages.stream()
                    .filter(m -> m.getSender().equalsIgnoreCase("admin"))
                    .count();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "ticketId", ticketId,
                "stats", Map.of(
                    "totalMessages", messages.size(),
                    "userMessages", userMessages,
                    "adminMessages", adminMessages,
                    "hasNewMessages", ticket.isHasNewMessages(),
                    "lastMessageAt", messages.isEmpty() ? null : messages.get(messages.size() - 1).getCreatedAt()
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il recupero delle statistiche: " + e.getMessage()));
        }
    }
}

