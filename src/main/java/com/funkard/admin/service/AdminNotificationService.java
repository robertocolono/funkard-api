package com.funkard.admin.service;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import com.funkard.repository.CardRepository;
import com.funkard.market.trend.TrendRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminNotificationService {

    private final AdminNotificationRepository notificationRepo;
    private final CardRepository cardRepo;
    private final TrendRepository trendRepo;

    public AdminNotificationService(AdminNotificationRepository notificationRepo,
                                    CardRepository cardRepo,
                                    TrendRepository trendRepo) {
        this.notificationRepo = notificationRepo;
        this.cardRepo = cardRepo;
        this.trendRepo = trendRepo;
    }

    // 🔹 Restituisce tutte le notifiche attive
    public List<AdminNotification> getActive() {
        return notificationRepo.findByResolvedFalseOrderByCreatedAtDesc();
    }

    // 🔹 Aggiunge nuova notifica
    public void notifyNewProductWithoutTrend(String productId) {
        // Verifica se esiste un trend per questo prodotto
        boolean exists = trendRepo.existsByItemNameAndCategory(productId, "card");
        var product = cardRepo.findById(productId);
        
        if (product.isPresent() && !exists) {
            // Controlla se il prodotto ha un valore stimato
            // Assumiamo che se non ha trend, potrebbe aver bisogno di una stima manuale
            notificationRepo.save(new AdminNotification(productId, "Nuovo prodotto senza storico prezzi"));
        }
    }

    // 🔹 Marca come risolta
    public void markResolved(String id) {
        var notif = notificationRepo.findById(java.util.UUID.fromString(id));
        notif.ifPresent(n -> { 
            n.setResolved(true); 
            notificationRepo.save(n); 
        });
    }
}
