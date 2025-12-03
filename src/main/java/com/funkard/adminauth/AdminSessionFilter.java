package com.funkard.adminauth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 🔐 Filtro per validazione sessioni admin tramite cookie httpOnly
 * Applicato solo a /api/admin/**
 * Bypass per FUNKARD_CRON_SECRET (come JwtFilter)
 * Popola SecurityContext con ruoli admin per @PreAuthorize
 */
@Component
public class AdminSessionFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AdminSessionFilter.class);
    
    private static final String ADMIN_SESSION_COOKIE = "admin_session";
    
    private final AdminSessionService sessionService;
    private final AdminUserRepository adminUserRepository;

    public AdminSessionFilter(AdminSessionService sessionService, AdminUserRepository adminUserRepository) {
        this.sessionService = sessionService;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // 🔓 Applica solo a /api/admin/**
        if (path == null || !path.startsWith("/api/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 🔓 Bypass per token cron Cloudflare (PRIMA di qualsiasi logica sessione)
        // Stessa logica di JwtFilter per garantire compatibilità
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            String cronSecret = System.getenv("FUNKARD_CRON_SECRET");
            if (cronSecret == null || cronSecret.trim().isEmpty()) {
                cronSecret = System.getProperty("FUNKARD_CRON_SECRET", "");
            }
            cronSecret = cronSecret != null ? cronSecret.trim() : "";
            String expectedCronHeader = "Bearer " + cronSecret;
            
            if (authHeader.equals(expectedCronHeader)) {
                // Bypass: cron worker autorizzato
                logger.debug("🔓 Bypass cron per: {}", path);
                filterChain.doFilter(request, response);
                return;
            }
        }
        
        // 🔓 Endpoint pubblici admin (token-check, onboarding-complete, login)
        // Non richiedono autenticazione
        if (path.startsWith("/api/admin/auth/token-check") ||
            path.startsWith("/api/admin/auth/onboarding-complete") ||
            path.startsWith("/api/admin/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 🔐 Leggi cookie admin_session
        String sessionId = extractSessionId(request);
        
        if (sessionId != null) {
            Optional<UUID> adminUserIdOpt = sessionService.validateSession(sessionId);
            
            if (adminUserIdOpt.isPresent()) {
                UUID adminUserId = adminUserIdOpt.get();
                
                // Carica AdminUser
                Optional<AdminUser> adminOpt = adminUserRepository.findById(adminUserId);
                
                if (adminOpt.isPresent()) {
                    AdminUser admin = adminOpt.get();
                    
                    // Verifica che admin sia attivo e onboarding completato
                    if (admin.isActive() && admin.isOnboardingCompleted()) {
                        // Popola SecurityContext con ruoli admin
                        setSecurityContext(admin, request);
                        
                        logger.debug("✅ Admin autenticato via sessione: {} ({})", 
                            admin.getDisplayName() != null ? admin.getDisplayName() : admin.getName(), 
                            admin.getEmail());
                    } else {
                        logger.warn("⚠️ Admin non attivo o onboarding non completato: {}", adminUserId);
                    }
                } else {
                    logger.warn("⚠️ Admin non trovato per sessionId: {}", sessionId.substring(0, 8) + "...");
                }
            } else {
                logger.debug("🔍 Sessione non valida o scaduta: {}", 
                    sessionId != null ? sessionId.substring(0, 8) + "..." : "null");
            }
        } else {
            logger.debug("🔍 Nessun cookie admin_session trovato per: {}", path);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 🍪 Estrae sessionId dal cookie admin_session
     */
    private String extractSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if (ADMIN_SESSION_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * 🔐 Popola SecurityContext con ruoli admin
     */
    private void setSecurityContext(AdminUser admin, HttpServletRequest request) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Aggiungi ruolo base basato su admin.role
        String role = admin.getRole() != null ? admin.getRole().toUpperCase() : "ADMIN";
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        
        // Se è SUPER_ADMIN, aggiungi anche ADMIN per compatibilità
        if ("SUPER_ADMIN".equals(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        // Se è SUPERVISOR, aggiungi anche ADMIN per compatibilità
        if ("SUPERVISOR".equals(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        
        // Crea UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(admin.getEmail())
                .password(admin.getPasswordHash() != null ? admin.getPasswordHash() : "")
                .authorities(authorities)
                .build();
        
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}

