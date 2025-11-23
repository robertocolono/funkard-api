package com.funkard.repository;

import com.funkard.model.FranchiseCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 📚 Repository per catalogo franchise
 */
@Repository
public interface FranchiseCatalogRepository extends JpaRepository<FranchiseCatalog, Long> {
    
    /**
     * Trova tutti i franchise attivi
     */
    List<FranchiseCatalog> findByActiveTrueOrderByCategoryAscNameAsc();
    
    /**
     * Trova franchise per categoria (solo attivi)
     */
    List<FranchiseCatalog> findByCategoryAndActiveTrueOrderByNameAsc(String category);
    
    /**
     * Trova franchise per nome (case-insensitive)
     */
    @Query("SELECT f FROM FranchiseCatalog f WHERE LOWER(f.name) = LOWER(:name) AND f.active = true")
    Optional<FranchiseCatalog> findByNameIgnoreCaseAndActiveTrue(@Param("name") String name);
    
    /**
     * Trova franchise per categoria e nome (per verifica duplicati)
     */
    Optional<FranchiseCatalog> findByCategoryAndNameIgnoreCase(String category, String name);
    
    /**
     * Trova tutti i franchise (inclusi disattivati) - per admin
     */
    List<FranchiseCatalog> findAllByOrderByCategoryAscNameAsc();
    
    /**
     * Conta franchise attivi per categoria
     */
    long countByCategoryAndActiveTrue(String category);
}

