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
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔐 Filtro moderno per autenticazione admin tramite cookie ADMIN_SESSION
 * NON interferisce con AdminSessionFilter legacy
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
            // Il legacy AdminSessionFilter gestirà eventualmente admin_session
            filterChain.doFilter(request, response);
            return;
        }
        
        // Valida sessione moderna
        Optional<UUID> adminIdOpt = sessionService.validateSession(sessionId);
        
        if (adminIdOpt.isPresent()) {
            UUID adminId = adminIdOpt.get();
            
            // Carica AdminUser
            Optional<AdminUser> adminOpt = adminUserRepository.findById(adminId);
            
            if (adminOpt.isPresent()) {
                AdminUser admin = adminOpt.get();
                
                // Verifica che admin sia attivo e onboarding completato
                if (admin.isActive() && admin.isOnboardingCompleted()) {
                    // Popola SecurityContext con ruoli admin
                    setSecurityContext(admin, request);
                    
                    logger.debug("✅ Admin autenticato via sessione moderna: {} ({})", 
                        admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), 
                        admin.getEmail());
                } else {
                    logger.debug("⚠️ Admin non attivo o onboarding non completato: {}", adminId);
                }
            } else {
                logger.debug("⚠️ Admin non trovato per sessionId: {}", sessionId.substring(0, 8) + "...");
            }
        } else {
            logger.debug("🔍 Sessione moderna non valida o scaduta: {}", 
                sessionId != null && sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : "null");
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
     */
    private void setSecurityContext(AdminUser admin, HttpServletRequest request) {
        String role = "ROLE_" + admin.getRole();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            admin.getEmail(), // principal
            null, // credentials (non necessario)
            Collections.singletonList(authority) // authorities
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

