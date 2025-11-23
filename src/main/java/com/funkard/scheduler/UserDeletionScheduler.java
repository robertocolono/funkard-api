package com.funkard.scheduler;

import com.funkard.model.UserDeletion;
import com.funkard.model.User;
import com.funkard.repository.UserDeletionRepository;
import com.funkard.repository.UserRepository;
import com.funkard.service.UserDeletionService;
import com.funkard.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🗑️ Scheduler per cancellazione automatica account (GDPR Art. 17)
 * 
 * Esegue ogni ora per processare le richieste di cancellazione scadute.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeletionScheduler {
    
    private final UserDeletionRepository deletionRepository;
    private final UserRepository userRepository;
    private final UserDeletionService deletionService;
    private final EmailService emailService;
    
    /**
     * ⏰ Job schedulato: eseguito ogni ora
     * Cron: "0 0 * * * *" = ogni ora allo scoccare del minuto 0
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Europe/Rome")
    @Transactional
    public void processPendingDeletions() {
        log.info("🗑️ [SCHEDULER] Inizio processo cancellazione account in scadenza...");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Recupera tutte le richieste PENDING con scheduledDeletionAt <= now
        List<UserDeletion> pendingDeletions = deletionRepository.findByStatusAndScheduledDeletionAtLessThanEqual(
            UserDeletion.DeletionStatus.PENDING,
            now
        );
        
        if (pendingDeletions.isEmpty()) {
            log.debug("🗑️ [SCHEDULER] Nessuna cancellazione da processare");
            return;
        }
        
        log.info("🗑️ [SCHEDULER] Trovate {} richieste di cancellazione da processare", pendingDeletions.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (UserDeletion deletion : pendingDeletions) {
            try {
                log.info("🗑️ [SCHEDULER] Processando cancellazione per utente: {} ({})", 
                    deletion.getUserId(), deletion.getEmail());
                
                // 📧 Recupera dati utente PRIMA della cancellazione (per email)
                User user = userRepository.findById(deletion.getUserId()).orElse(null);
                String userLocale = "en"; // Default inglese (fallback sicuro)
                String userName = deletion.getEmail().split("@")[0]; // Fallback
                
                if (user != null) {
                    // Recupera locale da user.language se disponibile (supporta 25+ lingue)
                    if (user.getLanguage() != null && !user.getLanguage().isEmpty()) {
                        userLocale = user.getLanguage().toLowerCase();
                    }
                    
                    // Recupera nome utente se disponibile
                    if (user.getNome() != null && !user.getNome().isEmpty()) {
                        userName = user.getNome();
                    } else if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                        userName = user.getUsername();
                    }
                }
                
                // Esegui cancellazione definitiva
                boolean success = deletionService.permanentlyDeleteUser(
                    deletion.getUserId(),
                    deletion.getEmail()
                );
                
                if (success) {
                    // 📧 Invia email di conferma cancellazione DOPO cancellazione riuscita
                    try {
                        boolean emailSent = emailService.sendAccountDeletionCompletedEmailWithRetry(
                            deletion.getEmail(),
                            userLocale,
                            userName
                        );
                        
                        if (emailSent) {
                            log.info("✅ Email conferma cancellazione inviata con successo a: {}", deletion.getEmail());
                        } else {
                            log.warn("⚠️ Invio email conferma cancellazione fallito dopo retry per: {}", deletion.getEmail());
                        }
                    } catch (Exception e) {
                        log.error("❌ Errore critico durante invio email conferma cancellazione per utente {}: {}", 
                            deletion.getUserId(), e.getMessage(), e);
                        // Non bloccare il processo se l'email fallisce
                    }
                    
                    // Aggiorna stato a COMPLETED dopo invio email
                    deletion.setStatus(UserDeletion.DeletionStatus.COMPLETED);
                    deletion.setCompletedAt(LocalDateTime.now());
                    deletionRepository.save(deletion);
                    
                    log.info("✅ [SCHEDULER] Utente {} ({}) cancellato definitivamente il {}", 
                        deletion.getUserId(), deletion.getEmail(), LocalDateTime.now());
                    successCount++;
                } else {
                    throw new RuntimeException("Cancellazione fallita senza eccezione");
                }
                
            } catch (Exception e) {
                log.error("❌ [SCHEDULER] Errore durante cancellazione utente {} ({}): {}", 
                    deletion.getUserId(), deletion.getEmail(), e.getMessage(), e);
                
                // Marca come FAILED per ritentare al prossimo ciclo
                deletion.setStatus(UserDeletion.DeletionStatus.FAILED);
                deletionRepository.save(deletion);
                
                failureCount++;
            }
        }
        
        log.info("🗑️ [SCHEDULER] Processo completato: {} successi, {} fallimenti", successCount, failureCount);
    }
    
}

