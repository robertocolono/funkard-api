package com.funkard.grading.repository;

import com.funkard.grading.model.GradingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradingRepository extends JpaRepository<GradingRequest, Long> {
    Optional<GradingRequest> findByCardId(Long cardId);
}
