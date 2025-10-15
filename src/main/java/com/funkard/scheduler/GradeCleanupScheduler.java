package com.funkard.scheduler;

import com.funkard.model.UserCard;
import com.funkard.repository.UserCardRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class GradeCleanupScheduler {

    private final UserCardRepository userCardRepository;

    public GradeCleanupScheduler(UserCardRepository userCardRepository) {
        this.userCardRepository = userCardRepository;
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Rome") // ogni giorno alle 3:00 ora di Roma
    public void deleteExpiredGrades() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<UserCard> expired = userCardRepository.findByGradedAtBeforeAndPermanentFalse(cutoff);
        if (!expired.isEmpty()) {
            userCardRepository.deleteAll(expired);
        }
        System.out.println("🧹 Pulite " + expired.size() + " carte scadute (" + LocalDateTime.now() + ")");
    }
}
