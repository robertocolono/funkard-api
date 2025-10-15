package com.funkard.market.service;

import com.funkard.market.model.MarketValuation;
import com.funkard.market.repository.MarketValuationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.List;
import java.util.Set;

@Service
public class MarketValuationService {

    @Autowired
    private MarketValuationRepository repo;

    @Autowired
    private AdminNotifierService notifier;

    @Autowired(required = false)
    private com.funkard.market.repository.MarketListingRepository listingRepo;

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

    public void refreshOnlyRecentSales() {
        if (listingRepo == null) {
            System.out.println("⚠️ listingRepo non configurato: skip refresh incrementale.");
            return;
        }
        LocalDateTime since = LocalDateTime.now().minusHours(6);
        List<com.funkard.market.model.MarketListing> recentSales = listingRepo.findSoldAfter(since);

        if (recentSales == null || recentSales.isEmpty()) {
            System.out.println("ℹ️ Nessuna vendita recente trovata, skip aggiornamento.");
            return;
        }

        Set<String> updatedKeys = new HashSet<>();
        for (com.funkard.market.model.MarketListing sale : recentSales) {
            String key = sale.getItemName() + "|" + sale.getSetName() + "|" + sale.getCategory() + "|" + sale.getCondition();
            updatedKeys.add(key);
        }

        for (String key : updatedKeys) {
            String[] parts = key.split("\\|");
            if (parts.length == 4) {
                recalcValuation(parts[0], parts[1], parts[2], parts[3]);
            }
        }

        System.out.println("✅ Aggiornati " + updatedKeys.size() + " item con vendite recenti.");
    }

    private void recalcValuation(String itemName, String setName, String category, String condition) {
        if (listingRepo == null) return;
        List<com.funkard.market.model.MarketListing> recent = listingRepo.findRecentSold(
                itemName, setName, category, condition, LocalDateTime.now().minusDays(30)
        );
        if (recent == null || recent.isEmpty()) return;

        double avg = recent.stream().mapToDouble(com.funkard.market.model.MarketListing::getPriceEUR).average().orElse(0);
        double last = recent.get(recent.size() - 1).getPriceEUR();

        MarketValuation v = repo.findByItemNameAndSetNameAndCategoryAndCondition(itemName, setName, category, condition)
                .orElse(new MarketValuation());

        v.setItemName(itemName);
        v.setSetName(setName);
        v.setCategory(category);
        v.setCondition(condition);
        v.setAvgPrice(avg);
        v.setLastSoldPrice(last);
        v.setEstimatedValueProvvisorio(false);
        v.setManualCheck(false);
        v.setUpdatedAt(LocalDateTime.now());

        repo.save(v);
    }
}
