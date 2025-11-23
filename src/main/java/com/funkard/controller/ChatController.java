package com.funkard.controller;

import com.funkard.dto.ChatMessageDTO;
import com.funkard.model.User;
import com.funkard.repository.UserRepository;
import com.funkard.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 💬 Controller per chat tra utenti
 * 
 * Supporta traduzione automatica quando mittente e destinatario
 * hanno lingue diverse.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class ChatController {
    
    private final ChatService chatService;
    private final UserRepository userRepository;
    
    /**
     * 💬 POST /api/chat/message
     * Invia messaggio tra utenti con traduzione automatica
     * 
     * Request:
     * {
     *   "recipientId": 123,
     *   "text": "Ciao, come stai?"
     * }
     * 
     * Response:
     * {
     *   "id": "...",
     *   "originalText": "Ciao, come stai?",
     *   "translatedText": "Hi, how are you?",
     *   "originalLanguage": "it",
     *   "targetLanguage": "en",
     *   "isTranslated": true,
     *   ...
     * }
     */
    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // Recupera ID mittente da JWT o authentication
            Long senderId = getUserIdFromAuthentication(authentication);
            if (senderId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Long recipientId = Long.parseLong(request.get("recipientId").toString());
            String text = request.get("text").toString();
            
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Il messaggio non può essere vuoto"));
            }
            
            ChatMessageDTO message = chatService.sendMessage(senderId, recipientId, text);
            return ResponseEntity.ok(message);
            
        } catch (IllegalArgumentException e) {
            log.warn("Richiesta non valida: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante invio messaggio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📋 GET /api/chat/conversation/{userId}
     * Recupera conversazione tra utente corrente e altro utente
     */
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<List<ChatMessageDTO>> getConversation(
            @PathVariable Long userId,
            Authentication authentication) {
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long currentUserId = getUserIdFromAuthentication(authentication);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<ChatMessageDTO> messages = chatService.getConversation(currentUserId, userId);
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            log.error("Errore durante recupero conversazione: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 📬 GET /api/chat/unread
     * Recupera messaggi non letti per utente corrente
     */
    @GetMapping("/unread")
    public ResponseEntity<List<ChatMessageDTO>> getUnreadMessages(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<ChatMessageDTO> messages = chatService.getUnreadMessages(userId);
            return ResponseEntity.ok(messages);
            
        } catch (Exception e) {
            log.error("Errore durante recupero messaggi non letti: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * ✅ PUT /api/chat/message/{messageId}/read
     * Marca messaggio come letto
     */
    @PutMapping("/message/{messageId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable UUID messageId,
            Authentication authentication) {
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            chatService.markAsRead(messageId, userId);
            return ResponseEntity.ok(Map.of("success", true));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante marcatura messaggio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 🔍 Helper per recuperare userId da Authentication
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Estrai email da UserDetails
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername(); // Assumendo che username sia email
            
            // Recupera userId da email
            User user = userRepository.findByEmail(email);
            return user != null ? user.getId() : null;
        }
        return null;
    }
}

