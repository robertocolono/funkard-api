package com.funkard.market.service;

import com.funkard.market.model.MarketListing;
import com.funkard.market.repository.MarketListingRepository;
import com.funkard.market.trend.TrendDTO;
import com.funkard.service.AdminNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TrendService {
    
    private final MarketListingRepository listingRepo;
    private final AdminNotifier adminNotifier;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public TrendService(MarketListingRepository listingRepo, AdminNotifier adminNotifier) {
        this.listingRepo = listingRepo;
        this.adminNotifier = adminNotifier;
    }
    
    public Optional<Double> getLastSoldPrice(String itemName, String category) {
        List<MarketListing> list = listingRepo.findLastSold(itemName, category);
        return list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0).getPriceEUR());
    }
    
    public TrendDTO getTrendDTO(String itemName, String category, String rangeType) {
        var dto = new TrendDTO();
        dto.itemName = itemName;
        dto.category = category;
        dto.rangeType = rangeType;

        // Per ora simuliamo la ricerca del trend (dovresti implementare TrendRepository)
        // var opt = trendRepo.findByItemNameAndCategoryAndRangeType(itemName, category, rangeType);
        
        int minPoints = switch (rangeType) {
            case "7d" -> 3;
            case "30d" -> 5;     // 1 mese
            case "1y" -> 8;
            default -> 5;
        };

        // Simuliamo che non esista il trend (nuovo item)
        boolean trendExists = false; // opt.isPresent();
        
        if (!trendExists) {
            // nuovo item
            dto.status = "new_item";
            dto.manualCheck = true;
            adminNotifier.notifyMissingHistory(itemName, category, rangeType);
            dto.lastSoldPrice = getLastSoldPrice(itemName, category).orElse(null);
            return dto;
        }

        // Se il trend esiste, dovresti processare i dati
        // var t = opt.get();
        // dto.updatedAt = t.getUpdatedAt();
        // try {
        //     @SuppressWarnings("unchecked")
        //     List<Map<String,Object>> pts = new ObjectMapper().readValue(t.getDataPoints(), List.class);
        //     dto.points = pts;
        // } catch (Exception e) { dto.points = List.of(); }

        dto.lastSoldPrice = getLastSoldPrice(itemName, category).orElse(null);

        // Simuliamo dati insufficienti per testare le notifiche
        dto.points = List.of(); // Dati vuoti per simulare insufficient data
        
        if (dto.points == null || dto.points.size() < minPoints) {
            dto.status = "insufficient_data";
            dto.manualCheck = true;
            adminNotifier.notifyMissingHistory(itemName, category, rangeType);
        } else {
            dto.status = "ok";
            dto.manualCheck = false;
        }
        return dto;
    }
}
