package com.funkard.admin.notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private String type; // INFO | WARNING | ERROR | SUCCESS
    
    @Builder.Default
    private boolean resolved = false;
    
    @Builder.Default
    private boolean read = false;

    private String referenceType; // es. "CARD", "TRANSACTION", "GRADING"
    private Long referenceId;     // id dell'oggetto collegato
    
    private String userId;        // ID dell'utente destinatario

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime resolvedAt;

    public enum NotificationType {
        INFO, WARNING, ERROR, SUCCESS
    }
}
