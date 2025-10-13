package com.funkard.repository;

import com.funkard.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteAllByExpiryDateBefore(LocalDateTime dateTime);
}