package com.funkard.adminauth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final AccessRequestService requestService;
    private final AdminUserService userService;

    public AccessRequestController(AccessRequestService requestService, AdminUserService userService) {
        this.requestService = requestService;
        this.userService = userService;
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
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !"SUPER_ADMIN".equals(requester.getRole())) {
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
     */
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveRequest(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !"SUPER_ADMIN".equals(requester.getRole())) {
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
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", approvedUser.getId().toString(),
                    "name", approvedUser.getName(),
                    "email", approvedUser.getEmail(),
                    "role", approvedUser.getRole(),
                    "token", approvedUser.getAccessToken()
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
     */
    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectRequest(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !"SUPER_ADMIN".equals(requester.getRole())) {
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

