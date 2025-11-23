package com.funkard.controller;

import com.funkard.dto.PendingValueDTO;
import com.funkard.dto.SubmitPendingValueRequest;
import com.funkard.model.PendingValue;
import com.funkard.service.PendingValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ⏳ Controller per gestione valori personalizzati "Altro"
 * 
 * Gestisce proposte di nuovi valori TCG o Lingua che richiedono
 * approvazione admin prima di essere aggiunti alle liste ufficiali.
 */
@RestController
@RequestMapping("/api/pending-values")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "https://admin.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class PendingValueController {
    
    private final PendingValueService pendingValueService;
    private final com.funkard.repository.UserRepository userRepository;
    
    /**
     * 📝 POST /api/pending-values/submit
     * Invia proposta di valore personalizzato "Altro"
     * 
     * Request:
     * {
     *   "type": "TCG" | "LANGUAGE",
     *   "value": "Nome valore personalizzato"
     * }
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitPendingValue(
            @Valid @RequestBody SubmitPendingValueRequest request,
            Authentication authentication) {
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            PendingValue pendingValue = pendingValueService.submitPendingValue(
                request.getType(), 
                request.getValue(), 
                userId
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PendingValueDTO(pendingValue));
            
        } catch (IllegalArgumentException e) {
            log.warn("Richiesta non valida: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Stato non valido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante submit proposta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📋 GET /api/pending-values
     * Recupera tutte le proposte pending (solo admin)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")
    public ResponseEntity<List<PendingValueDTO>> getPendingValues(
            @RequestParam(required = false) PendingValue.ValueType type) {
        
        try {
            List<PendingValue> pendingValues = type != null 
                ? pendingValueService.getPendingValuesByType(type)
                : pendingValueService.getPendingValues();
            
            List<PendingValueDTO> dtos = pendingValues.stream()
                .map(PendingValueDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
            
        } catch (Exception e) {
            log.error("Errore durante recupero proposte: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 📋 GET /api/pending-values/my
     * Recupera proposte dell'utente corrente
     */
    @GetMapping("/my")
    public ResponseEntity<List<PendingValueDTO>> getMyPendingValues(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<PendingValue> pendingValues = pendingValueService.getUserPendingValues(userId);
            List<PendingValueDTO> dtos = pendingValues.stream()
                .map(PendingValueDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
            
        } catch (Exception e) {
            log.error("Errore durante recupero proposte utente: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * ✅ POST /api/pending-values/{id}/approve
     * Approva una proposta (solo admin)
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")
    public ResponseEntity<?> approvePendingValue(
            @PathVariable UUID id,
            Authentication authentication) {
        
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            PendingValue approved = pendingValueService.approvePendingValue(id, adminId);
            return ResponseEntity.ok(new PendingValueDTO(approved));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante approvazione: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * ❌ DELETE /api/pending-values/{id}
     * Rifiuta una proposta (solo admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")
    public ResponseEntity<?> rejectPendingValue(
            @PathVariable UUID id,
            Authentication authentication) {
        
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            pendingValueService.rejectPendingValue(id, adminId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Proposta rifiutata"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante rifiuto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📊 GET /api/pending-values/stats
     * Statistiche proposte (solo admin)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            long pendingTcg = pendingValueService.countPendingByType(PendingValue.ValueType.TCG);
            long pendingLanguage = pendingValueService.countPendingByType(PendingValue.ValueType.LANGUAGE);
            
            return ResponseEntity.ok(Map.of(
                "pendingTcg", pendingTcg,
                "pendingLanguage", pendingLanguage,
                "totalPending", pendingTcg + pendingLanguage
            ));
            
        } catch (Exception e) {
            log.error("Errore durante recupero statistiche: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 🔍 Helper per recuperare userId da Authentication
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            
            com.funkard.model.User user = userRepository.findByEmail(email);
            return user != null ? user.getId() : null;
        }
        return null;
    }
}

