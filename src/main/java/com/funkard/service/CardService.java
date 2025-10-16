package com.funkard.service;

import com.funkard.admin.service.AdminNotificationService;
import com.funkard.model.Card;
import com.funkard.repository.CardRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CardService {
    private final CardRepository repo;
    private final AdminNotificationService adminNotificationService;

    public CardService(CardRepository repo, AdminNotificationService adminNotificationService) {
        this.repo = repo;
        this.adminNotificationService = adminNotificationService;
    }

    public List<Card> getAll() {
        return repo.findAll();
    }

    public Card create(Card card) {
        Card saved = repo.save(card);
               // Notifica admin per nuovo prodotto senza storico
               adminNotificationService.addNotification("new_card", "Nuova carta aggiunta", "Carta: " + saved.getName(), "normal");
        return saved;
    }
}