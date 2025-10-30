package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID; // unused but requested
import java.time.Instant; // unused but requested

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    private String password;  // hashed password
    
    @Column(unique = true)
    private String handle;
    private String nome;
    private String paese;
    private String tipoUtente; // PRIVATO o BUSINESS

    private String indirizzo;
    private String citta;
    private String cap;
    private String telefono;
    private String metodoPagamento;

    private Boolean accettaTermini;
    private Boolean verified = false;
    private Boolean flagged = false;

    // Campi esistenti per compatibilità
    private String username;
    private String avatarUrl;
    private String role = "USER";
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Campi profilo aggiuntivi per compatibilità con UserService
    private String language;
    private String theme;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // 🔹 Nuovo campo valuta preferita
    @Column(nullable = false, length = 3)
    private String preferredCurrency = "EUR";

    // Metodi di compatibilità per DTO/Service
    public String getName() { return this.nome; }
    public void setName(String name) { this.nome = name; }
}