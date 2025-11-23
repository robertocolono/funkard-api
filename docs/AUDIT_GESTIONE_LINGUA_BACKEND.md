# 🔍 AUDIT COMPLETO: Gestione Lingua nel Backend Funkard

**Data Audit:** 2025-01-XX  
**Versione Backend:** Spring Boot 3 + Java 21  
**Scope:** Solo `User.language` (ignorando traduzioni GPT/DeepL)

---

## 📋 INDICE

1. [Punti di Lettura/Scrittura/Restituzione](#1-punti-di-letturascritturarestituzione)
2. [Whitelist Lingue Supportate](#2-whitelist-lingue-supportate)
3. [Normalizzazione](#3-normalizzazione)
4. [Migrazione Database](#4-migrazione-database)
5. [Hardcoded "en" e Default](#5-hardcoded-en-e-default)
6. [Disallineamenti](#6-disallineamenti)
7. [Liste Lingue in Altri Servizi](#7-liste-lingue-in-altri-servizi)
8. [Problemi Reali e Fix Necessari](#8-problemi-reali-e-fix-necessari)

---

## 1. PUNTI DI LETTURA/SCRITTURA/RESTITUZIONE

### 1.1 LETTURA `user.getLanguage()`

| File | Linea | Contesto | Fallback |
|------|-------|----------|----------|
| `UserService.java` | 116 | `getUserProfile()` → `dto.setLanguage(user.getLanguage())` | Nessuno (può essere null) |
| `AuthController.java` | 82 | `register()` → `LoginResponse` | `"en"` se null |
| `AuthController.java` | 111 | `login()` → `LoginResponse` | `"en"` se null |
| `ChatService.java` | 45 | `sendMessage()` → rileva lingua mittente | `"en"` se null |
| `ChatService.java` | 46 | `sendMessage()` → rileva lingua destinatario | `"en"` se null |
| `SupportMessageService.java` | 144 | `detectSenderLanguage()` → rileva lingua utente | `"en"` se null |
| `UserDeletionScheduler.java` | 72 | `run()` → recupera locale per email | `"en"` se null/empty |
| `EmailLocaleHelper.java` | 30 | `getUserLocale()` → converte a Locale | `Locale.ENGLISH` se null/empty |

**Totale letture:** 8 punti

### 1.2 SCRITTURA `user.setLanguage()`

| File | Linea | Contesto | Validazione |
|------|-------|----------|-------------|
| `AuthController.java` | 66 | `register()` → `user.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en")` | ❌ Nessuna |
| `UserService.java` | 156 | `updateUserProfile()` → `user.setLanguage(dto.getLanguage())` | ❌ Nessuna |
| `UserController.java` | 395 | `updatePreferences()` → `user.setLanguage(dto.getLanguage().toLowerCase())` | ⚠️ Solo length ≤ 5 |

**Totale scritture:** 3 punti

### 1.3 RESTITUZIONE AL FRONTEND

| Endpoint | File | Linea | DTO | Campo |
|----------|------|-------|-----|-------|
| `POST /api/auth/register` | `AuthController.java` | 82 | `LoginResponse` | `language` |
| `POST /api/auth/login` | `AuthController.java` | 111 | `LoginResponse` | `language` |
| `GET /api/user/me` | `UserController.java` | 69 | `UserProfileDTO` | `language` |
| `PUT /api/user/me` | `UserController.java` | 93 | `UserProfileDTO` | `language` (restituito dopo update) |
| `PATCH /api/user/preferences` | `UserController.java` | 431, 437 | `UserPreferencesDTO` | `language` |

**Totale restituzioni:** 5 endpoint

---

## 2. WHITELIST LINGUE SUPPORTATE

### 2.1 Whitelist per `User.language`

**❌ NON ESISTE**

Nessuna validazione whitelist per `User.language` in:
- `UserService.updateUserProfile()` (linea 155-156)
- `AuthController.register()` (linea 66)
- `UserController.updatePreferences()` (linea 392-399) - solo length check

### 2.2 Whitelist in Altri Servizi

#### A) `TranslateController.SUPPORTED_LANGUAGES`
**File:** `TranslateController.java` (linea 41-43)
```java
private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
    "en", "it", "es", "fr", "de", "pt", "ru", "ja", "zh"
);
```
**Lingue:** 9  
**Usato per:** Validazione `POST /api/translate`  
**Usato per `User.language`?** ❌ NO

#### B) `EmailTemplateManager.SUPPORTED_LANGUAGES`
**File:** `EmailTemplateManager.java` (linea 32-36)
```java
private static final String[] SUPPORTED_LANGUAGES = {
    "it", "en", "es", "de", "fr", "pt", "nl", "pl", "ja", "zh",
    "ko", "id", "hi", "th", "ms", "vi", "fil", "tr", "ar", "he",
    "fa", "sw", "zu", "es-419", "en-us", "en-gb", "fr-ca"
};
```
**Lingue:** 25+  
**Usato per:** Caricamento template email  
**Usato per `User.language`?** ❌ NO

#### C) `TranslationService.normalizeLanguage()` - validCodes
**File:** `TranslationService.java` (linea 226-229)
```java
String[] validCodes = {"en", "it", "es", "de", "fr", "pt", "nl", "pl", "ja", "zh", 
                      "ko", "id", "hi", "th", "ms", "vi", "fil", "tr", "ar", "he", 
                      "fa", "sw", "zu", "ru", "uk", "cs", "sk", "hu", "ro", "bg", 
                      "hr", "sr", "sl", "et", "lv", "lt", "fi", "sv", "da", "no", "is"};
```
**Lingue:** 40+  
**Usato per:** Normalizzazione traduzioni  
**Usato per `User.language`?** ❌ NO

### 2.3 Risultato Whitelist

| Componente | Lingue | Usato per User.language? |
|------------|--------|--------------------------|
| `TranslateController` | 9 | ❌ NO |
| `EmailTemplateManager` | 25+ | ❌ NO |
| `TranslationService` | 40+ | ❌ NO |
| **`User.language`** | **Nessuna whitelist** | **N/A** |

**PROBLEMA CRITICO:** Nessuna validazione whitelist per `User.language`, ma esistono 3 liste diverse in altri servizi.

---

## 3. NORMALIZZAZIONE

### 3.1 Normalizzazione per `User.language`

#### A) `UserController.updatePreferences()`
**File:** `UserController.java` (linea 395)
```java
user.setLanguage(dto.getLanguage().toLowerCase());
```
**Normalizzazione:** ✅ `toLowerCase()`  
**Trim:** ❌ NO  
**Estrae codice principale:** ❌ NO (es. "en-US" → "en")

#### B) `UserService.updateUserProfile()`
**File:** `UserService.java` (linea 156)
```java
user.setLanguage(dto.getLanguage());
```
**Normalizzazione:** ❌ Nessuna

#### C) `AuthController.register()`
**File:** `AuthController.java` (linea 66)
```java
user.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
```
**Normalizzazione:** ❌ Nessuna

### 3.2 Normalizzazione in Altri Servizi

#### A) `TranslateController`
**File:** `TranslateController.java` (linea 78-87)
```java
String normalizedLanguage = request.getTargetLanguage().trim().toLowerCase();
if (normalizedLanguage.contains("-")) {
    normalizedLanguage = normalizedLanguage.substring(0, normalizedLanguage.indexOf("-"));
}
if (normalizedLanguage.length() > 2) {
    normalizedLanguage = normalizedLanguage.substring(0, 2);
}
```
**Normalizzazione:** ✅ `trim()` + `toLowerCase()` + estrae codice principale

#### B) `UnifiedTranslationService`
**File:** `UnifiedTranslationService.java` (linea 92-109)
```java
private String normalizeLanguage(String lang) {
    if (lang == null || lang.trim().isEmpty()) {
        return "en";
    }
    String normalized = lang.trim().toLowerCase();
    if (normalized.contains("-")) {
        normalized = normalized.substring(0, normalized.indexOf("-"));
    }
    if (normalized.length() > 2) {
        normalized = normalized.substring(0, 2);
    }
    return normalized;
}
```
**Normalizzazione:** ✅ Completa (trim + lowercase + estrae codice)

#### C) `EmailTemplateManager`
**File:** `EmailTemplateManager.java` (linea 91-120)
```java
private String normalizeLanguage(Locale locale) {
    // Gestisce varianti regionali (es-419, en-us, en-gb, fr-ca)
    // Verifica whitelist
    // Fallback a DEFAULT_LANGUAGE
}
```
**Normalizzazione:** ✅ Avanzata (gestisce varianti regionali)

### 3.3 Risultato Normalizzazione

| Componente | Normalizzazione User.language |
|------------|-------------------------------|
| `UserController.updatePreferences()` | ⚠️ Parziale (solo lowercase) |
| `UserService.updateUserProfile()` | ❌ Nessuna |
| `AuthController.register()` | ❌ Nessuna |
| `TranslateController` | ✅ Completa (non usata per User.language) |
| `UnifiedTranslationService` | ✅ Completa (non usata per User.language) |

**PROBLEMA:** Normalizzazione inconsistente. Solo `PATCH /api/user/preferences` normalizza (parzialmente).

---

## 4. MIGRAZIONE DATABASE

### 4.1 Migration V12

**File:** `src/main/resources/db/migration/V12__add_language_to_users.sql`

```sql
-- Aggiungi colonna language se non esiste
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS language VARCHAR(5) DEFAULT 'en';

-- Aggiorna utenti esistenti con lingua di default se NULL
UPDATE users 
SET language = 'en' 
WHERE language IS NULL;

-- 🔍 Aggiungi indice per performance (opzionale)
CREATE INDEX IF NOT EXISTS idx_users_language ON users(language);

-- 📝 Commenti per documentazione
COMMENT ON COLUMN users.language IS 'Lingua preferita utente (es. en, it, es, de, fr)';
```

### 4.2 Schema Database

| Proprietà | Valore |
|-----------|--------|
| **Tipo** | `VARCHAR(5)` |
| **Nullable** | ✅ SÌ (nessun `NOT NULL`) |
| **Default** | `'en'` |
| **Indice** | ✅ `idx_users_language` |
| **Constraint CHECK** | ❌ Nessuno |
| **Commento** | ✅ Presente |

### 4.3 Entity Mapping

**File:** `User.java` (linea 53-54)
```java
@Column(name = "language", length = 5)
private String language = "en";
```

**Allineamento DB ↔ Entity:** ✅ Corretto

---

## 5. HARDCODED "en" E DEFAULT

### 5.1 Hardcoded "en" nel Codice

| File | Linea | Contesto | Tipo |
|------|-------|----------|------|
| `User.java` | 54 | `private String language = "en";` | Default Java |
| `AuthController.java` | 66 | `request.getLanguage() != null ? request.getLanguage() : "en"` | Fallback |
| `AuthController.java` | 82 | `savedUser.getLanguage() != null ? savedUser.getLanguage() : "en"` | Fallback |
| `AuthController.java` | 111 | `user.getLanguage() != null ? user.getLanguage() : "en"` | Fallback |
| `ChatService.java` | 45 | `sender.getLanguage() != null ? sender.getLanguage() : "en"` | Fallback |
| `ChatService.java` | 46 | `recipient.getLanguage() != null ? recipient.getLanguage() : "en"` | Fallback |
| `SupportMessageService.java` | 136 | `return "en";` (admin default) | Hardcoded |
| `SupportMessageService.java` | 153 | `return "en";` (fallback) | Hardcoded |
| `SupportMessageService.java` | 166 | `return "en";` (admin target) | Hardcoded |
| `UserDeletionScheduler.java` | 67 | `String userLocale = "en";` | Default |
| `EmailLocaleHelper.java` | 27 | `return Locale.ENGLISH;` | Default |
| `EmailLocaleHelper.java` | 32 | `return Locale.ENGLISH;` | Default |
| `EmailLocaleHelper.java` | 46 | `return Locale.ENGLISH;` | Default |
| `EmailLocaleHelper.java` | 61 | `return Locale.ENGLISH;` | Default |
| `EmailService.java` | 189 | `locale != null ? locale.getLanguage() : "en"` | Fallback |
| `EmailService.java` | 238 | `locale != null ? locale.getLanguage() : "en"` | Fallback |
| `EmailTemplateManager.java` | 38 | `private static final String DEFAULT_LANGUAGE = "en";` | Costante |
| `TranslationService.java` | 215 | `return "en";` (default) | Fallback |
| `TranslationService.java` | 239 | `return "en";` (fallback) | Fallback |
| `UnifiedTranslationService.java` | 94 | `return "en";` (fallback) | Fallback |

**Totale hardcoded "en":** 20+ occorrenze

### 5.2 Default Impliciti

| Location | Default Implicito | Tipo |
|----------|-------------------|------|
| Database `DEFAULT 'en'` | `'en'` | SQL |
| Entity `= "en"` | `"en"` | Java |
| `EmailTemplateManager.DEFAULT_LANGUAGE` | `"en"` | Costante |

### 5.3 Risultato Hardcoded

**PROBLEMA:** "en" hardcoded in 20+ punti. Se cambia il default, serve refactoring massivo.

---

## 6. DISALLINEAMENTI

### 6.1 Disallineamento Controller ↔ Service

| Endpoint | Controller | Service | Validazione |
|-----------|------------|---------|-------------|
| `PUT /api/user/me` | `UserController.updateProfile()` | `UserService.updateUserProfile()` | ❌ Nessuna (linea 155-156) |
| `PATCH /api/user/preferences` | `UserController.updatePreferences()` | Diretto su `userRepository` | ⚠️ Solo length ≤ 5 (linea 394) |

**Problema:** Validazione inconsistente tra endpoint.

### 6.2 Disallineamento DTO

| DTO | Campo `language` | Usato in |
|-----|------------------|----------|
| `UserProfileDTO` | ✅ Presente | `GET /api/user/me`, `PUT /api/user/me` |
| `UserPreferencesDTO` | ✅ Presente | `PATCH /api/user/preferences` |
| `LoginResponse` | ✅ Presente | `POST /api/auth/login`, `POST /api/auth/register` |
| `UserDTO` | ❌ **MANCANTE** | `UserService.toDTO()` (linea 206-215) |

**Problema:** `UserDTO` non include `language`, ma include `preferredCurrency`.

### 6.3 Disallineamento Normalizzazione

| Componente | Normalizzazione |
|------------|-----------------|
| `UserController.updatePreferences()` | ✅ `toLowerCase()` |
| `UserService.updateUserProfile()` | ❌ Nessuna |
| `AuthController.register()` | ❌ Nessuna |

**Problema:** Normalizzazione inconsistente.

### 6.4 Disallineamento Validazione

| Componente | Validazione |
|------------|-------------|
| `UserController.updatePreferences()` | ⚠️ Solo `length <= 5` |
| `UserService.updateUserProfile()` | ❌ Nessuna |
| `AuthController.register()` | ❌ Nessuna |

**Problema:** Nessuna validazione whitelist.

---

## 7. LISTE LINGUE IN ALTRI SERVIZI

### 7.1 Confronto Liste

| Servizio | File | Lingue | Usato per |
|----------|------|--------|-----------|
| `TranslateController` | `TranslateController.java:41` | 9: `["en","it","es","fr","de","pt","ru","ja","zh"]` | Validazione traduzione |
| `EmailTemplateManager` | `EmailTemplateManager.java:32` | 25+: `["it","en","es","de","fr","pt","nl","pl","ja","zh","ko","id","hi","th","ms","vi","fil","tr","ar","he","fa","sw","zu","es-419","en-us","en-gb","fr-ca"]` | Template email |
| `TranslationService` | `TranslationService.java:226` | 40+: `["en","it","es","de","fr","pt","nl","pl","ja","zh","ko","id","hi","th","ms","vi","fil","tr","ar","he","fa","sw","zu","ru","uk","cs","sk","hu","ro","bg","hr","sr","sl","et","lv","lt","fi","sv","da","no","is"]` | Normalizzazione traduzione |
| **`User.language`** | **N/A** | **Nessuna lista** | **Validazione utente** |

### 7.2 Inconsistenze

1. **Numero lingue diverso:** 9 vs 25+ vs 40+
2. **Lingue mancanti in alcune liste:**
   - `TranslateController` non ha: `nl`, `pl`, `ko`, `id`, `hi`, `th`, `ms`, `vi`, `fil`, `tr`, `ar`, `he`, `fa`, `sw`, `zu`
   - `EmailTemplateManager` non ha: `ru`, `uk`, `cs`, `sk`, `hu`, `ro`, `bg`, `hr`, `sr`, `sl`, `et`, `lv`, `lt`, `fi`, `sv`, `da`, `no`, `is`
3. **Varianti regionali:** Solo `EmailTemplateManager` supporta `es-419`, `en-us`, `en-gb`, `fr-ca`

### 7.3 Risultato Liste

**PROBLEMA CRITICO:** 3 liste diverse, nessuna usata per validare `User.language`. Disallineamento totale.

---

## 8. PROBLEMI REALI E FIX NECESSARI

### 8.1 Problemi Critici (Bloccanti)

#### ❌ **PROBLEMA #1: Nessuna Whitelist per User.language**
- **Severità:** 🔴 CRITICA
- **Impatto:** Possibile salvare lingue non supportate (es. "xx", "zz", "abc")
- **File coinvolti:**
  - `UserService.java:155-156`
  - `AuthController.java:66`
  - `UserController.java:392-399`
- **Fix necessario:** Creare costante `SUPPORTED_USER_LANGUAGES` e validare in tutti i punti di scrittura

#### ❌ **PROBLEMA #2: Normalizzazione Inconsistente**
- **Severità:** 🟡 MEDIA
- **Impatto:** Possibili duplicati ("EN" vs "en"), formati non standard ("en-US" non normalizzato)
- **File coinvolti:**
  - `UserService.java:156` (nessuna normalizzazione)
  - `AuthController.java:66` (nessuna normalizzazione)
  - `UserController.java:395` (solo lowercase, no trim, no estrazione codice)
- **Fix necessario:** Metodo centralizzato `normalizeUserLanguage(String lang)` usato ovunque

#### ❌ **PROBLEMA #3: Hardcoded "en" in 20+ Punti**
- **Severità:** 🟡 MEDIA
- **Impatto:** Difficile cambiare default, refactoring massivo necessario
- **File coinvolti:** 20+ file
- **Fix necessario:** Costante `DEFAULT_USER_LANGUAGE = "en"` centralizzata

### 8.2 Problemi Non Critici (Raccomandati)

#### ⚠️ **PROBLEMA #4: Disallineamento Liste Lingue**
- **Severità:** 🟢 BASSA
- **Impatto:** Confusione su lingue supportate, manutenzione difficile
- **Fix necessario:** Unificare liste in un'unica costante `SUPPORTED_LANGUAGES`

#### ⚠️ **PROBLEMA #5: UserDTO Non Include language**
- **Severità:** 🟢 BASSA
- **Impatto:** Disallineamento FE/BE se `UserDTO` viene usato
- **File:** `UserService.java:206-215`
- **Fix necessario:** Aggiungere `language` a `UserDTO`

#### ⚠️ **PROBLEMA #6: Nessun Constraint CHECK nel DB**
- **Severità:** 🟢 BASSA
- **Impatto:** Database non previene valori invalidi
- **Fix necessario:** `ALTER TABLE users ADD CONSTRAINT check_language CHECK (language IN (...))`

### 8.3 Fix Prioritari PRIMA di Aggiungere 20 Nuove Lingue

#### 🔴 **PRIORITÀ 1: Creare Costante Centralizzata**
```java
// File: src/main/java/com/funkard/config/LanguageConfig.java
public class LanguageConfig {
    public static final String DEFAULT_USER_LANGUAGE = "en";
    public static final List<String> SUPPORTED_USER_LANGUAGES = Arrays.asList(
        "en", "it", "es", "fr", "de", "pt", "nl", "pl", "ja", "zh",
        "ko", "id", "hi", "th", "ms", "vi", "fil", "tr", "ar", "he",
        "fa", "sw", "zu", "ru", "uk", "cs", "sk", "hu", "ro", "bg",
        "hr", "sr", "sl", "et", "lv", "lt", "fi", "sv", "da", "no", "is"
    );
    
    public static String normalizeUserLanguage(String lang) {
        if (lang == null || lang.trim().isEmpty()) {
            return DEFAULT_USER_LANGUAGE;
        }
        String normalized = lang.trim().toLowerCase();
        if (normalized.contains("-")) {
            normalized = normalized.substring(0, normalized.indexOf("-"));
        }
        if (normalized.length() > 2) {
            normalized = normalized.substring(0, 2);
        }
        return SUPPORTED_USER_LANGUAGES.contains(normalized) 
            ? normalized 
            : DEFAULT_USER_LANGUAGE;
    }
}
```

#### 🔴 **PRIORITÀ 2: Aggiungere Validazione Whitelist**
- `UserService.updateUserProfile()`: Validare contro `SUPPORTED_USER_LANGUAGES`
- `AuthController.register()`: Validare contro `SUPPORTED_USER_LANGUAGES`
- `UserController.updatePreferences()`: Validare contro `SUPPORTED_USER_LANGUAGES` (oltre a length)

#### 🔴 **PRIORITÀ 3: Normalizzazione Unificata**
- Sostituire tutti gli hardcoded "en" con `LanguageConfig.DEFAULT_USER_LANGUAGE`
- Usare `LanguageConfig.normalizeUserLanguage()` in tutti i punti di scrittura

#### 🟡 **PRIORITÀ 4: Allineare Liste Lingue**
- Unificare `TranslateController`, `EmailTemplateManager`, `TranslationService` con `LanguageConfig.SUPPORTED_USER_LANGUAGES`

#### 🟡 **PRIORITÀ 5: Aggiungere language a UserDTO**
- Aggiungere campo `language` a `UserDTO`
- Aggiornare `UserService.toDTO()` per includere `language`

---

## 9. RIEPILOGO FINALE

### 9.1 Statistiche

| Metrica | Valore |
|---------|--------|
| **Punti lettura `user.getLanguage()`** | 8 |
| **Punti scrittura `user.setLanguage()`** | 3 |
| **Endpoint che restituiscono `language`** | 5 |
| **Hardcoded "en"** | 20+ |
| **Whitelist per `User.language`** | 0 |
| **Liste lingue in altri servizi** | 3 (9, 25+, 40+ lingue) |
| **Normalizzazione consistente** | ❌ NO |

### 9.2 Problemi Trovati

| # | Problema | Severità | Fix Necessario |
|---|----------|----------|----------------|
| 1 | Nessuna whitelist per `User.language` | 🔴 CRITICA | Costante + validazione |
| 2 | Normalizzazione inconsistente | 🟡 MEDIA | Metodo centralizzato |
| 3 | Hardcoded "en" in 20+ punti | 🟡 MEDIA | Costante centralizzata |
| 4 | Disallineamento liste lingue | 🟢 BASSA | Unificare liste |
| 5 | `UserDTO` non include `language` | 🟢 BASSA | Aggiungere campo |
| 6 | Nessun constraint CHECK nel DB | 🟢 BASSA | Aggiungere constraint |

### 9.3 Raccomandazioni Finali

**PRIMA di aggiungere 20 nuove lingue:**

1. ✅ Creare `LanguageConfig` con costanti centralizzate
2. ✅ Aggiungere validazione whitelist in tutti i punti di scrittura
3. ✅ Normalizzazione unificata con metodo centralizzato
4. ✅ Sostituire hardcoded "en" con costante
5. ⚠️ Allineare liste lingue tra servizi (opzionale ma raccomandato)

**DOPO aver sistemato i problemi critici:**

- Aggiungere nuove lingue a `LanguageConfig.SUPPORTED_USER_LANGUAGES`
- Tutti i servizi useranno automaticamente la nuova lista
- Nessun refactoring necessario

---

**Fine Report Audit**

