package com.funkard.config;

import com.funkard.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
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
        
        // 🌍 Origini permesse
        config.setAllowedOrigins(List.of(
            "https://www.funkard.com",
            "https://funkard.com",
            "http://localhost:3000",
            "http://localhost:3002"
        ));
        
        // 🔑 Metodi e header consentiti
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-User-Id", "X-Admin-Token"));
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
            .csrf(AbstractHttpConfigurer::disable)

            // 🌐 Abilita CORS personalizzato
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ⚙️ Sessione stateless (JWT)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 🔓 Regole di accesso
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/api/listings/**",
                    "/api/cards/**",
                    "/api/usercards/**",
                    "/api/collection/**",
                    "/api/gradelens/**",
                    "/api/collection/**",
                    "/api/admin/**",
                    "/api/user/payments/**",
                    "/api/user/me",
                    "/api/user/address/**",
                    "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )

            // 🔐 Aggiunge filtro JWT prima dell'autenticazione base
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // ❌ Disabilita form login e basic auth HTML
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        // 👇 Log delle chiamate provenienti dal pannello admin (dopo CORS)
        http.addFilterAfter((servletRequest, response, chain) -> {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String origin = request.getHeader("Origin");
            if (origin != null && (origin.contains("funkard.com") || origin.contains("localhost"))) {
                System.out.println("✅ Request from Funkard detected: " + request.getRequestURI());
            }
            chain.doFilter(servletRequest, response);
        }, org.springframework.web.filter.CorsFilter.class);

        return http.build();
    }

}