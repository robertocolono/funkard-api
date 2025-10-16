package com.funkard.common;

import com.funkard.admin.service.AdminNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AdminNotificationService notifications;

    public GlobalExceptionHandler(AdminNotificationService notifications) {
        this.notifications = notifications;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        notifications.systemError("Errore runtime backend", ex.getMessage(), Map.of());
        return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        notifications.systemError("Errore generico sistema", ex.getMessage(), Map.of());
        return ResponseEntity.internalServerError().body(Map.of("error", "Errore interno del server"));
    }
}
