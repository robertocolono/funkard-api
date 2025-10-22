package com.funkard.user.payment;

import com.funkard.user.payment.dto.PaymentMethodDTO;
import com.funkard.user.payment.dto.PaymentMethodRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 💳 Controller per gestione metodi di pagamento
 * 
 * 🔒 SICUREZZA: Autenticazione richiesta
 * ✅ Endpoint REST completi
 * ✅ Gestione errori integrata
 */
@RestController
@RequestMapping("/api/user/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"https://funkard.vercel.app", "https://funkardnew.vercel.app", "http://localhost:3000"})
public class PaymentMethodController {

    private final PaymentMethodService service;

    /**
     * 📋 GET /api/user/payments
     * Ottieni tutti i metodi di pagamento dell'utente
     */
    @GetMapping
    public ResponseEntity<List<PaymentMethodDTO>> getAllMethods(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Richiesta metodi di pagamento per utente: {}", userId);
        
        try {
            List<PaymentMethodDTO> methods = service.getMethods(userId);
            return ResponseEntity.ok(methods);
        } catch (Exception e) {
            log.error("Errore nel recupero metodi di pagamento per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ➕ POST /api/user/payments
     * Aggiungi nuovo metodo di pagamento
     */
    @PostMapping
    public ResponseEntity<?> addMethod(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody PaymentMethodRequest request) {
        
        log.info("Aggiunta metodo di pagamento per utente: {}", userId);
        
        try {
            PaymentMethodDTO newMethod = service.addMethod(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newMethod);
        } catch (IllegalArgumentException e) {
            log.warn("Richiesta non valida per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Limite raggiunto per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore nell'aggiunta metodo di pagamento per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * 🗑️ DELETE /api/user/payments/{id}
     * Elimina metodo di pagamento
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMethod(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        
        log.info("Eliminazione metodo di pagamento {} per utente: {}", id, userId);
        
        try {
            service.deleteMethod(userId, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Metodo non trovato per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("Accesso negato per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accesso negato"));
        } catch (Exception e) {
            log.error("Errore nell'eliminazione metodo di pagamento {} per utente {}: {}", 
                     id, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * 🎯 PATCH /api/user/payments/{id}/default
     * Imposta metodo di pagamento predefinito
     */
    @PatchMapping("/{id}/default")
    public ResponseEntity<?> setDefaultMethod(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String id) {
        
        log.info("Impostazione metodo predefinito {} per utente: {}", id, userId);
        
        try {
            service.setDefaultMethod(userId, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Metodo non trovato per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("Accesso negato per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accesso negato"));
        } catch (Exception e) {
            log.error("Errore nell'impostazione metodo predefinito {} per utente {}: {}", 
                     id, userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * 🔍 GET /api/user/payments/default
     * Ottieni metodo di pagamento predefinito
     */
    @GetMapping("/default")
    public ResponseEntity<PaymentMethodDTO> getDefaultMethod(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Richiesta metodo predefinito per utente: {}", userId);
        
        try {
            return service.getDefaultMethod(userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Errore nel recupero metodo predefinito per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📊 GET /api/user/payments/stats
     * Ottieni statistiche metodi di pagamento
     */
    @GetMapping("/stats")
    public ResponseEntity<PaymentMethodService.PaymentMethodStats> getStats(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Richiesta statistiche metodi di pagamento per utente: {}", userId);
        
        try {
            PaymentMethodService.PaymentMethodStats stats = service.getStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Errore nel recupero statistiche per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🧹 POST /api/user/payments/cleanup
     * Pulisci metodi di pagamento scaduti
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredMethods(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Pulizia metodi scaduti per utente: {}", userId);
        
        try {
            int cleanedCount = service.cleanupExpiredMethods(userId);
            return ResponseEntity.ok(Map.of(
                "message", "Pulizia completata",
                "cleanedCount", cleanedCount,
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Errore nella pulizia metodi scaduti per utente {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }

    /**
     * ❓ GET /api/user/payments/validate
     * Valida un metodo di pagamento senza salvarlo
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateMethod(
            @Valid @RequestBody PaymentMethodRequest request) {
        
        log.info("Validazione metodo di pagamento");
        
        try {
            boolean isValidCard = request.isValidCardNumber();
            boolean isValidExpiry = request.isValidExpiryDate();
            boolean isValid = isValidCard && isValidExpiry;
            
            return ResponseEntity.ok(Map.of(
                "isValid", isValid,
                "cardValid", isValidCard,
                "expiryValid", isValidExpiry,
                "errors", List.of(
                    !isValidCard ? "Numero di carta non valido" : null,
                    !isValidExpiry ? "Data di scadenza non valida" : null
                ).stream().filter(java.util.Objects::nonNull).toList()
            ));
        } catch (Exception e) {
            log.error("Errore nella validazione: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore interno del server"));
        }
    }
}
