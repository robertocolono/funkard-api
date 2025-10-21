package com.funkard.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 📡 Controller SSE per notifiche real-time agli utenti
 * Gestisce le connessioni streaming per gli utenti finali
 */
@Slf4j
@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = {"https://funkard.vercel.app", "https://funkardnew.vercel.app", "http://localhost:3000"})
public class SupportSseController {

    // Mappa globale per tenere aperte le connessioni SSE per ciascun utente
    private static final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    /**
     * 🔌 Endpoint SSE per connessione utente
     * GET /api/support/stream?email=user@example.com
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestParam String email, 
            HttpServletResponse response) {
        
        // Configura headers per SSE
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Nessun timeout
        
        // Memorizza connessione per utente
        userEmitters.put(email, emitter);
        log.info("🟢 Utente connesso al support stream: {}", email);

        // Gestione eventi connessione
        emitter.onCompletion(() -> {
            userEmitters.remove(email);
            log.info("🔌 Utente disconnesso dal support stream: {}", email);
        });
        
        emitter.onTimeout(() -> {
            userEmitters.remove(email);
            log.info("⏰ Timeout connessione support stream per: {}", email);
        });
        
        emitter.onError((e) -> {
            userEmitters.remove(email);
            log.warn("❌ Errore connessione support stream per {}: {}", email, e.getMessage());
        });

        // Messaggio di conferma connessione
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                        "message", "✅ Connessione al supporto Funkard attiva",
                        "timestamp", System.currentTimeMillis(),
                        "userEmail", email
                    )));
        } catch (IOException e) {
            log.error("❌ Errore invio messaggio di conferma per {}: {}", email, e.getMessage());
        }

        return emitter;
    }

    /**
     * 🔔 Metodo statico per inviare eventi agli utenti
     * Utilizzato dai servizi per notificare gli utenti
     */
    public static void sendEventToUser(String email, String eventName, Object data) {
        SseEmitter emitter = userEmitters.get(email);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.debug("📡 Evento '{}' inviato a {}: {}", eventName, email, data);
            } catch (IOException e) {
                log.warn("❌ Errore invio evento '{}' a {}: {}", eventName, email, e.getMessage());
                emitter.complete();
                userEmitters.remove(email);
            }
        } else {
            log.debug("⚠️ Nessuna connessione attiva per utente: {}", email);
        }
    }

    /**
     * 🔔 Metodo per inviare notifica nuova risposta
     */
    public static void notifyNewReply(String userEmail, String ticketId, String agentName, String messagePreview) {
        sendEventToUser(userEmail, "ticket-reply", Map.of(
            "type", "ticket-reply",
            "ticketId", ticketId,
            "email", userEmail,
            "agentName", agentName,
            "messagePreview", messagePreview,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 🔔 Metodo per notificare ticket risolto
     */
    public static void notifyTicketResolved(String userEmail, String ticketId, String status) {
        sendEventToUser(userEmail, "ticket-resolved", Map.of(
            "type", "ticket-resolved",
            "ticketId", ticketId,
            "email", userEmail,
            "status", status,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 🔔 Metodo per notificare ticket chiuso
     */
    public static void notifyTicketClosed(String userEmail, String ticketId) {
        sendEventToUser(userEmail, "ticket-closed", Map.of(
            "type", "ticket-closed",
            "ticketId", ticketId,
            "email", userEmail,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 🔔 Metodo per notificare conferma apertura ticket
     */
    public static void notifyTicketCreated(String userEmail, String ticketId, String subject) {
        sendEventToUser(userEmail, "ticket-created", Map.of(
            "type", "ticket-created",
            "ticketId", ticketId,
            "email", userEmail,
            "subject", subject,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 🔔 Metodo per notificare aggiornamento stato
     */
    public static void notifyStatusUpdate(String userEmail, String ticketId, String oldStatus, String newStatus) {
        sendEventToUser(userEmail, "status-update", Map.of(
            "type", "status-update",
            "ticketId", ticketId,
            "email", userEmail,
            "oldStatus", oldStatus,
            "newStatus", newStatus,
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * 📊 Metodo per ottenere statistiche connessioni
     */
    @GetMapping("/stream/stats")
    public Map<String, Object> getStreamStats() {
        return Map.of(
            "connectedUsers", userEmitters.size(),
            "status", "active",
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 🔄 Keep-alive per mantenere connessioni attive
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30000)
    public void keepAlive() {
        if (!userEmitters.isEmpty()) {
            userEmitters.forEach((email, emitter) -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("ping")
                            .data("keep-alive"));
                } catch (IOException e) {
                    emitter.complete();
                    userEmitters.remove(email);
                }
            });
            log.debug("🔄 Keep-alive inviato a {} utenti", userEmitters.size());
        }
    }
}
