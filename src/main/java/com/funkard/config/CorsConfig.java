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

        // ✅ domini consentiti (pannello admin + sito principale)
        config.setAllowedOrigins(List.of(
            "https://funkard-admin.vercel.app",
            "https://funkard.vercel.app",
            "http://localhost:3000"
        ));

        // ✅ metodi consentiti
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ header consentiti
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // ✅ abilita credenziali e risposte corrette
        config.setAllowCredentials(true);

        // ✅ applica a tutte le rotte
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
