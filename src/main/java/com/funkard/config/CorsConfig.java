package com.funkard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ Domini autorizzati
        config.setAllowedOrigins(List.of(
            "https://funkard.vercel.app",
            "https://funkard-admin.vercel.app",
            "http://localhost:3000" // per sviluppo
        ));

        // ✅ Metodi consentiti
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ Header consentiti
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Admin-Token"));

        // ✅ Permette credenziali e risposte corrette
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
