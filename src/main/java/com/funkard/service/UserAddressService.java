package com.funkard.service;

import com.funkard.model.User;
import com.funkard.model.UserAddress;
import com.funkard.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 🏠 Service per gestione indirizzi utente
 * 
 * ✅ Logica business completa
 * ✅ Gestione default address
 * ✅ Validazioni di sicurezza
 * ✅ Transazioni ottimizzate
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressService {

    private final UserAddressRepository repository;
    private static final int MAX_ADDRESSES_PER_USER = 10;

    /**
     * 📋 Ottieni tutti gli indirizzi di un utente
     */
    public List<UserAddress> getAddresses(User user) {
        log.info("Recupero indirizzi per utente: {}", user.getId());
        return repository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * ➕ Aggiungi nuovo indirizzo
     */
    @Transactional
    public UserAddress addAddress(User user, UserAddress address) {
        log.info("Aggiunta nuovo indirizzo per utente: {}", user.getId());
        
        // Controllo limite indirizzi
        long currentCount = repository.countByUser(user);
        if (currentCount >= MAX_ADDRESSES_PER_USER) {
            throw new IllegalStateException("Limite massimo di " + MAX_ADDRESSES_PER_USER + " indirizzi raggiunto");
        }

        // Se è il primo indirizzo, impostalo come predefinito
        if (currentCount == 0) {
            address.setDefault(true);
        }

        // Se questo indirizzo è marcato come default, rimuovi il default dagli altri
        if (address.isDefault()) {
            repository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(existingDefault -> {
                        existingDefault.setDefault(false);
                        repository.save(existingDefault);
                    });
        }

        address.setUser(user);
        UserAddress saved = repository.save(address);
        
        log.info("Indirizzo aggiunto con successo: {}", saved.getId());
        return saved;
    }

    /**
     * ✏️ Aggiorna indirizzo esistente
     */
    @Transactional
    public UserAddress updateAddress(Long addressId, User user, UserAddress updatedAddress) {
        log.info("Aggiornamento indirizzo {} per utente: {}", addressId, user.getId());
        
        UserAddress existing = repository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new RuntimeException("Indirizzo non trovato o non autorizzato"));

        // Se questo indirizzo è marcato come default, rimuovi il default dagli altri
        if (updatedAddress.isDefault() && !existing.isDefault()) {
            repository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(currentDefault -> {
                        currentDefault.setDefault(false);
                        repository.save(currentDefault);
                    });
        }

        // Aggiorna i campi
        existing.setFullName(updatedAddress.getFullName());
        existing.setStreet(updatedAddress.getStreet());
        existing.setCity(updatedAddress.getCity());
        existing.setState(updatedAddress.getState());
        existing.setPostalCode(updatedAddress.getPostalCode());
        existing.setCountry(updatedAddress.getCountry());
        existing.setPhone(updatedAddress.getPhone());
        existing.setAddressLabel(updatedAddress.getAddressLabel());
        existing.setDefault(updatedAddress.isDefault());

        UserAddress saved = repository.save(existing);
        log.info("Indirizzo aggiornato con successo: {}", saved.getId());
        return saved;
    }

    /**
     * 🗑️ Elimina indirizzo
     */
    @Transactional
    public void deleteAddress(Long addressId, User user) {
        log.info("Eliminazione indirizzo {} per utente: {}", addressId, user.getId());
        
        UserAddress address = repository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new RuntimeException("Indirizzo non trovato o non autorizzato"));

        boolean wasDefault = address.isDefault();
        repository.delete(address);

        // Se l'indirizzo eliminato era il default, imposta un nuovo default
        if (wasDefault) {
            repository.findByUserOrderByCreatedAtDesc(user).stream()
                    .findFirst()
                    .ifPresent(newDefault -> {
                        newDefault.setDefault(true);
                        repository.save(newDefault);
                        log.info("Nuovo indirizzo predefinito impostato: {}", newDefault.getId());
                    });
        }

        log.info("Indirizzo eliminato con successo: {}", addressId);
    }

    /**
     * 🎯 Imposta indirizzo predefinito
     */
    @Transactional
    public UserAddress setDefaultAddress(Long addressId, User user) {
        log.info("Impostazione indirizzo predefinito {} per utente: {}", addressId, user.getId());
        
        UserAddress address = repository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new RuntimeException("Indirizzo non trovato o non autorizzato"));

        // Rimuovi default da tutti gli altri indirizzi
        repository.findByUserAndIsDefaultTrue(user)
                .ifPresent(currentDefault -> {
                    currentDefault.setDefault(false);
                    repository.save(currentDefault);
                });

        // Imposta questo come default
        address.setDefault(true);
        UserAddress saved = repository.save(address);
        
        log.info("Indirizzo predefinito impostato: {}", saved.getId());
        return saved;
    }

    /**
     * 🏠 Ottieni indirizzo predefinito
     */
    public Optional<UserAddress> getDefaultAddress(User user) {
        return repository.findByUserAndIsDefaultTrue(user);
    }

    /**
     * 📊 Statistiche indirizzi utente
     */
    public long getAddressCount(User user) {
        return repository.countByUser(user);
    }

    /**
     * ✅ Verifica se un indirizzo appartiene all'utente
     */
    public boolean isAddressOwnedByUser(Long addressId, User user) {
        return repository.existsByIdAndUserId(addressId, user.getId());
    }
}
