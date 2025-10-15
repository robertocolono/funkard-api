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

    @Scheduled(cron = "0 0 3 * * *") // ogni giorno alle 3:00
    public void deleteExpiredGrades() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<UserCard> expired = userCardRepository.findByGradedAtBeforeAndPermanentFalse(cutoff);

        for (UserCard card : expired) {
            card.setGradeService(null);
            card.setGradeOverall(null);
            card.setGradeLabel(null);
            card.setSubgrades(null);
            card.setGradedAt(null);
            userCardRepository.save(card);
        }

        System.out.println("🧹 Pulizia completata: " + expired.size() + " grading rimossi.");
    }
}
