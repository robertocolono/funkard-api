package com.funkard.repository;

import com.funkard.model.GradeLensResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface GradeLensRepository extends JpaRepository<GradeLensResult, Long> {
    List<GradeLensResult> findByUserId(String userId);

    @Modifying
    @Transactional
    void deleteAllBySourceAndAddedToCollectionFalseAndCreatedAtBefore(String source, LocalDateTime cutoff);
}
