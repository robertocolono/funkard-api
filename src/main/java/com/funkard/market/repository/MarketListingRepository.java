package com.funkard.market.repository;

import com.funkard.market.model.MarketListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MarketListingRepository extends JpaRepository<MarketListing, Long> {
    
    @Query("SELECT m FROM MarketListing m WHERE m.sold = true AND m.soldAt >= :since")
    List<MarketListing> findSoldAfter(LocalDateTime since);
    
    @Query("SELECT m FROM MarketListing m WHERE m.sold = true AND m.itemName = :itemName AND m.setName = :setName AND m.category = :category AND m.condition = :condition AND m.soldAt >= :since")
    List<MarketListing> findRecentSold(String itemName, String setName, String category, String condition, LocalDateTime since);
    
    @Query("""
        SELECT m FROM MarketListing m
        WHERE m.sold = true AND m.itemName = :itemName AND m.category = :category
        ORDER BY m.soldAt DESC
        """)
    List<MarketListing> findLastSold(String itemName, String category);
}
