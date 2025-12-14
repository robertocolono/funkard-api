package com.funkard.adminauthmodern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 🔐 Controller v2 per autenticazione admin
 * Namespace: /api/admin/v2/auth
 * Formato response: {success, data, error}
 */
@RestController
@RequestMapping("/api/admin/v2/auth")
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "https://admin.funkard.com", "http://localhost:3000", "http://localhost:3002"}, allowCredentials = "true")
public class AdminAuthControllerV2 {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthControllerV2.class);
    
    private final AdminAuthServiceModern authService;
    
    public AdminAuthControllerV2(AdminAuthServiceModern authService) {
        this.authService = authService;
    }
    
    /**
     * 🔐 POST /api/admin/v2/auth/login
     * Login admin con email e password
     * Body: { "email": "...", "password": "..." }
     * 
     * Risposta 200: { "success": true, "data": {...}, "error": null }
     * Risposta 400: { "success": false, "data": null, "error": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, 
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            Map<String, Object> result = authService.login(email, password);
            String sessionId = (String) result.get("sessionId");
            
            // Validazione sessionId
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.error("❌ SessionId null o vuoto dopo login per: {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                            "success", false,
                            "data", null,
                            "error", "Errore durante la creazione della sessione"
                        ));
            }
            
            // Crea cookie httpOnly (stesso meccanismo v1)
            ResponseCookie cookie = createSessionCookie(sessionId, httpRequest);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            
            // Validazione admin
            Object adminObj = result.get("admin");
            if (adminObj == null) {
                logger.error("❌ Admin null dopo login per: {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                            "success", false,
                            "data", null,
                            "error", "Errore durante il recupero dati admin"
                        ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", adminObj,
                "error", null
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "data", null,
                        "error", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Errore durante login v2", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "data", null,
                        "error", "Errore durante il login: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * 👤 GET /api/admin/v2/auth/me
     * Restituisce i dati dell'admin corrente
     * Legge da cookie ADMIN_SESSION
     * 
     * Risposta 200: { "success": true, "data": {...}, "error": null }
     * Risposta 401: { "success": false, "data": null, "error": "..." }
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        try {
            String sessionId = extractSessionId(request);
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                            "success", false,
                            "data", null,
                            "error", "Sessione non valida"
                        ));
            }
            
            Map<String, Object> admin = authService.getCurrentAdmin(sessionId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", admin,
                "error", null
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "data", null,
                        "error", e.getMessage()
                    ));
        } catch (Exception e) {
            logger.error("Errore durante recupero admin corrente v2", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "data", null,
                        "error", "Errore durante il recupero dati: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * 🍪 Crea cookie di sessione (stesso meccanismo v1)
     * Riusa stessa configurazione: Domain=.funkard.com, SameSite=None, Secure=true
     */
    private ResponseCookie createSessionCookie(String sessionId, HttpServletRequest request) {
        logger.debug("🍪 Cookie ADMIN_SESSION creato (v2): Domain=.funkard.com, SameSite=None, Secure=true");
        
        return ResponseCookie.from("ADMIN_SESSION", sessionId)
                .domain(".funkard.com")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(4 * 60 * 60) // 4 ore in secondi (14400)
                .sameSite("None")
                .build();
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

