package com.funkard.market.service;

import com.funkard.admin.service.AdminNotificationService;
import com.funkard.market.model.Product;
import com.funkard.market.repository.ProductRepository;
import com.funkard.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository repo;
    private final AdminNotificationService notifications;
    private final OpenAiTranslateService openAiService;
    private final DeepLTranslateService deepLService;

    public ProductService(ProductRepository repo, 
                          AdminNotificationService notifications,
                          OpenAiTranslateService openAiService,
                          DeepLTranslateService deepLService) {
        this.repo = repo;
        this.notifications = notifications;
        this.openAiService = openAiService;
        this.deepLService = deepLService;
    }

    @Transactional
    public Product createProduct(Product p) {
        try {
            // 💱 Valida e imposta currency (default USD se non fornita)
            if (p.getCurrency() == null || p.getCurrency().trim().isEmpty()) {
                p.setCurrency("USD");
            } else {
                String currency = p.getCurrency().trim().toUpperCase();
                if (!com.funkard.config.SupportedCurrencies.isValid(currency)) {
                    throw new IllegalArgumentException("Valuta non supportata: " + currency + 
                        ". Valute supportate: EUR, USD, GBP, JPY, BRL, CAD, AUD");
                }
                p.setCurrency(currency);
            }
            
            // 🌍 Genera automaticamente nameEn se non fornito
            if (p.getNameEn() == null || p.getNameEn().trim().isEmpty()) {
                String generatedNameEn = generateGlobalEnglishName(p);
                if (generatedNameEn != null && !generatedNameEn.trim().isEmpty()) {
                    p.setNameEn(generatedNameEn);
                }
            }
            
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
    
    /**
     * 🌍 Genera il nome globale inglese della carta usando GPT-4o-mini
     * 
     * Usa un prompt specializzato per riconoscere il nome ufficiale collezionistico,
     * non una traduzione letterale. Normalizza varianti, edizioni, sottoserie.
     * 
     * @param product Prodotto da cui generare il nome inglese
     * @return Nome inglese normalizzato o null se non generabile
     */
    private String generateGlobalEnglishName(Product product) {
        String originalName = product.getName();
        
        // Validazioni: se il nome è vuoto o troppo corto, non generare
        if (originalName == null || originalName.trim().isEmpty()) {
            log.debug("Nome prodotto vuoto, nameEn rimane null");
            return null;
        }
        
        String trimmedName = originalName.trim();
        
        // Evita chiamate inutili per nomi troppo corti o generici
        if (trimmedName.length() <= 3 || 
            trimmedName.toLowerCase().matches("^(a|aaa|\\?+)$") ||
            trimmedName.equalsIgnoreCase("card")) {
            log.debug("Nome prodotto troppo corto o generico: '{}', uso nome originale", trimmedName);
            return trimmedName;
        }
        
        try {
            // Costruisci prompt avanzato per GPT
            String prompt = buildCardNameNormalizationPrompt(trimmedName, product.getDescriptionLanguage());
            
            // 1. Prova GPT-4o-mini con prompt personalizzato
            if (openAiService.isAvailable()) {
                try {
                    String nameEn = openAiService.executeWithCustomPrompt(prompt);
                    if (nameEn != null && !nameEn.trim().isEmpty()) {
                        log.info("✅ Nome inglese globale generato con GPT per prodotto: {} -> {}", 
                            trimmedName.length() > 30 ? trimmedName.substring(0, 30) + "..." : trimmedName,
                            nameEn.length() > 30 ? nameEn.substring(0, 30) + "..." : nameEn);
                        return nameEn.trim();
                    }
                } catch (TranslationException e) {
                    log.warn("⚠️ GPT fallito nella generazione nameEn: {}, provo fallback DeepL", e.getMessage());
                } catch (Exception e) {
                    log.warn("⚠️ Errore imprevisto GPT nella generazione nameEn: {}, provo fallback DeepL", e.getMessage());
                }
            } else {
                log.warn("⚠️ GPT non disponibile per generazione nameEn, provo DeepL");
            }
            
            // 2. Fallback a DeepL (traduzione semplice)
            if (deepLService.isAvailable()) {
                try {
                    String nameEn = deepLService.translate(trimmedName, "en");
                    if (nameEn != null && !nameEn.trim().isEmpty()) {
                        log.warn("⚠️ Nome inglese generato con DeepL (fallback) per prodotto: {} -> {}", 
                            trimmedName.length() > 30 ? trimmedName.substring(0, 30) + "..." : trimmedName,
                            nameEn.length() > 30 ? nameEn.substring(0, 30) + "..." : nameEn);
                        return nameEn.trim();
                    }
                } catch (TranslationException e) {
                    log.error("❌ DeepL fallito nella generazione nameEn: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("❌ Errore imprevisto DeepL nella generazione nameEn: {}", e.getMessage());
                }
            }
            
            // 3. Fallback finale: usa nome originale
            log.error("❌ Entrambi i provider hanno fallito nella generazione nameEn, uso nome originale");
            return trimmedName;
            
        } catch (Exception e) {
            log.error("❌ Errore imprevisto durante generazione nameEn: {}", e.getMessage());
            // In caso di errore imprevisto, usa il nome originale
            return trimmedName;
        }
    }
    
    /**
     * 📝 Costruisce il prompt avanzato per normalizzazione nome carta
     * 
     * @param originalName Nome originale della carta
     * @param originalLanguage Lingua originale (opzionale, può essere null)
     * @return Prompt completo per GPT
     */
    private String buildCardNameNormalizationPrompt(String originalName, String originalLanguage) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert in trading cards (TCG/CCG). Given a card name written by a user in any language or any non-standard format, your task is to output ONLY the official collectible card name in English, exactly as used in the global marketplace.\n\n");
        prompt.append("Do NOT translate literally.\n\n");
        prompt.append("Do NOT output explanations.\n\n");
        prompt.append("Normalize accents, edition markers, variant identifiers, rarity abbreviations, set codes, and special editions.\n\n");
        prompt.append("If the user provided incomplete or unclear names, infer the most likely official collectible name.\n\n");
        prompt.append("Output ONLY the corrected English collectible name.\n\n");
        
        if (originalLanguage != null && !originalLanguage.trim().isEmpty()) {
            prompt.append("Original language: ").append(originalLanguage.trim()).append("\n\n");
        }
        
        prompt.append("Card name: ").append(originalName).append("\n\n");
        prompt.append("Official English collectible name:");
        
        return prompt.toString();
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
