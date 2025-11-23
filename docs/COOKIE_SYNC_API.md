# 🍪 Cookie Preferences Sync API - GDPR Compliance

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0  
**Conformità:** GDPR Art. 7 (Consenso Cookie) + Audit Trail

---

## 📋 Panoramica

Sistema centralizzato di sincronizzazione preferenze cookie per utenti autenticati, con audit log completo per tracciabilità legale e conformità GDPR.

---

## ✅ Componenti Implementati

### **1. Modello UserPreferences**
- `cookiesAccepted` (Boolean)
- `cookiesPreferences` (String/JSON)
- `cookiesAcceptedAt` (LocalDateTime)

### **2. Modello CookieConsentLog (Audit)**
- `userId` (Long)
- `oldPreferences` (String/JSON)
- `newPreferences` (String/JSON)
- `changedAt` (LocalDateTime)
- `ipAddress` (String, opzionale - NULL per minimizzazione GDPR)

### **3. Repository**
- `UserPreferencesRepository` - Gestione preferenze
- `CookieConsentLogRepository` - Audit log

### **4. Service**
- `UserPreferencesService` - Business logic con audit logging automatico

---

## 🌐 API Endpoints

### **GET /api/user/preferences/cookies**

Ottieni preferenze cookie dell'utente autenticato.

**⚠️ RICHIEDE AUTENTICAZIONE OBBLIGATORIA (solo JWT)**

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Response Success (200):**
```json
{
  "cookiesAccepted": true,
  "cookiesPreferences": {
    "essential": true,
    "analytics": false,
    "functional": true,
    "marketing": false
  },
  "cookiesAcceptedAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

**Response Unauthorized (401):**
```json
{
  "error": "Autenticazione richiesta per accedere alle preferenze cookie"
}
```

---

### **POST /api/user/preferences/cookies**

Salva o aggiorna preferenze cookie dell'utente autenticato.

**⚠️ RICHIEDE AUTENTICAZIONE OBBLIGATORIA (solo JWT)**

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "cookiesAccepted": true,
  "cookiesPreferences": {
    "essential": true,
    "analytics": false,
    "functional": true,
    "marketing": false
  }
}
```

**Response Success (200):**
```json
{
  "message": "Preferenze cookie aggiornate correttamente",
  "preferences": {
    "cookiesAccepted": true,
    "cookiesPreferences": {
      "essential": true,
      "analytics": false,
      "functional": true,
      "marketing": false
    },
    "cookiesAcceptedAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  }
}
```

**Response Unauthorized (401):**
```json
{
  "error": "Autenticazione richiesta per salvare le preferenze cookie"
}
```

**Response Error (500):**
```json
{
  "error": "Errore interno del server"
}
```

---

## 📋 Audit Log Automatico

Ogni modifica alle preferenze cookie viene automaticamente registrata nella tabella `cookie_consent_logs` per audit GDPR.

**Campi registrati:**
- `userId` - ID utente
- `oldPreferences` - Preferenze precedenti (JSON, NULL se prima accettazione)
- `newPreferences` - Nuove preferenze (JSON)
- `changedAt` - Timestamp modifica

**Principio di minimizzazione GDPR:**
- ❌ Non salviamo IP address (campo presente ma sempre NULL)
- ❌ Non salviamo dati utente non necessari
- ✅ Solo dati essenziali per dimostrare consenso/revoca

---

## 💻 Integrazione Frontend

### **Esempio: Sincronizzazione Preferenze Cookie**

```typescript
// hooks/useCookieSync.ts
'use client';

import { useAuth } from './useAuth';

interface CookiePreferences {
  essential: boolean;
  analytics: boolean;
  functional: boolean;
  marketing: boolean;
}

export function useCookieSync() {
  const { token, isAuthenticated } = useAuth();

  /**
   * Carica preferenze dal backend (solo se autenticato)
   */
  const loadPreferences = async (): Promise<CookiePreferences | null> => {
    if (!isAuthenticated || !token) {
      // Utente non autenticato: usa solo localStorage
      const stored = localStorage.getItem('cookie_preferences');
      return stored ? JSON.parse(stored) : null;
    }

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/user/preferences/cookies`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );

      if (response.ok) {
        const data = await response.json();
        // Sincronizza con localStorage
        localStorage.setItem('cookie_preferences', JSON.stringify(data.cookiesPreferences));
        return data.cookiesPreferences;
      }
    } catch (error) {
      console.error('Errore nel caricamento preferenze:', error);
    }

    return null;
  };

  /**
   * Salva preferenze sul backend (solo se autenticato)
   */
  const savePreferences = async (
    cookiesAccepted: boolean,
    preferences: CookiePreferences
  ): Promise<boolean> => {
    // Salva sempre in localStorage (cache locale)
    localStorage.setItem('cookie_preferences', JSON.stringify(preferences));

    // Se autenticato, sincronizza con backend
    if (!isAuthenticated || !token) {
      return true; // Salvato solo in localStorage
    }

    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/user/preferences/cookies`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            cookiesAccepted,
            cookiesPreferences: preferences,
          }),
        }
      );

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Preferenze sincronizzate:', data.message);
        return true;
      } else {
        console.warn('Errore nella sincronizzazione preferenze');
        return false; // Salvato in localStorage ma non sincronizzato
      }
    } catch (error) {
      console.error('Errore nella sincronizzazione:', error);
      return false; // Salvato in localStorage ma non sincronizzato
    }
  };

  return {
    loadPreferences,
    savePreferences,
  };
}
```

---

## 🔐 Sicurezza e Validazione

### **Autenticazione Obbligatoria**

Gli endpoint `/api/user/preferences/cookies` richiedono **autenticazione JWT obbligatoria**:

- ✅ Utenti autenticati: Sincronizzazione backend + localStorage
- ❌ Utenti anonimi: Solo localStorage (nessuna chiamata API)

### **Validazione Input**

- `@Valid` su `CookiePreferencesDTO`
- Validazione formato JSON per `cookiesPreferences`
- Timestamp automatico su `cookiesAcceptedAt`

---

## 📊 Query Database per Audit

### **Storico Modifiche Utente**

```sql
SELECT 
    id,
    user_id,
    old_preferences,
    new_preferences,
    changed_at
FROM cookie_consent_logs
WHERE user_id = ?
ORDER BY changed_at DESC;
```

### **Report GDPR Compliance**

```sql
SELECT 
    u.id,
    u.email,
    up.cookies_accepted,
    up.cookies_accepted_at,
    COUNT(ccl.id) AS total_changes
FROM users u
LEFT JOIN user_preferences up ON u.id = up.user_id
LEFT JOIN cookie_consent_logs ccl ON u.id = ccl.user_id
WHERE u.id = ?
GROUP BY u.id, u.email, up.cookies_accepted, up.cookies_accepted_at;
```

### **Ultima Modifica Utente**

```sql
SELECT 
    old_preferences,
    new_preferences,
    changed_at
FROM cookie_consent_logs
WHERE user_id = ?
ORDER BY changed_at DESC
LIMIT 1;
```

---

## 🔒 Conformità GDPR

### **Requisiti Soddisfatti**

✅ **Art. 7 GDPR - Consenso:**
- Consenso esplicito tracciato
- Timestamp accettazione salvato
- Storico modifiche completo

✅ **Art. 13 GDPR - Informazioni:**
- Informazioni chiare su tipi di cookie
- Accesso facilitato alle preferenze

✅ **Art. 30 GDPR - Registro:**
- Audit log completo (`cookie_consent_logs`)
- Tracciabilità per ispezioni
- Storico modifiche conservato

✅ **Principio di Minimizzazione:**
- Solo dati necessari salvati
- IP address non salvato (campo NULL)
- Nessun dato utente non essenziale

✅ **Diritto alla Cancellazione (Art. 17):**
- `DELETE CASCADE` su `cookie_consent_logs`
- Eliminazione automatica log alla cancellazione utente

---

## ✅ Checklist Implementazione

### **Backend:**
- [x] Modello `UserPreferences` con campi cookie
- [x] Modello `CookieConsentLog` per audit
- [x] Repository per entrambi i modelli
- [x] Service con audit logging automatico
- [x] Endpoint GET `/api/user/preferences/cookies`
- [x] Endpoint POST `/api/user/preferences/cookies`
- [x] Validazione autenticazione obbligatoria
- [x] Migration per `user_preferences`
- [x] Migration per `cookie_consent_logs`
- [x] Gestione errori completa

### **Frontend (da implementare):**
- [ ] Hook `useCookieSync` per sincronizzazione
- [ ] Integrazione con cookie banner
- [ ] Sincronizzazione al login
- [ ] Gestione fallback localStorage
- [ ] Test end-to-end

---

## 🚀 Flusso Completo

### **1. Utente NON Autenticato:**
```
Frontend → localStorage → ✅ Salvato localmente
Backend → ❌ Nessuna chiamata API
```

### **2. Utente Autenticato (Prima Volta):**
```
Frontend → POST /api/user/preferences/cookies
Backend → Crea UserPreferences + CookieConsentLog
Response → ✅ Preferenze salvate + localStorage sincronizzato
```

### **3. Utente Autenticato (Modifica):**
```
Frontend → POST /api/user/preferences/cookies
Backend → Aggiorna UserPreferences + Crea nuovo CookieConsentLog
Response → ✅ Preferenze aggiornate + localStorage sincronizzato
```

### **4. Utente Autenticato (Caricamento):**
```
Frontend → GET /api/user/preferences/cookies
Backend → Restituisce preferenze dal database
Response → ✅ Preferenze caricate + localStorage sincronizzato
```

---

## 📝 Note Importanti

1. **Utenti Anonimi:** Non possono accedere agli endpoint cookie (401 Unauthorized)
2. **Audit Log:** Creato automaticamente ad ogni modifica
3. **Minimizzazione GDPR:** IP address non salvato (campo sempre NULL)
4. **Cascade Delete:** Log eliminati automaticamente alla cancellazione utente
5. **Timestamp:** Sempre in UTC per consistenza

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

