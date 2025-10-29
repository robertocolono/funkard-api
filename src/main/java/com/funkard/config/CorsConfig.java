package com.funkard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
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

        return new CorsFilter(source);
    }
}
