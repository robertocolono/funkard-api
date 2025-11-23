package com.funkard.service;

import com.funkard.model.EmailLog;
import com.funkard.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 📧 Service per gestione log email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailLogService {
    
    private final EmailLogRepository logRepository;
    
    /**
     * 📝 Registra log email inviata
     */
    @Transactional
    public EmailLog logEmailSent(
            String recipient,
            String sender,
            String subject,
            String type,
            String locale,
            String templateName) {
        
        return logEmailSent(recipient, sender, subject, type, locale, templateName, false);
    }
    
    /**
     * 📝 Registra log email inviata con flag fallback
     */
    @Transactional
    public EmailLog logEmailSent(
            String recipient,
            String sender,
            String subject,
            String type,
            String locale,
            String templateName,
            boolean usedFallback) {
        
        EmailLog emailLog = new EmailLog();
        emailLog.setRecipient(recipient);
        emailLog.setSender(sender);
        emailLog.setSubject(subject);
        emailLog.setType(type);
        emailLog.setStatus(EmailLog.EmailStatus.SENT);
        emailLog.setLocale(locale != null ? locale : "en");
        emailLog.setTemplateName(templateName);
        emailLog.setRetryCount(0);
        
        // Se usato fallback, aggiungi nota nell'error message (non è un errore, ma info)
        if (usedFallback) {
            emailLog.setErrorMessage("Template fallback used: requested locale not available");
        }
        
        EmailLog saved = logRepository.save(emailLog);
        log.debug("✅ Log email salvato: {} - {} - Fallback: {}", saved.getId(), type, usedFallback);
        return saved;
    }
    
    /**
     * ❌ Registra log email fallita
     */
    @Transactional
    public EmailLog logEmailFailed(
            String recipient,
            String sender,
            String subject,
            String type,
            String locale,
            String templateName,
            String errorMessage) {
        
        EmailLog emailLog = new EmailLog();
        emailLog.setRecipient(recipient);
        emailLog.setSender(sender);
        emailLog.setSubject(subject);
        emailLog.setType(type);
        emailLog.setStatus(EmailLog.EmailStatus.FAILED);
        emailLog.setErrorMessage(errorMessage);
        emailLog.setLocale(locale != null ? locale : "it");
        emailLog.setTemplateName(templateName);
        emailLog.setRetryCount(0);
        
        EmailLog saved = logRepository.save(emailLog);
        log.warn("❌ Log email fallita salvato: {} - {} - {}", saved.getId(), type, errorMessage);
        return saved;
    }
    
    /**
     * 🔄 Aggiorna log con retry
     */
    @Transactional
    public EmailLog updateLogWithRetry(UUID logId, int retryCount, boolean success, String errorMessage) {
        return logRepository.findById(logId).map(log -> {
            log.setRetryCount(retryCount);
            if (success) {
                log.setStatus(EmailLog.EmailStatus.SENT);
                log.setErrorMessage(null);
            } else {
                log.setStatus(EmailLog.EmailStatus.RETRIED);
                log.setErrorMessage(errorMessage);
            }
            return logRepository.save(log);
        }).orElse(null);
    }
    
    /**
     * 🔍 Trova log per ID
     */
    public EmailLog findById(UUID id) {
        return logRepository.findById(id).orElse(null);
    }
}

