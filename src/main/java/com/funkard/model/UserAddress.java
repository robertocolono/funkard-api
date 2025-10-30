package com.funkard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 🏠 Entity per gestione indirizzi utente
 * 
 * ✅ Validazione completa
 * ✅ Relazione con User
 * ✅ Timestamps automatici
 */
@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Il nome completo è obbligatorio")
    @Size(min = 2, max = 100, message = "Il nome deve essere tra 2 e 100 caratteri")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "L'indirizzo è obbligatorio")
    @Size(min = 2, max = 200, message = "L'indirizzo deve essere tra 2 e 200 caratteri")
    @Column(name = "street", nullable = false)
    private String street;

    @NotBlank(message = "La città è obbligatoria")
    @Size(min = 2, max = 100, message = "La città deve essere tra 2 e 100 caratteri")
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank(message = "La provincia/regione è obbligatoria")
    @Size(min = 2, max = 100, message = "La provincia deve essere tra 2 e 100 caratteri")
    @Column(name = "state", nullable = false)
    private String state;

    @NotBlank(message = "Il CAP è obbligatorio")
    @Pattern(regexp = "^[A-Za-z0-9\\- ]{3,10}$", message = "Formato CAP non valido")
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @NotBlank(message = "Il paese è obbligatorio")
    @Size(min = 2, max = 100, message = "Il paese deve essere tra 2 e 100 caratteri")
    @Column(name = "country", nullable = false)
    private String country;

    @Pattern(regexp = "^[0-9+\\- ]{6,20}$", message = "Formato telefono non valido")
    @Column(name = "phone")
    private String phone;

    @Column(name = "address_label")
    private String addressLabel; // "Casa", "Ufficio", "Spedizione"

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public String getFullAddress() {
        return String.format("%s, %s %s, %s, %s", 
            street, postalCode, city, state, country);
    }

    public boolean isComplete() {
        return fullName != null && !fullName.trim().isEmpty() &&
               street != null && !street.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               state != null && !state.trim().isEmpty() &&
               postalCode != null && !postalCode.trim().isEmpty() &&
               country != null && !country.trim().isEmpty();
    }
}
