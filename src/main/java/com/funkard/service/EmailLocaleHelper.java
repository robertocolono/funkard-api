package com.funkard.service;

import com.funkard.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 🌍 Helper per gestione locale email
 * 
 * Rileva automaticamente la lingua utente da user.language
 * con fallback sicuro all'inglese.
 */
@Component
@Slf4j
public class EmailLocaleHelper {
    
    /**
     * 🌍 Rileva locale utente da User object
     * 
     * @param user Utente (può essere null)
     * @return Locale (default: Locale.ENGLISH)
     */
    public Locale getUserLocale(User user) {
        if (user == null) {
            return Locale.ENGLISH;
        }
        
        String userLanguage = user.getLanguage();
        if (userLanguage == null || userLanguage.isEmpty()) {
            return Locale.ENGLISH;
        }
        
        return parseLocale(userLanguage);
    }
    
    /**
     * 🌍 Rileva locale da stringa
     * 
     * @param localeStr Stringa locale (es. "it", "en", "es-419")
     * @return Locale (default: Locale.ENGLISH)
     */
    public Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.isEmpty()) {
            return Locale.ENGLISH;
        }
        
        // Supporta formati: "it", "it-IT", "en-US", "es-419", ecc.
        String[] parts = localeStr.split("-");
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            // Gestisci casi speciali
            if (parts[0].equals("es") && parts[1].equals("419")) {
                return new Locale("es", "419");
            }
            return new Locale(parts[0], parts[1]);
        }
        
        return Locale.ENGLISH; // Fallback sicuro
    }
    
    /**
     * 🌍 Rileva locale string da User object
     * 
     * @param user Utente (può essere null)
     * @return Stringa locale (default: "en")
     */
    public String getUserLocaleString(User user) {
        Locale locale = getUserLocale(user);
        return locale.getLanguage();
    }
}

