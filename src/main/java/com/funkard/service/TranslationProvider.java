package com.funkard.service;

/**
 * 🌍 Interfaccia per provider di traduzione
 * 
 * Definisce il contratto per servizi di traduzione (OpenAI, DeepL, ecc.)
 */
public interface TranslationProvider {
    
    /**
     * 🌍 Traduce un testo in una lingua target
     * 
     * @param text Testo da tradurre
     * @param targetLanguage Lingua di destinazione (codice ISO 639-1, es. "it", "en", "es")
     * @return Testo tradotto
     * @throws TranslationException se la traduzione fallisce
     */
    String translate(String text, String targetLanguage) throws TranslationException;
    
    /**
     * 🔍 Verifica se il provider è disponibile (API key configurata, ecc.)
     * 
     * @return true se il provider è disponibile, false altrimenti
     */
    boolean isAvailable();
}

