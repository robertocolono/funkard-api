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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🔒 Disabilita CSRF (non serve per REST API stateless)
            .csrf(AbstractHttpConfigurer::disable)

            // 🌐 Abilita CORS di default
            .cors(Customizer.withDefaults())

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

        // 👇 Log delle chiamate provenienti dal pannello admin
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