package com.funkard.adminaccess.controller;

import com.funkard.adminaccess.model.AdminAccessRequest;
import com.funkard.adminaccess.model.AdminAccessToken;
import com.funkard.adminaccess.service.AdminAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 🌐 Controller per gestione token e richieste di accesso admin
 */
@RestController
@RequestMapping("/api/admin/access")
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "http://localhost:3002"})
@RequiredArgsConstructor
public class AdminAccessController {

    private final AdminAccessService accessService;

    /**
     * ➕ POST /api/admin/access/generate?role={ROLE}
     * Genera un nuovo token (solo Super Admin)
     * Header: Authorization: Bearer {SUPER_ADMIN_TOKEN}
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateToken(
            @RequestParam String role,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Verifica autorizzazione Super Admin
        String superAdminToken = System.getenv("SUPER_ADMIN_TOKEN");
        if (superAdminToken == null || authHeader == null || !authHeader.contains(superAdminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accesso non autorizzato. Solo Super Admin può generare token."));
        }

        // Valida ruolo
        if (!role.equals("ADMIN") && !role.equals("SUPERVISOR") && !role.equals("SUPER_ADMIN")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Ruolo non valido. Usa: ADMIN, SUPERVISOR o SUPER_ADMIN"));
        }

        try {
            String token = accessService.generateToken(role, "RootSuperAdmin");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "role", role,
                "message", "Token generato con successo. Salvalo ora, non verrà mostrato di nuovo."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la generazione: " + e.getMessage()));
        }
    }

    /**
     * 📝 POST /api/admin/access/request
     * Utente invia richiesta di accesso
     * Body: { "email": "...", "token": "..." }
     */
    @PostMapping("/request")
    public ResponseEntity<?> submitRequest(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String token = body.get("token");

        if (email == null || token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Campi mancanti: email, token"));
        }

        try {
            AdminAccessRequest request = accessService.submitRequest(email, token);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "status", "PENDING",
                "requestedRole", request.getRequestedRole(),
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
     * 📋 GET /api/admin/access/requests
     * Elenco richieste pendenti (solo Super Admin)
     * Header: Authorization: Bearer {SUPER_ADMIN_TOKEN}
     */
    @GetMapping("/requests")
    public ResponseEntity<?> getRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Verifica autorizzazione Super Admin
        String superAdminToken = System.getenv("SUPER_ADMIN_TOKEN");
        if (superAdminToken == null || authHeader == null || !authHeader.contains(superAdminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accesso non autorizzato. Solo Super Admin può visualizzare richieste."));
        }

        List<AdminAccessRequest> requests = accessService.getAllRequests();
        
        List<Map<String, Object>> requestsList = requests.stream()
                .map(r -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", r.getId().toString());
                    map.put("email", r.getEmail());
                    map.put("requestedRole", r.getRequestedRole());
                    map.put("status", r.getStatus());
                    map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt() : "");
                    map.put("approvedBy", r.getApprovedBy() != null ? r.getApprovedBy() : "");
                    map.put("approvedAt", r.getApprovedAt() != null ? r.getApprovedAt() : "");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "requests", requestsList
        ));
    }

    /**
     * ✅ POST /api/admin/access/approve/{id}
     * Approva una richiesta (solo Super Admin)
     * Header: Authorization: Bearer {SUPER_ADMIN_TOKEN}
     */
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveRequest(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Verifica autorizzazione Super Admin
        String superAdminToken = System.getenv("SUPER_ADMIN_TOKEN");
        if (superAdminToken == null || authHeader == null || !authHeader.contains(superAdminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accesso non autorizzato. Solo Super Admin può approvare richieste."));
        }

        try {
            AdminAccessRequest approved = accessService.approveRequest(id, "RootSuperAdmin");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Richiesta approvata con successo",
                "email", approved.getEmail(),
                "role", approved.getRequestedRole()
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
     * ❌ POST /api/admin/access/reject/{id}
     * Rifiuta una richiesta (solo Super Admin)
     * Header: Authorization: Bearer {SUPER_ADMIN_TOKEN}
     */
    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectRequest(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Verifica autorizzazione Super Admin
        String superAdminToken = System.getenv("SUPER_ADMIN_TOKEN");
        if (superAdminToken == null || authHeader == null || !authHeader.contains(superAdminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accesso non autorizzato. Solo Super Admin può rifiutare richieste."));
        }

        try {
            AdminAccessRequest rejected = accessService.rejectRequest(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Richiesta rifiutata con successo",
                "email", rejected.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il rifiuto: " + e.getMessage()));
        }
    }

    /**
     * 📋 GET /api/admin/access/tokens
     * Lista token attivi (solo Super Admin)
     * Header: Authorization: Bearer {SUPER_ADMIN_TOKEN}
     */
    @GetMapping("/tokens")
    public ResponseEntity<?> listTokens(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Verifica autorizzazione Super Admin
        String superAdminToken = System.getenv("SUPER_ADMIN_TOKEN");
        if (superAdminToken == null || authHeader == null || !authHeader.contains(superAdminToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accesso non autorizzato. Solo Super Admin può visualizzare token."));
        }

        List<AdminAccessToken> tokens = accessService.listTokens();
        
        List<Map<String, Object>> tokensList = tokens.stream()
                .map(t -> {
                    String tokenPreview = t.getToken() != null && t.getToken().length() >= 12
                        ? t.getToken().substring(0, 12) + "..."
                        : "***";
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", t.getId().toString());
                    map.put("role", t.getRole());
                    map.put("token", tokenPreview);
                    map.put("active", t.isActive());
                    map.put("createdBy", t.getCreatedBy() != null ? t.getCreatedBy() : "");
                    map.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt() : "");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "tokens", tokensList
        ));
    }
}

