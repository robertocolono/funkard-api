package com.funkard.admin.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationEventService {

    // private final AdminNotificationService adminNotificationService; // Temporarily disabled
    private final UserNotificationService userNotificationService;

    // 🔔 Eventi automatici per admin
    public void notifyNewCard(String cardName, String username, Long cardId) {
        adminNotificationService.createWithReference(
            "Nuova carta caricata",
            "Utente " + username + " ha caricato: " + cardName,
            "INFO",
            "CARD",
            cardId
        );
    }

    public void notifyCardReported(String cardName, String reason, Long cardId) {
        adminNotificationService.createWithReference(
            "Carta segnalata",
            "Carta " + cardName + " segnalata per: " + reason,
            "WARNING",
            "CARD",
            cardId
        );
    }

    public void notifyValuationRequested(String username, String cardName, Long cardId) {
        adminNotificationService.createWithReference(
            "Richiesta valutazione",
            "Utente " + username + " richiede valutazione per: " + cardName,
            "INFO",
            "CARD",
            cardId
        );
    }

    public void notifyTransactionFailed(String transactionId, String error) {
        adminNotificationService.createWithReference(
            "Transazione fallita",
            "Errore pagamento ID#" + transactionId + ": " + error,
            "ERROR",
            "TRANSACTION",
            Long.valueOf(transactionId)
        );
    }

    public void notifySupportTicketOpened(String username, String subject, Long ticketId) {
        adminNotificationService.createWithReference(
            "Nuovo ticket supporto",
            "Ticket da " + username + ": " + subject,
            "SUPPORT",
            "TICKET",
            ticketId
        );
    }

    public void notifySystemError(String component, String error) {
        adminNotificationService.createWithReference(
            "Errore sistema",
            "Errore in " + component + ": " + error,
            "ERROR",
            "SYSTEM",
            null
        );
    }

    // 🔔 Eventi automatici per utenti
    public void notifyUserCardEvaluated(String userId, String cardName, String grade) {
        userNotificationService.createForUser(
            userId,
            "Valutazione completata",
            "La tua carta " + cardName + " è stata valutata: " + grade,
            "SUCCESS",
            "CARD",
            null
        );
    }

    public void notifyUserSaleCompleted(String userId, String cardName, double price) {
        userNotificationService.createForUser(
            userId,
            "Vendita completata",
            "Hai venduto " + cardName + " per €" + price,
            "SUCCESS",
            "TRANSACTION",
            null
        );
    }

    public void notifyUserSupportResponse(String userId, String ticketSubject) {
        userNotificationService.createForUser(
            userId,
            "Risposta al tuo ticket",
            "Abbiamo risposto al tuo ticket: " + ticketSubject,
            "INFO",
            "SUPPORT",
            null
        );
    }

    public void notifyUserCardRejected(String userId, String cardName, String reason) {
        userNotificationService.createForUser(
            userId,
            "Carta rifiutata",
            "La tua carta " + cardName + " è stata rifiutata: " + reason,
            "WARNING",
            "CARD",
            null
        );
    }
}
