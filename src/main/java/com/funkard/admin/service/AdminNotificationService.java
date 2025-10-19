package com.funkard.admin.service;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminNotificationService {

    private final AdminNotificationRepository repo;

    public AdminNotificationService(AdminNotificationRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public AdminNotification create(String type, String severity, String title, String message, Map<String, Object> metadata) {
        AdminNotification n = new AdminNotification();
        n.setType(type);
        n.setPriority(severity);
        n.setTitle(title);
        n.setMessage(message);
        return repo.save(n);
    }

    // Helpers rapidi
    public AdminNotification systemError(String title, String message, Map<String,Object> meta) {
        return create("SYSTEM", "ERROR", title, message, meta);
    }

    public AdminNotification systemWarn(String title, String message, Map<String,Object> meta) {
        return create("SYSTEM", "WARN", title, message, meta);
    }

    public AdminNotification marketEvent(String title, String message, Map<String,Object> meta) {
        return create("MARKET", "INFO", title, message, meta);
    }

    public AdminNotification gradingEvent(String title, String message, Map<String,Object> meta) {
        return create("GRADING", "INFO", title, message, meta);
    }

    public AdminNotification supportTicket(String title, String message, Map<String,Object> meta) {
        return create("SUPPORT", "INFO", title, message, meta);
    }

    @Transactional
    public AdminNotification markRead(UUID id, String userName) {
        AdminNotification n = repo.findById(id).orElseThrow();
        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
            n.setReadBy(userName);
            pushHistory(n, userName, "read", null);
            repo.save(n);
        }
        return n;
    }

    @Transactional
    public AdminNotification resolve(UUID id, String userName) {
        AdminNotification n = repo.findById(id).orElseThrow();
        if (n.getResolvedAt() == null) {
            n.setResolvedAt(LocalDateTime.now());
            n.setResolvedBy(userName);
            pushHistory(n, userName, "resolved", null);
            repo.save(n);
        }
        return n;
    }

    @Transactional
    public AdminNotification archive(UUID id, String userName) {
        AdminNotification n = repo.findById(id).orElseThrow();
        if (!n.isArchived()) {
            n.setArchived(true);
            n.setArchivedAt(LocalDateTime.now());
            pushHistory(n, userName, "archived", null);
            repo.save(n);
        }
        return n;
    }

    private void pushHistory(AdminNotification n, String user, String action, String details) {
        String history = n.getHistory() != null ? n.getHistory() : "";
        String entry = String.format("[%s] %s: %s%s", 
            LocalDateTime.now(), user, action, 
            details != null ? " - " + details : "");
        n.setHistory(history + (history.isEmpty() ? "" : "\n") + entry);
    }

    @Transactional
    public void resolve(UUID id) {
        var nOpt = repo.findById(id);
        if (nOpt.isEmpty()) return;
        var n = nOpt.get();
        if (!n.isRead()) {
            n.setRead(true);
            repo.save(n);
        }
    }

    public List<AdminNotification> getActiveNotifications() {
        return repo.findByReadFalseOrderByCreatedAtDesc();
    }

    @Transactional
    public int cleanupArchivedOlderThanDays(int days) {
        LocalDateTime limit = LocalDateTime.now().minusDays(days);
        return repo.deleteByArchivedTrueAndArchivedAtBefore(limit);
    }
}