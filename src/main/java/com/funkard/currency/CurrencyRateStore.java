package com.funkard.currency;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 💱 Store in-memory per tassi di cambio USD → valuta
 * 
 * Conserva la mappa dei tassi di cambio e il timestamp dell'ultimo aggiornamento.
 * Utilizzato per memorizzare i tassi recuperati da API esterna (gestita altrove).
 * 
 * Thread-safe per accessi concorrenti.
 */
@Service
public class CurrencyRateStore {
    
    /**
     * 💾 Mappa dei tassi di cambio: USD → valuta
     * 
     * Chiave: codice valuta (es. "EUR", "GBP", "JPY")
     * Valore: tasso di cambio rispetto a USD (es. 0.85 per EUR significa 1 USD = 0.85 EUR)
     */
    private final Map<String, Double> rates = new ConcurrentHashMap<>();
    
    /**
     * ⏰ Timestamp ultimo aggiornamento tassi
     * 
     * null se i tassi non sono mai stati aggiornati.
     */
    private volatile Instant lastUpdated;
    
    /**
     * 💾 Aggiorna la mappa dei tassi di cambio
     * 
     * Sostituisce completamente la mappa esistente con la nuova.
     * Aggiorna il timestamp dell'ultimo aggiornamento.
     * 
     * @param newRates Mappa dei nuovi tassi (USD → valuta)
     *                 Deve contenere valori già validi (nessuna validazione qui)
     */
    public void updateRates(Map<String, Double> newRates) {
        if (newRates == null) {
            throw new IllegalArgumentException("Rates map cannot be null");
        }
        
        // Sostituisce completamente la mappa esistente
        this.rates.clear();
        this.rates.putAll(newRates);
        
        // Aggiorna timestamp
        this.lastUpdated = Instant.now();
    }
    
    /**
     * 📊 Recupera la mappa corrente dei tassi di cambio
     * 
     * @return Mappa non modificabile dei tassi (USD → valuta)
     * @throws IllegalStateException se i tassi non sono ancora disponibili
     */
    public Map<String, Double> getRates() {
        if (rates.isEmpty()) {
            throw new IllegalStateException("Exchange rates not available yet");
        }
        
        // Restituisce vista non modificabile per sicurezza
        return Collections.unmodifiableMap(rates);
    }
    
    /**
     * ⏰ Recupera il timestamp dell'ultimo aggiornamento
     * 
     * @return Timestamp dell'ultimo aggiornamento, o null se mai aggiornato
     */
    public Instant getLastUpdated() {
        return lastUpdated;
    }
}

