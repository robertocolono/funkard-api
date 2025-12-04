package com.funkard.adminauthmodern;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 🔐 Controller moderno per autenticazione admin
 * Endpoint completamente separati dal sistema legacy
 */
@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "https://admin.funkard.com", "http://localhost:3000", "http://localhost:3002"}, allowCredentials = "true")
public class AdminAuthControllerModern {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthControllerModern.class);
    
    private final AdminAuthServiceModern authService;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    public AdminAuthControllerModern(AdminAuthServiceModern authService) {
        this.authService = authService;
    }
    
    /**
     * 🔐 POST /api/admin/auth/login
     * Login admin con email e password
     * Body: { "email": "...", "password": "..." }
     * 
     * Risposta 200: { "success": true, "sessionId": "...", "admin": {...} }
     * Risposta 400: { "error": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpServletResponse response) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            Map<String, Object> result = authService.login(email, password);
            String sessionId = (String) result.get("sessionId");
            
            // Crea cookie httpOnly
            Cookie cookie = createSessionCookie(sessionId);
            response.addCookie(cookie);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "admin", result.get("admin")
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Errore durante login moderno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il login: " + e.getMessage()));
        }
    }
    
    /**
     * ✅ POST /api/admin/auth/onboarding-complete
     * Completa onboarding di un admin con token monouso (AdminToken)
     * Body: { "token": "...", "email": "...", "password": "...", "displayName": "..." }
     * 
     * Risposta 200: { "success": true, "admin": { "id", "email", "role", "displayName" } }
     * Risposta 400: { "error": "..." } - Token non valido o formato errato
     * Risposta 404: { "error": "Token non trovato" }
     * Risposta 410: { "error": "Token scaduto" }
     * Risposta 409: { "error": "Email già registrata" }
     */
    @PostMapping("/onboarding-complete")
    public ResponseEntity<?> onboardingComplete(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String email = request.get("email");
            String password = request.get("password");
            String displayName = request.get("displayName");
            
            Map<String, Object> result = authService.completeOnboarding(token, email, password, displayName);
            
            return ResponseEntity.ok(result);
            
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Gestione errori con status code specifici
            HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
            return ResponseEntity.status(status)
                    .body(Map.of("error", e.getReason() != null ? e.getReason() : e.getMessage()));
        } catch (Exception e) {
            logger.error("Errore durante onboarding moderno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il completamento onboarding: " + e.getMessage()));
        }
    }
    
    /**
     * 👤 GET /api/admin/auth/me
     * Restituisce i dati dell'admin corrente
     * Legge da cookie ADMIN_SESSION
     * 
     * Risposta 200: { "id": "...", "email": "...", "role": "...", "displayName": "..." }
     * Risposta 401: { "error": "..." }
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        try {
            String sessionId = extractSessionId(request);
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Sessione non valida"));
            }
            
            Map<String, Object> admin = authService.getCurrentAdmin(sessionId);
            
            return ResponseEntity.ok(admin);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Errore durante recupero admin corrente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il recupero dati: " + e.getMessage()));
        }
    }
    
    /**
     * 🚪 POST /api/admin/auth/logout
     * Logout admin: invalida sessione e rimuove cookie
     * 
     * Risposta 200: { "success": true }
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String sessionId = extractSessionId(request);
            
            if (sessionId != null && !sessionId.trim().isEmpty()) {
                authService.logout(sessionId);
            }
            
            // Rimuovi cookie
            Cookie cookie = createSessionCookie("");
            cookie.setMaxAge(0); // Elimina cookie
            response.addCookie(cookie);
            
            return ResponseEntity.ok(Map.of("success", true));
            
        } catch (Exception e) {
            logger.error("Errore durante logout moderno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il logout: " + e.getMessage()));
        }
    }
    
    /**
     * 🍪 Crea cookie di sessione
     */
    private Cookie createSessionCookie(String sessionId) {
        Cookie cookie = new Cookie("ADMIN_SESSION", sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure("prod".equals(activeProfile)); // Solo HTTPS in produzione
        cookie.setPath("/");
        cookie.setMaxAge(4 * 60 * 60); // 4 ore in secondi
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
    
    /**
     * 🍪 Estrae sessionId dal cookie ADMIN_SESSION
     */
    private String extractSessionId(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if ("ADMIN_SESSION".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        
        return null;
    }
}

