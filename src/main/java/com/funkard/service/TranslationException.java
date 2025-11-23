package com.funkard.service;

/**
 * ❌ Eccezione per errori di traduzione
 */
public class TranslationException extends Exception {
    
    public TranslationException(String message) {
        super(message);
    }
    
    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}

