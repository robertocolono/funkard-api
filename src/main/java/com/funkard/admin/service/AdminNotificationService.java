package com.funkard.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AdminNotificationService {

    private final AdminNotificationRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public AdminNotificationService(AdminNotificationRepository repo) {
        this.repo = repo;
    }

    public List<AdminNotification> listActiveChrono() {
        return repo.findByArchivedFalseOrderByCreatedAtAsc();
    }
    
    public org.springframework.data.domain.Page<AdminNotification> listActiveChronoPaginated(org.springframework.data.domain.Pageable pageable) {
        return repo.findByArchivedFalseOrderByCreatedAtDesc(pageable);
    }

    public List<AdminNotification> filter(String type, String priority, String status) {
        return repo.filter(emptyToNull(type), emptyToNull(priority), emptyToNull(status));
    }
    
    public org.springframework.data.domain.Page<AdminNotification> filterPaginated(String type, String priority, String status, org.springframework.data.domain.Pageable pageable) {
        return repo.filterPaginated(emptyToNull(type), emptyToNull(priority), emptyToNull(status), pageable);
    }

    public Optional<AdminNotification> get(UUID id) {
        return repo.findById(id);
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
    public AdminNotification archive(UUID id, String userName, String note) {
        AdminNotification n = repo.findById(id).orElseThrow();
        n.setArchived(true);
        n.setArchivedAt(LocalDateTime.now());
        pushHistory(n, userName, "archive", note);
        return repo.save(n);
    }

    @Transactional
    public AdminNotification assign(UUID id, String userName) {
        AdminNotification n = repo.findById(id).orElseThrow();
        if (n.getAssignedTo() == null) {
            n.setAssignedTo(userName);
            n.setAssignedAt(LocalDateTime.now());
            pushHistory(n, userName, "assigned", null);
            return repo.save(n);
        }
        return n; // Già assegnata, ritorna senza modifiche
    }

    @Transactional
    public int cleanupArchivedOlderThanDays(int days) {
        LocalDateTime limit = LocalDateTime.now().minusDays(days);
        return repo.deleteByArchivedTrueAndArchivedAtBefore(limit);
    }

    private void pushHistory(AdminNotification n, String user, String action, String note) {
        try {
            List<Map<String, Object>> history = new ArrayList<>();
            if (n.getHistory() != null && !n.getHistory().isBlank()) {
                history = mapper.readValue(n.getHistory(), new TypeReference<>() {});
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("user", user);
            entry.put("action", action);
            entry.put("at", LocalDateTime.now().toString());
            if (note != null && !note.isBlank()) entry.put("note", note);
            history.add(entry);

            n.setHistory(mapper.writeValueAsString(history));
        } catch (Exception e) {
            n.setHistory("[{\"user\":\"" + user + "\",\"action\":\"" + action + "\",\"at\":\"" + LocalDateTime.now() + "\"}]");
        }
    }

    private String emptyToNull(String v) {
        return (v == null || v.isBlank()) ? null : v;
    }

    // === SSE Broadcasting ===
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // infinito
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    public void createAdminNotification(String title, String message, String priority, String type) {
        AdminNotification n = new AdminNotification();
        n.setTitle(title);
        n.setMessage(message);
        n.setPriority(priority);
        n.setType(type);
        n.setRead(false);
        AdminNotification saved = repo.save(n);

        broadcast(saved); // 🚀 invia live agli admin
    }

    private void broadcast(AdminNotification n) {
        List<SseEmitter> dead = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(n));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }

        emitters.removeAll(dead);
    }

    // Metodi aggiuntivi per compatibilità
    public List<AdminNotification> getActiveNotifications() {
        return listActiveChrono();
    }

    public void systemError(String title, String message, Map<String, Object> metadata) {
        createAdminNotification(title, message, "error", "system");
    }

    public void gradingEvent(String title, String message, Map<String, Object> metadata) {
        createAdminNotification(title, message, "normal", "grading");
    }

    // Metodi aggiuntivi per compatibilità con altri servizi
    public void marketEvent(String title, String message, Map<String, Object> metadata) {
        createAdminNotification(title, message, "normal", "market");
    }

    public void systemWarn(String title, String message, Map<String, Object> metadata) {
        createAdminNotification(title, message, "warn", "system");
    }

    // Metodo resolve con firma corretta per compatibilità
    @Transactional
    public void resolve(UUID id, String resolvedBy, String note) {
        var notification = repo.findById(id).orElse(null);
        if (notification != null) {
            notification.setArchivedAt(LocalDateTime.now());
            notification.setRead(true);
            notification.setReadBy(resolvedBy);
            notification.setResolvedAt(LocalDateTime.now());
            notification.setResolvedBy(resolvedBy);
            pushHistory(notification, resolvedBy, "resolve", note);
            repo.save(notification);
        }
    }


    // Metodo di supporto per creare notifiche
    private void createNotification(String type, String title, String message, Map<String, ?> data) {
        var notif = new AdminNotification();
        notif.setType(type);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setPriority("normal");
        notif.setCreatedAt(LocalDateTime.now());
        notif.setRead(false);
        AdminNotification saved = repo.save(notif);
        broadcast(saved);
    }

    // Metodi per notifiche non lette
    public long getUnreadCount() {
        return repo.countByReadFalse();
    }

    public List<AdminNotification> getUnreadLatest() {
        return repo.findTop10ByReadFalseAndArchivedFalseOrderByCreatedAtDesc();
    }
}