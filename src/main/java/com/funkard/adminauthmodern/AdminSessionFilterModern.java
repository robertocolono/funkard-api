package com.funkard.adminauthmodern;

import com.funkard.adminauth.AdminUser;
import com.funkard.adminauth.AdminUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔐 Filtro moderno per autenticazione admin tramite cookie ADMIN_SESSION
 * Sostituisce completamente AdminSessionFilter legacy (disabilitato 2025-12-06)
 * Si attiva solo se esiste cookie ADMIN_SESSION
 */
@Component
public class AdminSessionFilterModern extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AdminSessionFilterModern.class);
    
    private final AdminSessionServiceModern sessionService;
    private final AdminUserRepository adminUserRepository;
    
    public AdminSessionFilterModern(
            AdminSessionServiceModern sessionService,
            AdminUserRepository adminUserRepository) {
        this.sessionService = sessionService;
        this.adminUserRepository = adminUserRepository;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Applica solo a /api/admin/**
        if (!path.startsWith("/api/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 🔓 Endpoint pubblici moderni (non richiedono autenticazione)
        if (path.startsWith("/api/admin/auth/login") ||
            path.startsWith("/api/admin/auth/onboarding-complete")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 🔍 Leggi cookie ADMIN_SESSION (solo se presente)
        String sessionId = extractSessionId(request);
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            // Nessun cookie ADMIN_SESSION, continua senza errori
            // Il filtro legacy AdminSessionFilter è stato disabilitato (2025-12-06)
            filterChain.doFilter(request, response);
            return;
        }
        
        // Valida sessione moderna
        logger.warn("🔍 [FILTER] Validando sessione per path: {}", path);
        logger.warn("  - sessionId estratto dal cookie: {}", sessionId != null ? (sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : sessionId) : "NULL");
        
        Optional<UUID> adminIdOpt = sessionService.validateSession(sessionId);
        
        logger.warn("  - validateSession() ritornato: {}", adminIdOpt.isPresent() ? "PRESENT" : "EMPTY");
        
        if (adminIdOpt.isPresent()) {
            UUID adminId = adminIdOpt.get();
            logger.warn("  - ✅ adminId ottenuto: {}", adminId);
            
            // Carica AdminUser
            logger.warn("  - Cercando admin nel database...");
            Optional<AdminUser> adminOpt = adminUserRepository.findById(adminId);
            
            logger.warn("  - Admin trovato nel DB?: {}", adminOpt.isPresent());
            
            if (adminOpt.isPresent()) {
                AdminUser admin = adminOpt.get();
                logger.warn("  - ✅ Admin trovato:");
                logger.warn("    - admin.id: {}", admin.getId());
                logger.warn("    - admin.email: {}", admin.getEmail());
                logger.warn("    - admin.role: {}", admin.getRole());
                logger.warn("    - admin.active: {}", admin.isActive());
                logger.warn("    - admin.onboardingCompleted: {}", admin.isOnboardingCompleted());
                
                // Verifica che admin sia attivo e onboarding completato
                boolean isActive = admin.isActive();
                boolean isOnboardingCompleted = admin.isOnboardingCompleted();
                logger.warn("  - Verificando condizioni per SecurityContext:");
                logger.warn("    - admin.isActive(): {}", isActive);
                logger.warn("    - admin.isOnboardingCompleted(): {}", isOnboardingCompleted);
                logger.warn("    - Condizione totale (active && onboardingCompleted): {}", isActive && isOnboardingCompleted);
                
                if (isActive && isOnboardingCompleted) {
                    // Popola SecurityContext con ruoli admin
                    logger.warn("  - ✅ Condizioni soddisfatte, popolando SecurityContext...");
                    setSecurityContext(admin, request);
                    
                    logger.warn("✅ [FILTER] Admin autenticato via sessione moderna: {} ({})", 
                        admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), 
                        admin.getEmail());
                } else {
                    logger.warn("⚠️ [FILTER] Admin non attivo o onboarding non completato:");
                    logger.warn("    - admin.id: {}", adminId);
                    logger.warn("    - admin.active: {}", isActive);
                    logger.warn("    - admin.onboardingCompleted: {}", isOnboardingCompleted);
                    logger.warn("    - SecurityContext NON popolato");
                }
            } else {
                logger.warn("⚠️ [FILTER] Admin non trovato per sessionId: {}", sessionId.substring(0, 8) + "...");
                logger.warn("    - adminId cercato: {}", adminId);
            }
        } else {
            logger.warn("🔍 [FILTER] Sessione moderna non valida o scaduta: {}", 
                sessionId != null && sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : "null");
            logger.warn("    - validateSession() ritornato EMPTY (verificare log validateSession)");
        }
        
        filterChain.doFilter(request, response);
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
    
    /**
     * 🔐 Popola SecurityContext con dati admin
     * 
     * Aggiunge ROLE_ADMIN per compatibilità se ruolo è SUPER_ADMIN o SUPERVISOR
     * (comportamento allineato con filtro legacy AdminSessionFilter)
     */
    private void setSecurityContext(AdminUser admin, HttpServletRequest request) {
        String role = admin.getRole();
        if (role == null || role.trim().isEmpty()) {
            logger.warn("⚠️ Admin senza ruolo: {}", admin.getEmail());
            return;
        }
        
        // Normalizza ruolo a uppercase per sicurezza
        String normalizedRole = role.toUpperCase();
        String roleAuthority = "ROLE_" + normalizedRole;
        
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(roleAuthority));
        
        // Aggiungi ROLE_ADMIN per compatibilità (come filtro legacy)
        // Garantisce che SUPER_ADMIN e SUPERVISOR abbiano anche ROLE_ADMIN
        // per compatibilità con endpoint che usano hasAnyRole('ADMIN', 'SUPER_ADMIN')
        if ("SUPER_ADMIN".equals(normalizedRole) || "SUPERVISOR".equals(normalizedRole)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            logger.debug("✅ Aggiunto ROLE_ADMIN per compatibilità (ruolo: {})", normalizedRole);
        }
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            admin.getEmail(), // principal
            null, // credentials (non necessario)
            authorities // authorities (può contenere ROLE_SUPER_ADMIN + ROLE_ADMIN)
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        logger.debug("✅ SecurityContext popolato per admin: {} con authorities: {}", 
            admin.getEmail(), authorities);
    }
}

