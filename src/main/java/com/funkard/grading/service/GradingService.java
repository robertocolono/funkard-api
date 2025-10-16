package com.funkard.grading.service;

import com.funkard.admin.service.AdminNotificationService;
import com.funkard.admin.notification.AdminNotification;
import com.funkard.grading.model.GradingRequest;
import com.funkard.grading.repository.GradingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class GradingService {

    private final GradingRepository repo;
    private final AdminNotificationService notifications;

    public GradingService(GradingRepository repo, AdminNotificationService notifications) {
        this.repo = repo;
        this.notifications = notifications;
    }

    @Transactional
    public void sendForGrading(GradingRequest request) {
        try {
            // 👉 qui metti la logica reale di invio grading (es. chiamata API)
            // gradingApi.send(request);
            request.setStatus("IN_PROGRESS");
            request.setUpdatedAt(LocalDateTime.now());
            repo.save(request);

        } catch (Exception e) {
            // 🔴 Se qualcosa va storto, invia notifica admin
            notifications.create(
                    AdminNotification.Type.GRADING,
                    AdminNotification.Severity.ERROR,
                    "Errore invio grading",
                    "Errore durante l'invio della carta #" + request.getCardId() + " per grading: " + e.getMessage(),
                    Map.of("cardId", request.getCardId(), "error", e.getMessage(), "userId", request.getUserId())
            );

            // Rilancia per non nascondere l'errore
            throw e;
        }
    }

    @Transactional
    public void updateGradingStatus(Long cardId, String status) {
        try {
            var grading = repo.findByCardId(cardId)
                    .orElseThrow(() -> new RuntimeException("Grading non trovato per cardId=" + cardId));

            grading.setStatus(status);
            grading.setUpdatedAt(LocalDateTime.now());
            repo.save(grading);

        } catch (Exception e) {
            // 🔴 Notifica anche in caso di errore aggiornamento
            notifications.create(
                    AdminNotification.Type.GRADING,
                    AdminNotification.Severity.ERROR,
                    "Errore aggiornamento grading",
                    "Errore durante aggiornamento status grading carta #" + cardId + ": " + e.getMessage(),
                    Map.of("cardId", cardId, "error", e.getMessage())
            );
            throw e;
        }
    }

    @Transactional
    public void markGradingAsFailed(Long cardId, String errorMessage) {
        try {
            var grading = repo.findByCardId(cardId)
                    .orElseThrow(() -> new RuntimeException("Grading non trovato per cardId=" + cardId));

            grading.setStatus("FAILED");
            grading.setErrorMessage(errorMessage);
            grading.setUpdatedAt(LocalDateTime.now());
            repo.save(grading);

            // 🔴 Notifica admin per grading fallito
            notifications.create(
                    AdminNotification.Type.GRADING,
                    AdminNotification.Severity.ERROR,
                    "Grading fallito",
                    "Il grading della carta #" + cardId + " è fallito: " + errorMessage,
                    Map.of("cardId", cardId, "errorMessage", errorMessage)
            );

        } catch (Exception e) {
            // 🔴 Notifica anche in caso di errore nel salvataggio del fallimento
            notifications.create(
                    AdminNotification.Type.GRADING,
                    AdminNotification.Severity.ERROR,
                    "Errore salvataggio grading fallito",
                    "Errore durante il salvataggio del fallimento grading carta #" + cardId + ": " + e.getMessage(),
                    Map.of("cardId", cardId, "originalError", errorMessage, "saveError", e.getMessage())
            );
            throw e;
        }
    }

    @Transactional
    public void markGradingAsCompleted(Long cardId, String result) {
        try {
            var grading = repo.findByCardId(cardId)
                    .orElseThrow(() -> new RuntimeException("Grading non trovato per cardId=" + cardId));

            grading.setStatus("COMPLETED");
            grading.setUpdatedAt(LocalDateTime.now());
            repo.save(grading);

            // ℹ️ Notifica info per grading completato
            notifications.create(
                    AdminNotification.Type.GRADING,
                    AdminNotification.Severity.INFO,
                    "Grading completato",
                    "Il grading della carta #" + cardId + " è stato completato con successo",
                    Map.of("cardId", cardId, "result", result)
            );

        } catch (Exception e) {
            // 🔴 Notifica anche in caso di errore nel salvataggio del completamento
            notifications.create(
                    AdminNotification.Type.GRADING,
                    AdminNotification.Severity.ERROR,
                    "Errore salvataggio grading completato",
                    "Errore durante il salvataggio del completamento grading carta #" + cardId + ": " + e.getMessage(),
                    Map.of("cardId", cardId, "result", result, "saveError", e.getMessage())
            );
            throw e;
        }
    }
}
