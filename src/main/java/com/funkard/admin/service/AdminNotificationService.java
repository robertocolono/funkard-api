package com.funkard.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.repository.AdminNotificationRepository;
import com.funkard.admin.service.HumanReadableNumberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AdminNotificationService {

    private final AdminNotificationRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final HumanReadableNumberService numberService;

    public AdminNotificationService(AdminNotificationRepository repo, HumanReadableNumberService numberService) {
        this.repo = repo;
        this.numberService = numberService;
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

    private Map<String, Object> buildErrorContext(Map<String, Object> metadata) {
        Map<String, Object> context = new HashMap<>();
        
        // Source sempre "backend"
        context.put("source", "backend");
        
        // Recupera request da RequestContextHolder (se disponibile)
        try {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();
                if (request != null) {
                    // Endpoint: method + URI
                    String method = request.getMethod();
                    String uri = request.getRequestURI();
                    if (method != null && uri != null) {
                        context.put("endpoint", method + " " + uri);
                    }
                }
            }
        } catch (Exception e) {
            // Fallback: non aggiungere contesto request se non disponibile
        }
        
        // Aggiungi metadata passati (service, action, ecc.)
        if (metadata != null) {
            if (metadata.containsKey("service")) {
                context.put("service", metadata.get("service"));
            }
            if (metadata.containsKey("action")) {
                context.put("action", metadata.get("action"));
            }
            // Aggiungi altri campi metadata se presenti
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                if (!context.containsKey(entry.getKey()) && 
                    !entry.getKey().equals("service") && 
                    !entry.getKey().equals("action")) {
                    context.put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        // Environment
        String env = System.getenv("ENVIRONMENT");
        if (env == null || env.isBlank()) {
            env = System.getProperty("spring.profiles.active", "production");
        }
        if (env != null && !env.isBlank()) {
            context.put("environment", env);
        }
        
        return context.isEmpty() ? null : context;
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
        createAdminNotification(title, message, priority, type, null);
    }

    public void createAdminNotification(String title, String message, String priority, String type, Map<String, Object> errorContext) {
        AdminNotification n = new AdminNotification();
        n.setTitle(title);
        n.setMessage(message);
        n.setPriority(priority);
        n.setType(type);
        n.setRead(false);
        
        // Salva errorContext solo per notifiche system/error|warn
        if (errorContext != null && "system".equals(type) && ("error".equals(priority) || "warn".equals(priority))) {
            try {
                n.setErrorContext(mapper.writeValueAsString(errorContext));
            } catch (Exception e) {
                // Fallback: non salvare contesto se serializzazione fallisce
                n.setErrorContext(null);
            }
        } else {
            n.setErrorContext(null);
        }
        
        // Genera numero umano (solo per system/error|warn)
        String prefix = numberService.determinePrefixForNotification(type, priority);
        if (prefix != null) {
            try {
                String humanNumber = numberService.generateHumanReadableNumber(prefix);
                n.setHumanReadableNumber(humanNumber);
            } catch (Exception e) {
                // Fallback: non bloccare creazione notifica se generazione numero fallisce
                // Log warning già gestito in HumanReadableNumberService
                n.setHumanReadableNumber(null);
            }
        }
        
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
        Map<String, Object> errorContext = buildErrorContext(metadata);
        createAdminNotification(title, message, "error", "system", errorContext);
    }

    public void gradingEvent(String title, String message, Map<String, Object> metadata) {
        createAdminNotification(title, message, "normal", "grading");
    }

    // Metodi aggiuntivi per compatibilità con altri servizi
    public void marketEvent(String title, String message, Map<String, Object> metadata) {
        createAdminNotification(title, message, "normal", "market");
    }

    public void systemWarn(String title, String message, Map<String, Object> metadata) {
        Map<String, Object> errorContext = buildErrorContext(metadata);
        createAdminNotification(title, message, "warn", "system", errorContext);
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
    // Metodo privato non utilizzato localmente ma potrebbe essere usato via reflection o in futuro
    @SuppressWarnings("unused")
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