package com.funkard.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 🗄️ Configurazione Cache Caffeine
 * 
 * Abilita cache per metodi pubblici read-only (homepage, marketplace, reference data).
 * Configurazione ottimizzata per ridurre carico database senza impattare performance.
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * 🔧 Configura CacheManager con Caffeine
     * 
     * Impostazioni:
     * - expireAfterWrite: 25 secondi (TTL per invalidazione automatica)
     * - maximumSize: 500 entry (limite memoria)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(25, TimeUnit.SECONDS)
            .maximumSize(500)
            .recordStats() // Abilita statistiche per monitoraggio
        );
        
        return cacheManager;
    }
}

