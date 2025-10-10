
package com.funkard.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String email;
    private String name;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
        private String username;
        private String avatarUrl;
        private String role = "USER";
        private LocalDateTime createdAt = LocalDateTime.now();

        @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
        private List<Listing> listings;
}
