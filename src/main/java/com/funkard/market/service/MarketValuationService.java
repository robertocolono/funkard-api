package com.funkard.market.service;

import com.funkard.market.model.MarketValuation;
import com.funkard.market.repository.MarketValuationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MarketValuationService {

    @Autowired
    private MarketValuationRepository repo;

    @Autowired
    private AdminNotifierService notifier;

    public MarketValuation getOrCreateValuation(
            String itemName,
            String setName,
            String category,
            String condition,
            Double grade // può essere null
    ) {
        Optional<MarketValuation> existing = repo
                .findByItemNameAndSetNameAndCategoryAndCondition(itemName, setName, category, condition);

        if (existing.isPresent()) {
            return existing.get();
        }

        double provvisorio = calculateFallbackValue(category, condition, grade);

        MarketValuation v = new MarketValuation();
        v.setItemName(itemName);
        v.setSetName(setName);
        v.setCategory(category);
        v.setCondition(condition);
        v.setAvgPrice(provvisorio);
        v.setLastSoldPrice(null);
        v.setEstimatedValueProvvisorio(true);
        v.setManualCheck(true);
        v.setUpdatedAt(LocalDateTime.now());
        repo.save(v);

        notifier.notifyNewValuation("Nuovo asset senza storico: " + itemName + " (" + category + ")");

        return v;
    }

    private double calculateFallbackValue(String category, String condition, Double grade) {
        double base = switch (category) {
            case "CARD" -> 50.0;
            case "BOX" -> 200.0;
            case "ETB" -> 120.0;
            case "BOOSTER" -> 15.0;
            case "SLAB" -> 100.0;
            case "ACCESSORY" -> 25.0;
            default -> 50.0;
        };

        double multiplier = switch (condition) {
            case "SEALED" -> 1.2;
            case "MINT" -> 1.1;
            case "NM" -> 1.0;
            case "LP" -> 0.8;
            case "HP" -> 0.6;
            case "DAMAGED" -> 0.4;
            default -> 1.0;
        };

        double gradeFactor = grade != null ? (grade / 10.0) : 1.0;

        return base * multiplier * gradeFactor;
    }
}
