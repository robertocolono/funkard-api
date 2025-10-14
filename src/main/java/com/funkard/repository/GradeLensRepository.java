package com.funkard.repository;

import com.funkard.model.GradeLensResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface GradeLensRepository extends JpaRepository<GradeLensResult, Long> {
    List<GradeLensResult> findByUserId(String userId);

    @Modifying
    @Transactional
    void deleteAllBySourceAndAddedToCollectionFalseAndCreatedAtBefore(String source, LocalDateTime cutoff);

    @Modifying
    @Transactional
    @Query("DELETE FROM GradeLensResult g WHERE g.source = :source AND g.createdAt < :cutoff")
    void deleteExpired(@Param("source") String source, @Param("cutoff") LocalDateTime cutoff);
}
