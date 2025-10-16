package com.funkard.market.repository;

import com.funkard.market.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    int countByNameIgnoreCase(String name);
    
    long countByCreatedAtAfter(LocalDate date);
    
    @Query("SELECT AVG((p.price - p.estimatedValue) / p.estimatedValue * 100) FROM Product p WHERE p.createdAt >= :date AND p.price IS NOT NULL AND p.estimatedValue IS NOT NULL")
    double calculateAverageValueChangeLast30Days(@Param("date") LocalDate date);
    
    default double calculateAverageValueChangeLast30Days() {
        return calculateAverageValueChangeLast30Days(LocalDate.now().minusDays(30));
    }
}
