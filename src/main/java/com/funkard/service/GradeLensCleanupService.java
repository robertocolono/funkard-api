package com.funkard.service;

import com.funkard.repository.GradeLensRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Service
public class GradeLensCleanupService {

    @Autowired
    private GradeLensRepository repo;

    // Esegue ogni ora (3600000 ms)
    @Scheduled(fixedRate = 3600000)
    public void deleteExpiredGradings() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(36);
        repo.deleteAllBySourceAndAddedToCollectionFalseAndCreatedAtBefore("gradelens", cutoff);
    }
}
