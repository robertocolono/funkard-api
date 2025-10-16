package com.funkard.admin.service;

import com.funkard.admin.notification.AdminNotification;
import com.funkard.admin.notification.AdminNotification.Severity;
import com.funkard.admin.notification.AdminNotification.Type;
import com.funkard.admin.notification.AdminNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AdminNotificationService {

    private final AdminNotificationRepository repo;

    public AdminNotificationService(AdminNotificationRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public AdminNotification create(Type type, Severity severity, String title, String message, Map<String, Object> metadata) {
        AdminNotification n = new AdminNotification();
        n.setType(type);
        n.setSeverity(severity);
        n.setTitle(title);
        n.setMessage(message);
        n.setMetadata(metadata);
        return repo.save(n);
    }

    // Helpers rapidi
    public AdminNotification systemError(String title, String message, Map<String,Object> meta) {
        return create(Type.SYSTEM, Severity.ERROR, title, message, meta);
    }

    public AdminNotification systemWarn(String title, String message, Map<String,Object> meta) {
        return create(Type.SYSTEM, Severity.WARN, title, message, meta);
    }

    public AdminNotification marketEvent(String title, String message, Map<String,Object> meta) {
        return create(Type.MARKET, Severity.INFO, title, message, meta);
    }

    public AdminNotification gradingEvent(String title, String message, Map<String,Object> meta) {
        return create(Type.GRADING, Severity.INFO, title, message, meta);
    }

    public AdminNotification supportTicket(String title, String message, Map<String,Object> meta) {
        return create(Type.SUPPORT, Severity.INFO, title, message, meta);
    }

    @Transactional
    public void resolve(Long id) {
        var nOpt = repo.findById(id);
        if (nOpt.isEmpty()) return;
        var n = nOpt.get();
        if (!n.isResolved()) {
            n.setResolved(true);
            n.setResolvedAt(LocalDateTime.now());
            repo.save(n);
        }
    }
}