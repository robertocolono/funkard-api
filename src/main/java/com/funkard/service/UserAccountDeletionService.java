package com.funkard.service;

import com.funkard.model.User;
import com.funkard.model.UserDeletion;
import com.funkard.repository.UserDeletionRepository;
import com.funkard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 🗑️ Service per gestione richieste cancellazione account
 * 
 * Funzionalità:
 * - Registrazione richiesta cancellazione
 * - Disabilitazione account immediata
 * - Creazione record per scheduler
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountDeletionService {
    
    private final UserRepository userRepository;
    private final UserDeletionRepository deletionRepository;
    
    /**
     * 📝 Registra richiesta cancellazione account
     * 
     * @param user Utente che richiede cancellazione
     * @param reason Motivo cancellazione (opzionale)
     * @return UserDeletion creato
     */
    @Transactional
    public UserDeletion requestAccountDeletion(User user, String reason) {
        log.info("📝 Richiesta cancellazione account per utente: {} ({})", user.getId(), user.getEmail());
        
        // Verifica se esiste già una richiesta
        deletionRepository.findByUserId(user.getId()).ifPresent(existing -> {
            log.warn("Richiesta cancellazione già esistente per utente: {}", user.getId());
            throw new IllegalStateException("Richiesta di cancellazione già presente per questo account");
        });
        
        // Marca account come pending deletion
        user.setDeletionPending(true);
        user.setDeletionRequestedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Crea record UserDeletion
        UserDeletion deletion = new UserDeletion();
        deletion.setUserId(user.getId());
        deletion.setEmail(user.getEmail());
        deletion.setRequestedAt(LocalDateTime.now());
        deletion.setScheduledDeletionAt(LocalDateTime.now().plusDays(7)); // 7 giorni di grazia
        deletion.setStatus(UserDeletion.DeletionStatus.PENDING);
        deletion.setReason(reason);
        
        UserDeletion saved = deletionRepository.save(deletion);
        
        log.info("✅ Richiesta cancellazione registrata per utente: {} - Cancellazione programmata per: {}", 
            user.getId(), saved.getScheduledDeletionAt());
        
        return saved;
    }
    
    /**
     * 🔍 Verifica se un utente ha una richiesta di cancellazione pending
     * @param userId ID utente
     * @return true se ha richiesta pending
     */
    public boolean hasPendingDeletionRequest(Long userId) {
        return deletionRepository.findByUserId(userId)
            .map(d -> d.getStatus() == UserDeletion.DeletionStatus.PENDING)
            .orElse(false);
    }
    
    /**
     * 🔍 Verifica se un utente ha account disabilitato per cancellazione
     * @param user Utente
     * @return true se account è pending deletion
     */
    public boolean isAccountPendingDeletion(User user) {
        return Boolean.TRUE.equals(user.getDeletionPending());
    }
}

