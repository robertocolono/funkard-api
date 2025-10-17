package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.service.AdminNotificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = {"https://funkard-admin.vercel.app", "https://funkard.vercel.app", "http://localhost:3000"})
public class AdminNotificationStreamController {

    private final AdminNotificationService notificationService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public AdminNotificationStreamController(AdminNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        SseEmitter emitter = new SseEmitter(30000L); // 30 secondi timeout
        
        try {
            emitters.add(emitter);
            
            // Invia evento di connessione
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("SSE connection established"));

            // Invia notifiche esistenti
            List<AdminNotification> activeNotifications = notificationService.getActiveNotifications();
            for (AdminNotification notification : activeNotifications) {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            }
            
        } catch (IOException e) {
            System.err.println("Error in SSE stream: " + e.getMessage());
            emitter.completeWithError(e);
            return emitter;
        }
        
        // Gestione eventi
        emitter.onCompletion(() -> {
            System.out.println("SSE connection completed");
            emitters.remove(emitter);
        });
        
        emitter.onTimeout(() -> {
            System.out.println("SSE connection timed out");
            emitters.remove(emitter);
        });
        
        emitter.onError((ex) -> {
            System.err.println("SSE connection error: " + ex.getMessage());
            emitters.remove(emitter);
        });
        
        return emitter;
    }

    // Metodo per inviare notifiche a tutti i client connessi
    public void broadcastNotification(AdminNotification notification) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        
        emitters.removeAll(deadEmitters);
    }

    // Endpoint di test per verificare che SSE funzioni
    @GetMapping("/test")
    public ResponseEntity<String> testSSE() {
        return ResponseEntity.ok("SSE endpoint is working. Connect to /stream for real-time notifications.");
    }

}
