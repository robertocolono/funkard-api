package com.funkard.adminauthmodern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
                        .body(Map.of("error", "Errore durante la creazione della sessione"));
            }
            
            // Crea cookie httpOnly con SameSite corretto
            ResponseCookie cookie = createSessionCookie(sessionId, httpRequest);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            
            // Validazione admin
            Object adminObj = result.get("admin");
            if (adminObj == null) {
                logger.error("❌ Admin null dopo login per: {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Errore durante il recupero dati admin"));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "admin", adminObj
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
            
            // Rimuovi cookie con stessa configurazione di creazione (domain, secure, sameSite)
            logger.debug("🍪 Cookie ADMIN_SESSION rimosso: Domain=.funkard.com, SameSite=None, Secure=true");
            
            ResponseCookie cookie = ResponseCookie.from("ADMIN_SESSION", "")
                    .domain(".funkard.com")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0) // Elimina cookie
                    .sameSite("None")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            
            return ResponseEntity.ok(Map.of("success", true));
            
        } catch (Exception e) {
            logger.error("Errore durante logout moderno", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Errore durante il logout: " + e.getMessage()));
        }
    }
    
    /**
     * 🍪 Crea cookie di sessione con dominio condiviso per tutti i sottodomini funkard.com
     * Usa ResponseCookie di Spring Framework per supporto SameSite completo
     * 
     * Configurazione:
     * - Domain: .funkard.com (condiviso tra admin.funkard.com, funkard.com, api.funkard.com)
     * - SameSite: None (per cross-site requests)
     * - Secure: true (obbligatorio con SameSite=None)
     * - HttpOnly: true (protezione XSS)
     * - Path: / (disponibile su tutto il dominio)
     * - Max-Age: 14400 secondi (4 ore)
     */
    private ResponseCookie createSessionCookie(String sessionId, HttpServletRequest request) {
        logger.debug("🍪 Cookie ADMIN_SESSION creato: Domain=.funkard.com, SameSite=None, Secure=true");
        
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
     * 🔍 Rileva se la richiesta è cross-site confrontando Origin header con server origin
     * 
     * @param request HttpServletRequest
     * @param origin Origin header (può essere null)
     * @return true se cross-site, false se same-site
     */
    private boolean isCrossSiteRequest(HttpServletRequest request, String origin) {
        // Se Origin è assente, assume cross-site per sicurezza
        if (origin == null || origin.trim().isEmpty()) {
            logger.debug("🔍 Origin header assente, assume cross-site per sicurezza");
            return true;
        }
        
        // Costruisci server origin considerando X-Forwarded headers (proxy-aware)
        String serverScheme = getServerScheme(request); // Usa X-Forwarded-Proto se disponibile
        String serverHost = getServerHost(request); // Usa X-Forwarded-Host se disponibile
        String serverOrigin = serverScheme + "://" + serverHost;
        
        // Normalizza origin e serverOrigin per confronto:
        // 1. Rimuovi trailing slash
        // 2. Normalizza porte (rimuovi porta 80 per http, 443 per https)
        String normalizedOrigin = normalizeOrigin(origin);
        String normalizedServerOrigin = normalizeOrigin(serverOrigin);
        
        // Confronta: se diversi → cross-site
        boolean isCrossSite = !normalizedOrigin.equalsIgnoreCase(normalizedServerOrigin);
        
        logger.debug("🔍 Cross-site detection: Origin={}, ServerOrigin={}, isCrossSite={}", 
            normalizedOrigin, normalizedServerOrigin, isCrossSite);
        
        return isCrossSite;
    }
    
    /**
     * 🔍 Recupera scheme del server considerando X-Forwarded-Proto (proxy-aware)
     * 
     * @param request HttpServletRequest
     * @return scheme (http o https)
     */
    private String getServerScheme(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null && !forwardedProto.trim().isEmpty()) {
            return forwardedProto.trim().toLowerCase();
        }
        return request.getScheme();
    }
    
    /**
     * 🔍 Recupera host del server considerando X-Forwarded-Host (proxy-aware)
     * 
     * @param request HttpServletRequest
     * @return hostname del server
     */
    private String getServerHost(HttpServletRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.trim().isEmpty()) {
            // X-Forwarded-Host può includere porta, estrai solo hostname
            String host = forwardedHost.split(":")[0];
            return host.trim();
        }
        return request.getServerName();
    }
    
    /**
     * 🔍 Normalizza origin rimuovendo trailing slash e porte default
     * 
     * @param origin Origin da normalizzare
     * @return Origin normalizzato
     */
    private String normalizeOrigin(String origin) {
        if (origin == null || origin.trim().isEmpty()) {
            return origin;
        }
        
        // Rimuovi trailing slash
        String normalized = origin.endsWith("/") 
            ? origin.substring(0, origin.length() - 1) 
            : origin;
        
        // Normalizza porte default: rimuovi :80 per http, :443 per https
        if (normalized.startsWith("http://") && normalized.contains(":80")) {
            normalized = normalized.replace(":80", "");
        } else if (normalized.startsWith("https://") && normalized.contains(":443")) {
            normalized = normalized.replace(":443", "");
        }
        
        return normalized;
    }
    
    /**
     * 🔍 Verifica se la richiesta arriva da localhost
     * 
     * ⚠️ DEPRECATO per logica cookie: Non più usato in createSessionCookie() e logout()
     * perché inaffidabile quando il backend gira su Render (serverName non è mai localhost).
     * Ora si usa Origin header per rilevare localhost.
     * 
     * Mantenuto per compatibilità o uso futuro in altri contesti.
     */
    @SuppressWarnings("unused")
    private boolean isLocalhostRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        
        String remoteAddr = request.getRemoteAddr();
        String serverName = request.getServerName();
        
        // Controlla IP localhost (IPv4 e IPv6)
        boolean isLocalhostIP = "127.0.0.1".equals(remoteAddr) 
                               || "::1".equals(remoteAddr)
                               || "0:0:0:0:0:0:0:1".equals(remoteAddr);
        
        // Controlla hostname localhost
        boolean isLocalhostHost = "localhost".equalsIgnoreCase(serverName)
                                || (serverName != null && serverName.startsWith("localhost"));
        
        return isLocalhostIP || isLocalhostHost;
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

