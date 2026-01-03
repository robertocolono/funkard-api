package com.funkard.service;

import com.funkard.dto.CreateListingRequest;
import com.funkard.model.Listing;
import com.funkard.model.PendingValue;
import com.funkard.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {
    private final ListingRepository repo;
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
     * 📝 Crea listing con gestione valori personalizzati "Altro"
     */
    @Transactional
    public Listing create(Listing listing, CreateListingRequest request, Long userId) {
        // 💱 Valida e imposta currency (default USD se non fornita)
        if (request != null && request.getCurrency() != null && !request.getCurrency().trim().isEmpty()) {
            String currency = request.getCurrency().trim().toUpperCase();
            if (!com.funkard.config.SupportedCurrencies.isValid(currency)) {
                throw new IllegalArgumentException("Valuta non supportata: " + currency + 
                    ". Valute supportate: EUR, USD, GBP, JPY, BRL, CAD, AUD");
            }
            listing.setCurrency(currency);
        } else {
            listing.setCurrency("USD");
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
}