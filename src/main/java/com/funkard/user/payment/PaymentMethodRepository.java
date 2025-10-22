package com.funkard.user.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 💳 Repository per gestione metodi di pagamento
 * 
 * 🔍 Query ottimizzate per operazioni comuni
 * ✅ Filtri per utente e stato
 * ✅ Supporto per metodo predefinito
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    
    /**
     * 📋 Trova tutti i metodi di pagamento di un utente
     */
    List<PaymentMethod> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * 🎯 Trova il metodo di pagamento predefinito di un utente
     */
    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(String userId);
    
    /**
     * 🔢 Conta i metodi di pagamento di un utente
     */
    long countByUserId(String userId);
    
    /**
     * ❌ Trova metodi di pagamento scaduti
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.userId = :userId AND " +
           "EXTRACT(YEAR FROM CURRENT_DATE) > CAST(SUBSTRING(pm.expiryDate, 4, 2) AS int) + 2000 OR " +
           "(EXTRACT(YEAR FROM CURRENT_DATE) = CAST(SUBSTRING(pm.expiryDate, 4, 2) AS int) + 2000 AND " +
           "EXTRACT(MONTH FROM CURRENT_DATE) > CAST(SUBSTRING(pm.expiryDate, 1, 2) AS int))")
    List<PaymentMethod> findExpiredByUserId(@Param("userId") String userId);
    
    /**
     * 🏷️ Trova metodi di pagamento per brand
     */
    List<PaymentMethod> findByUserIdAndBrand(String userId, String brand);
    
    /**
     * 🗑️ Elimina tutti i metodi di pagamento di un utente
     */
    void deleteByUserId(String userId);
    
    /**
     * 🔍 Verifica se un utente ha metodi di pagamento
     */
    boolean existsByUserId(String userId);
    
    /**
     * 📊 Statistiche metodi di pagamento per utente
     */
    @Query("SELECT pm.brand, COUNT(pm) FROM PaymentMethod pm WHERE pm.userId = :userId GROUP BY pm.brand")
    List<Object[]> getBrandStatistics(@Param("userId") String userId);
}
