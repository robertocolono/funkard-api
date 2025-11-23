package com.funkard.api.i18n;

import java.util.List;
import java.util.Set;

/**
 * 🌍 Lingue supportate da Funkard
 * 
 * Lista centralizzata di tutte le lingue supportate dall'applicazione.
 * Utilizzata come "fonte della verità" per validazioni e configurazioni.
 */
public final class SupportedLanguages {
    
    /**
     * 🔒 Costruttore privato per prevenire istanziazione
     */
    private SupportedLanguages() {
        throw new UnsupportedOperationException("Classe utility - non istanziabile");
    }
    
    /**
     * ✅ Set di tutte le lingue supportate (31 lingue)
     * 
     * Utilizzato per validazioni rapide (contains check).
     */
    public static final Set<String> ALL = Set.of(
        "en", "it", "es", "fr", "de", "pt", "ja", "zh", "ru",
        "ar", "hi", "ko", "tr", "id", "vi", "bn", "tl", "pl", "nl", "sv", "no", "da",
        "el", "cs", "hu", "ro", "uk", "th", "ms", "fa", "sq"
    );
    
    /**
     * 📋 Lista ordinata di tutte le lingue supportate (31 lingue)
     * 
     * Utilizzato per iterazioni ordinate e visualizzazioni UI.
     */
    public static final List<String> ORDERED = List.of(
        "en", "it", "es", "fr", "de", "pt", "ja", "zh", "ru",
        "ar", "hi", "ko", "tr", "id", "vi", "bn", "tl", "pl", "nl", "sv", "no", "da",
        "el", "cs", "hu", "ro", "uk", "th", "ms", "fa", "sq"
    );
}

