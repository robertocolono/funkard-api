package com.funkard.config;

import com.funkard.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 🌍 Origini permesse (rimossi domini Vercel vecchi)
        config.setAllowedOrigins(List.of(
            "https://www.funkard.com",
            "https://funkard.com",
            "https://admin.funkard.com",
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

    @Bean
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
                
                // 🔐 Endpoint currency richiedono autenticazione
                .requestMatchers("/api/currency/**").authenticated()
                
                // 🔓 Endpoint cron currency refresh-rates (protetto da Bearer token nel controller)
                .requestMatchers("/api/currency/refresh-rates").permitAll()
                
                // 🔐 Tutti gli altri endpoint richiedono autenticazione
                .anyRequest().authenticated()
            )

            // 🔐 Aggiunge filtro JWT prima dell'autenticazione base
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // ❌ Disabilita form login e basic auth HTML
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

}