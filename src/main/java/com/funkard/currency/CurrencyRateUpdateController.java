package com.funkard.currency;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 💱 Controller REST per aggiornamento tassi di cambio
 * 
 * Espone endpoint per aggiornare manualmente i tassi di cambio da API esterna.
 * Utilizzato da Cloudflare Cron per aggiornamento periodico.
 */
@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002",
    "https://funkard-currency-updater.roberto-colono17.workers.dev"
}, allowCredentials = "true")
public class CurrencyRateUpdateController {
    
    private final CurrencyRateUpdateService currencyRateUpdateService;
    private final CurrencyRateStore currencyRateStore;
    
    /**
     * 🔄 POST /api/currency/refresh-rates
     * Aggiorna i tassi di cambio da API esterna
     * 
     * Chiama CurrencyRateUpdateService per recuperare e salvare i nuovi tassi.
     * Restituisce informazioni sull'aggiornamento completato.
     * 
     * Autenticazione: Bearer FUNKARD_CRON_SECRET_CURRENCY (solo Cloudflare Worker)
     * 
     * @param authHeader Header Authorization con Bearer token
     * @return ResponseEntity con stato aggiornamento e dettagli
     */
    @PostMapping("/refresh-rates")
    public ResponseEntity<?> refreshRates(@RequestHeader("Authorization") String authHeader) {
        // Verifica autenticazione Bearer token
        // Leggi variabile d'ambiente con fallback a system property
        String secretValue = System.getenv("FUNKARD_CRON_SECRET_CURRENCY");
        if (secretValue == null || secretValue.trim().isEmpty()) {
            secretValue = System.getProperty("FUNKARD_CRON_SECRET_CURRENCY", "");
        }
        secretValue = secretValue != null ? secretValue.trim() : "";
        String expected = "Bearer " + secretValue;
        
        // Log di debug per verificare valori letti
        log.warn("🔍 [DEBUG] FUNKARD_CRON_SECRET_CURRENCY letto: {}", secretValue);
        log.warn("🔍 [DEBUG] Authorization header ricevuto: {}", authHeader);
        
        if (authHeader == null || !expected.equals(authHeader)) {
            log.warn("❌ Tentativo di accesso non autorizzato a /api/currency/refresh-rates");
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "error", "Unauthorized",
                "message", "Invalid or missing Bearer token"
            ));
        }
        
        try {
            log.info("🔄 Richiesta aggiornamento tassi di cambio");
            
            // Chiama il servizio per aggiornare i tassi
            currencyRateUpdateService.updateRates();
            
            // Recupera informazioni dallo store dopo l'aggiornamento
            Instant lastUpdated = currencyRateStore.getLastUpdated();
            Map<String, Double> rates = currencyRateStore.getRates();
            
            // Costruisci risposta di successo
            CurrencyRefreshResponse response = new CurrencyRefreshResponse();
            response.setSuccess(true);
            response.setMessage("Rates updated successfully");
            response.setLastUpdated(lastUpdated);
            response.setCurrenciesCount(rates.size());
            
            log.info("✅ Tassi di cambio aggiornati con successo. Valute: {}", rates.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalStateException e) {
            // Store vuoto (tassi non disponibili)
            log.error("❌ Tassi non disponibili dopo aggiornamento: {}", e.getMessage());
            
            CurrencyRefreshResponse response = new CurrencyRefreshResponse();
            response.setSuccess(false);
            response.setMessage("Failed to update rates: " + e.getMessage());
            response.setLastUpdated(null);
            response.setCurrenciesCount(0);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        } catch (Exception e) {
            // Errore generico
            log.error("❌ Errore durante aggiornamento tassi: {}", e.getMessage(), e);
            
            CurrencyRefreshResponse response = new CurrencyRefreshResponse();
            response.setSuccess(false);
            response.setMessage("Failed to update rates");
            response.setLastUpdated(null);
            response.setCurrenciesCount(0);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 📊 DTO per risposta aggiornamento tassi
     */
    @Data
    public static class CurrencyRefreshResponse {
        private boolean success;
        private String message;
        private Instant lastUpdated;
        private int currenciesCount;
    }
}

