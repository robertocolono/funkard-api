package com.funkard.admin.service;

import com.funkard.admin.repository.AdminNotificationRepository;
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

    public AdminNotificationCleanupService(AdminNotificationRepository repository) {
        this.repository = repository;
    }

    // 🧹 Pulizia automatica ogni giorno alle 03:00
    @Async
    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Rome")
    @Transactional
    public void cleanOldNotifications() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            
            // 1. Pulisce notifiche risolte da più di 30 giorni
            long deletedResolved = repository.deleteAllResolvedBefore(cutoff);
            
            long totalDeleted = deletedResolved;
            
            if (totalDeleted > 0) {
                System.out.println("🧹 [CLEANUP] Pulizia completata:");
                System.out.println("   - Notifiche risolte rimosse: " + deletedResolved);
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
            
            long deletedResolved = repository.deleteAllResolvedBefore(cutoff);
            
            System.out.println("🧹 [MANUAL CLEANUP] Rimossi " + deletedResolved + " record più vecchi di " + daysOld + " giorni");
            
            return (int) deletedResolved;
            
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
            
            System.out.println("📊 [CLEANUP STATS] Stato attuale:");
            System.out.println("   - Notifiche totali: " + resolvedCount);
            System.out.println("   - Prossima pulizia: notifiche più vecchie di " + cutoff);
            
        } catch (Exception e) {
            System.err.println("❌ [CLEANUP STATS] Errore: " + e.getMessage());
        }
    }
}
