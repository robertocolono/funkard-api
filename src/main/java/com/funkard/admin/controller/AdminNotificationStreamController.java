package com.funkard.admin.controller;

import com.funkard.admin.model.AdminNotification;
import com.funkard.admin.service.AdminNotificationService;
import org.springframework.http.MediaType;
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
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitters.add(emitter);
        
        // Rimuovi l'emitter quando si disconnette
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));
        
        // Invia notifiche esistenti
        try {
            List<AdminNotification> activeNotifications = notificationService.getActiveNotifications();
            for (AdminNotification notification : activeNotifications) {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            }
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        
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

}
