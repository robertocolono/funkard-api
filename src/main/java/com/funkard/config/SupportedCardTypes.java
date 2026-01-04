package com.funkard.config;

import java.util.List;
import java.util.Set;

/**
 * 📦 Tipi di Card supportati nel Marketplace Funkard
 * 
 * Lista centralizzata di tutti i tipi di Card supportati.
 * Utilizzata come "fonte della verità" per validazioni e configurazioni.
 * 
 * Dominio chiuso: valori ammessi sono limitati e non estendibili senza modifica backend.
 */
public final class SupportedCardTypes {
    
    /**
     * 🔒 Costruttore privato per prevenire istanziazione
     */
    private SupportedCardTypes() {
        throw new UnsupportedOperationException("Classe utility - non istanziabile");
    }
    
    /**
     * ✅ Set di tutti i tipi supportati (7 tipi)
     * 
     * Utilizzato per validazioni rapide (contains check).
     */
    public static final Set<String> SUPPORTED_TYPES = Set.of(
        "SINGLE_CARD",
        "SEALED_BOX",
        "BOOSTER_PACK",
        "STARTER_DECK",
        "COMPLETE_SET",
        "PROMO",
        "ACCESSORY"
    );
    
    /**
     * 📋 Lista ordinata di tutti i tipi supportati (7 tipi)
     * 
     * Utilizzato per iterazioni ordinate, messaggi di errore e validazioni dinamiche.
     * Ordine stabile e non modificabile.
     */
    public static final List<String> ORDERED = List.of(
        "SINGLE_CARD",
        "SEALED_BOX",
        "BOOSTER_PACK",
        "STARTER_DECK",
        "COMPLETE_SET",
        "PROMO",
        "ACCESSORY"
    );
    
    /**
     * ✅ Verifica se un tipo è supportato
     * 
     * @param type Tipo di Card (es. "SINGLE_CARD", "SEALED_BOX")
     * @return true se il tipo è supportato, false altrimenti
     */
    public static boolean isValid(String type) {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }
        // Normalizza a uppercase per il confronto
        return SUPPORTED_TYPES.contains(type.trim().toUpperCase());
    }
    
    /**
     * 📋 Restituisce la lista ordinata dei tipi supportati
     * 
     * Utilizzato per costruire messaggi di errore e validazioni dinamiche.
     * 
     * @return Lista non modificabile dei tipi supportati nell'ordine stabile
     */
    public static List<String> getSupportedTypesOrdered() {
        return ORDERED;
    }
    
    /**
     * 📋 Restituisce stringa formattata per messaggi di errore
     * 
     * @return Stringa con valori separati da virgola (es. "SINGLE_CARD, SEALED_BOX, ...")
     */
    public static String getSupportedTypesAsString() {
        return String.join(", ", ORDERED);
    }
}

