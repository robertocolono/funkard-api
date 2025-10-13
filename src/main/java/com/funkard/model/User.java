package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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

    // Campi esistenti per compatibilità
    private String username;
    private String avatarUrl;
    private String role = "USER";
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Listing> listings;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private VerificationToken verificationToken;

    // Metodo di compatibilità per UserService
    public String getName() { return nome; }
    public void setName(String name) { this.nome = name; }
}