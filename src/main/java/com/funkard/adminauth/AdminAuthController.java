package com.funkard.adminauth;

import com.funkard.adminauth.dto.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * 🔐 Controller per autenticazione admin
 */
@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "http://localhost:3002"})
public class AdminAuthController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);

    private final AdminUserService userService;
    private final AdminTokenService tokenService;
    private final AccessRequestService accessRequestService;
    private final AdminSessionService sessionService;

    public AdminAuthController(AdminUserService userService, AdminTokenService tokenService, 
                              AccessRequestService accessRequestService, AdminSessionService sessionService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.accessRequestService = accessRequestService;
        this.sessionService = sessionService;
    }

    /**
     * 🔐 Helper: Recupera admin corrente da SecurityContext
     * Usa autenticazione moderna basata su sessioni httpOnly
     * @return AdminUser corrente o null se non autenticato
     */
    private AdminUser getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("⚠️ Unauthorized admin access attempt: no authentication in SecurityContext");
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
            logger.warn("⚠️ Unauthorized admin access attempt: unable to extract email from principal");
            return null;
        }
        
        // Trova admin per email
        AdminUser admin = userService.getByEmail(email);
        if (admin == null) {
            logger.warn("⚠️ Unauthorized admin access attempt: admin not found for email: {}", email);
            return null;
        }
        
        logger.debug("✅ Admin authenticated: {} ({})", admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), email);
        return admin;
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
     * Usa autenticazione moderna basata su sessioni httpOnly
     * 
     * Body: { "name": "...", "email": "...", "role": "ADMIN|SUPERVISOR" }
     */
    @PostMapping("/users/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: createUser requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!"SUPER_ADMIN".equals(requester.getRole())) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to create user", requester.getEmail());
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
            logger.info("✅ Admin {} created new user: {} ({})", requester.getEmail(), newUser.getEmail(), role);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", newUser.getId().toString(),
                    "name", newUser.getName(),
                    "email", newUser.getEmail(),
                    "role", newUser.getRole()
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
     * Usa autenticazione moderna basata su sessioni httpOnly
     * 
     * NOTA: Questo endpoint rigenera accessToken legacy (deprecato).
     * Gli utenti moderni non usano più accessToken dopo onboarding.
     */
    @PatchMapping("/users/{id}/regenerate-token")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> regenerateToken(@PathVariable UUID id) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: regenerateToken requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!"SUPER_ADMIN".equals(requester.getRole())) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to regenerate token", requester.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può rigenerare token"));
        }

        try {
            AdminUser updated = userService.regenerateToken(id, requester);
            logger.info("✅ Admin {} regenerated token for user: {}", requester.getEmail(), updated.getEmail());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", updated.getId().toString(),
                    "email", updated.getEmail()
                ),
                "message", "Token rigenerato (legacy, deprecato per utenti moderni)"
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
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @PatchMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deactivateUser(@PathVariable UUID id) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: deactivateUser requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!"SUPER_ADMIN".equals(requester.getRole())) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to deactivate user", requester.getEmail());
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
            logger.info("✅ Admin {} deactivated user: {}", requester.getEmail(), id);
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
     * Riattiva un utente admin (solo SUPER_ADMIN)
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @PatchMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> activateUser(@PathVariable UUID id) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: activateUser requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!"SUPER_ADMIN".equals(requester.getRole())) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to activate user", requester.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo SUPER_ADMIN può riattivare utenti"));
        }

        try {
            userService.activate(id, requester);
            logger.info("✅ Admin {} activated user: {}", requester.getEmail(), id);
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
     * Cambia ruolo di un utente (solo SUPER_ADMIN)
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> changeRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: changeRole requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!"SUPER_ADMIN".equals(requester.getRole())) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to change role", requester.getEmail());
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
            logger.info("✅ Admin {} changed role for user {} to {}", requester.getEmail(), id, newRole);
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
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @GetMapping("/team/list")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> listTeam() {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: listTeam requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!"SUPER_ADMIN".equals(requester.getRole())) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to list team", requester.getEmail());
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
     * Crea un nuovo token di ruolo (AdminToken moderno)
     * Solo SUPER_ADMIN root può creare token
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @PostMapping("/tokens/create")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createToken(@RequestBody Map<String, String> request) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: createToken requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!requester.isRoot()) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to create token (not root)", requester.getEmail());
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
            String tokenValue = tokenService.createToken(role, requester.getId());
            logger.info("✅ Admin {} (root) created new token for role: {}", requester.getEmail(), role);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", Map.of(
                    "token", tokenValue,
                    "role", role
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
     * Lista tutti i token di ruolo (AdminToken moderno)
     * Solo SUPER_ADMIN root può visualizzare token
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @GetMapping("/tokens/list")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> listTokens() {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: listTokens requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!requester.isRoot()) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to list tokens (not root)", requester.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo Root Super Admin può visualizzare token"));
        }

        List<AdminToken> tokens = tokenService.getAllActiveTokens();
        List<Map<String, Object>> tokensList = tokens.stream()
                .map(t -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", t.getId().toString());
                    map.put("role", t.getRole());
                    map.put("active", t.isActive());
                    map.put("createdAt", t.getCreatedAt());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "tokens", tokensList
        ));
    }

    /**
     * 🚫 POST /api/admin/auth/tokens/{id}/disable
     * Disabilita un token di ruolo (AdminToken moderno)
     * Solo SUPER_ADMIN root può disabilitare token
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @PostMapping("/tokens/{id}/disable")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> disableToken(@PathVariable UUID id) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: disableToken requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!requester.isRoot()) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to disable token (not root)", requester.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo Root Super Admin può disabilitare token"));
        }

        try {
            tokenService.deactivateToken(id);
            logger.info("✅ Admin {} (root) disabled token: {}", requester.getEmail(), id);
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
     * Rigenera un token di ruolo (AdminToken moderno)
     * Solo SUPER_ADMIN root può rigenerare token
     * Usa autenticazione moderna basata su sessioni httpOnly
     */
    @PostMapping("/tokens/{id}/regenerate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> regenerateToken(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        // Recupera admin corrente da SecurityContext (autenticazione moderna)
        AdminUser requester = getCurrentAdmin();
        if (requester == null) {
            logger.warn("🚫 Legacy token bypass removed: regenerateToken requires modern session authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Autenticazione richiesta"));
        }
        
        if (!requester.isRoot()) {
            logger.warn("🚫 Unauthorized admin access attempt: {} tried to regenerate token (not root)", requester.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Solo Root Super Admin può rigenerare token"));
        }

        String newTokenValue = request.get("token");
        if (newTokenValue == null || newTokenValue.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Campo 'token' richiesto"));
        }

        try {
            String newToken = tokenService.regenerateToken(id, requester.getId());
            logger.info("✅ Admin {} (root) regenerated token: {}", requester.getEmail(), id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", Map.of(
                    "token", newToken
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

    // ==================== TOKEN CHECK (legacy, mantenuto per compatibilità) ====================

    /**
     * 🔍 GET /api/admin/auth/token-check?token=...
     * Valida token di onboarding (monouso, per profilo)
     * 
     * Query param: token (obbligatorio)
     * 
     * Risposta 200: { id, role, onboardingCompleted, displayName?, email? }
     * Risposta 400: token mancante
     * Risposta 401: token non valido o utente inattivo
     * Risposta 410: token già usato (onboardingCompleted = true)
     * 
     * NOTA: Gli endpoint login, onboarding-complete, logout e me sono stati spostati
     * in AdminAuthControllerModern per evitare conflitti di mapping.
     */
    @GetMapping("/token-check")
    public ResponseEntity<?> tokenCheck(@RequestParam(required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Token richiesto come query parameter: ?token=..."));
        }

        try {
            AdminUser user = userService.validateOnboardingToken(token);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token non valido o utente inattivo"));
            }

            TokenCheckResponse response = new TokenCheckResponse(
                    user.getId(),
                    user.getRole(),
                    user.isOnboardingCompleted(),
                    user.getDisplayName(),
                    user.getEmail()
            );

            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Token già usato (onboardingCompleted = true)
            return ResponseEntity.status(HttpStatus.GONE) // 410 Gone
                    .body(Map.of("error", "Token già utilizzato per onboarding"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante la validazione: " + e.getMessage()));
        }
    }

    // ==================== ENDPOINT MODERNI SPOSTATI ====================
    // Gli endpoint login, onboarding-complete, logout e me sono stati spostati
    // in AdminAuthControllerModern per evitare conflitti di mapping.
    // Usa AdminAuthControllerModern per questi endpoint.

}

