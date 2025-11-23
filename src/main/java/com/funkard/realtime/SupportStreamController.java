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
 * 📡 Controller SSE per real-time support stream (utenti finali)
 * 
 * Endpoint: /api/support/stream?email={userEmail}
 * Autenticazione: JWT Bearer token
 * 
 * Gestisce connessioni SSE per notifiche real-time agli utenti:
 * - Nuovi messaggi nei ticket
 * - Aggiornamenti stato ticket
 * - Notifiche varie
 */
@Slf4j
@RestController
@RequestMapping("/api/support/stream")
@CrossOrigin(origins = {"https://funkard.com", "https://www.funkard.com", "http://localhost:3000", "http://localhost:3002"})
public class SupportStreamController {

    /**
     * 📦 Mappa connessioni SSE attive per utente (email → SseEmitter)
     */
    private static final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    /**
     * 🔌 Endpoint SSE per connessione utente
     * GET /api/support/stream?email={userEmail}
     * 
     * Headers richiesti:
     * - Authorization: Bearer {jwt_token}
     * 
     * @param email Email dell'utente
     * @param response HttpServletResponse per configurare headers SSE
     * @return SseEmitter per stream eventi
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER')")
    public SseEmitter stream(
            @RequestParam String email,
            HttpServletResponse response) {
        
        // Configura headers SSE
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no"); // Disabilita buffering nginx
        
        // Crea emitter senza timeout
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Memorizza connessione
        SseEmitter previous = userEmitters.put(email, emitter);
        if (previous != null) {
            try {
                previous.complete();
            } catch (Exception e) {
                log.warn("⚠️ Chiusura connessione precedente per {}: {}", email, e.getMessage());
            }
        }
        
        log.info("✅ Utente connesso al support stream: {} (totale connessioni: {})", email, userEmitters.size());

        // Gestione eventi lifecycle
        emitter.onCompletion(() -> {
            userEmitters.remove(email);
            log.info("🔌 Utente disconnesso dal support stream: {}", email);
        });
        
        emitter.onTimeout(() -> {
            userEmitters.remove(email);
            log.warn("⏰ Timeout connessione support stream per: {}", email);
        });
        
        emitter.onError((e) -> {
            userEmitters.remove(email);
            log.error("❌ Errore connessione support stream per {}: {}", email, e.getMessage(), e);
        });

        // Messaggio di conferma connessione
        try {
            sendEvent(emitter, EventType.CONNECTED, Map.of(
                "message", "✅ Connessione al supporto Funkard attiva",
                "email", email,
                "timestamp", System.currentTimeMillis()
            ));
        } catch (IOException e) {
            log.error("❌ Errore invio messaggio di conferma per {}: {}", email, e.getMessage());
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 📡 Metodo statico per inviare eventi agli utenti
     * Utilizzato dai servizi per notificare gli utenti
     * 
     * @param email Email dell'utente destinatario
     * @param eventType Tipo di evento
     * @param data Dati dell'evento
     */
    public static void sendEventToUser(String email, EventType eventType, Map<String, Object> data) {
        SseEmitter emitter = userEmitters.get(email);
        if (emitter != null) {
            try {
                sendEvent(emitter, eventType, data);
                log.debug("📡 Evento '{}' inviato a {}: {}", eventType, email, data);
            } catch (IOException e) {
                log.warn("❌ Errore invio evento '{}' a {}: {}", eventType, email, e.getMessage());
                emitter.complete();
                userEmitters.remove(email);
            }
        } else {
            log.debug("⚠️ Nessuna connessione attiva per utente: {}", email);
        }
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
     * 🧪 Endpoint di test per invio manuale eventi (solo per sviluppo/test)
     * POST /api/support/stream/events
     * 
     * Body: {
     *   "email": "user@example.com",
     *   "eventType": "NEW_REPLY",
     *   "data": {...}
     * }
     */
    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> sendTestEvent(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        String eventTypeStr = (String) request.get("eventType");
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) request.get("data");

        if (email == null || eventTypeStr == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Email e eventType sono obbligatori"
            ));
        }

        try {
            EventType eventType = EventType.valueOf(eventTypeStr);
            sendEventToUser(email, eventType, data != null ? data : Map.of());
            
            log.info("🧪 Evento di test '{}' inviato a {}", eventType, email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Evento inviato con successo",
                "email", email,
                "eventType", eventType.getValue()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EventType non valido: " + eventTypeStr
            ));
        }
    }

    /**
     * 📊 Statistiche connessioni attive
     * GET /api/support/stream/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "connectedUsers", userEmitters.size(),
            "status", "active",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 🔄 Keep-alive automatico (ogni 30 secondi)
     * Mantiene connessioni attive e rileva connessioni morte
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30000)
    public void keepAlive() {
        if (!userEmitters.isEmpty()) {
            userEmitters.entrySet().removeIf(entry -> {
                try {
                    sendEvent(entry.getValue(), EventType.PING, Map.of("message", "keep-alive"));
                    return false; // Non rimuovere
                } catch (IOException e) {
                    log.debug("🔌 Rimozione connessione morta: {}", entry.getKey());
                    return true; // Rimuovi
                }
            });
            
            if (!userEmitters.isEmpty()) {
                log.debug("🔄 Keep-alive inviato a {} utenti", userEmitters.size());
            }
        }
    }
}

