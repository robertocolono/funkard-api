package com.funkard.admin.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminActionLogger {

    private final AdminActionLogRepository logRepository;

    public void log(Long targetId, String targetType, String action, String performedBy, String role, String notes) {
        AdminActionLog log = new AdminActionLog();
        log.setTargetId(targetId);
        log.setTargetType(targetType.toUpperCase());
        log.setAction(action.toUpperCase());
        log.setPerformedBy(performedBy != null ? performedBy : "SYSTEM");
        log.setRole(role != null ? role : "SYSTEM");
        log.setNotes(notes);
        logRepository.save(log);
    }
}
