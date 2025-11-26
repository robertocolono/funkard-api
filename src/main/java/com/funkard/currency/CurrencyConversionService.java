package com.funkard.currency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 💱 Servizio conversione valute basato su CurrencyRateStore
 * 
 * Converte importi tra valute supportate utilizzando esclusivamente i tassi
 * salvati in CurrencyRateStore (base USD).
 * 
 * Tutti i tassi sono relativi a USD: rates.get("EUR") = quante EUR per 1 USD
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConversionService {
    
    private final CurrencyRateStore currencyRateStore;
    
    /**
     * 💱 Converte un importo da una valuta a un'altra
     * 
     * Utilizza esclusivamente i tassi salvati in CurrencyRateStore (base USD).
     * 
     * @param amount Importo da convertire
     * @param fromCurrency Valuta di origine (es. "USD")
     * @param toCurrency Valuta di destinazione (es. "EUR")
     * @return Importo convertito
     * @throws IllegalArgumentException se le valute non sono supportate o il tasso non è disponibile
     */
    public double convert(double amount, String fromCurrency, String toCurrency) {
        // Normalizza valute a uppercase
        String from = fromCurrency.toUpperCase();
        String to = toCurrency.toUpperCase();
        
        // Se stessa valuta, ritorna l'importo originale
        if (from.equals(to)) {
            return amount;
        }
        
        // Recupera tassi dalla store (base USD)
        Map<String, Double> rates = currencyRateStore.getRates();
        
        // Caso 1: from è USD → converti direttamente usando rates.get(to)
        if ("USD".equals(from)) {
            Double rate = rates.get(to);
            if (rate == null) {
                throw new IllegalArgumentException("Rate not available for currency: " + to);
            }
            double converted = amount * rate;
            log.debug("💱 Conversione: {} {} -> {} {} (rate: {})", amount, from, converted, to, rate);
            return converted;
        }
        
        // Caso 2: to è USD → converti usando il tasso inverso di rates.get(from)
        if ("USD".equals(to)) {
            Double rate = rates.get(from);
            if (rate == null) {
                throw new IllegalArgumentException("Rate not available for currency: " + from);
            }
            // Conversione inversa: da from a USD = amount / rate
            double converted = amount / rate;
            log.debug("💱 Conversione: {} {} -> {} {} (inverse rate: {})", amount, from, converted, to, rate);
            return converted;
        }
        
        // Caso 3: entrambe NON sono USD → conversione passando da USD
        // Step 1: da FROM a USD
        Double fromRate = rates.get(from);
        if (fromRate == null) {
            throw new IllegalArgumentException("Rate not available for currency: " + from);
        }
        double amountInUSD = amount / fromRate;
        
        // Step 2: da USD a TO
        Double toRate = rates.get(to);
        if (toRate == null) {
            throw new IllegalArgumentException("Rate not available for currency: " + to);
        }
        double converted = amountInUSD * toRate;
        
        log.debug("💱 Conversione: {} {} -> {} {} (via USD: {} USD, rate from: {}, rate to: {})", 
                amount, from, converted, to, amountInUSD, fromRate, toRate);
        
        return converted;
    }
}

