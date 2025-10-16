package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final AdminNotificationRepository repository;
    private final AdminNotificationStreamController streamController;

    public List<AdminNotification> getAll() {
        return repository.findAll();
    }

    public List<AdminNotification> getUnread() {
        return repository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    public AdminNotification create(AdminNotification notification) {
        AdminNotification saved = repository.save(notification);
        streamController.sendNotification(saved); // 🔔 invio in tempo reale
        return saved;
    }

    public void markAsRead(UUID id) {
        repository.findById(id).ifPresent(n -> {
            n.setRead(true);
            repository.save(n);
        });
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
