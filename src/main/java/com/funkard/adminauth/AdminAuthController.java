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
    private final AdminTokenService tokenService;
    private final AccessRequestService accessRequestService;

    public AdminAuthController(AdminUserService userService, AdminTokenService tokenService, AccessRequestService accessRequestService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.accessRequestService = accessRequestService;
    }

    /**
     * 🔍 GET /api/admin/auth/token/{token}
     * Valida un token e restituisce i dati dell'utente
     * Gestisce anche token di ruolo (admin_tokens) creando richieste pending
     * 
     * Risposta: { "name": "...", "email": "...", "role": "..." }
     * Include isRoot solo se l'utente è root
     * Se pending=true → HTTP 202 + status pending
     */
    @GetMapping("/token/{token}")
    public ResponseEntity<?> validateToken(@PathVariable String token) {
        // Prima controlla se è un token di ruolo (admin_tokens)
        var tokenOpt = tokenService.validateToken(token);
        if (tokenOpt.isPresent()) {
            AdminToken adminToken = tokenOpt.get();
            
            // Verifica se esiste già una richiesta per questo token
            var requestOpt = accessRequestService.getPendingRequests().stream()
                    .filter(r -> r.getTokenUsed().equals(token))
                    .findFirst();
            
            if (requestOpt.isPresent()) {
                // Richiesta già esistente e pending
                AccessRequest request = requestOpt.get();
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(Map.of(
                            "status", "pending",
                            "message", "Richiesta di accesso in attesa di approvazione",
                            "requestId", request.getId().toString(),
                            "role", request.getRole(),
                            "createdAt", request.getCreatedAt()
                        ));
            }
            
            // Token valido ma nessuna richiesta ancora creata
            // L'utente deve prima creare una richiesta tramite POST /api/admin/access-requests/create
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(
                        "status", "token_valid",
                        "message", "Token valido. Crea una richiesta di accesso con POST /api/admin/access-requests/create",
                        "role", adminToken.getRole()
                    ));
        }
        
        // Altrimenti controlla se è un token utente normale
        AdminUser user = userService.getByToken(token);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token non valido o utente inattivo"));
        }

        // Se l'utente non è attivo, restituisci 401
        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utente non attivo"));
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        
        // Includi isRoot solo se l'utente è root
        if (user.isRoot()) {
            response.put("isRoot", true);
        }
        
        return ResponseEntity.ok(response);
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
            AdminUser newUser = userService.createUser(name, email, role, requester);
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
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
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
            AdminUser updated = userService.regenerateToken(id, requester);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", updated.getId().toString(),
                    "email", updated.getEmail(),
                    "newToken", updated.getAccessToken()
                )
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
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
            userService.deactivate(id, requester);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Utente disattivato con successo"
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la disattivazione: " + e.getMessage()));
        }
    }
    
    /**
     * ✅ PATCH /api/admin/auth/users/{id}/activate
     * Riattiva un utente admin (solo Root per SUPER_ADMIN)
     */
    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<?> activateUser(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !"SUPER_ADMIN".equals(requester.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può riattivare utenti"));
        }

        try {
            userService.activate(id, requester);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Utente riattivato con successo"
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la riattivazione: " + e.getMessage()));
        }
    }
    
    /**
     * 🔄 PATCH /api/admin/auth/users/{id}/role
     * Cambia ruolo di un utente (solo Root per promuovere/demotere SUPER_ADMIN)
     */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> changeRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !"SUPER_ADMIN".equals(requester.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può cambiare ruoli"));
        }

        String newRole = request.get("role");
        if (newRole == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Campo 'role' richiesto"));
        }

        try {
            userService.changeRole(id, newRole, requester);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ruolo cambiato con successo"
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il cambio ruolo: " + e.getMessage()));
        }
    }
    
    /**
     * 📋 GET /api/admin/auth/team/list
     * Lista tutti gli utenti admin del team
     * Mostra isRoot solo se il chiamante è root
     */
    @GetMapping("/team/list")
    public ResponseEntity<?> listTeam(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !"SUPER_ADMIN".equals(requester.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può visualizzare il team"));
        }

        List<Map<String, Object>> users = userService.listAllUsers(requester);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "users", users
        ));
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

    // ==================== TOKEN DI RUOLO (solo Root) ====================

    /**
     * ➕ POST /api/admin/auth/tokens/create
     * Crea un nuovo token di ruolo (solo Root)
     */
    @PostMapping("/tokens/create")
    public ResponseEntity<?> createToken(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !requester.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo Root Super Admin può creare token di ruolo"));
        }

        String token = request.get("token");
        String role = request.get("role");
        String description = request.get("description");

        if (token == null || role == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Campi mancanti: token, role"));
        }

        try {
            AdminToken created = tokenService.createToken(token, role, description, requester);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", Map.of(
                    "id", created.getId().toString(),
                    "role", created.getRole(),
                    "description", created.getDescription() != null ? created.getDescription() : ""
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
     * 📋 GET /api/admin/auth/tokens/list
     * Lista tutti i token di ruolo (solo Root)
     */
    @GetMapping("/tokens/list")
    public ResponseEntity<?> listTokens(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !requester.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo Root Super Admin può visualizzare token"));
        }

        List<AdminToken> tokens = tokenService.listAllTokens();
        List<Map<String, Object>> tokensList = tokens.stream()
                .map(t -> Map.of(
                    "id", t.getId().toString(),
                    "role", t.getRole(),
                    "active", t.isActive(),
                    "description", t.getDescription() != null ? t.getDescription() : "",
                    "createdAt", t.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "tokens", tokensList
        ));
    }

    /**
     * 🚫 POST /api/admin/auth/tokens/{id}/disable
     * Disabilita un token di ruolo (solo Root)
     */
    @PostMapping("/tokens/{id}/disable")
    public ResponseEntity<?> disableToken(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !requester.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo Root Super Admin può disabilitare token"));
        }

        try {
            tokenService.disableToken(id, requester);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token disabilitato con successo"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la disabilitazione: " + e.getMessage()));
        }
    }

    /**
     * 🔄 POST /api/admin/auth/tokens/{id}/regenerate
     * Rigenera un token di ruolo (solo Root)
     */
    @PostMapping("/tokens/{id}/regenerate")
    public ResponseEntity<?> regenerateToken(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken) {
        
        if (adminToken == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Token admin richiesto"));
        }

        AdminUser requester = userService.getByToken(adminToken);
        if (requester == null || !requester.isRoot()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo Root Super Admin può rigenerare token"));
        }

        String newTokenValue = request.get("token");
        if (newTokenValue == null || newTokenValue.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Campo 'token' richiesto"));
        }

        try {
            AdminToken updated = tokenService.regenerateToken(id, newTokenValue, requester);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", Map.of(
                    "id", updated.getId().toString(),
                    "role", updated.getRole(),
                    "description", updated.getDescription() != null ? updated.getDescription() : ""
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

}

