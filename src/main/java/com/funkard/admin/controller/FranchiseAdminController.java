package com.funkard.admin.controller;

import com.funkard.dto.CreateFranchiseRequest;
import com.funkard.dto.FranchiseDTO;
import com.funkard.dto.FranchiseProposalDTO;
import com.funkard.model.Franchise;
import com.funkard.model.FranchiseProposal;
import com.funkard.model.User;
import com.funkard.repository.UserRepository;
import com.funkard.service.FranchiseAdminService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 📚 Controller Admin per gestione franchise e proposte
 * 
 * Endpoint per approvazione, rifiuto, abilitazione/disabilitazione
 * e creazione manuale di franchise.
 */
@RestController
@RequestMapping("/api/admin/franchises")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "https://admin.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
})
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR', 'ADMIN')")
public class FranchiseAdminController {
    
    private final FranchiseAdminService franchiseAdminService;
    private final UserRepository userRepository;
    
    /**
     * 📋 GET /api/admin/franchises
     * Recupera tutte le proposte e franchise
     * 
     * Query Params:
     * - status: Filtra per stato (pending, active, disabled)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFranchisesAndProposals(
            @RequestParam(required = false) String status) {
        
        try {
            Map<String, Object> result = franchiseAdminService.getAllFranchisesAndProposals(status);
            
            // Converti a DTO
            @SuppressWarnings("unchecked")
            List<FranchiseProposal> proposals = (List<FranchiseProposal>) result.get("proposals");
            @SuppressWarnings("unchecked")
            List<Franchise> franchises = (List<Franchise>) result.get("franchises");
            
            List<FranchiseProposalDTO> proposalDTOs = proposals.stream()
                .map(FranchiseProposalDTO::new)
                .collect(Collectors.toList());
            
            List<FranchiseDTO> franchiseDTOs = franchises.stream()
                .map(f -> new FranchiseDTO(f.getId(), f.getCategory(), f.getName(), 
                    f.getStatus() == Franchise.FranchiseStatus.ACTIVE))
                .collect(Collectors.toList());
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("proposals", proposalDTOs);
            response.put("franchises", franchiseDTOs);
            response.put("stats", result.get("stats"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Errore durante recupero franchise e proposte: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * ✅ POST /api/admin/franchises/approve/{proposalId}
     * Approva proposta franchise
     * 
     * Comportamento:
     * - Cambia status proposta a APPROVED
     * - Crea nuovo record in Franchise
     * - Aggiorna franchises.json
     * - Invia notifica admin
     */
    @PostMapping("/approve/{proposalId}")
    public ResponseEntity<?> approveProposal(
            @PathVariable Long proposalId,
            Authentication authentication) {
        
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Franchise franchise = franchiseAdminService.approveProposal(proposalId, adminId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Franchise '%s' approvato e aggiunto al catalogo", 
                    franchise.getName()),
                "franchise", franchise
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante approvazione proposta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * ❌ POST /api/admin/franchises/reject/{proposalId}
     * Rifiuta proposta franchise
     * 
     * Comportamento:
     * - Imposta status=REJECTED
     * - Invia notifica utente (se userEmail presente)
     */
    @PostMapping("/reject/{proposalId}")
    public ResponseEntity<?> rejectProposal(
            @PathVariable Long proposalId,
            Authentication authentication) {
        
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            franchiseAdminService.rejectProposal(proposalId, adminId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Proposta rifiutata"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante rifiuto proposta: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 🚫 PATCH /api/admin/franchises/{id}/disable
     * Disabilita franchise
     * 
     * Rimuove temporaneamente dal JSON pubblico
     */
    @PatchMapping("/{id}/disable")
    public ResponseEntity<?> disableFranchise(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Franchise franchise = franchiseAdminService.disableFranchise(id, adminId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Franchise '%s' disabilitato", franchise.getName()),
                "franchise", franchise
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante disabilitazione franchise: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * ✅ PATCH /api/admin/franchises/{id}/enable
     * Riabilita franchise
     * 
     * Aggiunge di nuovo al JSON pubblico
     */
    @PatchMapping("/{id}/enable")
    public ResponseEntity<?> enableFranchise(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Franchise franchise = franchiseAdminService.enableFranchise(id, adminId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Franchise '%s' riabilitato", franchise.getName()),
                "franchise", franchise
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante riabilitazione franchise: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * ➕ POST /api/admin/franchises/add
     * Crea franchise manualmente
     * 
     * Request:
     * {
     *   "category": "TCG / Anime",
     *   "name": "Bleach Card Game"
     * }
     */
    @PostMapping("/add")
    public ResponseEntity<?> createFranchise(
            @Valid @RequestBody CreateFranchiseRequest request,
            Authentication authentication) {
        
        try {
            Long adminId = getUserIdFromAuthentication(authentication);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Franchise franchise = franchiseAdminService.createFranchise(
                request.getCategory(),
                request.getName(),
                adminId
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "success", true,
                    "message", String.format("Franchise '%s' creato con successo", franchise.getName()),
                    "franchise", franchise
                ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante creazione franchise: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 🔍 Helper per recuperare userId da Authentication
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            
            User user = userRepository.findByEmail(email);
            return user != null ? user.getId() : null;
        }
        return null;
    }
}

