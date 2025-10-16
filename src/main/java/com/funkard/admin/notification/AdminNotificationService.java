package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final AdminNotificationRepository repository;
    private final AdminNotificationArchiveRepository archiveRepository;
    private final AdminNotificationStreamController streamController;

    public List<AdminNotification> getAll() {
        return repository.findAll();
    }

    public List<AdminNotification> getUnread() {
        return repository.findByReadFalseOrderByCreatedAtDesc();
    }

    public List<AdminNotification> getUnresolved() {
        return repository.findByResolvedFalseOrderByCreatedAtDesc();
    }

    public List<AdminNotification> getByReference(String referenceType, Long referenceId) {
        return repository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    public List<AdminNotification> getByType(String type) {
        return repository.findByType(type);
    }

    public AdminNotification create(AdminNotification notification) {
        AdminNotification saved = repository.save(notification);
        streamController.sendNotification(saved); // 🔔 invio in tempo reale
        return saved;
    }

    public AdminNotification createWithReference(String title, String message, String type, 
                                                String referenceType, Long referenceId) {
        AdminNotification notification = AdminNotification.builder()
                .title(title)
                .message(message)
                .type(type)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();
        
        return create(notification);
    }

    public void markAsRead(Long id) {
        repository.findById(id).ifPresent(n -> {
            n.setRead(true);
            repository.save(n);
        });
    }

    public void markAsResolved(Long id) {
        repository.findById(id).ifPresent(n -> {
            n.setResolved(true);
            n.setResolvedAt(LocalDateTime.now());
            repository.save(n);
        });
    }

    public void archiveNotification(Long id) {
        Optional<AdminNotification> notificationOpt = repository.findById(id);
        if (notificationOpt.isPresent()) {
            AdminNotification notification = notificationOpt.get();
            
            // Crea archivio
            AdminNotificationArchive archive = AdminNotificationArchive.builder()
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .type(notification.getType())
                    .referenceType(notification.getReferenceType())
                    .referenceId(notification.getReferenceId())
                    .resolvedAt(notification.getResolvedAt())
                    .build();
            
            archiveRepository.save(archive);
            repository.deleteById(id);
        }
    }

    public void resolveAndArchive(Long id) {
        Optional<AdminNotification> notificationOpt = repository.findById(id);
        if (notificationOpt.isPresent()) {
            AdminNotification notification = notificationOpt.get();
            
            // Segna come risolta
            notification.setResolved(true);
            notification.setResolvedAt(LocalDateTime.now());
            repository.save(notification);
            
            // Archivia automaticamente
            archiveNotification(id);
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public long countUnread() {
        return repository.countByReadFalse();
    }

    public long countUnresolved() {
        return repository.countByResolvedFalse();
    }

    public long countByType(String type) {
        return repository.countByType(type);
    }
}
