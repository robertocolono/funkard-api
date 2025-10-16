package com.funkard.market.trend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrendRepository extends JpaRepository<Trend, Long> {
    List<Trend> findByItemNameAndCategoryAndRangeType(String itemName, String category, String rangeType);
    List<Trend> findByManualCheckTrue();
    long countByManualCheckTrue();
    long countByManualCheckTrueAndUpdatedAtAfter(LocalDateTime start);
    boolean existsByItemNameAndCategory(String itemName, String category);
    
    List<Trend> findByUpdatedAtAfter(LocalDateTime date);
}
