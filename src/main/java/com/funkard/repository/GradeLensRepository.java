package com.funkard.repository;

import com.funkard.model.GradeLensResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GradeLensRepository extends JpaRepository<GradeLensResult, String> {
    List<GradeLensResult> findByUserCardId(String userCardId);
}
