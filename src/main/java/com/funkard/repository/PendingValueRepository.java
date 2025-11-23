package com.funkard.repository;

import com.funkard.model.PendingValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ⏳ Repository per valori personalizzati pending
 */
@Repository
public interface PendingValueRepository extends JpaRepository<PendingValue, UUID> {
    
    /**
     * Trova tutte le proposte pending (non approvate)
     */
    List<PendingValue> findByApprovedFalseOrderByCreatedAtDesc();
    
    /**
     * Trova proposte per tipo
     */
    List<PendingValue> findByTypeAndApprovedFalseOrderByCreatedAtDesc(PendingValue.ValueType type);
    
    /**
     * Trova proposte per utente
     */
    List<PendingValue> findBySubmittedByIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Verifica se esiste già una proposta identica (pending o approvata)
     */
    @Query("SELECT p FROM PendingValue p WHERE p.type = :type AND LOWER(TRIM(p.value)) = LOWER(TRIM(:value))")
    Optional<PendingValue> findByTypeAndValueIgnoreCase(@Param("type") PendingValue.ValueType type, @Param("value") String value);
    
    /**
     * Conta proposte pending per tipo
     */
    long countByTypeAndApprovedFalse(PendingValue.ValueType type);
    
    /**
     * Trova proposte approvate
     */
    List<PendingValue> findByApprovedTrueOrderByApprovedAtDesc();
}

