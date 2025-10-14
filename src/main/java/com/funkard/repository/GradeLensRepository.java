package com.funkard.repository;

import com.funkard.model.GradeLensResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GradeLensRepository extends JpaRepository<GradeLensResult, Long> {
    List<GradeLensResult> findByUserId(String userId);
}
