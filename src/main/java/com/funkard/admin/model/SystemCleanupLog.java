package com.funkard.admin.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_cleanup_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemCleanupLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String result; // "success" | "error"
    private Integer deleted; // notifiche/log eliminati
    private LocalDateTime timestamp;

    private String details; // opzionale: eventuale errore o info extra
}
