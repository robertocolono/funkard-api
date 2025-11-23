# 🔍 ANALISI FILE E RIFERIMENTI LINGUE - Backend Funkard

**Data Analisi:** 2025-01-XX  
**Tipo:** Analisi Read-Only (Nessuna Modifica)  
**Obiettivo:** Trovare tutte le liste, whitelist, mapping e riferimenti centralizzati alle lingue

---

## 📋 RIEPILOGO ESECUTIVO

**Risultato:** ❌ **NESSUN FILE CENTRALIZZATO TROVATO**

- ❌ Nessuna classe `LanguageConfig` o equivalente
- ❌ Nessun enum `Language`
- ❌ Nessun file dedicato alle lingue
- ✅ Trovate **5 liste diverse** sparse in vari servizi
- ✅ Trovati **mapping hardcoded** in `EmailService`
- ✅ Trovati **helper per normalizzazione** ma senza whitelist centralizzata

---

## 📁 FILE TROVATI CON LISTE LINGUE

### **1. TranslateController.java**

**Percorso:** `src/main/java/com/funkard/controller/TranslateController.java`

**Tipo:** Controller pubblico

**Lista Lingue (linea 41-43):**
```java
private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
    "en", "it", "es", "fr", "de", "pt", "ru", "ja", "zh"
);
```

**Numero Lingue:** 9

**Uso nel Backend:**
- ✅ **Usato per validazione** endpoint `POST /api/translate` (linea 91-96)
- ✅ **Whitelist rigida** - rifiuta lingue non in lista
- ⚠️ **Solo per questo endpoint** - non usato altrove

**Contiene Lista Lingue:** ✅ SÌ  
**È "Fonte della Verità"?** ❌ NO (solo per endpoint traduzione)

---

### **2. EmailTemplateManager.java**

**Percorso:** `src/main/java/com/funkard/service/EmailTemplateManager.java`

**Tipo:** Service (gestione template email)

**Lista Lingue (linea 32-36):**
```java
private static final String[] SUPPORTED_LANGUAGES = {
    "it", "en", "es", "de", "fr", "pt", "nl", "pl", "ja", "zh",
    "ko", "id", "hi", "th", "ms", "vi", "fil", "tr", "ar", "he",
    "fa", "sw", "zu", "es-419", "en-us", "en-gb", "fr-ca"
};
```

**Numero Lingue:** 25 (inclusi varianti regionali)

**Costante Default (linea 38):**
```java
private static final String DEFAULT_LANGUAGE = "en";
```

**Uso nel Backend:**
- ✅ **Usato per caricamento template email** (linea 112-116)
- ✅ **Usato per normalizzazione locale** (linea 91-120)
- ✅ **Supporta varianti regionali** (es-419, en-us, en-gb, fr-ca)
- ⚠️ **Solo per template email** - non usato per validazione `User.language`

**Contiene Lista Lingue:** ✅ SÌ  
**È "Fonte della Verità"?** ❌ NO (solo per template email)

---

### **3. DeepLTranslateService.java**

**Percorso:** `src/main/java/com/funkard/service/DeepLTranslateService.java`

**Tipo:** Service (traduzione DeepL)

**Lista Lingue (linea 140-144):**
```java
private String[] supportedLangs = {
    "EN", "IT", "ES", "FR", "DE", "PT", "PL", "RU", "JA", "ZH",
    "NL", "SV", "DA", "FI", "EL", "CS", "RO", "HU", "BG", "SK",
    "SL", "ET", "LV", "LT", "MT", "GA", "HR", "SR"
};
```

**Numero Lingue:** 28 (in UPPERCASE per DeepL API)

**Uso nel Backend:**
- ✅ **Usato per normalizzazione DeepL** (linea 131-166)
- ✅ **Metodo privato** `normalizeLanguageForDeepL()` (linea 131)
- ⚠️ **Solo per DeepL API** - non usato per validazione input
- ⚠️ **Formato UPPERCASE** - diverso da altre liste

**Contiene Lista Lingue:** ✅ SÌ  
**È "Fonte della Verità"?** ❌ NO (solo per DeepL, formato diverso)

---

### **4. TranslationService.java**

**Percorso:** `src/main/java/com/funkard/service/TranslationService.java`

**Tipo:** Service (traduzione legacy)

**Lista Lingue (linea 226-229):**
```java
String[] validCodes = {"en", "it", "es", "de", "fr", "pt", "nl", "pl", "ja", "zh", 
                      "ko", "id", "hi", "th", "ms", "vi", "fil", "tr", "ar", "he", 
                      "fa", "sw", "zu", "ru", "uk", "cs", "sk", "hu", "ro", "bg", 
                      "hr", "sr", "sl", "et", "lv", "lt", "fi", "sv", "da", "no", "is"};
```

**Numero Lingue:** 40

**Uso nel Backend:**
- ✅ **Usato per normalizzazione** (linea 213-240)
- ✅ **Metodo privato** `normalizeLanguage()` (linea 213)
- ⚠️ **Solo per validazione ISO 639-1** - non usato per validazione `User.language`
- ⚠️ **Lista più completa** (40 lingue vs 9/25/28)

**Contiene Lista Lingue:** ✅ SÌ  
**È "Fonte della Verità"?** ❌ NO (solo per normalizzazione, lista locale al metodo)

---

### **5. EmailTemplateTestService.java**

**Percorso:** `src/main/java/com/funkard/service/EmailTemplateTestService.java`

**Tipo:** Service (test template email)

**Lista Lingue (linea 38-42):**
```java
String[] languages = {
    "it", "en", "es", "de", "fr", "pt", "nl", "pl", "ja", "zh",
    "ko", "id", "hi", "th", "ms", "vi", "fil", "tr", "ar", "he",
    "fa", "sw", "zu", "es-419", "en-us", "en-gb", "fr-ca"
};
```

**Numero Lingue:** 25 (identica a `EmailTemplateManager`)

**Uso nel Backend:**
- ✅ **Usato solo per test** (linea 56)
- ⚠️ **Non usato in produzione** - solo per testing
- ⚠️ **Duplicato** di `EmailTemplateManager.SUPPORTED_LANGUAGES`

**Contiene Lista Lingue:** ✅ SÌ  
**È "Fonte della Verità"?** ❌ NO (solo per test, duplicato)

---

## 🗺️ MAPPING HARDCODED TROVATI

### **6. EmailService.java**

**Percorso:** `src/main/java/com/funkard/service/EmailService.java`

**Tipo:** Service (invio email)

**Mapping Hardcoded (linea 310-373):**

**Subject Email per Tipo (Map<String, Map<String, String>>):**

#### **A) Account Confirmation (linea 310-313):**
```java
accountConfirmation.put("en", "Verify your Funkard account");
accountConfirmation.put("it", "Verifica il tuo account Funkard");
accountConfirmation.put("es", "Verifica tu cuenta de Funkard");
accountConfirmation.put("de", "Bestätige dein Funkard-Konto");
```

**Lingue:** `en, it, es, de` (4 lingue)

#### **B) Password Reset (linea 320-325):**
```java
passwordReset.put("en", "Reset your Funkard password");
passwordReset.put("it", "Reset password Funkard");
passwordReset.put("es", "Restablecer contraseña de Funkard");
passwordReset.put("de", "Funkard-Passwort zurücksetzen");
passwordReset.put("fr", "Réinitialiser votre mot de passe Funkard");
passwordReset.put("pt", "Redefinir senha Funkard");
```

**Lingue:** `en, it, es, de, fr, pt` (6 lingue)

#### **C) Order Confirmation (linea 330-333):**
```java
orderConfirmation.put("en", "Order Confirmation - Funkard");
orderConfirmation.put("it", "Conferma ordine - Funkard");
orderConfirmation.put("es", "Confirmación de pedido - Funkard");
orderConfirmation.put("de", "Bestellbestätigung - Funkard");
```

**Lingue:** `en, it, es, de` (4 lingue)

#### **D) Order Shipped (linea 340-345):**
```java
orderShipped.put("en", "Your Funkard order has been shipped");
orderShipped.put("it", "Il tuo ordine Funkard è stato spedito");
orderShipped.put("es", "Tu pedido de Funkard ha sido enviado");
orderShipped.put("de", "Deine Funkard-Bestellung wurde versendet");
orderShipped.put("pt", "Seu pedido Funkard foi enviado");
```

**Lingue:** `en, it, es, de, pt` (5 lingue)

#### **E) Account Deletion (linea 350-354):**
```java
accountDeletion.put("en", "Funkard — Account deletion completed");
accountDeletion.put("it", "Funkard — Cancellazione account completata");
accountDeletion.put("es", "Funkard — Eliminación de cuenta completada");
accountDeletion.put("de", "Funkard — Kontolöschung abgeschlossen");
accountDeletion.put("fr", "Funkard — Suppression de compte terminée");
```

**Lingue:** `en, it, es, de, fr` (5 lingue)

#### **F) Ticket Opened (linea 360-362):**
```java
ticketOpened.put("en", "Support Ticket Opened - Funkard");
ticketOpened.put("it", "Ticket supporto aperto - Funkard");
ticketOpened.put("es", "Ticket de soporte abierto - Funkard");
```

**Lingue:** `en, it, es` (3 lingue)

#### **G) Default Subject (linea 370-373):**
```java
defaultSubject.put("en", "Notification from Funkard");
defaultSubject.put("it", "Notifica da Funkard");
defaultSubject.put("es", "Notificación de Funkard");
defaultSubject.put("de", "Benachrichtigung von Funkard");
```

**Lingue:** `en, it, es, de` (4 lingue)

**Uso nel Backend:**
- ✅ **Usato per subject email** (linea 293-310)
- ⚠️ **Mapping hardcoded** - non centralizzato
- ⚠️ **Lingue diverse per tipo email** (3-6 lingue per tipo)
- ⚠️ **Non completo** - non tutte le lingue per tutti i tipi

**Contiene Mapping Lingue:** ✅ SÌ  
**È "Fonte della Verità"?** ❌ NO (solo per subject email, incompleto)

---

## 🔧 HELPER E NORMALIZZAZIONE

### **7. EmailLocaleHelper.java**

**Percorso:** `src/main/java/com/funkard/service/EmailLocaleHelper.java`

**Tipo:** Component (helper locale)

**Contenuto:**
- ✅ **Metodo `getUserLocale(User user)`** - converte `user.language` a `Locale`
- ✅ **Metodo `parseLocale(String localeStr)`** - parse stringa a Locale
- ✅ **Metodo `getUserLocaleString(User user)`** - estrae stringa locale

**Lista Lingue:** ❌ NO (non contiene lista lingue)

**Uso nel Backend:**
- ✅ **Usato da `EmailService`** per recuperare locale utente
- ✅ **Supporta varianti regionali** (es-419, en-US, ecc.)
- ⚠️ **Non valida** contro whitelist - accetta qualsiasi stringa

**Contiene Lista Lingue:** ❌ NO  
**È "Fonte della Verità"?** ❌ NO (solo helper, nessuna lista)

---

### **8. UnifiedTranslationService.java**

**Percorso:** `src/main/java/com/funkard/service/UnifiedTranslationService.java`

**Tipo:** Service (traduzione unificata GPT+DeepL)

**Contenuto:**
- ✅ **Metodo `normalizeLanguage(String lang)`** (linea 92-110)
- ❌ **Nessuna whitelist** - solo normalizzazione
- ❌ **Nessuna lista lingue** - estrae solo codice principale

**Lista Lingue:** ❌ NO

**Uso nel Backend:**
- ✅ **Usato per normalizzazione** prima di chiamare GPT/DeepL
- ⚠️ **Non valida** contro whitelist - accetta qualsiasi codice

**Contiene Lista Lingue:** ❌ NO  
**È "Fonte della Verità"?** ❌ NO (solo normalizzazione, nessuna lista)

---

## 📊 CONFRONTO LISTE TROVATE

| File | Tipo | Lingue | Formato | Uso | Validazione |
|------|------|--------|---------|-----|-------------|
| **TranslateController** | Controller | 9 | `List<String>` lowercase | Endpoint traduzione | ✅ Whitelist rigida |
| **EmailTemplateManager** | Service | 25 | `String[]` lowercase + varianti | Template email | ⚠️ Solo per template |
| **DeepLTranslateService** | Service | 28 | `String[]` UPPERCASE | Normalizzazione DeepL | ⚠️ Solo per DeepL |
| **TranslationService** | Service | 40 | `String[]` lowercase | Normalizzazione ISO 639-1 | ⚠️ Solo per normalizzazione |
| **EmailTemplateTestService** | Service | 25 | `String[]` lowercase + varianti | Test template | ❌ Solo test |
| **EmailService** | Service | 3-6 per tipo | `Map<String, String>` | Subject email | ❌ Mapping hardcoded |

---

## 🔍 FILE NON TROVATI

### **❌ Classi/File NON Esistenti:**

1. ❌ `LanguageConfig.java` - NON esiste
2. ❌ `LanguageService.java` - NON esiste
3. ❌ `LanguageHelper.java` - NON esiste (esiste solo `EmailLocaleHelper`)
4. ❌ `LanguageUtil.java` - NON esiste
5. ❌ `LanguageConstants.java` - NON esiste
6. ❌ `Language.java` (enum) - NON esiste
7. ❌ `SupportedLanguages.java` - NON esiste
8. ❌ File dedicato in `config/` - NON esiste

### **❌ Pattern NON Trovati:**

- ❌ Nessuna classe con `public static final` lista lingue centralizzata
- ❌ Nessun file `*Language*.java` (tranne `EmailLocaleHelper`)
- ❌ Nessun file `*Locale*.java` (tranne `EmailLocaleHelper`)
- ❌ Nessun file `*i18n*.java`
- ❌ Nessun mapping centralizzato GPT
- ❌ Nessun mapping centralizzato DeepL

---

## 🎯 ANALISI "FONTE DELLA VERITÀ"

### **Candidati Potenziali:**

#### **1. EmailTemplateManager.SUPPORTED_LANGUAGES**
**Pro:**
- ✅ Lista più completa (25 lingue)
- ✅ Supporta varianti regionali
- ✅ Costante `DEFAULT_LANGUAGE` presente

**Contro:**
- ❌ Solo per template email
- ❌ Non usato per validazione `User.language`
- ❌ Non usato da altri servizi

**Verdetto:** ❌ NON è "fonte della verità"

#### **2. TranslationService.validCodes**
**Pro:**
- ✅ Lista più completa (40 lingue)
- ✅ Usato per validazione ISO 639-1

**Contro:**
- ❌ Lista locale al metodo (non costante di classe)
- ❌ Non usato per validazione `User.language`
- ❌ Non accessibile da altri servizi

**Verdetto:** ❌ NON è "fonte della verità"

#### **3. TranslateController.SUPPORTED_LANGUAGES**
**Pro:**
- ✅ Whitelist rigida usata per validazione
- ✅ Costante di classe

**Contro:**
- ❌ Solo 9 lingue (troppo poche)
- ❌ Solo per endpoint traduzione
- ❌ Non usato per validazione `User.language`

**Verdetto:** ❌ NON è "fonte della verità"

---

## 📝 CONCLUSIONI

### **Stato Attuale:**

1. ❌ **Nessun file centralizzato** per gestione lingue
2. ❌ **Nessuna "fonte della verità"** unica
3. ✅ **5 liste diverse** sparse in vari servizi
4. ✅ **Mapping hardcoded** in `EmailService` per subject email
5. ⚠️ **Disallineamento totale** - ogni servizio ha la sua lista

### **File Più Rilevanti:**

| File | Rilevanza | Motivo |
|------|-----------|--------|
| `EmailTemplateManager.java` | 🟡 Media | Lista più completa (25 lingue) |
| `TranslationService.java` | 🟡 Media | Lista più completa (40 lingue) ma locale al metodo |
| `TranslateController.java` | 🟢 Bassa | Solo 9 lingue, solo per endpoint |
| `DeepLTranslateService.java` | 🟢 Bassa | Formato UPPERCASE, solo per DeepL |
| `EmailService.java` | 🟢 Bassa | Mapping hardcoded, incompleto |

### **Raccomandazione:**

**Nessuno dei file trovati può essere considerato "fonte della verità"** perché:
- Sono tutti specifici per un singolo scopo (traduzione, template, DeepL)
- Nessuno è usato per validazione `User.language`
- Nessuno è accessibile centralmente da altri servizi
- Liste diverse con numeri diversi di lingue

**Serve creare una classe centralizzata** (es. `LanguageConfig.java`) che:
- Contenga lista unificata di 31 lingue
- Sia accessibile da tutti i servizi
- Sia usata per validazione `User.language`
- Sia usata da tutti i servizi di traduzione

---

**Fine Analisi - Nessuna Modifica Eseguita**

