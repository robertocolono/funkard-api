package com.funkard.controller;

import com.funkard.dto.CreateListingRequest;
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

    @GetMapping
    @Cacheable(value = "marketplace:filters", key = "'all'")
    public List<Listing> getAllListings() {
        return service.getAll();
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
            
            // TODO: Impostare seller da userId
            // listing.setSeller(userId.toString());
            
            // Crea listing con gestione valori "Altro"
            Listing created = service.create(listing, request, userId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
            
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
}
