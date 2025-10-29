package com.funkard.repository;

import com.funkard.model.UserAddress;
import com.funkard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * 🏠 Repository per gestione indirizzi utente
 * 
 * ✅ Query ottimizzate
 * ✅ Metodi per default address
 * ✅ Filtri per utente
 */
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    
    /**
     * 📋 Ottieni tutti gli indirizzi di un utente
     */
    List<UserAddress> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 🎯 Ottieni indirizzo predefinito di un utente
     */
    Optional<UserAddress> findByUserAndIsDefaultTrue(User user);
    
    /**
     * 📊 Conta indirizzi per utente
     */
    long countByUser(User user);
    
    /**
     * 🔍 Verifica se un indirizzo appartiene a un utente
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM UserAddress a WHERE a.id = :addressId AND a.user.id = :userId")
    boolean existsByIdAndUserId(@Param("addressId") Long addressId, @Param("userId") Long userId);
    
    /**
     * 🏠 Ottieni indirizzo per ID e utente (sicurezza)
     */
    @Query("SELECT a FROM UserAddress a WHERE a.id = :addressId AND a.user.id = :userId")
    Optional<UserAddress> findByIdAndUserId(@Param("addressId") Long addressId, @Param("userId") Long userId);
    
    /**
     * 🗑️ Elimina indirizzo per ID e utente
     */
    void deleteByIdAndUserId(Long addressId, Long userId);
}
