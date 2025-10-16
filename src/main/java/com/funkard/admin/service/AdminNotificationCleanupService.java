package com.funkard.admin.service;

import com.funkard.admin.notification.AdminNotificationRepository;
import com.funkard.admin.notification.AdminNotificationArchiveRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@EnableAsync
public class AdminNotificationCleanupService {

    private final AdminNotificationRepository repository;
    private final AdminNotificationArchiveRepository archiveRepository;

    public AdminNotificationCleanupService(AdminNotificationRepository repository, 
                                         AdminNotificationArchiveRepository archiveRepository) {
        this.repository = repository;
        this.archiveRepository = archiveRepository;
    }

    // 🧹 Pulizia automatica ogni giorno alle 03:00
    @Async
    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Rome")
    @Transactional
    public void cleanOldNotifications() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            
            // 1. Pulisce notifiche risolte da più di 30 giorni
            int deletedResolved = repository.deleteAllResolvedBefore(cutoff);
            
            // 2. Pulisce archivio da più di 30 giorni
            archiveRepository.deleteByResolvedAtBefore(cutoff);
            int deletedArchived = 0; // Il metodo non restituisce il count
            
            int totalDeleted = deletedResolved + deletedArchived;
            
            if (totalDeleted > 0) {
                System.out.println("🧹 [CLEANUP] Pulizia completata:");
                System.out.println("   - Notifiche risolte rimosse: " + deletedResolved);
                System.out.println("   - Archivio pulito: " + deletedArchived);
                System.out.println("   - Totale rimosso: " + totalDeleted + " record");
            } else {
                System.out.println("🧹 [CLEANUP] Nessuna notifica da pulire (tutte più recenti di 30 giorni)");
            }
            
        } catch (Exception e) {
            System.err.println("❌ [CLEANUP] Errore durante la pulizia notifiche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🧹 Pulizia manuale per test
    @Transactional
    public int manualCleanup(int daysOld) {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
            
            int deletedResolved = repository.deleteAllResolvedBefore(cutoff);
            archiveRepository.deleteByResolvedAtBefore(cutoff);
            int deletedArchived = 0; // Il metodo non restituisce il count
            
            System.out.println("🧹 [MANUAL CLEANUP] Rimossi " + (deletedResolved + deletedArchived) + " record più vecchi di " + daysOld + " giorni");
            
            return deletedResolved + deletedArchived;
            
        } catch (Exception e) {
            System.err.println("❌ [MANUAL CLEANUP] Errore: " + e.getMessage());
            return 0;
        }
    }

    // 📊 Statistiche pulizia
    public void printCleanupStats() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            
            long resolvedCount = repository.count();
            long archivedCount = archiveRepository.count();
            
            System.out.println("📊 [CLEANUP STATS] Stato attuale:");
            System.out.println("   - Notifiche risolte: " + resolvedCount);
            System.out.println("   - Archivio totale: " + archivedCount);
            System.out.println("   - Prossima pulizia: notifiche più vecchie di " + cutoff);
            
        } catch (Exception e) {
            System.err.println("❌ [CLEANUP STATS] Errore: " + e.getMessage());
        }
    }
}
