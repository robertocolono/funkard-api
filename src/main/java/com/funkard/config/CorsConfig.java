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

        // ✅ Domini consentiti
        config.setAllowedOrigins(List.of(
            "https://funkard-admin.vercel.app",
            "https://funkard.vercel.app",
            "http://localhost:3000"
        ));

        // ✅ Metodi consentiti
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ Header consentiti
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Admin-Token"));

        // ✅ Permetti credenziali e risposte corrette
        config.setAllowCredentials(true);

        // ✅ Applica a tutto
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
