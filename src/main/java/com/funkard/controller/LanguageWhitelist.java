package com.funkard.controller;

import java.util.Set;

/**
 * ✅ Whitelist lingue supportate per validazione input utente
 * 
 * Lista isolata e sicura delle 31 lingue ufficiali supportate dal frontend.
 * Utilizzata esclusivamente per validazione input nei controller.
 */
public final class LanguageWhitelist {
    
    /**
     * 🔒 Costruttore privato per prevenire istanziazione
     */
    private LanguageWhitelist() {
        throw new UnsupportedOperationException("Classe utility - non istanziabile");
    }
    
    /**
     * ✅ Set di lingue supportate (31 lingue ufficiali frontend)
     * 
     * Utilizzato per validazione input utente nei controller.
     */
    public static final Set<String> SUPPORTED = Set.of(
        "en", "it", "es", "fr", "de", "pt", "ja", "zh", "ru",
        "ar", "hi", "ko", "tr", "id", "vi", "bn", "tl", "pl",
        "nl", "sv", "no", "da", "el", "cs", "hu", "ro", "uk",
        "th", "ms", "fa", "sq"
    );
    
    /**
     * ✅ Verifica se una lingua è supportata
     * 
     * @param language Codice lingua (es. "en", "it", "es")
     * @return true se la lingua è supportata, false altrimenti
     */
    public static boolean isValid(String language) {
        if (language == null || language.trim().isEmpty()) {
            return false;
        }
        return SUPPORTED.contains(language.trim().toLowerCase());
    }
}

