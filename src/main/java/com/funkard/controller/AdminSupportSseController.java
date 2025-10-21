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
 * 📡 Controller SSE avanzato per admin panel con filtraggio per ruoli
 * Gestisce connessioni separate per super_admin, admin e support
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/support")
@CrossOrigin(origins = {"https://funkard-admin.vercel.app", "https://funkard.vercel.app", "http://localhost:3000"})
public class AdminSupportSseController {

    // Mappe separate per ruolo per filtraggio eventi
    private static final Map<String, SseEmitter> superAdminEmitters = new ConcurrentHashMap<>();
    private static final Map<String, SseEmitter> adminEmitters = new ConcurrentHashMap<>();
    private static final Map<String, SseEmitter> supportEmitters = new ConcurrentHashMap<>();

    /**
     * 🔌 Endpoint SSE per connessione admin con filtraggio per ruolo
     * GET /api/admin/support/stream?userId=123&role=admin
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestParam String userId,
            @RequestParam String role,
            HttpServletResponse response) {
        
        // Configura headers per SSE
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        // Memorizza connessione in base al ruolo
        switch (role.toLowerCase()) {
            case "super_admin" -> {
                superAdminEmitters.put(userId, emitter);
                log.info("👑 Super Admin connesso: {} ({} attivi)", userId, superAdminEmitters.size());
            }
            case "admin" -> {
                adminEmitters.put(userId, emitter);
                log.info("🧭 Admin connesso: {} ({} attivi)", userId, adminEmitters.size());
            }
            case "support" -> {
                supportEmitters.put(userId, emitter);
                log.info("🎧 Support connesso: {} ({} attivi)", userId, supportEmitters.size());
            }
            default -> {
                log.warn("⚠️ Ruolo non riconosciuto: {}", role);
                return emitter;
            }
        }

        // Gestione eventi connessione
        emitter.onCompletion(() -> {
            removeEmitter(userId, role);
            log.info("🔌 {} disconnesso dal support stream", role);
        });
        
        emitter.onTimeout(() -> {
            removeEmitter(userId, role);
            log.info("⏰ Timeout connessione {} stream per: {}", role, userId);
        });
        
        emitter.onError((e) -> {
            removeEmitter(userId, role);
            log.warn("❌ Errore connessione {} stream per {}: {}", role, userId, e.getMessage());
        });

        // Messaggio di conferma connessione
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                        "message", "✅ Connessione al support admin attiva per " + role,
                        "role", role,
                        "userId", userId,
                        "timestamp", System.currentTimeMillis()
                    )));
        } catch (IOException e) {
            log.error("❌ Errore invio messaggio di conferma per {}: {}", userId, e.getMessage());
        }

        return emitter;
    }

    /**
     * 🗑️ Rimuove emitter dalla mappa corretta
     */
    private static void removeEmitter(String userId, String role) {
        switch (role.toLowerCase()) {
            case "super_admin" -> superAdminEmitters.remove(userId);
            case "admin" -> adminEmitters.remove(userId);
            case "support" -> supportEmitters.remove(userId);
        }
    }

    /**
     * 📡 Broadcast globale a tutti i ruoli
     */
    public static void broadcastEvent(String eventName, Object data) {
        sendToEmitters(superAdminEmitters, eventName, data);
        sendToEmitters(adminEmitters, eventName, data);
        sendToEmitters(supportEmitters, eventName, data);
        log.debug("📡 Evento '{}' broadcasted a tutti i ruoli", eventName);
    }

    /**
     * 🎯 Invia evento solo a un ruolo specifico
     */
    public static void sendToRole(String role, String eventName, Object data) {
        switch (role.toLowerCase()) {
            case "super_admin" -> {
                sendToEmitters(superAdminEmitters, eventName, data);
                log.debug("👑 Evento '{}' inviato a Super Admin", eventName);
            }
            case "admin" -> {
                sendToEmitters(adminEmitters, eventName, data);
                log.debug("🧭 Evento '{}' inviato a Admin", eventName);
            }
            case "support" -> {
                sendToEmitters(supportEmitters, eventName, data);
                log.debug("🎧 Evento '{}' inviato a Support", eventName);
            }
            default -> log.warn("⚠️ Ruolo non riconosciuto per invio evento: {}", role);
        }
    }

    /**
     * 👤 Invia evento a un utente specifico
     */
    public static void sendToUser(String userId, String role, String eventName, Object data) {
        SseEmitter emitter = switch (role.toLowerCase()) {
            case "super_admin" -> superAdminEmitters.get(userId);
            case "admin" -> adminEmitters.get(userId);
            case "support" -> supportEmitters.get(userId);
            default -> null;
        };

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.debug("📡 Evento '{}' inviato a {} ({}): {}", eventName, role, userId, data);
            } catch (IOException e) {
                log.warn("❌ Errore invio evento '{}' a {} ({}): {}", eventName, role, userId, e.getMessage());
                emitter.complete();
                removeEmitter(userId, role);
            }
        } else {
            log.debug("⚠️ Nessuna connessione attiva per {} ({}): {}", role, userId, eventName);
        }
    }

    /**
     * 📡 Invia evento a tutti gli emitter di una mappa
     */
    private static void sendToEmitters(Map<String, SseEmitter> emitters, String eventName, Object data) {
        emitters.entrySet().removeIf(entry -> {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                return false; // Non rimuovere
            } catch (IOException e) {
                log.warn("❌ Emitter morto rimosso: {}", entry.getKey());
                return true; // Rimuovi
            }
        });
    }

    /**
     * 🎫 Notifica nuovo ticket (admin e super_admin)
     */
    public static void notifyNewTicket(Object ticketData) {
        // Assicurati che ticketData contenga email per filtraggio frontend
        sendToRole("admin", "new-ticket", ticketData);
        sendToRole("super_admin", "new-ticket", ticketData);
    }

    /**
     * 💬 Notifica nuovo messaggio (admin e super_admin)
     */
    public static void notifyNewMessage(Object messageData) {
        sendToRole("admin", "new-message", messageData);
        sendToRole("super_admin", "new-message", messageData);
    }

    /**
     * 🎯 Notifica ticket assegnato (solo al support specifico)
     */
    public static void notifyTicketAssigned(String assignedToId, Object ticketData) {
        sendToUser(assignedToId, "support", "ticket-assigned", ticketData);
        sendToRole("super_admin", "ticket-assigned", ticketData);
    }

    /**
     * ✅ Notifica ticket risolto (super_admin)
     */
    public static void notifyTicketResolved(Object ticketData) {
        sendToRole("super_admin", "ticket-resolved", ticketData);
    }

    /**
     * 🔒 Notifica ticket chiuso (super_admin)
     */
    public static void notifyTicketClosed(Object ticketData) {
        sendToRole("super_admin", "ticket-closed", ticketData);
    }

    /**
     * 🔄 Notifica aggiornamento stato (admin e super_admin)
     */
    public static void notifyStatusUpdate(Object ticketData) {
        sendToRole("admin", "status-update", ticketData);
        sendToRole("super_admin", "status-update", ticketData);
    }

    /**
     * 📊 Statistiche connessioni per ruolo
     */
    @GetMapping("/stream/stats")
    public Map<String, Object> getStreamStats() {
        return Map.of(
            "superAdminConnections", superAdminEmitters.size(),
            "adminConnections", adminEmitters.size(),
            "supportConnections", supportEmitters.size(),
            "totalConnections", superAdminEmitters.size() + adminEmitters.size() + supportEmitters.size(),
            "status", "active",
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 🔄 Keep-alive per mantenere connessioni attive
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30000)
    public void keepAlive() {
        if (!superAdminEmitters.isEmpty() || !adminEmitters.isEmpty() || !supportEmitters.isEmpty()) {
            sendToEmitters(superAdminEmitters, "ping", "keep-alive");
            sendToEmitters(adminEmitters, "ping", "keep-alive");
            sendToEmitters(supportEmitters, "ping", "keep-alive");
            
            int total = superAdminEmitters.size() + adminEmitters.size() + supportEmitters.size();
            log.debug("🔄 Keep-alive inviato a {} connessioni totali", total);
        }
    }
}
