package com.funkard.market.repository;

import com.funkard.market.model.MarketValuation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MarketValuationRepository extends JpaRepository<MarketValuation, Long> {
    Optional<MarketValuation> findByItemNameAndSetNameAndCategoryAndCondition(
            String itemName, String setName, String category, String condition
    );
}
