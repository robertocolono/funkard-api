package com.funkard.user.payment;

import com.funkard.user.payment.dto.PaymentMethodDTO;
import com.funkard.user.payment.dto.PaymentMethodRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 💳 Service per gestione metodi di pagamento
 * 
 * 🔒 SICUREZZA: Mai salva numeri completi
 * ✅ Logica business completa
 * ✅ Integrazione futura con Stripe
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodService {

    private final PaymentMethodRepository repository;

    /**
     * 📋 Ottieni tutti i metodi di pagamento di un utente
     */
    public List<PaymentMethodDTO> getMethods(String userId) {
        log.info("Recuperando metodi di pagamento per utente: {}", userId);
        
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * ➕ Aggiungi nuovo metodo di pagamento
     */
    @Transactional
    public PaymentMethodDTO addMethod(String userId, PaymentMethodRequest request) {
        log.info("Aggiungendo metodo di pagamento per utente: {}", userId);
        
        // Validazione
        if (!request.isValidCardNumber()) {
            throw new IllegalArgumentException("Numero di carta non valido");
        }
        
        if (!request.isValidExpiryDate()) {
            throw new IllegalArgumentException("Data di scadenza non valida");
        }
        
        // Controlla limite metodi per utente
        long methodCount = repository.countByUserId(userId);
        if (methodCount >= 5) {
            throw new IllegalStateException("Limite massimo di 5 metodi di pagamento raggiunto");
        }
        
        // Crea nuovo metodo
        PaymentMethod method = PaymentMethod.builder()
                .userId(userId)
                .cardHolder(request.getCardHolder())
                .cardNumberMasked(maskCardNumber(request.getCardNumber()))
                .expiryDate(request.getExpiryDate())
                .brand(request.getBrand())
                .isDefault(methodCount == 0 || request.isSetAsDefault())
                .build();

        // Se è il primo metodo o è impostato come predefinito, 
        // rimuovi il flag predefinito dagli altri
        if (method.isDefault()) {
            clearDefaultMethods(userId);
        }

        PaymentMethod saved = repository.save(method);
        log.info("Metodo di pagamento aggiunto con ID: {}", saved.getId());
        
        return toDTO(saved);
    }

    /**
     * 🗑️ Elimina metodo di pagamento
     */
    @Transactional
    public void deleteMethod(String userId, String methodId) {
        log.info("Eliminando metodo di pagamento {} per utente: {}", methodId, userId);
        
        Optional<PaymentMethod> methodOpt = repository.findById(methodId);
        if (methodOpt.isEmpty()) {
            throw new IllegalArgumentException("Metodo di pagamento non trovato");
        }
        
        PaymentMethod method = methodOpt.get();
        if (!method.getUserId().equals(userId)) {
            throw new SecurityException("Accesso negato al metodo di pagamento");
        }
        
        boolean wasDefault = method.isDefault();
        repository.delete(method);
        
        // Se era il metodo predefinito, imposta un altro come predefinito
        if (wasDefault) {
            setNewDefaultMethod(userId);
        }
        
        log.info("Metodo di pagamento eliminato: {}", methodId);
    }

    /**
     * 🎯 Imposta metodo predefinito
     */
    @Transactional
    public void setDefaultMethod(String userId, String methodId) {
        log.info("Impostando metodo predefinito {} per utente: {}", methodId, userId);
        
        Optional<PaymentMethod> methodOpt = repository.findById(methodId);
        if (methodOpt.isEmpty()) {
            throw new IllegalArgumentException("Metodo di pagamento non trovato");
        }
        
        PaymentMethod method = methodOpt.get();
        if (!method.getUserId().equals(userId)) {
            throw new SecurityException("Accesso negato al metodo di pagamento");
        }
        
        // Rimuovi flag predefinito da tutti i metodi
        clearDefaultMethods(userId);
        
        // Imposta nuovo predefinito
        method.setDefault(true);
        repository.save(method);
        
        log.info("Metodo predefinito impostato: {}", methodId);
    }

    /**
     * 🔍 Ottieni metodo predefinito
     */
    public Optional<PaymentMethodDTO> getDefaultMethod(String userId) {
        return repository.findByUserIdAndIsDefaultTrue(userId)
                .map(this::toDTO);
    }

    /**
     * 📊 Statistiche metodi di pagamento
     */
    public PaymentMethodStats getStats(String userId) {
        long totalMethods = repository.countByUserId(userId);
        List<PaymentMethod> expiredMethods = repository.findExpiredByUserId(userId);
        
        return PaymentMethodStats.builder()
                .totalMethods(totalMethods)
                .expiredMethods(expiredMethods.size())
                .hasDefaultMethod(repository.findByUserIdAndIsDefaultTrue(userId).isPresent())
                .build();
    }

    /**
     * 🧹 Pulisci metodi scaduti
     */
    @Transactional
    public int cleanupExpiredMethods(String userId) {
        List<PaymentMethod> expired = repository.findExpiredByUserId(userId);
        if (!expired.isEmpty()) {
            repository.deleteAll(expired);
            log.info("Eliminati {} metodi di pagamento scaduti per utente: {}", expired.size(), userId);
        }
        return expired.size();
    }

    // ========== METODI PRIVATI ==========

    /**
     * 🎭 Mascheratura numero carta
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        
        String cleanNumber = cardNumber.replaceAll("\\s", "");
        if (cleanNumber.length() < 4) {
            return "****";
        }
        
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    /**
     * 🔄 Rimuovi flag predefinito da tutti i metodi
     */
    private void clearDefaultMethods(String userId) {
        List<PaymentMethod> methods = repository.findByUserIdOrderByCreatedAtDesc(userId);
        for (PaymentMethod method : methods) {
            if (method.isDefault()) {
                method.setDefault(false);
                repository.save(method);
            }
        }
    }

    /**
     * 🎯 Imposta nuovo metodo predefinito
     */
    private void setNewDefaultMethod(String userId) {
        List<PaymentMethod> methods = repository.findByUserIdOrderByCreatedAtDesc(userId);
        if (!methods.isEmpty()) {
            PaymentMethod firstMethod = methods.get(0);
            firstMethod.setDefault(true);
            repository.save(firstMethod);
        }
    }

    /**
     * 🔄 Conversione Entity -> DTO
     */
    private PaymentMethodDTO toDTO(PaymentMethod entity) {
        return PaymentMethodDTO.builder()
                .id(entity.getId())
                .cardHolder(entity.getCardHolder())
                .cardNumberMasked(entity.getCardNumberMasked())
                .expiryDate(entity.getExpiryDate())
                .brand(entity.getBrand())
                .isDefault(entity.isDefault())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .displayName(entity.getDisplayName())
                .isExpired(entity.isExpired())
                .lastFourDigits(entity.getCardNumberMasked().substring(
                    entity.getCardNumberMasked().length() - 4))
                .build();
    }

    /**
     * 📊 Classe per statistiche metodi di pagamento
     */
    @lombok.Data
    @lombok.Builder
    public static class PaymentMethodStats {
        private long totalMethods;
        private int expiredMethods;
        private boolean hasDefaultMethod;
    }
}
