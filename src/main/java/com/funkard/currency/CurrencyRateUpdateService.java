package com.funkard.currency;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.funkard.config.SupportedCurrencies;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 💱 Servizio per aggiornamento tassi di cambio da API esterna
 * 
 * Recupera i tassi di cambio da ExchangeRate-API (base USD) e li salva in CurrencyRateStore.
 * Filtra solo le valute supportate da SupportedCurrencies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyRateUpdateService {
    
    private static final String API_BASE_URL = "https://open.er-api.com/v6/latest/USD";
    
    private final CurrencyRateStore currencyRateStore;
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 🔄 Aggiorna i tassi di cambio da API esterna
     * 
     * Chiama l'API ExchangeRate-API con base USD, filtra le valute supportate
     * e salva la mappa filtrata in CurrencyRateStore.
     * 
     * In caso di errore, logga l'errore e non aggiorna la store.
     */
    public void updateRates() {
        try {
            log.info("📥 Fetching exchange rates from API: {}", API_BASE_URL);
            
            // Chiama API esterna
            ExchangeRateResponse response = restTemplate.getForObject(API_BASE_URL, ExchangeRateResponse.class);
            
            // Verifica che il responso sia valido
            if (response == null || response.getRates() == null || response.getRates().isEmpty()) {
                log.error("❌ Risposta API non valida o vuota");
                return;
            }
            
            // Ottiene la mappa "rates" dal JSON
            Map<String, Double> allRates = response.getRates();
            
            // Filtra la mappa tenendo solo le valute presenti in SupportedCurrencies
            Map<String, Double> filteredRates = allRates.entrySet().stream()
                    .filter(entry -> SupportedCurrencies.SUPPORTED_CURRENCIES.contains(entry.getKey()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
            
            log.info("✅ Filtro completato: {} valute supportate su {} totali", 
                    filteredRates.size(), allRates.size());
            
            // Passa la mappa filtrata a currencyRateStore
            currencyRateStore.updateRates(filteredRates);
            
            log.info("✅ Tassi di cambio aggiornati con successo. Valute: {}", filteredRates.keySet());
            
        } catch (RestClientException e) {
            log.error("❌ Errore durante chiamata API ExchangeRate: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Errore imprevisto durante aggiornamento tassi: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 📊 DTO per risposta ExchangeRate-API
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ExchangeRateResponse {
        @JsonProperty("result")
        private String result;
        
        @JsonProperty("base_code")
        private String baseCode;
        
        @JsonProperty("rates")
        private Map<String, Double> rates;
    }
}

