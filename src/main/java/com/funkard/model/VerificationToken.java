package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public static VerificationToken generate(User user) {
        VerificationToken vt = new VerificationToken();
        vt.token = UUID.randomUUID().toString();
        vt.expiryDate = LocalDateTime.now().plusHours(24);
        vt.user = user;
        return vt;
    }
}