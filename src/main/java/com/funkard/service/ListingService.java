package com.funkard.service;

import com.funkard.config.SupportedCardTypes;
import com.funkard.dto.CreateListingRequest;
import com.funkard.model.Card;
import com.funkard.model.Listing;
import com.funkard.model.PendingValue;
import com.funkard.repository.CardRepository;
import com.funkard.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {
    private final ListingRepository repo;
    private final CardRepository cardRepository;
    private final PendingValueService pendingValueService;

    public List<Listing> getAll() {
        return repo.findAll();
    }

    /**
     * 🔍 Trova listing filtrati per category della Card associata
     * @param category Categoria da filtrare (TCG, SPORT, ENTERTAINMENT, VINTAGE)
     * @return Lista di listing con Card.category = category
     * @throws IllegalArgumentException se category non è valida
     */
    public List<Listing> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category non può essere vuota");
        }
        
        // Normalizza a uppercase per case-insensitive
        String normalizedCategory = category.trim().toUpperCase();
        
        // Valida valori ammessi
        if (!isValidCategory(normalizedCategory)) {
            throw new IllegalArgumentException("Categoria non valida: " + category + 
                ". Valori ammessi: TCG, SPORT, ENTERTAINMENT, VINTAGE");
        }
        
        return repo.findByCardCategory(normalizedCategory);
    }

    /**
     * ✅ Valida se category è uno dei valori ammessi
     */
    private boolean isValidCategory(String category) {
        return "TCG".equals(category) || 
               "SPORT".equals(category) || 
               "ENTERTAINMENT".equals(category) || 
               "VINTAGE".equals(category);
    }

    /**
     * 🔍 Trova listing filtrati per type della Card associata
     * @param type Tipo da filtrare (SINGLE_CARD, SEALED_BOX, BOOSTER_PACK, CASE, BOX, STARTER_DECK, COMPLETE_SET, PROMO, ACCESSORY)
     * @return Lista di listing con Card.type = type
     * @throws IllegalArgumentException se type non è valido
     * 
     * Note: SEALED_BOX è accettato come legacy per retrocompatibilità.
     */
    public List<Listing> findByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type non può essere vuoto");
        }
        
        // Normalizza a uppercase per case-insensitive
        String normalizedType = type.trim().toUpperCase();
        
        // Valida valori ammessi
        if (!SupportedCardTypes.isValid(normalizedType)) {
            throw new IllegalArgumentException("Tipo non valido: " + type + 
                ". Valori ammessi: " + SupportedCardTypes.getSupportedTypesAsString());
        }
        
        return repo.findByCardType(normalizedType);
    }

    /**
     * 🔍 Trova listing filtrati per category e type della Card associata
     * @param category Categoria da filtrare (TCG, SPORT, ENTERTAINMENT, VINTAGE)
     * @param type Tipo da filtrare (SINGLE_CARD, SEALED_BOX, BOOSTER_PACK, CASE, BOX, STARTER_DECK, COMPLETE_SET, PROMO, ACCESSORY)
     * @return Lista di listing con Card.category = category AND Card.type = type
     * @throws IllegalArgumentException se category o type non sono validi
     * 
     * Note: SEALED_BOX è accettato come legacy per retrocompatibilità.
     */
    public List<Listing> findByCategoryAndType(String category, String type) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category non può essere vuota");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type non può essere vuoto");
        }
        
        // Normalizza a uppercase per case-insensitive
        String normalizedCategory = category.trim().toUpperCase();
        String normalizedType = type.trim().toUpperCase();
        
        // Valida valori ammessi
        if (!isValidCategory(normalizedCategory)) {
            throw new IllegalArgumentException("Categoria non valida: " + category + 
                ". Valori ammessi: TCG, SPORT, ENTERTAINMENT, VINTAGE");
        }
        if (!SupportedCardTypes.isValid(normalizedType)) {
            throw new IllegalArgumentException("Tipo non valido: " + type + 
                ". Valori ammessi: " + SupportedCardTypes.getSupportedTypesAsString());
        }
        
        return repo.findByCardCategoryAndType(normalizedCategory, normalizedType);
    }

    /**
     * 🔍 Trova listing filtrati per condition
     * @param condition Condizione da filtrare (RAW, MINT, NEAR_MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR, POOR, SEALED)
     * @return Lista di listing con Listing.condition = condition
     * @throws IllegalArgumentException se condition non è valida
     */
    public List<Listing> findByCondition(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition non può essere vuota");
        }
        
        // Normalizza a uppercase per case-insensitive
        String normalizedCondition = condition.trim().toUpperCase();
        
        // Valida valori ammessi
        if (!isValidCondition(normalizedCondition)) {
            throw new IllegalArgumentException("Condizione non valida: " + condition + 
                ". Valori ammessi: RAW, MINT, NEAR_MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR, POOR, SEALED");
        }
        
        return repo.findByCondition(normalizedCondition);
    }

    /**
     * ✅ Valida se condition è uno dei valori ammessi
     */
    private boolean isValidCondition(String condition) {
        return "RAW".equals(condition) ||
               "MINT".equals(condition) ||
               "NEAR_MINT".equals(condition) ||
               "EXCELLENT".equals(condition) ||
               "VERY_GOOD".equals(condition) ||
               "GOOD".equals(condition) ||
               "FAIR".equals(condition) ||
               "POOR".equals(condition) ||
               "SEALED".equals(condition);
        // ⚠️ PLAYED è ESPLICITAMENTE ESCLUSO
    }

    /**
     * 🔍 Trova listing filtrati per category e condition
     * @param category Categoria da filtrare (TCG, SPORT, ENTERTAINMENT, VINTAGE)
     * @param condition Condizione da filtrare (RAW, MINT, NEAR_MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR, POOR, SEALED)
     * @return Lista di listing con Card.category = category AND Listing.condition = condition
     * @throws IllegalArgumentException se category o condition non sono validi
     */
    public List<Listing> findByCategoryAndCondition(String category, String condition) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category non può essere vuota");
        }
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition non può essere vuota");
        }
        
        // Normalizza a uppercase per case-insensitive
        String normalizedCategory = category.trim().toUpperCase();
        String normalizedCondition = condition.trim().toUpperCase();
        
        // Valida valori ammessi
        if (!isValidCategory(normalizedCategory)) {
            throw new IllegalArgumentException("Categoria non valida: " + category + 
                ". Valori ammessi: TCG, SPORT, ENTERTAINMENT, VINTAGE");
        }
        if (!isValidCondition(normalizedCondition)) {
            throw new IllegalArgumentException("Condizione non valida: " + condition + 
                ". Valori ammessi: RAW, MINT, NEAR_MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR, POOR");
        }
        
        return repo.findByCardCategoryAndCondition(normalizedCategory, normalizedCondition);
    }

    /**
     * 🔍 Trova listing filtrati per type e condition
     * @param type Tipo da filtrare (SINGLE_CARD, SEALED_BOX, BOOSTER_PACK, CASE, BOX, STARTER_DECK, COMPLETE_SET, PROMO, ACCESSORY)
     * @param condition Condizione da filtrare (RAW, MINT, NEAR_MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR, POOR, SEALED)
     * @return Lista di listing con Card.type = type AND Listing.condition = condition
     * @throws IllegalArgumentException se type o condition non sono validi
     */
    public List<Listing> findByTypeAndCondition(String type, String condition) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type non può essere vuoto");
        }
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition non può essere vuota");
        }
        
        // Normalizza a uppercase per case-insensitive
        String normalizedType = type.trim().toUpperCase();
        String normalizedCondition = condition.trim().toUpperCase();
        
        // Valida valori ammessi
        if (!SupportedCardTypes.isValid(normalizedType)) {
            throw new IllegalArgumentException("Tipo non valido: " + type + 
                ". Valori ammessi: " + SupportedCardTypes.getSupportedTypesAsString());
        }
        if (!isValidCondition(normalizedCondition)) {
            throw new IllegalArgumentException("Condizione non valida: " + condition + 
                ". Valori ammessi: RAW, MINT, NEAR_MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR, POOR");
        }
        
        return repo.findByCardTypeAndCondition(normalizedType, normalizedCondition);
    }

    /**
     * 🔍 Trova listing filtrati per category, type e condition
     * @param category Categoria da filtrare (TCG, SPORT, ENTERTAINMENT, VINTAGE)
     * @param type Tipo da filtrare (SINGLE_CARD, SEALED_BOX, BOOSTER_PACK, CASE, BOX, STARTER_DECK, COMPLETE_SET, PROMO, ACCESSORY)
     * @param condition Condizione da filtrare (RAW, MINT, NEAR_MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR, POOR, SEALED)
     * @return Lista di listing con Card.category = category AND Card.type = type AND Listing.condition = condition
     * @throws IllegalArgumentException se category, type o condition non sono validi
     */
    public List<Listing> findByCategoryAndTypeAndCondition(String category, String type, String condition) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category non può essere vuota");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type non può essere vuoto");
        }
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition non può essere vuota");
        }
        
        // Normalizza a uppercase per case-insensitive
        String normalizedCategory = category.trim().toUpperCase();
        String normalizedType = type.trim().toUpperCase();
        String normalizedCondition = condition.trim().toUpperCase();
        
        // Valida valori ammessi
        if (!isValidCategory(normalizedCategory)) {
            throw new IllegalArgumentException("Categoria non valida: " + category + 
                ". Valori ammessi: TCG, SPORT, ENTERTAINMENT, VINTAGE");
        }
        if (!SupportedCardTypes.isValid(normalizedType)) {
            throw new IllegalArgumentException("Tipo non valido: " + type + 
                ". Valori ammessi: " + SupportedCardTypes.getSupportedTypesAsString());
        }
        if (!isValidCondition(normalizedCondition)) {
            throw new IllegalArgumentException("Condizione non valida: " + condition + 
                ". Valori ammessi: RAW, MINT, NEAR_MINT, EXCELLENT, VERY_GOOD, GOOD, FAIR, POOR");
        }
        
        return repo.findByCardCategoryAndTypeAndCondition(normalizedCategory, normalizedType, normalizedCondition);
    }

    /**
     * 🔍 Trova listing con filtri opzionali (query unificata)
     * @param category Categoria (TCG, SPORT, ENTERTAINMENT, VINTAGE) - opzionale
     * @param type Tipo (SINGLE_CARD, SEALED_BOX, ecc.) - opzionale, multiselect (lista)
     * @param condition Condizione (RAW, MINT, NEAR_MINT, SEALED, ecc.) - opzionale, multiselect (lista)
     * @param language Lingua (ENGLISH, JAPANESE, KOREAN, CHINESE_SIMPLIFIED, ecc.) - opzionale, multiselect (lista)
     * @param franchise Franchise (Pokémon, One Piece, ecc.) - opzionale, multiselect (lista)
     * @param search Testo libero per ricerca (opzionale)
     * @return Lista di listing filtrati
     * 
     * Note: Nessuna validazione rigida. Se un valore non matcha, restituisce array vuoto.
     */
    public List<Listing> findByFilters(
        String category,
        List<String> type,
        List<String> condition,
        List<String> language,
        List<String> franchise,
        String search,
        Boolean acceptTrades
    ) {
        // Normalizzazione category (se fornita)
        String normalizedCategory = null;
        if (category != null && !category.trim().isEmpty()) {
            normalizedCategory = category.trim().toUpperCase();
        }
        
        // Normalizzazione type (multiselect): normalizza lista, rimuove duplicati, ordina
        List<String> normalizedType = null;
        if (type != null && !type.isEmpty()) {
            normalizedType = type.stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .map(t -> t.trim().toUpperCase())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            // Se lista risultante è vuota dopo normalizzazione, trattare come null
            if (normalizedType.isEmpty()) {
                normalizedType = null;
            }
        }
        
        // Normalizzazione condition (multiselect): normalizza lista, rimuove duplicati, ordina
        List<String> normalizedCondition = null;
        if (condition != null && !condition.isEmpty()) {
            normalizedCondition = condition.stream()
                .filter(c -> c != null && !c.trim().isEmpty())
                .map(c -> c.trim().toUpperCase())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            // Se lista risultante è vuota dopo normalizzazione, trattare come null
            if (normalizedCondition.isEmpty()) {
                normalizedCondition = null;
            }
        }
        
        // Normalizzazione language (multiselect): normalizza lista, rimuove duplicati, ordina
        // Mappa nomi completi a codici normalizzati (es. "Chinese (Simplified)" → "CHINESE_SIMPLIFIED")
        List<String> normalizedLanguage = null;
        if (language != null && !language.isEmpty()) {
            normalizedLanguage = language.stream()
                .filter(l -> l != null && !l.trim().isEmpty())
                .map(l -> normalizeLanguageCode(l.trim().toUpperCase()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            // Se lista risultante è vuota dopo normalizzazione, trattare come null
            if (normalizedLanguage.isEmpty()) {
                normalizedLanguage = null;
            }
        }
        
        // Normalizzazione franchise (multiselect): normalizza lista, rimuove duplicati, ordina
        List<String> normalizedFranchise = null;
        if (franchise != null && !franchise.isEmpty()) {
            normalizedFranchise = franchise.stream()
                .filter(f -> f != null && !f.trim().isEmpty())
                .map(f -> f.trim().toUpperCase())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            // Se lista risultante è vuota dopo normalizzazione, trattare come null
            if (normalizedFranchise.isEmpty()) {
                normalizedFranchise = null;
            }
        }

        // Normalizzazione search (se fornita) - trim e lowercase per match case-insensitive
        String normalizedSearch = null;
        if (search != null && !search.trim().isEmpty()) {
            normalizedSearch = "%" + search.trim().toLowerCase() + "%";
        }
        
        // Query unificata - nessuna validazione rigida, restituisce array vuoto se non matcha
        return repo.findByFilters(normalizedCategory, normalizedType, normalizedCondition, normalizedLanguage, normalizedFranchise, normalizedSearch, acceptTrades);
    }

    /**
     * 📝 Crea listing con gestione valori personalizzati "Altro"
     */
    @Transactional
    public Listing create(Listing listing, CreateListingRequest request, Long userId) {
        // 📂 Valida e crea Card con category
        if (request == null || request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("La categoria è obbligatoria");
        }
        
        String category = request.getCategory().trim().toUpperCase();
        if (!isValidCategory(category)) {
            throw new IllegalArgumentException("Categoria non valida: " + request.getCategory() + 
                ". Valori ammessi: TCG, SPORT, ENTERTAINMENT, VINTAGE");
        }
        
        // 📦 Valida e normalizza type (obbligatorio)
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Il tipo è obbligatorio");
        }
        
        String normalizedType = request.getType().trim().toUpperCase();
        if (!SupportedCardTypes.isValid(normalizedType)) {
            throw new IllegalArgumentException("Tipo non valido: " + request.getType() + 
                ". Valori ammessi: " + SupportedCardTypes.getSupportedTypesAsString());
        }
        
        // Crea Card con category
        Card card = new Card();
        card.setCategory(category);
        card.setType(normalizedType);
        log.debug("✅ Type impostato: {}", normalizedType);
        
        // 📝 Imposta nome carta (obbligatorio)
        if (request.getCardName() != null && !request.getCardName().trim().isEmpty()) {
            card.setName(request.getCardName().trim());
            log.debug("✅ Nome carta impostato: {}", request.getCardName().trim());
        }
        
        // 📚 Imposta serie/espansione se presente (opzionale)
        if (request.getSeries() != null && !request.getSeries().trim().isEmpty()) {
            card.setSetName(request.getSeries().trim());
            log.debug("✅ Serie impostata: {}", request.getSeries().trim());
        }
        
        // 🎮 Imposta franchise se presente (opzionale, normalizzato uppercase)
        if (request.getFranchise() != null && !request.getFranchise().trim().isEmpty()) {
            String franchise = request.getFranchise().trim().toUpperCase();
            card.setFranchise(franchise);
            log.debug("✅ Franchise impostato: {}", franchise);
        }
        
        Card savedCard = cardRepository.save(card);
        log.debug("✅ Card creata con category: {}", category);
        
        // Collega Listing a Card
        listing.setCard(savedCard);
        
        // 💱 Valida e imposta currency (default USD se non fornita)
        if (request.getCurrency() != null && !request.getCurrency().trim().isEmpty()) {
            String currency = request.getCurrency().trim().toUpperCase();
            if (!com.funkard.config.SupportedCurrencies.isValid(currency)) {
                throw new IllegalArgumentException("Valuta non supportata: " + currency + 
                    ". Valute supportate: EUR, USD, GBP, JPY, BRL, CAD, AUD");
            }
            listing.setCurrency(currency);
        } else {
            listing.setCurrency("USD");
        }
        
        // 🔒 Validazione cross-field: SEALED non valido con SINGLE_CARD
        // Ora listing.card è sempre impostato, quindi possiamo validare
        if (listing.getCard() != null && listing.getCard().getType() != null && 
            listing.getCondition() != null && !listing.getCondition().trim().isEmpty()) {
            String cardType = listing.getCard().getType().trim().toUpperCase();
            String listingCondition = listing.getCondition().trim().toUpperCase();
            if ("SEALED".equals(listingCondition) && "SINGLE_CARD".equals(cardType)) {
                throw new IllegalArgumentException(
                    "SEALED non è valido per prodotti SINGLE_CARD. " +
                    "SEALED può essere usato solo per prodotti sigillati (box, booster pack, ecc.)."
                );
            }
        }
        
        // Gestisci valori personalizzati "Altro"
        if (request != null) {
            // Se TCG è "Altro" e customTcg è fornito, salva proposta
            if ("Altro".equalsIgnoreCase(request.getTcg()) && 
                request.getCustomTcg() != null && !request.getCustomTcg().trim().isEmpty()) {
                try {
                    pendingValueService.submitPendingValue(
                        PendingValue.ValueType.TCG,
                        request.getCustomTcg(),
                        userId
                    );
                    log.info("✅ Proposta TCG personalizzato salvata: {}", request.getCustomTcg());
                } catch (Exception e) {
                    log.warn("⚠️ Errore durante salvataggio proposta TCG: {}", e.getMessage());
                    // Non bloccare la creazione listing se la proposta fallisce
                }
            }
            
            // Se Lingua è "Altro" e customLanguage è fornito, salva proposta
            if ("Altro".equalsIgnoreCase(request.getLanguage()) && 
                request.getCustomLanguage() != null && !request.getCustomLanguage().trim().isEmpty()) {
                try {
                    pendingValueService.submitPendingValue(
                        PendingValue.ValueType.LANGUAGE,
                        request.getCustomLanguage(),
                        userId
                    );
                    log.info("✅ Proposta Lingua personalizzata salvata: {}", request.getCustomLanguage());
                } catch (Exception e) {
                    log.warn("⚠️ Errore durante salvataggio proposta Lingua: {}", e.getMessage());
                    // Non bloccare la creazione listing se la proposta fallisce
                }
            }
            
            // Se Franchise è "Altro" e customFranchise è fornito, salva proposta
            if ("Altro".equalsIgnoreCase(request.getFranchise()) && 
                request.getCustomFranchise() != null && !request.getCustomFranchise().trim().isEmpty()) {
                try {
                    pendingValueService.submitPendingValue(
                        PendingValue.ValueType.FRANCHISE,
                        request.getCustomFranchise(),
                        userId
                    );
                    log.info("✅ Proposta Franchise personalizzato salvata: {}", request.getCustomFranchise());
                } catch (Exception e) {
                    log.warn("⚠️ Errore durante salvataggio proposta Franchise: {}", e.getMessage());
                    // Non bloccare la creazione listing se la proposta fallisce
                }
            }
        }
        
        // Salva listing
        return repo.save(listing);
    }

    /**
     * 📝 Crea listing (metodo legacy per retrocompatibilità)
     */
    public Listing create(Listing listing) {
        // 💱 Valida e imposta currency (default USD se non fornita)
        if (listing.getCurrency() == null || listing.getCurrency().trim().isEmpty()) {
            listing.setCurrency("USD");
        } else {
            String currency = listing.getCurrency().trim().toUpperCase();
            if (!com.funkard.config.SupportedCurrencies.isValid(currency)) {
                throw new IllegalArgumentException("Valuta non supportata: " + currency + 
                    ". Valute supportate: EUR, USD, GBP, JPY, BRL, CAD, AUD");
            }
            listing.setCurrency(currency);
        }
        
        return repo.save(listing);
    }

    /**
     * 🔄 Normalizza codice lingua da nome completo a codice standardizzato
     * Mappa valori "umani" dal frontend (es. "Chinese (Simplified)") a codici normalizzati (es. "CHINESE_SIMPLIFIED")
     * 
     * @param language Nome lingua (può essere già uppercase o nome completo)
     * @return Codice normalizzato uppercase (es. "ENGLISH", "CHINESE_SIMPLIFIED")
     */
    private String normalizeLanguageCode(String language) {
        if (language == null || language.trim().isEmpty()) {
            return language;
        }
        
        String normalized = language.trim().toUpperCase();
        
        // Mapping da nomi completi a codici normalizzati
        // Se il valore è già un codice normalizzato, viene restituito così com'è
        switch (normalized) {
            case "ENGLISH":
                return "ENGLISH";
            case "JAPANESE":
                return "JAPANESE";
            case "KOREAN":
                return "KOREAN";
            case "CHINESE (SIMPLIFIED)":
            case "CHINESE_SIMPLIFIED":
            case "CHINESE SIMPLIFIED":
                return "CHINESE_SIMPLIFIED";
            case "CHINESE (TRADITIONAL)":
            case "CHINESE_TRADITIONAL":
            case "CHINESE TRADITIONAL":
                return "CHINESE_TRADITIONAL";
            case "ITALIAN":
                return "ITALIAN";
            case "FRENCH":
                return "FRENCH";
            case "GERMAN":
                return "GERMAN";
            case "SPANISH":
                return "SPANISH";
            case "PORTUGUESE":
                return "PORTUGUESE";
            case "RUSSIAN":
                return "RUSSIAN";
            case "INDONESIAN":
                return "INDONESIAN";
            case "THAI":
                return "THAI";
            default:
                // Se non matcha nessun mapping, restituisce il valore uppercase così com'è
                // (per retrocompatibilità con valori già normalizzati o custom)
                return normalized;
        }
    }
}