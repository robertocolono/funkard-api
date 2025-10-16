package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final UserNotificationRepository repository;

    public List<UserNotification> getByUser(String userId) {
        return repository.findByUserId(userId);
    }

    public List<UserNotification> getUnreadByUser(String userId) {
        return repository.findByUserIdAndReadFalse(userId);
    }

    public List<UserNotification> getUnresolvedByUser(String userId) {
        return repository.findByUserIdAndResolvedFalse(userId);
    }

    public List<UserNotification> getByReference(String referenceType, Long referenceId) {
        return repository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    public List<UserNotification> getByType(String type) {
        return repository.findByType(type);
    }

    public List<UserNotification> getByUserAndType(String userId, String type) {
        return repository.findByUserIdAndType(userId, type);
    }

    public UserNotification create(UserNotification notification) {
        return repository.save(notification);
    }

    public UserNotification createForUser(String userId, String title, String message, String type,
                                         String referenceType, Long referenceId) {
        UserNotification notification = UserNotification.builder()
                .userId(userId)
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

    public void markAllAsReadForUser(String userId) {
        List<UserNotification> notifications = repository.findByUserIdAndReadFalse(userId);
        notifications.forEach(n -> {
            n.setRead(true);
            repository.save(n);
        });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public long countUnreadByUser(String userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    public long countUnresolvedByUser(String userId) {
        return repository.countByUserIdAndResolvedFalse(userId);
    }

    public long countByType(String type) {
        return repository.countByType(type);
    }
}
