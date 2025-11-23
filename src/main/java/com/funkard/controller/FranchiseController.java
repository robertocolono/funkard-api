package com.funkard.controller;

import com.funkard.dto.FranchiseDTO;
import com.funkard.dto.ProposeFranchiseRequest;
import com.funkard.model.FranchiseCatalog;
import com.funkard.model.PendingValue;
import com.funkard.service.FranchiseCatalogService;
import com.funkard.service.FranchiseJsonService;
import com.funkard.service.PendingValueService;
import com.funkard.service.FranchiseAdminService;
import com.funkard.admin.service.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 📚 Controller per gestione franchise
 * 
 * Espone API per recuperare liste franchise disponibili
 * da file JSON statico e da database.
 */
@RestController
@RequestMapping("/api/franchises")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "https://admin.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class FranchiseController {
    
    private final FranchiseCatalogService franchiseCatalogService;
    private final FranchiseJsonService franchiseJsonService;
    private final PendingValueService pendingValueService;
    private final AdminNotificationService adminNotificationService;
    private final com.funkard.service.FranchiseAdminService franchiseAdminService;
    private final com.funkard.repository.UserRepository userRepository;
    
    /**
     * 📋 GET /api/franchises
     * Recupera tutte le categorie e franchise da file JSON
     * 
     * Response:
     * [
     *   {
     *     "category": "TCG / Anime",
     *     "franchises": ["Pokémon", "Yu-Gi-Oh!", ...]
     *   },
     *   ...
     * ]
     */
    @GetMapping
    @Cacheable(value = "reference:brands", key = "'all'")
    public ResponseEntity<?> getFranchises() {
        try {
            if (!franchiseJsonService.isDataAvailable()) {
                log.error("❌ Dati franchise non disponibili");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Franchise data unavailable"));
            }
            
            List<Map<String, Object>> franchises = franchiseJsonService.getAllFranchises();
            return ResponseEntity.ok(franchises);
            
        } catch (IllegalStateException e) {
            log.error("❌ Dati franchise non disponibili: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Franchise data unavailable"));
        } catch (Exception e) {
            log.error("Errore durante recupero franchise: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📋 GET /api/franchises/database
     * Recupera franchise dal database (alternativa)
     * 
     * Query Params:
     * - category: Filtra per categoria (opzionale)
     * - grouped: true per raggruppare per categoria (default: false)
     */
    @GetMapping("/database")
    public ResponseEntity<?> getFranchisesFromDatabase(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean grouped) {
        
        try {
            if (grouped) {
                Map<String, List<FranchiseCatalog>> groupedFranchises = 
                    franchiseCatalogService.getFranchisesGroupedByCategory();
                
                Map<String, List<FranchiseDTO>> result = groupedFranchises.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                            .map(FranchiseDTO::new)
                            .collect(Collectors.toList())
                    ));
                
                return ResponseEntity.ok(result);
            } else if (category != null && !category.trim().isEmpty()) {
                List<FranchiseCatalog> franchises = franchiseCatalogService.getFranchisesByCategory(category);
                List<FranchiseDTO> dtos = franchises.stream()
                    .map(FranchiseDTO::new)
                    .collect(Collectors.toList());
                
                return ResponseEntity.ok(dtos);
            } else {
                List<FranchiseCatalog> franchises = franchiseCatalogService.getActiveFranchises();
                List<FranchiseDTO> dtos = franchises.stream()
                    .map(FranchiseDTO::new)
                    .collect(Collectors.toList());
                
                return ResponseEntity.ok(dtos);
            }
            
        } catch (Exception e) {
            log.error("Errore durante recupero franchise da database: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📋 GET /api/franchises/categories
     * Recupera lista categorie disponibili da JSON
     */
    @GetMapping("/categories")
    @Cacheable(value = "reference:brands", key = "'categories'")
    public ResponseEntity<?> getCategories() {
        try {
            if (!franchiseJsonService.isDataAvailable()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Franchise data unavailable"));
            }
            
            List<String> categories = franchiseJsonService.getCategories();
            return ResponseEntity.ok(categories);
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Franchise data unavailable"));
        } catch (Exception e) {
            log.error("Errore durante recupero categorie: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 📋 GET /api/franchises/category/{category}
     * Recupera franchise per categoria specifica
     */
    @GetMapping("/category/{category}")
    @Cacheable(value = "reference:brands", key = "#category")
    public ResponseEntity<?> getFranchisesByCategory(@PathVariable String category) {
        try {
            if (!franchiseJsonService.isDataAvailable()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Franchise data unavailable"));
            }
            
            List<String> franchises = franchiseJsonService.getFranchisesByCategory(category);
            return ResponseEntity.ok(Map.of(
                "category", category,
                "franchises", franchises
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Franchise data unavailable"));
        } catch (Exception e) {
            log.error("Errore durante recupero franchise per categoria: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 📝 POST /api/franchises/propose
     * Proposta nuovo franchise da parte utente
     * 
     * Request:
     * {
     *   "category": "Sportive",
     *   "franchise": "NBA Prizm"
     * }
     * 
     * Comportamento:
     * - Salva proposta in pending_values (type: FRANCHISE)
     * - Invia notifica admin
     */
    @PostMapping("/propose")
    public ResponseEntity<?> proposeFranchise(
            @Valid @RequestBody ProposeFranchiseRequest request,
            Authentication authentication) {
        
        try {
            // Recupera userId (opzionale - può essere anonimo)
            Long userId = null;
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                com.funkard.model.User user = userRepository.findByEmail(userDetails.getUsername());
                userId = user != null ? user.getId() : null;
            }
            
            // Crea proposta usando FranchiseAdminService
            String userEmail = null;
            if (userId != null) {
                com.funkard.model.User user = userRepository.findById(userId).orElse(null);
                userEmail = user != null ? user.getEmail() : null;
            }
            
            try {
                franchiseAdminService.createProposal(
                    request.getCategory(),
                    request.getFranchise(),
                    userEmail,
                    userId
                );
                
                log.info("✅ Proposta franchise creata: {} - {} (utente: {})", 
                    request.getCategory(), request.getFranchise(), userId);
            } catch (IllegalStateException e) {
                // Proposta duplicata o franchise già esistente
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
            } catch (Exception e) {
                log.warn("⚠️ Errore durante creazione proposta: {}", e.getMessage());
                // Continua comunque per inviare notifica
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Proposta inviata con successo. Verrà valutata dal team Funkard."
            ));
            
        } catch (Exception e) {
            log.error("Errore durante proposta franchise: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📊 GET /api/franchises/stats
     * Statistiche franchise (pubblico)
     */
    @GetMapping("/stats")
    @Cacheable(value = "reference:brands", key = "'stats'")
    public ResponseEntity<?> getStats() {
        try {
            // Statistiche da JSON
            List<Map<String, Object>> franchises = franchiseJsonService.getAllFranchises();
            long totalFranchises = franchises.stream()
                .mapToLong(item -> {
                    @SuppressWarnings("unchecked")
                    List<String> fs = (List<String>) item.get("franchises");
                    return fs != null ? fs.size() : 0;
                })
                .sum();
            
            Map<String, Long> byCategory = franchises.stream()
                .collect(Collectors.toMap(
                    item -> (String) item.get("category"),
                    item -> {
                        @SuppressWarnings("unchecked")
                        List<String> fs = (List<String>) item.get("franchises");
                        return fs != null ? (long) fs.size() : 0L;
                    }
                ));
            
            return ResponseEntity.ok(Map.of(
                "totalCategories", franchises.size(),
                "totalFranchises", totalFranchises,
                "byCategory", byCategory,
                "source", "json"
            ));
            
        } catch (Exception e) {
            log.error("Errore durante recupero statistiche: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

