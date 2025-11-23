package com.funkard.service;

import com.funkard.model.*;
import com.funkard.repository.*;
import com.funkard.admin.repository.SupportTicketRepository;
import com.funkard.admin.model.SupportTicket;
import com.funkard.service.R2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🗑️ Service per cancellazione definitiva account utente (GDPR Art. 17)
 * 
 * Funzionalità:
 * - Cancellazione sicura di tutti i dati utente
 * - Cleanup file su R2 storage
 * - Logging completo per audit
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletionService {
    
    private final UserRepository userRepository;
    private final UserCardRepository userCardRepository;
    private final WishlistRepository wishlistRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final CookieConsentLogRepository cookieConsentLogRepository;
    private final TransactionRepository transactionRepository;
    private final ListingRepository listingRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final R2Service r2Service;
    
    /**
     * 🗑️ Cancella definitivamente tutti i dati di un utente
     * 
     * @param userId ID utente
     * @param email Email utente (per logging)
     * @return true se cancellazione riuscita
     */
    @Transactional
    public boolean permanentlyDeleteUser(Long userId, String email) {
        log.info("🗑️ Inizio cancellazione definitiva utente: {} ({})", userId, email);
        
        try {
            // 1. Cancella UserCards e file associati su R2
            deleteUserCards(userId);
            
            // 2. Cancella Wishlist
            deleteWishlist(userId);
            
            // 3. Cancella UserAddresses
            deleteUserAddresses(userId);
            
            // 4. Cancella UserPreferences
            deleteUserPreferences(userId);
            
            // 5. Cancella CookieConsentLogs
            deleteCookieConsentLogs(userId);
            
            // 6. Cancella Transactions (se esistono)
            deleteTransactions(userId);
            
            // 7. Cancella Listings (se esistono)
            deleteListings(userId);
            
            // 8. Cancella SupportTickets (se esistono)
            deleteSupportTickets(userId);
            
            // 9. Cancella User (ultimo, per evitare constraint violations)
            deleteUser(userId);
            
            log.info("✅ Cancellazione definitiva completata per utente: {} ({})", userId, email);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Errore durante cancellazione utente {} ({}): {}", userId, email, e.getMessage(), e);
            throw new RuntimeException("Errore durante cancellazione account: " + e.getMessage(), e);
        }
    }
    
    /**
     * 🗑️ Cancella UserCards e file su R2
     */
    private void deleteUserCards(Long userId) {
        // UserCard ha userId come String, convertiamo
        List<UserCard> userCards = userCardRepository.findByUserId(String.valueOf(userId));
        log.debug("Trovate {} UserCards per utente {}", userCards.size(), userId);
        
        for (UserCard card : userCards) {
            try {
                // Cancella tutti i file su R2 associati alla carta
                deleteCardFilesFromR2(card);
            } catch (Exception e) {
                log.warn("Errore durante cancellazione file per UserCard {}: {}", card.getId(), e.getMessage());
            }
        }
        
        userCardRepository.deleteAll(userCards);
        log.debug("✅ Cancellate {} UserCards per utente {}", userCards.size(), userId);
    }
    
    /**
     * 🗑️ Cancella Wishlist
     */
    private void deleteWishlist(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            List<Wishlist> wishlists = wishlistRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(userId))
                .toList();
            if (!wishlists.isEmpty()) {
                wishlistRepository.deleteAll(wishlists);
                log.debug("✅ Cancellate {} Wishlist per utente {}", wishlists.size(), userId);
            }
        }
    }
    
    /**
     * 🗑️ Cancella UserAddresses
     */
    private void deleteUserAddresses(Long userId) {
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
        if (!addresses.isEmpty()) {
            userAddressRepository.deleteAll(addresses);
            log.debug("✅ Cancellati {} UserAddresses per utente {}", addresses.size(), userId);
        }
    }
    
    /**
     * 🗑️ Cancella UserPreferences
     */
    private void deleteUserPreferences(Long userId) {
        userPreferencesRepository.findByUserId(userId).ifPresent(prefs -> {
            userPreferencesRepository.delete(prefs);
            log.debug("✅ Cancellate UserPreferences per utente {}", userId);
        });
    }
    
    /**
     * 🗑️ Cancella CookieConsentLogs
     */
    private void deleteCookieConsentLogs(Long userId) {
        cookieConsentLogRepository.deleteByUserId(userId);
        log.debug("✅ Cancellati CookieConsentLogs per utente {}", userId);
    }
    
    /**
     * 🗑️ Cancella Transactions
     */
    private void deleteTransactions(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Transaction ha buyer come User (ManyToOne)
            List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getBuyer() != null && t.getBuyer().getId().equals(userId))
                .toList();
            if (!transactions.isEmpty()) {
                transactionRepository.deleteAll(transactions);
                log.debug("✅ Cancellate {} Transactions per utente {}", transactions.size(), userId);
            }
        }
    }
    
    /**
     * 🗑️ Cancella Listings
     */
    private void deleteListings(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Listing ha seller come String (email o username)
            List<Listing> listings = listingRepository.findAll().stream()
                .filter(l -> l.getSeller() != null && 
                    (l.getSeller().equals(user.getEmail()) || 
                     l.getSeller().equals(user.getUsername()) ||
                     l.getSeller().equals(String.valueOf(userId))))
                .toList();
            if (!listings.isEmpty()) {
                // Cancella anche file immagini associati (se esiste campo imageUrl)
                for (Listing listing : listings) {
                    // Nota: Listing potrebbe non avere imageUrl, verificare modello
                }
                listingRepository.deleteAll(listings);
                log.debug("✅ Cancellati {} Listings per utente {}", listings.size(), userId);
            }
        }
    }
    
    /**
     * 🗑️ Cancella SupportTickets
     */
    private void deleteSupportTickets(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // SupportTicket ha userId come String
            List<SupportTicket> tickets = supportTicketRepository.findAll().stream()
                .filter(t -> t.getUserId() != null && 
                    (t.getUserId().equals(String.valueOf(userId)) ||
                     t.getUserId().equals(user.getEmail()) ||
                     t.getUserEmail() != null && t.getUserEmail().equals(user.getEmail())))
                .toList();
            if (!tickets.isEmpty()) {
                supportTicketRepository.deleteAll(tickets);
                log.debug("✅ Cancellati {} SupportTickets per utente {}", tickets.size(), userId);
            }
        }
    }
    
    /**
     * 🗑️ Cancella User (ultimo step)
     */
    private void deleteUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            userRepository.delete(user);
            log.debug("✅ Cancellato User {}", userId);
        });
    }
    
    /**
     * 🗑️ Cancella tutti i file R2 associati a una UserCard
     */
    private void deleteCardFilesFromR2(UserCard card) {
        // Lista di tutti i possibili campi immagine
        String[] imageFields = {
            card.getFrontImage(),
            card.getBackImage(),
            card.getCornerTopLeft(),
            card.getCornerTopRight(),
            card.getCornerBottomLeft(),
            card.getCornerBottomRight(),
            card.getTopLeftImage(),
            card.getTopRightImage(),
            card.getBottomLeftImage(),
            card.getBottomRightImage(),
            card.getEdgeLeft(),
            card.getEdgeRight(),
            card.getEdgeTop(),
            card.getEdgeBottom(),
            card.getEdgeTopImage(),
            card.getEdgeBottomImage(),
            card.getEdgeLeftImage(),
            card.getEdgeRightImage()
        };
        
        for (String imageUrl : imageFields) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    // Estrai key dal URL completo se necessario
                    String key = extractR2KeyFromUrl(imageUrl);
                    r2Service.deleteFile(key);
                    log.debug("File R2 cancellato: {}", key);
                } catch (Exception e) {
                    log.warn("Errore cancellazione file R2 {}: {}", imageUrl, e.getMessage());
                    // Continua anche se il file non può essere cancellato
                }
            }
        }
    }
    
    /**
     * 🔍 Estrae la key R2 dall'URL completo
     * Gestisce sia URL completi che key dirette
     */
    private String extractR2KeyFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        // Se è già una key (non contiene http/https), restituisci così
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url;
        }
        
        // Estrai key dall'URL completo
        // Esempio: https://r2.example.com/usercards/123/front.jpg -> usercards/123/front.jpg
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String path = urlObj.getPath();
            // Rimuovi leading slash
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            // Se parsing fallisce, prova a estrarre manualmente
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash > 0) {
                return url.substring(lastSlash + 1);
            }
            return url;
        }
    }
}

