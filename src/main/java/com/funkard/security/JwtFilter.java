package com.funkard.security;

import com.funkard.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.stream.Collectors;

/**
 * 🔐 Filtro JWT per autenticazione e autorizzazione
 * Gestisce ruoli ADMIN e SUPER_ADMIN per @PreAuthorize
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Esclusione per endpoint currency refresh (autenticato con Bearer token dedicato)
        if ("/api/currency/refresh-rates".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                email = jwtUtil.getEmailFromToken(token);
            } catch (Exception e) {
                logger.warn("JWT token extraction failed: " + e.getMessage());
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var user = userRepository.findByEmail(email);
            if (user != null && user.getVerified()) {
                // 🗑️ Blocca accesso se account è in cancellazione
                if (Boolean.TRUE.equals(user.getDeletionPending())) {
                    logger.warn("❌ Tentativo di accesso da account in cancellazione: " + 
                        user.getEmail() + " (" + request.getRequestURI() + ")");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Account in cancellazione. Accesso negato.\"}");
                    return;
                }
                // 🔑 Costruisci authorities basate sul ruolo
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                
                // Aggiungi ruolo base
                String role = user.getRole() != null ? user.getRole().toUpperCase() : "USER";
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                
                // Se è ADMIN, aggiungi anche USER per compatibilità
                if ("ADMIN".equals(role) || "SUPER_ADMIN".equals(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
                
                // Se è SUPER_ADMIN, aggiungi anche ADMIN
                if ("SUPER_ADMIN".equals(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                // Create UserDetails con authorities
                UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .authorities(authorities)
                        .build();

                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                // 📝 Log autenticazione
                logger.info("✅ Authenticated request by " + user.getEmail() + 
                    " (" + role + ") to " + request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }
}