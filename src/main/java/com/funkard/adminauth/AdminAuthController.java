package com.funkard.adminauth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 🔐 Controller per autenticazione admin
 */
@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "http://localhost:3002"})
public class AdminAuthController {

    private final AdminUserService userService;

    public AdminAuthController(AdminUserService userService) {
        this.userService = userService;
    }

    /**
     * 🔍 GET /api/admin/auth/token/{token}
     * Valida un token e restituisce i dati dell'utente
     * 
     * Risposta: { "name": "...", "email": "...", "role": "..." }
     */
    @GetMapping("/token/{token}")
    public ResponseEntity<?> validateToken(@PathVariable String token) {
        AdminUser user = userService.getByToken(token);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token non valido o utente inattivo"));
        }

        return ResponseEntity.ok(Map.of(
            "name", user.getName(),
            "email", user.getEmail(),
            "role", user.getRole()
        ));
    }

    /**
     * ➕ POST /api/admin/auth/users/create
     * Crea un nuovo utente admin (solo SUPER_ADMIN)
     * 
     * Body: { "name": "...", "email": "...", "role": "ADMIN|SUPERVISOR" }
     * Header: X-Admin-Token (token del SUPER_ADMIN che richiede)
     */
    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        // Verifica che il richiedente sia SUPER_ADMIN
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !"SUPER_ADMIN".equals(requester.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può creare nuovi utenti"));
        }

        // Valida input
        String name = request.get("name");
        String email = request.get("email");
        String role = request.get("role");

        if (name == null || email == null || role == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Campi mancanti: name, email, role"));
        }

        // Valida ruolo
        if (!"ADMIN".equals(role) && !"SUPERVISOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Ruolo non valido. Usa: ADMIN o SUPERVISOR"));
        }

        try {
            AdminUser newUser = userService.createUser(name, email, role);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", newUser.getId().toString(),
                    "name", newUser.getName(),
                    "email", newUser.getEmail(),
                    "role", newUser.getRole(),
                    "token", newUser.getAccessToken()
                )
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
     * 🔄 PATCH /api/admin/auth/users/{id}/regenerate-token
     * Rigenera il token per un utente (solo SUPER_ADMIN)
     */
    @PatchMapping("/users/{id}/regenerate-token")
    public ResponseEntity<?> regenerateToken(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        // Verifica che il richiedente sia SUPER_ADMIN
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !"SUPER_ADMIN".equals(requester.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può rigenerare token"));
        }

        try {
            AdminUser updated = userService.regenerateToken(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", updated.getId().toString(),
                    "email", updated.getEmail(),
                    "newToken", updated.getAccessToken()
                )
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la rigenerazione: " + e.getMessage()));
        }
    }

    /**
     * 🚫 PATCH /api/admin/auth/users/{id}/deactivate
     * Disattiva un utente admin (solo SUPER_ADMIN)
     */
    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        // Verifica che il richiedente sia SUPER_ADMIN
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !"SUPER_ADMIN".equals(requester.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può disattivare utenti"));
        }

        // Non permettere di disattivare se stessi
        if (requester.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Non puoi disattivare il tuo stesso account"));
        }

        try {
            userService.deactivate(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Utente disattivato con successo"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la disattivazione: " + e.getMessage()));
        }
    }

    /**
     * 🔍 GET /api/admin/auth/diagnostic
     * Endpoint diagnostico per verificare stato Super Admin
     * Mostra tutti gli utenti admin e verifica/corregge il Super Admin
     */
    @GetMapping("/diagnostic")
    public ResponseEntity<?> diagnostic() {
        return ResponseEntity.ok(userService.diagnosticCheck());
    }

    /**
     * 🔧 POST /api/admin/auth/verify-and-fix
     * Verifica e corregge il Super Admin con token specifico
     * Body: { "token": "..." }
     */
    @PostMapping("/verify-and-fix")
    public ResponseEntity<?> verifyAndFix(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Token richiesto nel body: { \"token\": \"...\" }"));
        }
        
        return ResponseEntity.ok(userService.verifyAndFixSuperAdmin(token));
    }
}

