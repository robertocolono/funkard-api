package com.funkard.config;

// ⚠️ LEGACY - DISABILITATO 2025-12-06
// import com.funkard.adminauth.AdminSessionFilter; // Filtro legacy commentato
import com.funkard.adminauthmodern.AdminSessionFilterModern;
import com.funkard.security.JwtFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    private final JwtFilter jwtFilter;
    // ⚠️ LEGACY - DISABILITATO 2025-12-06
    // Filtro legacy commentato (sostituito da AdminSessionFilterModern)
    // private final AdminSessionFilter adminSessionFilter;
    private final AdminSessionFilterModern adminSessionFilterModern;

    public SecurityConfig(
            JwtFilter jwtFilter, 
            // AdminSessionFilter adminSessionFilter, // LEGACY - DISABILITATO
            AdminSessionFilterModern adminSessionFilterModern) {
        this.jwtFilter = jwtFilter;
        // this.adminSessionFilter = adminSessionFilter; // LEGACY - DISABILITATO
        this.adminSessionFilterModern = adminSessionFilterModern;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 🔧 Helper: determina se la richiesta è per endpoint v2
     */
    private boolean isV2Endpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.contains("/v2/");
    }

    /**
     * 🔧 Helper: crea risposta JSON per errori 401/403
     * Formato v2: {"success": false, "data": null, "error": "..."}
     * Formato v1: {"error": "..."}
     */
    private void writeJsonErrorResponse(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       HttpStatus status, 
                                       String errorMessage) throws IOException {
        // Verifica se la risposta è già stata committata
        if (response.isCommitted()) {
            logger.warn("⚠️ Risposta già committata, impossibile convertire in JSON per: {}", request.getRequestURI());
            return;
        }
        
        // Reset del buffer per sovrascrivere eventuali contenuti esistenti
        response.resetBuffer();
        
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> errorBody;

        if (isV2Endpoint(request)) {
            // Formato v2
            errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("data", null);
            errorBody.put("error", errorMessage);
        } else {
            // Formato v1 (retrocompatibilità)
            errorBody = Map.of("error", errorMessage);
        }

        mapper.writeValue(response.getWriter(), errorBody);
        response.flushBuffer();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 🌍 Origini permesse (rimossi domini Vercel vecchi)
        config.setAllowedOrigins(List.of(
            "https://www.funkard.com",
            "https://funkard.com",
            "https://admin.funkard.com",
            "https://funkard-adminreal.vercel.app",
            "http://localhost:3000",
            "http://localhost:3002"
        ));
        
        // 🔑 Metodi e header consentiti
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-User-Id"));
        config.setExposedHeaders(List.of("Authorization", "X-User-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        // 📦 Applica a tutte le rotte
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }

    /**
     * 🔐 SecurityFilterChain per /api/admin/** (sessioni stateful)
     * @Order(1) → valutata per prima (più specifica)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🔒 Applica solo a /api/admin/**
            .securityMatcher("/api/admin/**")
            
            // 🔒 Disabilita CSRF (non serve per REST API)
            .csrf(AbstractHttpConfigurer::disable)

            // 🌐 Abilita CORS personalizzato (con supporto cookie)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ⚙️ Sessione stateful (per cookie httpOnly)
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1) // Una sessione per admin
                .maxSessionsPreventsLogin(false) // Permette login multipli (logout del vecchio)
            )

            // 🔓 Regole di accesso
            .authorizeHttpRequests(auth -> auth
                // 🔓 Endpoint pubblici admin (onboarding e login)
                .requestMatchers("/api/admin/auth/token-check").permitAll()
                .requestMatchers("/api/admin/auth/onboarding-complete").permitAll()
                .requestMatchers("/api/admin/auth/login").permitAll()
                // 🔓 Endpoint pubblici admin v2
                .requestMatchers("/api/admin/v2/auth/login").permitAll()
                
                // 🔓 Endpoint fix temporaneo (protetto da FUNKARD_CRON_SECRET nel controller)
                .requestMatchers("/api/admin/fix/onboarding-column").permitAll()
                
                // 🔓 Cron endpoints (protetti dal secret nel controller)
                .requestMatchers("/api/admin/notifications/cleanup").permitAll()
                .requestMatchers("/api/admin/support/cleanup").permitAll()
                .requestMatchers("/api/admin/maintenance/cleanup-logs").permitAll()
                .requestMatchers("/api/admin/logs/cleanup").permitAll()
                .requestMatchers("/api/admin/system/cleanup/status").permitAll()
                
                // 🔐 Tutti gli altri endpoint admin richiedono autenticazione
                .anyRequest().authenticated()
            )

            // 🔐 Aggiunge filtri sessioni admin
            // Il filtro moderno gestisce ADMIN_SESSION (cookie maiuscolo, database-backed)
            // ⚠️ Filtro legacy (admin_session, in-memory) DISABILITATO 2025-12-06
            // Ordine: moderno → UsernamePasswordAuthenticationFilter
            .addFilterBefore(adminSessionFilterModern, UsernamePasswordAuthenticationFilter.class)
            // .addFilterBefore(adminSessionFilter, UsernamePasswordAuthenticationFilter.class) // LEGACY - DISABILITATO

            // 📦 Gestione errori 401/403 in formato JSON
            // Garantisce che tutti gli endpoint admin rispondano sempre in JSON,
            // anche quando Spring Security blocca la richiesta prima del controller
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    logger.debug("🔐 AuthenticationEntryPoint chiamato per: {} - {}", 
                        request.getRequestURI(), authException.getMessage());
                    writeJsonErrorResponse(request, response, HttpStatus.UNAUTHORIZED, 
                        "Sessione non valida o scaduta");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    logger.debug("🚫 AccessDeniedHandler chiamato per: {} - {}", 
                        request.getRequestURI(), accessDeniedException.getMessage());
                    writeJsonErrorResponse(request, response, HttpStatus.FORBIDDEN, 
                        "FORBIDDEN");
                })
            )

            // ❌ Disabilita form login e basic auth HTML
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * 🔐 SecurityFilterChain default per tutto il resto (JWT stateless)
     * @Order(2) → valutata per seconda (default)
     * INVARIATA rispetto alla versione precedente
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🔒 Disabilita CSRF (non serve per REST API stateless)
            // Nota: endpoint /api/currency/refresh-rates e /api/currency/refresh-rates/test
            // sono esplicitamente esclusi dal filtro JWT e non richiedono CSRF token
            .csrf(AbstractHttpConfigurer::disable)

            // 🌐 Abilita CORS personalizzato
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ⚙️ Sessione stateless (JWT)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 🔓 Regole di accesso
            .authorizeHttpRequests(auth -> auth
                // 🔓 Endpoint pubblici
                .requestMatchers("/public/**").permitAll()
                .requestMatchers(
                    "/api/auth/**",
                    "/api/translate/**",
                    "/",
                    "/health",
                    "/api/test/**",
                    "/actuator/**"
                ).permitAll()
                
                // 🔓 Cron endpoints (protetti dal secret nel controller)
                .requestMatchers("/api/admin/notifications/cleanup").permitAll()
                .requestMatchers("/api/admin/support/cleanup").permitAll()
                .requestMatchers("/api/admin/maintenance/cleanup-logs").permitAll()
                .requestMatchers("/api/admin/logs/cleanup").permitAll()
                .requestMatchers("/api/admin/system/cleanup/status").permitAll()
                .requestMatchers("/api/valuation/refreshIncremental").permitAll()
                
                // 🔓 Endpoint validazione token admin (PUBBLICO - validazione nel controller)
                .requestMatchers("/api/admin/auth/token/**").permitAll()
                
                // 🔐 Endpoint admin richiedono autenticazione (gestiti da @PreAuthorize)
                .requestMatchers("/api/admin/**").authenticated()
                
                // 🔐 Endpoint utente richiedono autenticazione
                .requestMatchers(
                    "/api/user/**",
                    "/api/support/**",
                    "/api/usercards/**",
                    "/api/collection/**",
                    "/api/wishlist/**",
                    "/api/gradelens/**",
                    "/api/grading/**"
                ).authenticated()
                
                // 🔓 Endpoint pubblici per marketplace
                .requestMatchers(
                    "/api/listings/**",
                    "/api/cards/**",
                    "/api/products/**",
                    "/api/valuation/**",
                    "/api/trends/**",
                    "/api/ads/**"
                ).permitAll()
                
                // 🔓 Endpoint cron currency refresh-rates (protetto da Bearer token nel controller)
                .requestMatchers("/api/currency/refresh-rates").permitAll()
                
                // 🔐 Endpoint currency richiedono autenticazione
                .requestMatchers("/api/currency/**").authenticated()
                
                // 🔐 Tutti gli altri endpoint richiedono autenticazione
                .anyRequest().authenticated()
            )

            // 🔐 Aggiunge filtro JWT prima dell'autenticazione base
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // 📦 Gestione errori 401/403 in formato JSON (solo per endpoint admin)
            // Garantisce che gli endpoint admin gestiti da questa chain rispondano sempre in JSON
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    // Applica solo agli endpoint admin
                    if (request.getRequestURI() != null && request.getRequestURI().startsWith("/api/admin/")) {
                        writeJsonErrorResponse(request, response, HttpStatus.UNAUTHORIZED, 
                            "Sessione non valida o scaduta");
                    } else {
                        // Per altri endpoint, comportamento default (potrebbe essere gestito da GlobalExceptionHandler)
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write("{\"error\":\"Unauthorized\"}");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // Applica solo agli endpoint admin
                    if (request.getRequestURI() != null && request.getRequestURI().startsWith("/api/admin/")) {
                        writeJsonErrorResponse(request, response, HttpStatus.FORBIDDEN, 
                            "FORBIDDEN");
                    } else {
                        // Per altri endpoint, comportamento default
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write("{\"error\":\"Forbidden\"}");
                    }
                })
            )

            // ❌ Disabilita form login e basic auth HTML
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

}