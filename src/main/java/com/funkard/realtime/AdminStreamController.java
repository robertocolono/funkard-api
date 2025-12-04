package com.funkard.realtime;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 📡 Controller SSE per real-time admin support stream
 * 
 * Endpoint: /api/admin/support/stream?userId={adminId}&role={role}
 * Autenticazione: JWT Bearer token con ruolo ADMIN o SUPER_ADMIN
 * 
 * Gestisce connessioni SSE separate per ruolo:
 * - SUPER_ADMIN: riceve tutti gli eventi
 * - ADMIN: riceve eventi generali
 * - SUPPORT: riceve solo ticket assegnati
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/support/stream")
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "https://admin.funkard.com", "http://localhost:3000", "http://localhost:3002"})
public class AdminStreamController {

    /**
     * 📦 Mappe connessioni SSE per ruolo
     */
    private static final Map<String, SseEmitter> superAdminEmitters = new ConcurrentHashMap<>();
    private static final Map<String, SseEmitter> adminEmitters = new ConcurrentHashMap<>();
    private static final Map<String, SseEmitter> supportEmitters = new ConcurrentHashMap<>();

    /**
     * 🔌 Endpoint SSE per connessione admin
     * GET /api/admin/support/stream?userId={adminId}&role={role}
     * 
     * Headers richiesti:
     * - Authorization: Bearer {jwt_token}
     * 
     * @param userId ID o email dell'admin
     * @param role Ruolo: SUPER_ADMIN, ADMIN, SUPPORT
     * @param response HttpServletResponse per configurare headers SSE
     * @return SseEmitter per stream eventi
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public SseEmitter stream(
            @RequestParam String userId,
            @RequestParam String role,
            HttpServletResponse response) {
        
        // Configura headers SSE
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");
        
        // Normalizza ruolo
        String normalizedRole = role.toUpperCase();
        
        // Crea emitter
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Memorizza connessione in base al ruolo
        Map<String, SseEmitter> targetMap = getEmitterMap(normalizedRole);
        if (targetMap == null) {
            log.warn("⚠️ Ruolo non riconosciuto: {}", role);
            emitter.completeWithError(new IllegalArgumentException("Ruolo non valido: " + role));
            return emitter;
        }
        
        // Rimuovi connessione precedente se esiste
        SseEmitter previous = targetMap.put(userId, emitter);
        if (previous != null) {
            try {
                previous.complete();
            } catch (Exception e) {
                log.warn("⚠️ Chiusura connessione precedente per {} ({}): {}", userId, normalizedRole, e.getMessage());
            }
        }
        
        log.info("✅ {} connesso al admin stream: {} (totale {}: {})", 
            normalizedRole, userId, normalizedRole, targetMap.size());

        // Gestione eventi lifecycle
        emitter.onCompletion(() -> {
            targetMap.remove(userId);
            log.info("🔌 {} disconnesso dal admin stream: {}", normalizedRole, userId);
        });
        
        emitter.onTimeout(() -> {
            targetMap.remove(userId);
            log.warn("⏰ Timeout connessione {} stream per: {}", normalizedRole, userId);
        });
        
        emitter.onError((e) -> {
            targetMap.remove(userId);
            log.error("❌ Errore connessione {} stream per {}: {}", normalizedRole, userId, e.getMessage(), e);
        });

        // Messaggio di conferma connessione
        try {
            sendEvent(emitter, EventType.CONNECTED, Map.of(
                "message", "✅ Connessione al support admin attiva per " + normalizedRole,
                "role", normalizedRole,
                "userId", userId,
                "timestamp", System.currentTimeMillis()
            ));
        } catch (IOException e) {
            log.error("❌ Errore invio messaggio di conferma per {}: {}", userId, e.getMessage());
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 📡 Invia evento a tutti i ruoli (broadcast)
     */
    public static void broadcastEvent(EventType eventType, Map<String, Object> data) {
        sendToEmitters(superAdminEmitters, eventType, data);
        sendToEmitters(adminEmitters, eventType, data);
        sendToEmitters(supportEmitters, eventType, data);
        log.debug("📡 Evento '{}' broadcasted a tutti i ruoli", eventType);
    }

    /**
     * 🎯 Invia evento solo a un ruolo specifico
     */
    public static void sendToRole(String role, EventType eventType, Map<String, Object> data) {
        Map<String, SseEmitter> targetMap = getEmitterMap(role.toUpperCase());
        if (targetMap != null) {
            sendToEmitters(targetMap, eventType, data);
            log.debug("📡 Evento '{}' inviato a ruolo {}", eventType, role);
        } else {
            log.warn("⚠️ Ruolo non riconosciuto per invio evento: {}", role);
        }
    }

    /**
     * 👤 Invia evento a un utente specifico
     */
    public static void sendToUser(String userId, String role, EventType eventType, Map<String, Object> data) {
        Map<String, SseEmitter> targetMap = getEmitterMap(role.toUpperCase());
        if (targetMap != null) {
            SseEmitter emitter = targetMap.get(userId);
            if (emitter != null) {
                try {
                    sendEvent(emitter, eventType, data);
                    log.debug("📡 Evento '{}' inviato a {} ({}): {}", eventType, role, userId, data);
                } catch (IOException e) {
                    log.warn("❌ Errore invio evento '{}' a {} ({}): {}", eventType, role, userId, e.getMessage());
                    emitter.complete();
                    targetMap.remove(userId);
                }
            } else {
                log.debug("⚠️ Nessuna connessione attiva per {} ({}): {}", role, userId, eventType);
            }
        }
    }

    /**
     * 📡 Invia evento a tutti gli emitter di una mappa
     */
    private static void sendToEmitters(Map<String, SseEmitter> emitters, EventType eventType, Map<String, Object> data) {
        emitters.entrySet().removeIf(entry -> {
            try {
                sendEvent(entry.getValue(), eventType, data);
                return false; // Non rimuovere
            } catch (IOException e) {
                log.debug("🔌 Rimozione emitter morto: {}", entry.getKey());
                return true; // Rimuovi
            }
        });
    }

    /**
     * 📡 Helper per inviare evento SSE
     */
    private static void sendEvent(SseEmitter emitter, EventType eventType, Map<String, Object> data) throws IOException {
        Map<String, Object> eventPayload = Map.of(
            "type", eventType.getValue(),
            "data", data,
            "timestamp", System.currentTimeMillis()
        );
        
        emitter.send(SseEmitter.event()
                .name(eventType.getValue())
                .data(eventPayload));
    }

    /**
     * 🗺️ Ottiene la mappa emitter per ruolo
     */
    private static Map<String, SseEmitter> getEmitterMap(String role) {
        return switch (role.toUpperCase()) {
            case "SUPER_ADMIN" -> superAdminEmitters;
            case "ADMIN" -> adminEmitters;
            case "SUPPORT" -> supportEmitters;
            default -> null;
        };
    }

    /**
     * 🧪 Endpoint di test per invio manuale eventi (solo per sviluppo/test)
     * POST /api/admin/support/stream/events
     */
    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> sendTestEvent(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String role = (String) request.get("role");
        String eventTypeStr = (String) request.get("eventType");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.get("data");

        if (userId == null || role == null || eventTypeStr == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "userId, role e eventType sono obbligatori"
            ));
        }

        try {
            EventType eventType = EventType.valueOf(eventTypeStr);
            
            if (userId.equals("*")) {
                // Broadcast a tutti
                broadcastEvent(eventType, data != null ? data : Map.of());
            } else if (role != null) {
                // Invia a utente specifico
                sendToUser(userId, role, eventType, data != null ? data : Map.of());
            } else {
                // Invia a ruolo
                sendToRole(role, eventType, data != null ? data : Map.of());
            }
            
            log.info("🧪 Evento di test '{}' inviato a {} ({})", eventType, userId, role);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Evento inviato con successo",
                "userId", userId,
                "role", role,
                "eventType", eventType.getValue()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EventType non valido: " + eventTypeStr
            ));
        }
    }

    /**
     * 📊 Statistiche connessioni per ruolo
     * GET /api/admin/support/stream/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        int total = superAdminEmitters.size() + adminEmitters.size() + supportEmitters.size();
        return ResponseEntity.ok(Map.of(
            "superAdminConnections", superAdminEmitters.size(),
            "adminConnections", adminEmitters.size(),
            "supportConnections", supportEmitters.size(),
            "totalConnections", total,
            "status", "active",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 🔄 Keep-alive automatico (ogni 30 secondi)
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30000)
    public void keepAlive() {
        sendToEmitters(superAdminEmitters, EventType.PING, Map.of("message", "keep-alive"));
        sendToEmitters(adminEmitters, EventType.PING, Map.of("message", "keep-alive"));
        sendToEmitters(supportEmitters, EventType.PING, Map.of("message", "keep-alive"));
        
        int total = superAdminEmitters.size() + adminEmitters.size() + supportEmitters.size();
        if (total > 0) {
            log.debug("🔄 Keep-alive inviato a {} connessioni admin", total);
        }
    }
}


