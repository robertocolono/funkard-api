package com.funkard.service;

import com.funkard.admin.service.AdminNotificationService;
import com.funkard.admin.model.AdminNotification;
import com.funkard.model.Card;
import com.funkard.model.User;
import com.funkard.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AdminNotificationService adminNotificationService;

    public List<Card> getAll() {
        return cardRepository.findAll();
    }

    public Card createCard(Card card, User user) {
        Card saved = cardRepository.save(card);

        // 🔔 Notifica automatica per nuova carta
        adminNotificationService.marketEvent(
            "Nuova carta aggiunta",
            "Carta: " + saved.getName() + " aggiunta da " + user.getUsername(),
            java.util.Map.of("cardId", saved.getId(), "cardName", saved.getName(), "userId", user.getId())
        );

        return saved;
    }

    // Metodo legacy per compatibilità
    public Card create(Card card) {
        Card saved = cardRepository.save(card);
        
        // 🔔 Notifica admin per nuovo prodotto senza storico
        adminNotificationService.marketEvent(
            "Nuova carta aggiunta",
            "Carta: " + saved.getName(),
            java.util.Map.of("cardId", saved.getId(), "cardName", saved.getName())
        );
        
        return saved;
    }
}