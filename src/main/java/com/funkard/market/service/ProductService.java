package com.funkard.market.service;

import com.funkard.admin.service.AdminNotificationService;
import com.funkard.market.model.Product;
import com.funkard.market.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final AdminNotificationService notifications;

    public ProductService(ProductRepository repo, AdminNotificationService notifications) {
        this.repo = repo;
        this.notifications = notifications;
    }

    @Transactional
    public Product createProduct(Product p) {
        try {
            Product saved = repo.save(p);

            // 🔔 Notifica di creazione
            notifications.marketEvent(
                    "Nuovo prodotto inserito",
                    "È stato aggiunto un nuovo prodotto: " + p.getName(),
                    Map.of("id", saved.getId(), "userId", p.getUserId())
            );

            // ⚠️ Controlli base
            if (p.getEstimatedValue() == null || p.getEstimatedValue() <= 0) {
                notifications.marketEvent(
                        "Prodotto senza valore stimato",
                        "Il prodotto \"" + p.getName() + "\" non ha un valore stimato impostato.",
                        Map.of("productId", saved.getId())
                );
            } else if (p.getPrice() != null) {
                double ratio = p.getPrice() / p.getEstimatedValue();
                if (ratio > 2.0 || ratio < 0.3) {
                    notifications.marketEvent(
                            "Prezzo anomalo",
                            "Il prezzo del prodotto \"" + p.getName() + "\" (" + p.getPrice() + "€) " +
                                    "è fuori dal range stimato (" + p.getEstimatedValue() + "€).",
                            Map.of("productId", saved.getId(), "priceRatio", ratio)
                    );
                }
            }

            // 🔁 Nome duplicato
            if (repo.countByNameIgnoreCase(p.getName()) > 1) {
                notifications.marketEvent(
                        "Nome prodotto duplicato",
                        "Trovato più di un prodotto con nome \"" + p.getName() + "\"",
                        Map.of("name", p.getName())
                );
            }

            return saved;
        } catch (Exception e) {
            notifications.marketEvent(
                    "Errore creazione prodotto",
                    e.getMessage(),
                    Map.of("name", p.getName())
            );
            throw e;
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        repo.findById(id).ifPresent(p -> {
            repo.delete(p);
            notifications.marketEvent(
                    "Prodotto rimosso",
                    "È stato eliminato il prodotto \"" + p.getName() + "\"",
                    Map.of("productId", id)
            );
        });
    }
}
