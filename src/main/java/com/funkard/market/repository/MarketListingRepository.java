package com.funkard.market.repository;

import com.funkard.market.model.MarketListing;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Repository
public class MarketListingRepository {
    public List<MarketListing> findSoldAfter(LocalDateTime since) {
        // TODO: implementare con datasource reale (DB o API)
        return Collections.emptyList();
    }

    public List<MarketListing> findRecentSold(String itemName, String setName, String category, String condition, LocalDateTime since) {
        // TODO: implementare con datasource reale (DB o API)
        return Collections.emptyList();
    }
}
