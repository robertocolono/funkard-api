package com.funkard.service;

import com.funkard.admin.notification.AdminNotification;
import com.funkard.admin.notification.AdminNotificationService;
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

        // 🔔 Crea una notifica admin automatica
        AdminNotification notification = AdminNotification.builder()
                .title("Nuova carta caricata")
                .message("Utente " + user.getUsername() +
                         " ha caricato una nuova carta: " + saved.getName())
                .type(AdminNotification.NotificationType.INFO)
                .build();

        adminNotificationService.create(notification);

        return saved;
    }

    // Metodo legacy per compatibilità
    public Card create(Card card) {
        Card saved = cardRepository.save(card);
        
        // 🔔 Notifica admin per nuovo prodotto senza storico
        AdminNotification notification = AdminNotification.builder()
                .title("Nuova carta aggiunta")
                .message("Carta: " + saved.getName())
                .type(AdminNotification.NotificationType.INFO)
                .build();

        adminNotificationService.create(notification);
        
        return saved;
    }
}