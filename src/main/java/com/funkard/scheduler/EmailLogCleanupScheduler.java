package com.funkard.scheduler;

import com.funkard.model.EmailLog;
import com.funkard.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🧹 Scheduler per cleanup log email vecchi (90 giorni)
 * 
 * Esegue ogni giorno alle 3:00 per rimuovere log più vecchi di 90 giorni.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailLogCleanupScheduler {
    
    private final EmailLogRepository logRepository;
    
    /**
     * ⏰ Job schedulato: eseguito ogni giorno alle 3:00
     * Cron: "0 0 3 * * *" = ogni giorno alle 3:00
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Rome")
    @Transactional
    public void cleanupOldEmailLogs() {
        log.info("🧹 [CLEANUP] Inizio pulizia log email vecchi...");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        
        List<EmailLog> oldLogs = logRepository.findOlderThan(cutoffDate);
        
        if (oldLogs.isEmpty()) {
            log.info("🧹 [CLEANUP] Nessun log email da pulire (tutti più recenti di 90 giorni)");
            return;
        }
        
        int count = oldLogs.size();
        logRepository.deleteAll(oldLogs);
        
        log.info("🧹 [CLEANUP] Puliti {} log email più vecchi di 90 giorni (cutoff: {})", count, cutoffDate);
    }
}

