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
        key = "(#category != null ? #category.toUpperCase() : 'ALL') + '_' + (#type != null ? #type.toUpperCase() : 'ALL')")
    public ResponseEntity<?> getAllListings(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String condition,
            Authentication authentication) {
        
        List<Listing> listings;
        
        try {
            // Gestione filtri combinati (category, type, condition)
            boolean hasCategory = category != null && !category.trim().isEmpty();
            boolean hasType = type != null && !type.trim().isEmpty();
            boolean hasCondition = condition != null && !condition.trim().isEmpty();
            
            if (hasCategory && hasType && hasCondition) {
                // Filtra per tutti e tre (AND)
                listings = service.findByCategoryAndTypeAndCondition(category, type, condition);
            } else if (hasCategory && hasType) {
                // Filtra per category e type
                listings = service.findByCategoryAndType(category, type);
            } else if (hasCategory && hasCondition) {
                // Filtra per category e condition
                listings = service.findByCategoryAndCondition(category, condition);
            } else if (hasType && hasCondition) {
                // Filtra per type e condition
                listings = service.findByTypeAndCondition(type, condition);
            } else if (hasCategory) {
                // Filtra solo per category
                listings = service.findByCategory(category);
            } else if (hasType) {
                // Filtra solo per type
                listings = service.findByType(type);
            } else if (hasCondition) {
                // Filtra solo per condition
                listings = service.findByCondition(condition);
            } else {
                // Nessun filtro
                listings = service.getAll();
            }
        } catch (IllegalArgumentException e) {
            // Validazione fallita → HTTP 400
            log.warn("Filtro non valido: category={}, type={}, condition={}, error={}", category, type, condition, e.getMessage());
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
            listing.setCondition(request.getCondition());
            
            // Imposta seller da userId
            listing.setSeller(userId.toString());
            
            // Crea listing con gestione valori "Altro"
            Listing created = service.create(listing, request, userId);
            
            // Converte a DTO con conversione valuta
            String targetCurrency = getTargetCurrency(authentication);
            ListingDTO dto = toListingDTO(created, targetCurrency);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
            
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
}
