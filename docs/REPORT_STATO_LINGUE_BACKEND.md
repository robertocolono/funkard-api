# 📊 REPORT STATO ATTUALE GESTIONE LINGUE - Backend Funkard

**Data Analisi:** 2025-01-XX  
**Tipo:** Analisi Read-Only (Nessuna Modifica)  
**Obiettivo:** Verificare supporto attuale per 31 lingue

---

## 1️⃣ ENUM DELLE LINGUE

### ❌ **RISULTATO: NESSUN ENUM TROVATO**

**Ricerca eseguita:**
- Pattern: `enum.*Language`, `Language.*enum`, `public enum Language`
- File analizzati: Tutti i file Java nel progetto
- **Risultato:** Nessun enum `Language` o equivalente trovato

**Conclusione:**
- Il backend **NON usa enum** per le lingue
- Tutte le lingue sono gestite come **String**

---

## 2️⃣ MODELLO UTENTE (User)

### **File:** `src/main/java/com/funkard/model/User.java`

#### **Campo `language` (linea 53-54):**
```java
@Column(name = "language", length = 5)
private String language = "en";
```

**Tipo:** `String` (NON enum)  
**Database:** `VARCHAR(5)`  
**Default:** `"en"`  
**Nullable:** SÌ (nessun `nullable = false`)  
**Validazione:** ❌ Nessuna whitelist

#### **Campo `descriptionLanguage` (linea 86-87):**
```java
@Column(name = "description_language", length = 5)
private String descriptionLanguage;
```

**Tipo:** `String` (NON enum)  
**Database:** `VARCHAR(5)`  
**Nullable:** SÌ  
**Validazione:** ❌ Nessuna whitelist

**Conclusione:**
- `User.language` accetta **qualsiasi stringa ≤ 5 caratteri**
- Nessuna validazione contro lista lingue supportate
- Nessun enum, solo String

---

## 3️⃣ DTO E REQUEST MODEL

### **A) UserProfileDTO**
**File:** `src/main/java/com/funkard/dto/UserProfileDTO.java`

**Campi lingua:**
- `language` (linea 20): `String` - ❌ Nessuna validazione
- `descriptionLanguage` (linea 37): `String` - ❌ Nessuna validazione

### **B) CreateListingRequest**
**File:** `src/main/java/com/funkard/dto/CreateListingRequest.java`

**Campi lingua:**
- `language` (linea 30): `String` - ❌ Nessuna validazione
- `customLanguage` (linea 35): `String` - ❌ Nessuna validazione

### **C) ChatMessageDTO**
**File:** `src/main/java/com/funkard/dto/ChatMessageDTO.java`

**Campi lingua:**
- `originalLanguage` (linea 28): `String` - ❌ Nessuna validazione
- `targetLanguage` (linea 29): `String` - ❌ Nessuna validazione

### **D) TranslateRequest**
**File:** `src/main/java/com/funkard/dto/TranslateRequest.java`

**Campi lingua:**
- `targetLanguage` (linea 20): `String` - ⚠️ Validato solo in controller

### **E) Product Model**
**File:** `src/main/java/com/funkard/market/model/Product.java`

**Campi lingua:**
- `descriptionLanguage` (linea 39-40): `String` - ❌ Nessuna validazione

### **F) ChatMessage Model**
**File:** `src/main/java/com/funkard/model/ChatMessage.java`

**Campi lingua:**
- `originalLanguage` (linea 43-44): `String` - ❌ Nessuna validazione
- `targetLanguage` (linea 49-50): `String` - ❌ Nessuna validazione

### **G) SupportMessage Model**
**File:** `src/main/java/com/funkard/admin/model/SupportMessage.java`

**Campi lingua:**
- `originalLanguage` (linea 28-29): `String` - ❌ Nessuna validazione
- `targetLanguage` (linea 34-35): `String` - ❌ Nessuna validazione

**Conclusione:**
- **Tutti i DTO/Model usano `String`** per i campi lingua
- **Nessuna validazione** a livello di DTO (nessun `@Valid`, nessun enum)
- Solo `TranslateController` valida contro whitelist (9 lingue)

---

## 4️⃣ SERVIZIO DI TRADUZIONE (UnifiedTranslationService)

### **File:** `src/main/java/com/funkard/service/UnifiedTranslationService.java`

#### **Normalizzazione (linea 92-110):**
```java
private String normalizeLanguage(String lang) {
    // Estrae solo codice principale (es. "en-US" → "en")
    // Limita a 2 caratteri (ISO 639-1)
    // Nessuna whitelist
}
```

**Mappatura GPT:** ❌ Nessuna mappatura specifica  
**Mappatura DeepL:** ❌ Nessuna mappatura specifica  
**Fallback:** ✅ GPT → DeepL → testo originale  
**Whitelist:** ❌ Nessuna whitelist nel servizio unificato

### **OpenAiTranslateService**
**File:** `src/main/java/com/funkard/service/OpenAiTranslateService.java`

**Mappatura GPT:**
- ❌ Nessuna mappatura codici lingua
- GPT accetta qualsiasi stringa come `targetLanguage`
- Usa direttamente il codice normalizzato nel prompt

**Lingue supportate:** ❌ Nessuna lista (accetta qualsiasi codice)

### **DeepLTranslateService**
**File:** `src/main/java/com/funkard/service/DeepLTranslateService.java`

**Mappatura DeepL (linea 140-144):**
```java
String[] supportedLangs = {
    "EN", "IT", "ES", "FR", "DE", "PT", "PL", "RU", "JA", "ZH",
    "NL", "SV", "DA", "FI", "EL", "CS", "RO", "HU", "BG", "SK",
    "SL", "ET", "LV", "LT", "MT", "GA", "HR", "SR"
};
```

**Lingue supportate DeepL:** 28 lingue  
**Normalizzazione:** Converte a UPPERCASE, estrae codice principale  
**Fallback:** Se non riconosciuto → "EN"

**Conclusione:**
- `UnifiedTranslationService`: Nessuna whitelist, solo normalizzazione
- `OpenAiTranslateService`: Nessuna whitelist, accetta qualsiasi codice
- `DeepLTranslateService`: Whitelist di **28 lingue** (ma non usata per validare input)

---

## 5️⃣ CONTROLLER

### **A) TranslateController**
**File:** `src/main/java/com/funkard/controller/TranslateController.java`

**Whitelist (linea 41-43):**
```java
private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
    "en", "it", "es", "fr", "de", "pt", "ru", "ja", "zh"
);
```

**Lingue supportate:** 9  
**Validazione:** ✅ Rifiuta lingue non in whitelist (linea 91-96)  
**Endpoint:** `POST /api/translate`

### **B) UserController**
**File:** `src/main/java/com/funkard/controller/UserController.java`

**Validazione `language` (linea 392-399):**
```java
if (dto.getLanguage() != null && !dto.getLanguage().trim().isEmpty()) {
    if (dto.getLanguage().length() <= 5) {
        user.setLanguage(dto.getLanguage().toLowerCase());
    }
}
```

**Validazione:** ⚠️ Solo `length <= 5`, nessuna whitelist  
**Endpoint:** `PATCH /api/user/preferences`

### **C) AuthController**
**File:** `src/main/java/com/funkard/controller/AuthController.java`

**Validazione `language` (linea 66):**
```java
user.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
```

**Validazione:** ❌ Nessuna validazione  
**Endpoint:** `POST /api/auth/register`

### **D) UserService**
**File:** `src/main/java/com/funkard/service/UserService.java`

**Validazione `language` (linea 155-156):**
```java
if (dto.getLanguage() != null) {
    user.setLanguage(dto.getLanguage());
}
```

**Validazione:** ❌ Nessuna validazione  
**Endpoint:** Usato da `PUT /api/user/me`

**Conclusione:**
- Solo `TranslateController` ha whitelist rigida (9 lingue)
- Altri controller/endpoint **NON validano** contro whitelist
- Possibile salvare lingue non supportate nel database

---

## 6️⃣ CRONJOB / JOB PERIODICI

### **A) UserDeletionScheduler**
**File:** `src/main/java/com/funkard/scheduler/UserDeletionScheduler.java`

**Uso lingua (linea 72-73):**
```java
if (user.getLanguage() != null && !user.getLanguage().isEmpty()) {
    userLocale = user.getLanguage().toLowerCase();
}
```

**Whitelist:** ❌ Nessuna whitelist  
**Fallback:** `"en"` se null/empty (linea 67)  
**Uso:** Recupera `user.language` per inviare email di cancellazione

**Conclusione:**
- Scheduler usa `user.language` direttamente
- Nessuna validazione contro whitelist
- Se `user.language` contiene valore non supportato, potrebbe causare problemi in `EmailService`

---

## 7️⃣ DB / JPA

### **Migration V12**
**File:** `src/main/resources/db/migration/V12__add_language_to_users.sql`

```sql
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS language VARCHAR(5) DEFAULT 'en';
```

**Tipo colonna:** `VARCHAR(5)` (NON ENUM)  
**Default:** `'en'`  
**Nullable:** SÌ (nessun `NOT NULL`)  
**Constraint CHECK:** ❌ Nessuno

### **Altre colonne language:**
- `chat_messages.original_language`: `VARCHAR(5)` (Migration V14)
- `chat_messages.target_language`: `VARCHAR(5)` (Migration V14)
- `support_messages.original_language`: `VARCHAR(5)` (Migration V13)
- `support_messages.target_language`: `VARCHAR(5)` (Migration V13)
- `products.description_language`: `VARCHAR(5)` (Migration V21)
- `users.description_language`: `VARCHAR(5)` (Migration V21)

**Tutte le colonne:** `VARCHAR(5)`, nessun ENUM, nessun CHECK constraint

**Conclusione:**
- Database usa **VARCHAR(5)** per tutte le colonne lingua
- **Nessun ENUM** nel database
- **Nessun constraint CHECK** per validare lingue supportate
- Possibile salvare qualsiasi stringa ≤ 5 caratteri

---

## 8️⃣ LISTE LINGUE TROVATE NEL BACKEND

### **A) TranslateController.SUPPORTED_LANGUAGES**
**File:** `TranslateController.java:41-43`  
**Lingue:** 9
```
en, it, es, fr, de, pt, ru, ja, zh
```

### **B) EmailTemplateManager.SUPPORTED_LANGUAGES**
**File:** `EmailTemplateManager.java:32-36`  
**Lingue:** 25
```
it, en, es, de, fr, pt, nl, pl, ja, zh,
ko, id, hi, th, ms, vi, fil, tr, ar, he,
fa, sw, zu, es-419, en-us, en-gb, fr-ca
```

### **C) DeepLTranslateService.supportedLangs**
**File:** `DeepLTranslateService.java:140-144`  
**Lingue:** 28
```
EN, IT, ES, FR, DE, PT, PL, RU, JA, ZH,
NL, SV, DA, FI, EL, CS, RO, HU, BG, SK,
SL, ET, LV, LT, MT, GA, HR, SR
```

### **D) TranslationService.validCodes**
**File:** `TranslationService.java:226-229`  
**Lingue:** 40
```
en, it, es, de, fr, pt, nl, pl, ja, zh,
ko, id, hi, th, ms, vi, fil, tr, ar, he,
fa, sw, zu, ru, uk, cs, sk, hu, ro, bg,
hr, sr, sl, et, lv, lt, fi, sv, da, no, is
```

**Conclusione:**
- **4 liste diverse** con numeri diversi di lingue (9, 25, 28, 40)
- **Nessuna lista usata per validare `User.language`**
- Disallineamento totale tra servizi

---

## 9️⃣ LINGUE MANCANTI (31 lingue target)

### **Lingue Target (31):**
```
en, it, es, fr, de, pt, nl, pl, ja, zh,
ko, id, hi, th, ms, vi, fil, tr, ar, he,
fa, sw, zu, ru, uk, cs, sk, hu, ro, bg,
hr, sr, sl, et, lv, lt, fi, sv, da, no,
is, el, mt, ga
```

### **Confronto con Liste Attuali:**

#### **TranslateController (9 lingue):**
✅ Presenti: `en, it, es, fr, de, pt, ru, ja, zh`  
❌ Mancanti: `nl, pl, ko, id, hi, th, ms, vi, fil, tr, ar, he, fa, sw, zu, uk, cs, sk, hu, ro, bg, hr, sr, sl, et, lv, lt, fi, sv, da, no, is, el, mt, ga` (22 mancanti)

#### **EmailTemplateManager (25 lingue):**
✅ Presenti: `it, en, es, de, fr, pt, nl, pl, ja, zh, ko, id, hi, th, ms, vi, fil, tr, ar, he, fa, sw, zu, es-419, en-us, en-gb, fr-ca`  
❌ Mancanti: `ru, uk, cs, sk, hu, ro, bg, hr, sr, sl, et, lv, lt, fi, sv, da, no, is, el, mt, ga` (21 mancanti)

#### **DeepLTranslateService (28 lingue):**
✅ Presenti: `EN, IT, ES, FR, DE, PT, PL, RU, JA, ZH, NL, SV, DA, FI, EL, CS, RO, HU, BG, SK, SL, ET, LV, LT, MT, GA, HR, SR`  
❌ Mancanti: `ko, id, hi, th, ms, vi, fil, tr, ar, he, fa, sw, zu, uk, no, is` (16 mancanti)

#### **TranslationService (40 lingue):**
✅ Presenti: `en, it, es, de, fr, pt, nl, pl, ja, zh, ko, id, hi, th, ms, vi, fil, tr, ar, he, fa, sw, zu, ru, uk, cs, sk, hu, ro, bg, hr, sr, sl, et, lv, lt, fi, sv, da, no, is`  
❌ Mancanti: `el, mt, ga` (3 mancanti)

---

## 🔟 PUNTI DOVE SERVE INTERVENIRE

### **🔴 PRIORITÀ CRITICA**

#### **1. User.language - Nessuna Validazione**
**File:** `User.java`, `UserService.java`, `AuthController.java`, `UserController.java`  
**Problema:** Accetta qualsiasi stringa ≤ 5 caratteri  
**Intervento:** Aggiungere validazione whitelist in tutti i punti di scrittura

#### **2. TranslateController - Solo 9 Lingue**
**File:** `TranslateController.java:41-43`  
**Problema:** Whitelist rigida di solo 9 lingue  
**Intervento:** Aggiornare `SUPPORTED_LANGUAGES` a 31 lingue

#### **3. EmailTemplateManager - 25 Lingue (mancano 6)**
**File:** `EmailTemplateManager.java:32-36`  
**Problema:** Mancano `ru, uk, cs, sk, hu, ro, bg, hr, sr, sl, et, lv, lt, fi, sv, da, no, is, el, mt, ga`  
**Intervento:** Aggiungere lingue mancanti a `SUPPORTED_LANGUAGES`

#### **4. DeepLTranslateService - 28 Lingue (mancano 3)**
**File:** `DeepLTranslateService.java:140-144`  
**Problema:** Mancano `ko, id, hi, th, ms, vi, fil, tr, ar, he, fa, sw, zu, uk, no, is`  
**Intervento:** Aggiungere lingue mancanti a `supportedLangs`

### **🟡 PRIORITÀ MEDIA**

#### **5. UnifiedTranslationService - Nessuna Whitelist**
**File:** `UnifiedTranslationService.java`  
**Problema:** Normalizza ma non valida contro whitelist  
**Intervento:** Aggiungere validazione whitelist prima di chiamare GPT/DeepL

#### **6. UserDeletionScheduler - Nessuna Validazione**
**File:** `UserDeletionScheduler.java:72-73`  
**Problema:** Usa `user.language` direttamente senza validazione  
**Intervento:** Validare contro whitelist prima di usare per email

#### **7. DTO - Nessuna Validazione**
**File:** `UserProfileDTO.java`, `CreateListingRequest.java`, `ChatMessageDTO.java`  
**Problema:** Campi lingua senza validazione  
**Intervento:** Aggiungere validazione `@Valid` con whitelist

### **🟢 PRIORITÀ BASSA**

#### **8. Database - Nessun Constraint CHECK**
**File:** Migration V12, V13, V14, V21  
**Problema:** Colonne `VARCHAR(5)` senza constraint  
**Intervento:** Aggiungere `CHECK (language IN (...))` (opzionale, validazione lato applicazione è sufficiente)

#### **9. Disallineamento Liste**
**Problema:** 4 liste diverse con lingue diverse  
**Intervento:** Unificare in un'unica costante centralizzata

---

## 1️⃣1️⃣ RIEPILOGO FINALE

### **Stato Attuale:**

| Componente | Tipo | Lingue Supportate | Validazione |
|------------|------|-------------------|-------------|
| **User.language** | `String` | ❌ Nessuna lista | ❌ Nessuna |
| **TranslateController** | `List<String>` | 9 | ✅ Whitelist rigida |
| **EmailTemplateManager** | `String[]` | 25 | ⚠️ Solo per template |
| **DeepLTranslateService** | `String[]` | 28 | ⚠️ Solo per DeepL |
| **TranslationService** | `String[]` | 40 | ⚠️ Solo per normalizzazione |
| **Database** | `VARCHAR(5)` | ❌ Nessuna | ❌ Nessun CHECK |

### **Lingue Mancanti per 31 Lingue Target:**

- **TranslateController:** 22 mancanti
- **EmailTemplateManager:** 21 mancanti (considerando varianti regionali)
- **DeepLTranslateService:** 16 mancanti
- **TranslationService:** 3 mancanti (`el, mt, ga`)

### **Punti Critici:**

1. ❌ **Nessun enum Language** - Tutto è String
2. ❌ **User.language non validato** - Accetta qualsiasi valore
3. ❌ **4 liste diverse** - Disallineamento totale
4. ❌ **Database VARCHAR senza constraint** - Possibile salvare valori invalidi
5. ⚠️ **Solo TranslateController valida** - Altri endpoint accettano qualsiasi lingua

---

## 1️⃣2️⃣ INDICAZIONI PER INTERVENTO

### **File da Modificare (quando richiesto):**

1. `TranslateController.java` - Aggiornare `SUPPORTED_LANGUAGES` a 31 lingue
2. `EmailTemplateManager.java` - Aggiungere lingue mancanti
3. `DeepLTranslateService.java` - Aggiungere lingue mancanti
4. `TranslationService.java` - Aggiungere `el, mt, ga`
5. `UserService.java` - Aggiungere validazione whitelist
6. `AuthController.java` - Aggiungere validazione whitelist
7. `UserController.java` - Aggiungere validazione whitelist (oltre a length)
8. `UserDeletionScheduler.java` - Aggiungere validazione whitelist
9. `User.java` - Considerare aggiungere validazione `@Size` o custom validator
10. DTO vari - Aggiungere validazione `@Valid` con whitelist

### **Database (opzionale):**

- Migration per aggiungere `CHECK (language IN (...))` constraint (opzionale)

---

**Fine Report - Nessuna Modifica Eseguita**

