package com.funkard.admin.service;

import com.funkard.admin.dto.NotificationDTO;
import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdminNotificationService {
    
    private final AdminNotificationRepository repository;
    
    public AdminNotificationService(AdminNotificationRepository repository) {
        this.repository = repository;
    }
    
    // 🔹 Lista tutte le notifiche
    public List<NotificationDTO> getAllNotifications() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }
    
    // 🔹 Lista notifiche non lette
    public List<NotificationDTO> getUnreadNotifications() {
        return repository.findByReadFalseOrderByCreatedAtDesc()
                .stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }
    
    // 🔹 Aggiungi nuova notifica
    public NotificationDTO addNotification(String type, String title, String message, String priority) {
        AdminNotification notification = new AdminNotification(type, title, message, priority);
        AdminNotification saved = repository.save(notification);
        return NotificationDTO.fromEntity(saved);
    }
    
    // 🔹 Marca come letta
    public void markAsRead(UUID id) {
        AdminNotification notification = repository.findById(id).orElseThrow();
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        repository.save(notification);
    }
    
    // 🔹 Elimina notifica
    public void deleteNotification(UUID id) {
        repository.deleteById(id);
    }
    
    // 🔹 Conta notifiche non lette
    public long getUnreadCount() {
        return repository.countByReadFalse();
    }
    
    // 🔹 Notifiche per tipo
    public List<NotificationDTO> getNotificationsByType(String type) {
        return repository.findByTypeOrderByCreatedAtDesc(type)
                .stream()
                .map(NotificationDTO::fromEntity)
                .toList();
    }
}