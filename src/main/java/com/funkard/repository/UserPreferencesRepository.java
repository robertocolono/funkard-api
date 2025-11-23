package com.funkard.repository;

import com.funkard.model.User;
import com.funkard.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 📦 Repository per gestione preferenze utente
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    
    /**
     * 🔍 Trova preferenze per utente
     * @param user Utente
     * @return Optional<UserPreferences>
     */
    Optional<UserPreferences> findByUser(User user);
    
    /**
     * 🔍 Trova preferenze per ID utente
     * @param userId ID utente
     * @return Optional<UserPreferences>
     */
    Optional<UserPreferences> findByUserId(Long userId);
    
    /**
     * 🗑️ Elimina preferenze per utente
     * @param user Utente
     */
    void deleteByUser(User user);
}

