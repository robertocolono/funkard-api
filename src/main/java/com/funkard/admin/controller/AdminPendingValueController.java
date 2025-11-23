package com.funkard.admin.controller;

import com.funkard.dto.PendingValueDTO;
import com.funkard.model.PendingValue;
import com.funkard.service.PendingValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ⏳ Controller Admin per gestione valori personalizzati "Altro"
 * 
 * Permette agli admin di visualizzare, approvare o rifiutare
 * proposte di nuovi valori TCG o Lingua.
 */
@RestController
@RequestMapping("/api/admin/pending-values")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "https://admin.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
})
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SUPERVISOR')")
public class AdminPendingValueController {
    
    private final PendingValueService pendingValueService;
    private final com.funkard.repository.UserRepository userRepository;
    
    /**
     * 📋 GET /api/admin/pending-values
     * Lista tutte le proposte pending con paginazione
     * 
     * Query Params:
     * - type: Filtra per tipo (TCG, LANGUAGE)
     * - page: Numero pagina (default: 0)
     * - size: Dimensione pagina (default: 20)
     * - sortBy: Campo ordinamento (default: createdAt)
     * - sortDir: Direzione (ASC, DESC, default: DESC)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPendingValues(
            @RequestParam(required = false) PendingValue.ValueType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        try {
            List<PendingValue> pendingValues = type != null 
                ? pendingValueService.getPendingValuesByType(type)
                : pendingValueService.getPendingValues();
            
            // Applica ordinamento manuale (per semplicità, in produzione usare Pageable)
            Sort.Direction direction = Sort.Direction.fromString(sortDir);
            if (direction == Sort.Direction.DESC) {
                pendingValues.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            } else {
                pendingValues.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            }
            
            // Paginazione manuale
            int start = page * size;
            int end = Math.min(start + size, pendingValues.size());
            List<PendingValue> pagedValues = start < pendingValues.size() 
                ? pendingValues.subList(start, end)
                : List.of();
            
            List<PendingValueDTO> dtos = pagedValues.stream()
                .map(PendingValueDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "content", dtos,
                "totalElements", pendingValues.size(),
                "totalPages", (int) Math.ceil((double) pendingValues.size() / size),
                "page", page,
                "size", size
            ));
            
        } catch (Exception e) {
            log.error("Errore durante recupero proposte: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * ✅ POST /api/admin/pending-values/{id}/approve
     * Approva una proposta
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approvePendingValue(
            @PathVariable UUID id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal 
            org.springframework.security.core.userdetails.UserDetails userDetails) {
        
        try {
            Long adminId = getUserIdFromEmail(userDetails.getUsername());
            if (adminId == null) {
                return ResponseEntity.status(401).build();
            }
            
            PendingValue approved = pendingValueService.approvePendingValue(id, adminId);
            return ResponseEntity.ok(new PendingValueDTO(approved));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante approvazione: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * ❌ DELETE /api/admin/pending-values/{id}
     * Rifiuta una proposta
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> rejectPendingValue(
            @PathVariable UUID id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal 
            org.springframework.security.core.userdetails.UserDetails userDetails) {
        
        try {
            Long adminId = getUserIdFromEmail(userDetails.getUsername());
            if (adminId == null) {
                return ResponseEntity.status(401).build();
            }
            
            pendingValueService.rejectPendingValue(id, adminId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Proposta rifiutata"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante rifiuto: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📊 GET /api/admin/pending-values/stats
     * Statistiche proposte
     */
    @GetMapping("/stats")
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
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 🔍 Helper per recuperare userId da email
     */
    private Long getUserIdFromEmail(String email) {
        com.funkard.model.User user = userRepository.findByEmail(email);
        return user != null ? user.getId() : null;
    }
}

