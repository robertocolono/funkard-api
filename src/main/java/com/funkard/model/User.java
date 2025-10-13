
package com.funkard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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

    // Campi esistenti
    private String username;
    private String avatarUrl;
    private String role = "USER";
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Listing> listings;

    // Getter/setter base
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    // Getter/setter per name (compatibilità con UserService)
    public String getName() { return nome; }
    public void setName(String name) { this.nome = name; }

    // Nuovi getter/setter
    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getPaese() { return paese; }
    public void setPaese(String paese) { this.paese = paese; }
    public String getTipoUtente() { return tipoUtente; }
    public void setTipoUtente(String tipoUtente) { this.tipoUtente = tipoUtente; }
    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }
    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta; }
    public String getCap() { return cap; }
    public void setCap(String cap) { this.cap = cap; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public Boolean getAccettaTermini() { return accettaTermini; }
    public void setAccettaTermini(Boolean accettaTermini) { this.accettaTermini = accettaTermini; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    // Getter/setter esistenti
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Listing> getListings() { return listings; }
    public void setListings(List<Listing> listings) { this.listings = listings; }
}