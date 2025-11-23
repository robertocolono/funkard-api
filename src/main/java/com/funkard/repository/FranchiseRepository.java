package com.funkard.repository;

import com.funkard.model.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 📚 Repository per franchise ufficiali
 */
@Repository
public interface FranchiseRepository extends JpaRepository<Franchise, Long> {
    
    /**
     * Trova tutti i franchise attivi
     */
    List<Franchise> findByStatusOrderByCategoryAscNameAsc(Franchise.FranchiseStatus status);
    
    /**
     * Trova franchise per categoria e stato
     */
    List<Franchise> findByCategoryAndStatusOrderByNameAsc(String category, Franchise.FranchiseStatus status);
    
    /**
     * Trova franchise per nome (case-insensitive)
     */
    Optional<Franchise> findByNameIgnoreCase(String name);
    
    /**
     * Verifica se franchise esiste per nome
     */
    boolean existsByNameIgnoreCase(String name);
    
    /**
     * Conta franchise per stato
     */
    long countByStatus(Franchise.FranchiseStatus status);
}

