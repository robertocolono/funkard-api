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

import java.util.HashMap;
import java.util.Map;

/**
 * 🔐 Controller v2 per autenticazione admin
 * Namespace: /api/admin/v2/auth
 * Formato response: {success, data, error}
 * 
 * ⚠️ IMPORTANTE: Non usare Map.of() con valori null (lancia NPE)
 * Usare buildResponse() helper per costruire response in modo safe
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
                        .body(buildErrorResponse("Errore durante la creazione della sessione"));
            }
            
            // Crea cookie httpOnly (stesso meccanismo v1)
            ResponseCookie cookie = createSessionCookie(sessionId, httpRequest);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            
            // Validazione admin
            Object adminObj = result.get("admin");
            if (adminObj == null) {
                logger.error("❌ Admin null dopo login per: {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(buildErrorResponse("Errore durante il recupero dati admin"));
            }
            
            return ResponseEntity.ok(buildSuccessResponse(adminObj));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Errore durante login v2", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Errore durante il login: " + e.getMessage()));
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
        logger.warn("🔍 [ME_V2] INIZIO GET /api/admin/v2/auth/me");
        logger.warn("  - Request URI: {}", request.getRequestURI());
        logger.warn("  - Request method: {}", request.getMethod());
        
        try {
            logger.warn("  - Estraendo sessionId dal cookie...");
            String sessionId = extractSessionId(request);
            
            logger.warn("  - sessionId estratto: {}", sessionId != null ? (sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : sessionId) : "NULL");
            logger.warn("  - sessionId null?: {}", sessionId == null);
            logger.warn("  - sessionId vuoto?: {}", sessionId != null && sessionId.trim().isEmpty());
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                logger.warn("❌ [ME_V2] RETURN 401: sessionId null o vuoto");
                logger.warn("    - Motivo: cookie ADMIN_SESSION non presente o vuoto");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(buildErrorResponse("Sessione non valida"));
            }
            
            logger.warn("  - Chiamando authService.getCurrentAdmin(sessionId)...");
            Map<String, Object> admin = authService.getCurrentAdmin(sessionId);
            
            logger.warn("✅ [ME_V2] RETURN 200: admin recuperato con successo");
            logger.warn("    - admin.id: {}", admin.get("id"));
            logger.warn("    - admin.email: {}", admin.get("email"));
            logger.warn("    - admin.role: {}", admin.get("role"));
            
            return ResponseEntity.ok(buildSuccessResponse(admin));
            
        } catch (IllegalArgumentException e) {
            logger.warn("❌ [ME_V2] RETURN 401: IllegalArgumentException");
            logger.warn("    - Exception message: {}", e.getMessage());
            logger.warn("    - Exception class: {}", e.getClass().getName());
            logger.warn("    - Stack trace:", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ [ME_V2] RETURN 500: Exception non gestita", e);
            logger.error("    - Exception class: {}", e.getClass().getName());
            logger.error("    - Exception message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Errore durante il recupero dati: " + e.getMessage()));
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
        logger.warn("  - [EXTRACT_SESSION_ID] Estraendo cookie ADMIN_SESSION");
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        
        logger.warn("    - cookies array: {}", cookies != null ? "PRESENT" : "NULL");
        logger.warn("    - cookies.length: {}", cookies != null ? cookies.length : 0);
        
        if (cookies == null) {
            logger.warn("    - ❌ cookies array è NULL");
            return null;
        }
        
        logger.warn("    - Iterando su {} cookies...", cookies.length);
        for (int i = 0; i < cookies.length; i++) {
            jakarta.servlet.http.Cookie cookie = cookies[i];
            logger.warn("    - cookie[{}]: name={}, value={}", 
                i, 
                cookie.getName(), 
                cookie.getValue() != null ? (cookie.getValue().length() > 8 ? cookie.getValue().substring(0, 8) + "..." : cookie.getValue()) : "NULL");
            
            if ("ADMIN_SESSION".equals(cookie.getName())) {
                String sessionId = cookie.getValue();
                logger.warn("    - ✅ Cookie ADMIN_SESSION trovato: {}", 
                    sessionId != null ? (sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : sessionId) : "NULL");
                return sessionId;
            }
        }
        
        logger.warn("    - ❌ Cookie ADMIN_SESSION non trovato tra {} cookies", cookies.length);
        return null;
    }
    
    /**
     * 🔧 Helper: costruisce response v2 di successo in modo safe
     * Non include "error" se null (Map.of() non accetta null)
     * @param data Dati da includere nella response
     * @return Map con formato {success: true, data: ...}
     */
    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        // Non inserire "error" se null (Map.of() non accetta null)
        return response;
    }
    
    /**
     * 🔧 Helper: costruisce response v2 di errore in modo safe
     * @param error Messaggio di errore
     * @return Map con formato {success: false, data: null, error: ...}
     */
    private Map<String, Object> buildErrorResponse(String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("data", null);
        response.put("error", error);
        return response;
    }
}

