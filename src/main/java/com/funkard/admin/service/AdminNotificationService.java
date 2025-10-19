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

    public List<AdminNotification> filter(String type, String priority, String status) {
        return repo.filter(emptyToNull(type), emptyToNull(priority), emptyToNull(status));
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
    public AdminNotification resolve(UUID id, String userName, String note) {
        AdminNotification n = repo.findById(id).orElseThrow();
        n.setResolvedAt(LocalDateTime.now());
        n.setResolvedBy(userName);
        pushHistory(n, userName, "resolve", note);
        return repo.save(n);
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
    public long cleanupArchivedOlderThanDays(int days) {
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
}