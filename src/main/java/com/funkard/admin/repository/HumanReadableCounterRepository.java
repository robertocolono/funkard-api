package com.funkard.admin.repository;

import com.funkard.admin.model.HumanReadableCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 📋 Repository per contatori numerazione umana
 * Usa SELECT FOR UPDATE per thread-safety
 */
public interface HumanReadableCounterRepository extends JpaRepository<HumanReadableCounter, Long> {
    
    /**
     * Lock esclusivo per thread-safety (SELECT FOR UPDATE)
     * Usato durante generazione numero per prevenire race condition
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM HumanReadableCounter c WHERE c.prefix = :prefix AND c.year = :year")
    Optional<HumanReadableCounter> findByPrefixAndYearForUpdate(
        @Param("prefix") String prefix, 
        @Param("year") Integer year
    );
    
    /**
     * Metodo standard (senza lock) per letture
     */
    Optional<HumanReadableCounter> findByPrefixAndYear(String prefix, Integer year);
}

