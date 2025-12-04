package com.funkard.adminauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 📝 Controller per gestione richieste di accesso
 */
@RestController
@RequestMapping("/api/admin/access-requests")
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "http://localhost:3002"})
public class AccessRequestController {

    private static final Logger logger = LoggerFactory.getLogger(AccessRequestController.class);

    private final AccessRequestService requestService;
    private final AdminUserService userService;

    public AccessRequestController(AccessRequestService requestService, AdminUserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    /**
     * 🔐 Helper: Recupera admin corrente da SecurityContext
     * Usa autenticazione moderna basata su sessioni httpOnly
     * @return AdminUser corrente o null se non autenticato
     */
    private AdminUser getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("⚠️ Unauthorized access attempt on AccessRequestController: no authentication in SecurityContext");
            return null;
        }
        
        // Estrai email da principal
        Object principal = authentication.getPrincipal();
        String email = null;
        
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        }
        
        if (email == null || email.trim().isEmpty()) {
            logger.warn("⚠️ Unauthorized access attempt on AccessRequestController: unable to extract email from principal");
            return null;
        }
        
        // Trova admin per email
        AdminUser admin = userService.getByEmail(email);
        if (admin == null) {
            logger.warn("⚠️ Unauthorized access attempt on AccessRequestController: admin not found for email: {}", email);
            return null;
        }
        
        logger.debug("✅ Admin authenticated: {} ({})", admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), email);
        return admin;
    }

    /**
     * 📝 POST /api/admin/access-requests/create
     * Utente invia email + token per richiedere accesso
     * Body: { "email": "...", "token": "..." }
     */
    @PostMapping("/create")
    public ResponseEntity<?> createRequest(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String token = request.get("token");

        if (email == null || token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Campi mancanti: email, token"));
        }

        try {
            AccessRequest accessRequest = requestService.createRequest(email, token);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "request", Map.of(
                    "id", accessRequest.getId().toString(),
                    "email", accessRequest.getEmail(),
                    "role", accessRequest.getRole(),
                    "status", accessRequest.getStatus(),
                    "createdAt", accessRequest.getCreatedAt()
                ),
                "message", "Richiesta di accesso creata con successo. In attesa di approvazione."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la creazione: " + e.getMessage()));
        }
    }

    /**
     * 📋 GET /api/admin/access-requests/pending
     * Lista tutte le richieste PENDING
     * Root → tutte le richieste
     * Super Admin → solo Admin e Supervisor
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getPendingRequests() {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: getPendingRequests requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!"SUPER_ADMIN".equals(requester.getRole())) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to get pending requests", requester.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può visualizzare richieste pending"));
        }

        List<AccessRequest> pending = requestService.getPendingRequests();
        
        // Se non è Root, filtra solo ADMIN e SUPERVISOR
        if (!requester.isRoot()) {
            pending = pending.stream()
                    .filter(r -> "ADMIN".equals(r.getRole()) || "SUPERVISOR".equals(r.getRole()))
                    .toList();
        }

        List<Map<String, Object>> requestsList = pending.stream()
                .map(r -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", r.getId().toString());
                    map.put("email", r.getEmail());
                    map.put("role", r.getRole());
                    map.put("status", r.getStatus());
                    map.put("createdAt", r.getCreatedAt());
                    return map;
                })
                .toList();

        logger.debug("✅ Admin {} retrieved {} pending access requests", requester.getEmail(), requestsList.size());
        return ResponseEntity.ok(Map.of(
            "success", true,
            "requests", requestsList
        ));
    }

    /**
     * ✅ POST /api/admin/access-requests/approve/{id}
     * Approva una richiesta e crea AdminUser
     * Root → tutte le richieste
     * Super Admin → solo Admin e Supervisor
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> approveRequest(@PathVariable UUID id) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: approveRequest requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!"SUPER_ADMIN".equals(requester.getRole())) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to approve request", requester.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può approvare richieste"));
        }

        try {
            // Verifica permessi: Root può approvare tutto, Super Admin solo ADMIN/SUPERVISOR
            List<AccessRequest> pending = requestService.getPendingRequests();
            AccessRequest targetRequest = pending.stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata o non pending"));

            if (!requester.isRoot() && "SUPER_ADMIN".equals(targetRequest.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Solo Root può approvare richieste SUPER_ADMIN"));
            }

            AdminUser approvedUser = requestService.approveRequest(id, requester.getId());
            logger.info("✅ Admin {} is approving access request {} (user: {})", requester.getEmail(), id, approvedUser.getEmail());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", approvedUser.getId().toString(),
                    "name", approvedUser.getName(),
                    "email", approvedUser.getEmail(),
                    "role", approvedUser.getRole()
                ),
                "message", "Richiesta approvata e utente creato con successo"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante l'approvazione: " + e.getMessage()));
        }
    }

    /**
     * ❌ POST /api/admin/access-requests/reject/{id}
     * Rifiuta una richiesta
     * Root → tutte le richieste
     * Super Admin → solo Admin e Supervisor
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @PostMapping("/reject/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> rejectRequest(@PathVariable UUID id) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: rejectRequest requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!"SUPER_ADMIN".equals(requester.getRole())) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to reject request", requester.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può rifiutare richieste"));
        }

        try {
            // Verifica permessi: Root può rifiutare tutto, Super Admin solo ADMIN/SUPERVISOR
            List<AccessRequest> pending = requestService.getPendingRequests();
            AccessRequest targetRequest = pending.stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Richiesta non trovata o non pending"));

            if (!requester.isRoot() && "SUPER_ADMIN".equals(targetRequest.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Solo Root può rifiutare richieste SUPER_ADMIN"));
            }

            requestService.rejectRequest(id);
            logger.info("✅ Admin {} is rejecting access request {}", requester.getEmail(), id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Richiesta rifiutata con successo"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il rifiuto: " + e.getMessage()));
        }
    }
}

