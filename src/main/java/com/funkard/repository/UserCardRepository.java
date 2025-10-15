package com.funkard.repository;

import com.funkard.model.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface UserCardRepository extends JpaRepository<UserCard, String> {
    List<UserCard> findByUserId(String userId);
    List<UserCard> findByGradedAtBeforeAndPermanentFalse(LocalDateTime cutoff);
}
