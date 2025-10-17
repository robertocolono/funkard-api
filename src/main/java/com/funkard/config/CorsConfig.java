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
        config.setAllowCredentials(true);

        // 🌍 Domini autorizzati (admin + sito principale + test locale)
        config.setAllowedOrigins(List.of(
            "https://funkard-admin.vercel.app",
            "https://www.funkard.com",
            "https://funkard.com",
            "http://localhost:3000"
        ));

        // ✅ Metodi consentiti
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ Header consentiti
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));

        // ✅ Header visibili al client
        config.setExposedHeaders(List.of("Authorization"));

        // Applica CORS a tutte le rotte
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
