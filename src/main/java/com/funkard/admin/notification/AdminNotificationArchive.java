package com.funkard.admin.notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_notification_archive")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotificationArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private String type; // INFO | WARNING | ERROR | SUPPORT
    
    @Builder.Default
    private boolean resolved = true; // Sempre true nell'archivio
    
    @Builder.Default
    private boolean read = true; // Sempre true nell'archivio

    private String referenceType; // es. "CARD", "USER", "TRANSACTION"
    private Long referenceId;     // id dell'oggetto collegato

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime resolvedAt;
    @Builder.Default
    private LocalDateTime archivedAt = LocalDateTime.now(); // Quando è stato archiviato

    public enum NotificationType {
        INFO, WARNING, ERROR, SUPPORT
    }
}
