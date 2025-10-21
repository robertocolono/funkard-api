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
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("🔌 Admin disconnected from ticket stream ({})", emitters.size());
        });
        
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.info("⏰ Admin timeout from ticket stream ({})", emitters.size());
        });

        log.info("✅ Admin connected to ticket stream ({})", emitters.size());
        return emitter;
    }

    /** Metodo per notificare tutti gli admin */
    public void sendEvent(Object eventData) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("ticket-update")
                        .data(eventData));
                log.debug("📡 Event sent to admin: {}", eventData);
            } catch (IOException e) {
                log.warn("❌ Failed to send event to admin: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        });
        
        // Rimuovi emitters morti
        emitters.removeAll(deadEmitters);
        
        if (!emitters.isEmpty()) {
            log.info("📡 Event broadcasted to {} admin(s)", emitters.size());
        }
    }

    /** Conta admin connessi */
    public int getConnectedAdmins() {
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
}
