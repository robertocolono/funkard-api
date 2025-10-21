package com.funkard.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/api/admin/support")
public class AdminSupportStreamController {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam(required = false) String token) {
        // TODO: Aggiungere validazione token se necessario
        // if (token == null || !validateToken(token)) {
        //     throw new UnauthorizedException("Token non valido");
        // }
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("🔌 Admin disconnected from ticket stream ({} attivi)", emitters.size());
        });
        
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.info("⏰ Admin timeout from ticket stream ({} attivi)", emitters.size());
        });
        
        emitter.onError((e) -> {
            emitters.remove(emitter);
            log.warn("❌ Admin connection error: {}", e.getMessage());
        });

        log.info("🟢 Admin connesso al ticket stream ({} attivi)", emitters.size());
        return emitter;
    }

    /** Metodo per notificare tutti gli admin */
    public void sendEvent(String eventName, Object eventData) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(eventData));
                log.debug("📡 Event '{}' sent to admin: {}", eventName, eventData);
            } catch (IOException e) {
                log.warn("❌ Failed to send event '{}' to admin: {}", eventName, e.getMessage());
                deadEmitters.add(emitter);
            }
        });
        
        // Rimuovi emitters morti
        emitters.removeAll(deadEmitters);
        
        if (!emitters.isEmpty()) {
            log.info("📡 Event '{}' broadcasted to {} admin(s)", eventName, emitters.size());
        }
    }

    /** Metodo di compatibilità per eventi generici */
    public void sendEvent(Object eventData) {
        sendEvent("ticket-update", eventData);
    }

    /** Conta admin connessi */
    public int getConnectedAdmins() {
        return emitters.size();
    }

    /** Alias per compatibilità */
    public int getActiveConnections() {
        return emitters.size();
    }

    /** Endpoint per statistiche connessioni */
    @GetMapping("/stream/stats")
    public Map<String, Object> getStreamStats() {
        return Map.of(
                "connectedAdmins", emitters.size(),
                "status", "active",
                "timestamp", System.currentTimeMillis()
        );
    }

    /** 🔄 Keep-alive per mantenere connessioni attive */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30000)
    public void keepAlive() {
        if (!emitters.isEmpty()) {
            sendEvent("ping", "keep-alive");
            log.debug("🔄 Keep-alive sent to {} admin(s)", emitters.size());
        }
    }
}
