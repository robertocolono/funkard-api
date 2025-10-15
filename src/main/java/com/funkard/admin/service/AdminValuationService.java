package com.funkard.admin.service;

import com.funkard.admin.dto.MarketOverviewDTO;
import com.funkard.market.trend.TrendRepository;
import com.funkard.market.repository.MarketListingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class AdminValuationService {

    private final MarketListingRepository productRepo;
    private final TrendRepository trendRepo;

    public AdminValuationService(MarketListingRepository productRepo, TrendRepository trendRepo) {
        this.productRepo = productRepo;
        this.trendRepo = trendRepo;
    }

    public List<MarketOverviewDTO> getOverviewLast7Days() {
        LocalDate today = LocalDate.now();
        List<MarketOverviewDTO> data = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long newProducts = productRepo.countByCreatedAtBetween(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            long pendingItems = trendRepo.countByManualCheckTrueAndUpdatedAtAfter(date.minusDays(1).atStartOfDay());
            data.add(new MarketOverviewDTO(date, newProducts, pendingItems));
        }
        return data;
    }
}
