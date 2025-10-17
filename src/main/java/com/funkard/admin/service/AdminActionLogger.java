package com.funkard.admin.service;

import com.funkard.admin.model.AdminActionLog;
import com.funkard.admin.repository.AdminActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminActionLogger {
    
    private final AdminActionLogRepository logRepository;

    public void log(Long targetId, String targetType, String action, String performedBy, String role, String notes) {
        AdminActionLog log = new AdminActionLog();
        log.setTargetId(targetId);
        log.setTargetType(targetType);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setRole(role);
        log.setNotes(notes);
        logRepository.save(log);
    }

    // Helper methods for common actions
    public void logNotificationRead(Long notificationId, String adminEmail) {
        log(notificationId, "NOTIFICATION", "READ", adminEmail, "ADMIN", "Notifica letta da " + adminEmail);
    }

    public void logNotificationArchived(Long notificationId, String adminEmail) {
        log(notificationId, "NOTIFICATION", "ARCHIVED", adminEmail, "ADMIN", "Notifica archiviata da " + adminEmail);
    }

    public void logNotificationResolved(Long notificationId, String adminEmail) {
        log(notificationId, "NOTIFICATION", "RESOLVED", adminEmail, "ADMIN", "Notifica risolta da " + adminEmail);
    }

    public void logSystemAction(Long targetId, String targetType, String action, String notes) {
        log(targetId, targetType, action, "SYSTEM", "SYSTEM", notes);
    }

    public void logPriceApproval(Long priceRequestId, String adminEmail) {
        log(priceRequestId, "PRICE_REQUEST", "APPROVED", adminEmail, "ADMIN", "Prezzo approvato da " + adminEmail);
    }

    public void logReportClosed(Long reportId, String adminEmail) {
        log(reportId, "REPORT", "CLOSED", adminEmail, "ADMIN", "Segnalazione chiusa da " + adminEmail);
    }
}
