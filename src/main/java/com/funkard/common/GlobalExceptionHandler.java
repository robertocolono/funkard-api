package com.funkard.common;

import com.funkard.admin.service.AdminNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AdminNotificationService notifications;

    public GlobalExceptionHandler(AdminNotificationService notifications) {
        this.notifications = notifications;
    }

    /**
     * 🔒 Gestisce errori di validazione Jakarta (es. @NotNull, @Valid)
     * Utile per validazione GDPR e altri campi obbligatori
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage() != null 
                        ? error.getDefaultMessage() 
                        : "Campo non valido",
                    (existing, replacement) -> existing
                ));
        
        // Se ci sono errori GDPR, restituisci messaggio specifico
        if (errors.containsKey("acceptTerms") || errors.containsKey("acceptPrivacy")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Per completare la registrazione devi accettare Termini e Privacy Policy.");
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
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
