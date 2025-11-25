package com.funkard.config;

import java.util.List;
import java.util.Set;

/**
 * 💱 Valute supportate da Funkard
 * 
 * Lista centralizzata di tutte le valute supportate dall'applicazione.
 * Utilizzata come "fonte della verità" per validazioni e configurazioni.
 * 
 * Supporta 38 valute basate su Stripe presentment currencies.
 */
public final class SupportedCurrencies {
    
    /**
     * 🔒 Costruttore privato per prevenire istanziazione
     */
    private SupportedCurrencies() {
        throw new UnsupportedOperationException("Classe utility - non istanziabile");
    }
    
    /**
     * ✅ Set di tutte le valute supportate (38 valute)
     * 
     * Utilizzato per validazioni rapide (contains check).
     */
    public static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "CHF", "SEK", "DKK", "NOK", "PLN", "CZK", "HUF",
        "RON", "BGN", "HRK", "RSD", "TRY", "ILS", "AED", "SAR", "CAD", "AUD",
        "NZD", "JPY", "SGD", "HKD", "MXN", "BRL", "CLP", "COP", "PEN", "ARS",
        "ZAR", "INR", "IDR", "MYR", "PHP", "THB", "KRW", "CNY"
    );
    
    /**
     * 📋 Lista ordinata di tutte le valute supportate (38 valute)
     * 
     * Utilizzato per iterazioni ordinate, messaggi di errore e validazioni dinamiche.
     * Ordine stabile e non modificabile.
     */
    public static final List<String> ORDERED = List.of(
        "USD", "EUR", "GBP", "CHF", "SEK", "DKK", "NOK", "PLN", "CZK", "HUF",
        "RON", "BGN", "HRK", "RSD", "TRY", "ILS", "AED", "SAR", "CAD", "AUD",
        "NZD", "JPY", "SGD", "HKD", "MXN", "BRL", "CLP", "COP", "PEN", "ARS",
        "ZAR", "INR", "IDR", "MYR", "PHP", "THB", "KRW", "CNY"
    );
    
    /**
     * ✅ Verifica se una valuta è supportata
     * 
     * @param currency Codice valuta (es. "EUR", "USD", "GBP")
     * @return true se la valuta è supportata, false altrimenti
     */
    public static boolean isValid(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            return false;
        }
        // Normalizza a uppercase per il confronto
        return SUPPORTED_CURRENCIES.contains(currency.trim().toUpperCase());
    }
    
    /**
     * 📋 Restituisce la lista ordinata delle valute supportate
     * 
     * Utilizzato per costruire messaggi di errore e validazioni dinamiche.
     * 
     * @return Lista non modificabile delle valute supportate nell'ordine stabile
     */
    public static List<String> getSupportedCurrenciesOrdered() {
        return ORDERED;
    }
}

