package com.funkard.repository;

import com.funkard.model.TranslationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 📜 Repository per log traduzioni
 */
@Repository
public interface TranslationLogRepository extends JpaRepository<TranslationLog, UUID> {
    
    // Metodi di query opzionali per statistiche e audit
    long countBySuccess(boolean success);
    long countBySourceLanguageAndTargetLanguage(String sourceLang, String targetLang);
}

