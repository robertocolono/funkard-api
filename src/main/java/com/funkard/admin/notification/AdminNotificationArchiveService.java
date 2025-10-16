package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationArchiveService {

    private final AdminNotificationArchiveRepository archiveRepository;

    public void archiveNotification(AdminNotification notification) {
        AdminNotificationArchive archive = AdminNotificationArchive.builder()
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .resolvedAt(notification.getResolvedAt())
                .build();
        
        archiveRepository.save(archive);
    }

    public List<AdminNotificationArchive> getArchivedNotifications() {
        return archiveRepository.findAll();
    }

    public List<AdminNotificationArchive> getArchivedByReference(String referenceType, Long referenceId) {
        return archiveRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    public List<AdminNotificationArchive> getArchivedByType(String type) {
        return archiveRepository.findByType(type);
    }

    // 🧹 Pulizia automatica ogni giorno alle 2:00
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldArchives() {
        LocalDateTime cutoff = LocalDate.now().minusDays(30).atStartOfDay();
        archiveRepository.deleteByResolvedAtBefore(cutoff);
        System.out.println("🧹 Cleanup archivio completato - rimossi record più vecchi di 30 giorni");
    }

    // 🧹 Pulizia manuale per test
    public void manualCleanup(int daysOld) {
        LocalDateTime cutoff = LocalDate.now().minusDays(daysOld).atStartOfDay();
        archiveRepository.deleteByResolvedAtBefore(cutoff);
    }

    public long countArchivedNotifications() {
        return archiveRepository.count();
    }

    public long countArchivedByType(String type) {
        return archiveRepository.countByType(type);
    }
}
