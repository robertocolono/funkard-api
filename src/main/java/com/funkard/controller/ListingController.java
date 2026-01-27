package com.funkard.controller;

import com.funkard.currency.CurrencyConversionService;
import com.funkard.dto.CreateListingRequest;
import com.funkard.dto.ListingDTO;
import com.funkard.model.Listing;
import com.funkard.model.User;
import com.funkard.repository.UserRepository;
import com.funkard.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 📝 Controller per gestione listing/vendite
 * 
 * Supporta valori personalizzati "Altro" per TCG e Lingua.
 */
@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class ListingController {

    private final ListingService service;
    private final UserRepository userRepository;
    private final CurrencyConversionService currencyConversionService;

    @GetMapping
    @Cacheable(value = "marketplace:filters", 
        key = "(#category != null ? #category.toUpperCase() : 'ALL') + '_' + T(com.funkard.controller.ListingController).buildTypeCacheKey(#type) + '_' + T(com.funkard.controller.ListingController).buildConditionCacheKey(#condition) + '_' + T(com.funkard.controller.ListingController).buildLanguageCacheKey(#language) + '_' + T(com.funkard.controller.ListingController).buildFranchiseCacheKey(#franchise) + '_' + (#search != null ? #search.trim().toUpperCase() : 'ALL') + '_' + (#acceptTrades != null ? (#acceptTrades ? 'TRUE' : 'FALSE') : 'ALL')")
    public ResponseEntity<?> getAllListings(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) List<String> condition,
            @RequestParam(required = false) List<String> language,
            @RequestParam(required = false) List<String> franchise,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean acceptTrades,
            Authentication authentication) {
        
        List<Listing> listings;
        
        try {
            // Query unificata - gestisce tutte le combinazioni automaticamente
            // Nessuna validazione rigida: se valore non matcha, restituisce array vuoto
            // type, condition, language e franchise sono multiselect: Spring converte automaticamente ?type=X in List<String> con 1 elemento
            listings = service.findByFilters(category, type, condition, language, franchise, search, acceptTrades);
        } catch (IllegalArgumentException e) {
            // Validazione fallita → HTTP 400 (solo per metodi legacy se ancora usati)
            log.warn("Filtro non valido: category={}, type={}, condition={}, language={}, franchise={}, search={}, acceptTrades={}, error={}", 
                category, type, condition, language, franchise, search, acceptTrades, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
        
        String targetCurrency = getTargetCurrency(authentication);
        
        List<ListingDTO> dtos = listings.stream()
            .map(listing -> toListingDTO(listing, targetCurrency))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    /**
     * 📝 POST /api/listings
     * Crea nuovo listing/vendita
     * 
     * Supporta valori personalizzati "Altro" per TCG e Lingua.
     * Se l'utente seleziona "Altro", il valore custom viene salvato
     * come proposta pending in attesa di approvazione admin.
     */
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody CreateListingRequest request,
            Authentication authentication) {
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // Recupera userId
            Long userId = getUserIdFromAuthentication(authentication);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Crea listing entity
            Listing listing = new Listing();
            listing.setTitle(request.getTitle());
            listing.setDescription(request.getDescription());
            listing.setPrice(request.getPrice());
            listing.setQuantity(request.getQuantity());
            listing.setOriginalPrice(request.getOriginalPrice());
            listing.setCondition(request.getCondition());
            listing.setSellerDeclarations(request.getSellerDeclarations());
            if (request.getAcceptTrades() != null) {
                listing.setAcceptTrades(request.getAcceptTrades());
            }
            
            // Imposta seller da userId
            listing.setSeller(userId.toString());
            
            // Crea listing con gestione valori "Altro"
            Listing created = service.create(listing, request, userId);
            
            // Converte a DTO con conversione valuta
            String targetCurrency = getTargetCurrency(authentication);
            ListingDTO dto = toListingDTO(created, targetCurrency);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
            
        } catch (IllegalArgumentException e) {
            // Validazione fallita (category non valida, valuta non supportata, ecc.)
            log.warn("Validazione fallita durante creazione listing: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Errore durante creazione listing: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Errore interno del server"));
        }
    }
    
    /**
     * 📝 POST /api/listings (legacy - retrocompatibilità)
     */
    @PostMapping("/legacy")
    public Listing createLegacy(@RequestBody Listing listing) {
        return service.create(listing);
    }
    
    /**
     * 🔍 Helper per recuperare userId da Authentication
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            
            User user = userRepository.findByEmail(email);
            return user != null ? user.getId() : null;
        }
        return null;
    }
    
    /**
     * 🔍 Helper per ottenere valuta target (preferredCurrency dell'utente o "USD")
     */
    private String getTargetCurrency(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername());
            if (user != null && user.getPreferredCurrency() != null && !user.getPreferredCurrency().trim().isEmpty()) {
                return user.getPreferredCurrency().toUpperCase();
            }
        }
        return "USD"; // Default per utenti non autenticati
    }
    
    /**
     * 🔄 Converte Listing entity a ListingDTO con conversione valuta
     */
    private ListingDTO toListingDTO(Listing listing, String targetCurrency) {
        ListingDTO dto = new ListingDTO();
        dto.setId(listing.getId() != null ? listing.getId().toString() : null);
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setPrice(listing.getPrice() != null ? listing.getPrice().doubleValue() : null);
        dto.setCurrency(listing.getCurrency());
        dto.setStatus(null); // Listing entity non ha status, lasciare null
        dto.setCreatedAt(null); // Listing entity non ha createdAt, lasciare null
        dto.setSellerId(listing.getSeller());
        dto.setCardId(listing.getCard() != null ? listing.getCard().getId().toString() : null);
        dto.setCategory(listing.getCard() != null ? listing.getCard().getCategory() : null);
        dto.setType(listing.getCard() != null ? listing.getCard().getType() : null);
        dto.setCondition(listing.getCondition());
        dto.setLanguage(listing.getCard() != null ? listing.getCard().getLanguage() : null);
        dto.setFranchise(listing.getCard() != null ? listing.getCard().getFranchise() : null);
        dto.setAcceptTrades(listing.isAcceptTrades());
        dto.setQuantity(listing.getQuantity());
        dto.setOriginalPrice(listing.getOriginalPrice());
        dto.setSellerDeclarations(listing.getSellerDeclarations());
        
        // Calcola convertedPrice e convertedCurrency
        if (listing.getPrice() != null && listing.getCurrency() != null) {
            try {
                double converted = currencyConversionService.convert(
                    listing.getPrice().doubleValue(),
                    listing.getCurrency(),
                    targetCurrency
                );
                dto.setConvertedPrice(converted);
                dto.setConvertedCurrency(targetCurrency);
            } catch (Exception e) {
                log.warn("Errore durante conversione valuta per listing {}: {}", listing.getId(), e.getMessage());
                // In caso di errore, usa il prezzo originale
                dto.setConvertedPrice(listing.getPrice().doubleValue());
                dto.setConvertedCurrency(listing.getCurrency());
            }
        } else {
            dto.setConvertedPrice(listing.getPrice() != null ? listing.getPrice().doubleValue() : null);
            dto.setConvertedCurrency(listing.getCurrency());
        }
        
        return dto;
    }

    /**
     * 🔑 Helper method per costruire cache key deterministica per type (multiselect)
     * Normalizza, ordina, deduplica e unisce con virgola
     * Usato da SpEL nella cache key
     */
    public static String buildTypeCacheKey(List<String> type) {
        if (type == null || type.isEmpty()) {
            return "ALL";
        }
        return type.stream()
            .filter(t -> t != null && !t.trim().isEmpty())
            .map(t -> t.trim().toUpperCase())
            .distinct()
            .sorted()
            .collect(Collectors.joining(","));
    }

    /**
     * 🔑 Helper method per costruire cache key deterministica per condition (multiselect)
     * Normalizza, ordina, deduplica e unisce con virgola
     * Usato da SpEL nella cache key
     */
    public static String buildConditionCacheKey(List<String> condition) {
        if (condition == null || condition.isEmpty()) {
            return "ALL";
        }
        return condition.stream()
            .filter(c -> c != null && !c.trim().isEmpty())
            .map(c -> c.trim().toUpperCase())
            .distinct()
            .sorted()
            .collect(Collectors.joining(","));
    }

    /**
     * 🔑 Helper method per costruire cache key deterministica per language (multiselect)
     * Normalizza, ordina, deduplica e unisce con virgola
     * Usato da SpEL nella cache key
     */
    public static String buildLanguageCacheKey(List<String> language) {
        if (language == null || language.isEmpty()) {
            return "ALL";
        }
        return language.stream()
            .filter(l -> l != null && !l.trim().isEmpty())
            .map(l -> l.trim().toUpperCase())
            .distinct()
            .sorted()
            .collect(Collectors.joining(","));
    }

    /**
     * 🔑 Helper method per costruire cache key deterministica per franchise (multiselect)
     * Normalizza, ordina, deduplica e unisce con virgola
     * Usato da SpEL nella cache key
     */
    public static String buildFranchiseCacheKey(List<String> franchise) {
        if (franchise == null || franchise.isEmpty()) {
            return "ALL";
        }
        return franchise.stream()
            .filter(f -> f != null && !f.trim().isEmpty())
            .map(f -> f.trim().toUpperCase())
            .distinct()
            .sorted()
            .collect(Collectors.joining(","));
    }
}
