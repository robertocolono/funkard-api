package com.funkard.admin.controller;

import com.funkard.dto.FranchiseDTO;
import com.funkard.model.FranchiseCatalog;
import com.funkard.service.FranchiseCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 📚 Controller Admin per gestione catalogo franchise
 * 
 * Permette agli admin di creare, aggiornare, disattivare
 * e eliminare franchise dal catalogo.
 */
@RestController
@RequestMapping("/api/admin/franchises/catalog")
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
public class AdminFranchiseController {
    
    private final FranchiseCatalogService franchiseService;
    
    /**
     * 📋 GET /api/admin/franchises
     * Lista tutti i franchise (inclusi disattivati)
     */
    @GetMapping
    public ResponseEntity<List<FranchiseDTO>> getAllFranchises() {
        try {
            List<FranchiseCatalog> franchises = franchiseService.getActiveFranchises();
            List<FranchiseDTO> dtos = franchises.stream()
                .map(FranchiseDTO::new)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Errore durante recupero franchise: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * ➕ POST /api/admin/franchises
     * Crea nuovo franchise
     * 
     * Request:
     * {
     *   "category": "TCG",
     *   "name": "UFC Prizm"
     * }
     */
    @PostMapping
    public ResponseEntity<?> createFranchise(@Valid @RequestBody Map<String, String> request) {
        try {
            String category = request.get("category");
            String name = request.get("name");
            
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "La categoria è obbligatoria"));
            }
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Il nome è obbligatorio"));
            }
            
            FranchiseCatalog franchise = franchiseService.createFranchise(category, name);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FranchiseDTO(franchise));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante creazione franchise: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * ✏️ PUT /api/admin/franchises/{id}
     * Aggiorna franchise
     * 
     * Request:
     * {
     *   "category": "TCG",
     *   "name": "MetaZoo",
     *   "active": false
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFranchise(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        try {
            String category = (String) request.get("category");
            String name = (String) request.get("name");
            Boolean active = request.get("active") != null ? 
                Boolean.parseBoolean(request.get("active").toString()) : null;
            
            FranchiseCatalog updated = franchiseService.updateFranchise(id, category, name, active);
            return ResponseEntity.ok(new FranchiseDTO(updated));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante aggiornamento franchise: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 🗑️ DELETE /api/admin/franchises/{id}
     * Elimina franchise
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFranchise(@PathVariable Long id) {
        try {
            franchiseService.deleteFranchise(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Franchise eliminato"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante eliminazione franchise: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📊 GET /api/admin/franchises/stats
     * Statistiche franchise (admin)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            return ResponseEntity.ok(franchiseService.getStats());
        } catch (Exception e) {
            log.error("Errore durante recupero statistiche: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}

