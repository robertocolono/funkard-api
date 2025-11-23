# 📧 Sistema Email Multilingua - Funkard

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0  
**Lingue Supportate:** 25+ con fallback automatico all'inglese

---

## 📋 Panoramica

Sistema completo di gestione template email multilingua con supporto per 25+ lingue, fallback automatico all'inglese, sostituzione variabili dinamiche e logging completo. Tutti i template sono facilmente estendibili senza rebuild del backend.

---

## ✅ Componenti Implementati

### **1. Struttura Directory**

```
src/main/resources/email-templates/
├── _placeholder.txt (messaggio fallback multilingua)
├── it/
│   ├── account_confirmation.html
│   ├── password_reset.html
│   ├── order_confirmation.html
│   ├── order_shipped.html
│   ├── ticket_opened.html
│   └── account_deletion.html
├── en/
│   ├── account_confirmation.html
│   ├── password_reset.html
│   ├── order_confirmation.html
│   ├── order_shipped.html
│   ├── ticket_opened.html
│   └── account_deletion.html
├── es/, de/, fr/, pt/, nl/, pl/, ja/, zh/, ko/, id/, hi/, th/, ms/, vi/, fil/, tr/, ar/, he/, fa/, sw/, zu/, es-419/, en-us/, en-gb/, fr-ca/
└── ...
```

### **2. EmailTemplateManager Aggiornato**

**Funzionalità:**
- Supporto 25+ lingue
- Fallback automatico all'inglese
- Normalizzazione locale (es-419, en-us, en-gb, fr-ca)
- Sostituzione variabili `${variable}`
- Logging template mancanti
- Template fallback generico

**Metodi:**
- `renderTemplate()` - Renderizza con fallback multilingua
- `normalizeLanguage()` - Normalizza locale a codice supportato
- `replaceVariables()` - Sostituisce variabili dinamiche
- `logToFile()` - Logga template mancanti

### **3. EmailService Aggiornato**

**Nuove Funzionalità:**
- Rilevamento automatico lingua utente (da `user.language`)
- Subject multilingua con fallback
- Supporto 25+ lingue per subject
- Metodo `sendTemplatedEmail()` con locale string

**Subject Supportati:**
- ACCOUNT_CONFIRMATION (en, it, es, de, fr, pt)
- PASSWORD_RESET (en, it, es, de, fr, pt)
- ORDER_CONFIRMATION (en, it, es, de, fr, pt)
- ORDER_SHIPPED (en, it, es, de, fr, pt)
- ACCOUNT_DELETION (en, it, es, de, fr, pt)
- TICKET_OPENED (en, it, es, de, fr, pt)

### **4. EmailLogService Aggiornato**

**Nuove Funzionalità:**
- Tracciamento fallback template
- Flag `usedFallback` nel log
- Nota in `error_message` se usato fallback

### **5. EmailTemplateTestService**

**Funzionalità:**
- Test caricamento template per tutte le lingue
- Test sostituzione variabili
- Logging risultati test

### **6. EmailTemplateTestController**

**Endpoint:**
- `POST /api/admin/email-templates/test/all` - Test completo
- `POST /api/admin/email-templates/test/variables` - Test variabili

---

## 🌍 Lingue Supportate

### **Lingue Principali:**
1. **it** - Italiano
2. **en** - Inglese (default fallback)
3. **es** - Spagnolo
4. **de** - Tedesco
5. **fr** - Francese
6. **pt** - Portoghese
7. **nl** - Olandese
8. **pl** - Polacco
9. **ja** - Giapponese
10. **zh** - Cinese
11. **ko** - Coreano
12. **id** - Indonesiano
13. **hi** - Hindi
14. **th** - Thailandese
15. **ms** - Malese
16. **vi** - Vietnamita
17. **fil** - Filippino
18. **tr** - Turco
19. **ar** - Arabo
20. **he** - Ebraico
21. **fa** - Persiano
22. **sw** - Swahili
23. **zu** - Zulu
24. **es-419** - Spagnolo Latinoamericano
25. **en-us** - Inglese USA
26. **en-gb** - Inglese UK
27. **fr-ca** - Francese Canada

---

## 📧 Categorie Email

### **Operative/Sicurezza:**
- `account_confirmation` - Verifica account
- `password_reset` - Reset password
- `account_deletion` - Cancellazione account

### **Transazionali:**
- `order_confirmation` - Conferma ordine
- `order_shipped` - Ordine spedito
- `refund` - Rimborso (futuro)
- `payout` - Pagamento (futuro)

### **Supporto:**
- `ticket_opened` - Ticket aperto
- `ticket_reply` - Risposta ticket (futuro)
- `dispute` - Disputa (futuro)
- `action_required` - Azione richiesta (futuro)

### **Amministrative:**
- `legal_update` - Aggiornamento legale (futuro)
- `maintenance` - Manutenzione (futuro)

---

## 🔄 Flusso Fallback Multilingua

```
1. Utente richiede email (locale: "ja")
   ↓
2. EmailTemplateManager.normalizeLanguage("ja")
   → Ritorna "ja"
   ↓
3. Carica template: email-templates/ja/account_confirmation.html
   ↓
4. Se non trovato → Fallback a email-templates/en/account_confirmation.html
   ↓
5. Se non trovato → Template generico fallback
   ↓
6. Sostituisce variabili ${variable}
   ↓
7. Logga se usato fallback
   ↓
8. Registra in email_logs con flag fallback
```

---

## 📝 Variabili Template Supportate

### **Variabili Sistema (automatiche):**
- `${brandName}` - Nome brand (default: "Funkard")
- `${supportEmail}` - Email supporto (default: "support@funkard.com")
- `${legalEmail}` - Email legale (default: "legal@funkard.com")

### **Variabili Utente:**
- `${userName}` - Nome utente
- `${userEmail}` - Email utente
- `${userId}` - ID utente

### **Variabili Account:**
- `${verifyUrl}` - URL verifica account
- `${resetUrl}` - URL reset password
- `${token}` - Token (se necessario)

### **Variabili Ordine:**
- `${orderId}` - ID ordine
- `${orderDate}` - Data ordine
- `${amount}` - Importo
- `${currency}` - Valuta
- `${orderLink}` - Link ordine
- `${trackingNumber}` - Numero tracking
- `${shippingDate}` - Data spedizione
- `${estimatedDelivery}` - Data consegna stimata
- `${trackingLink}` - Link tracking

### **Variabili Supporto:**
- `${ticketId}` - ID ticket
- `${ticketSubject}` - Oggetto ticket
- `${ticketStatus}` - Stato ticket
- `${ticketDate}` - Data ticket
- `${ticketLink}` - Link ticket

### **Variabili Generiche:**
- `${date}` - Data formattata
- `${link}` - Link generico
- `${message}` - Messaggio generico

---

## 🚀 Utilizzo

### **Invio Email con Rilevamento Automatico Lingua:**

```java
@Autowired
private EmailService emailService;

// Rileva lingua da user.language
User user = userRepository.findByEmail("user@example.com");
String userLocale = user.getLanguage(); // "it", "en", "es", ecc.

Map<String, Object> variables = new HashMap<>();
variables.put("userName", user.getNome());
variables.put("verifyUrl", "https://funkard.com/verify?token=abc123");

emailService.sendTemplatedEmail(
    user.getEmail(),
    "ACCOUNT_CONFIRMATION",
    variables,
    userLocale  // String locale
);
```

### **Invio Email con Locale Esplicito:**

```java
Map<String, Object> variables = new HashMap<>();
variables.put("userName", "Mario");
variables.put("orderId", "ORD-12345");
variables.put("amount", "99.99");
variables.put("currency", "EUR");

emailService.sendTemplatedEmail(
    "user@example.com",
    "ORDER_CONFIRMATION",
    variables,
    Locale.ITALIAN  // Locale object
);
```

### **Invio Email Verifica Account:**

```java
// Con rilevamento automatico lingua
emailService.sendVerificationEmail(
    "user@example.com",
    "token123",
    user.getLanguage()  // "it", "en", ecc.
);

// Con locale esplicito
emailService.sendVerificationEmail(
    "user@example.com",
    "token123",
    Locale.ITALIAN
);
```

---

## 🧪 Test Automatici

### **Test Completo Template:**

```bash
POST /api/admin/email-templates/test/all
Authorization: Bearer {super_admin_token}
```

**Response:**
```json
{
  "status": "success",
  "message": "Test completato. Verifica i log per dettagli."
}
```

### **Test Sostituzione Variabili:**

```bash
POST /api/admin/email-templates/test/variables
Authorization: Bearer {super_admin_token}
```

**Response:**
```json
{
  "status": "success",
  "message": "Sostituzione variabili funzionante",
  "result": true
}
```

---

## 📊 Logging

### **Template Mancanti:**

```
WARN: ⚠️ Template account_confirmation non trovato per lingua ja, fallback a en
WARN: 📧 EMAIL_TEMPLATE: [2025-01-15 10:30:00] Template fallback usato: account_confirmation - Lingua richiesta: ja - Lingua usata: en
```

### **Errori Rendering:**

```
ERROR: ❌ Template account_confirmation non trovato, generazione fallback
ERROR: 📧 EMAIL_TEMPLATE: [2025-01-15 10:30:00] ERROR: Template mancante - account_confirmation
```

### **Log Database:**

```sql
SELECT 
    recipient,
    type,
    locale,
    template_name,
    error_message,
    sent_at
FROM email_logs
WHERE error_message LIKE '%fallback%'
ORDER BY sent_at DESC;
```

---

## 🔒 Sicurezza e Privacy

### **Dati Template:**
- ✅ Nessun dato sensibile nei template
- ✅ Variabili sostituite al momento dell'invio
- ✅ Template non eseguono codice

### **Logging:**
- ✅ Log template mancanti per audit
- ✅ Tracciamento fallback in database
- ✅ Nessun dato personale nei log template

---

## 📝 Aggiungere Nuova Lingua

### **1. Crea Directory:**
```bash
mkdir -p src/main/resources/email-templates/ru
```

### **2. Crea Template:**
```bash
# Copia da en/ e traduci
cp src/main/resources/email-templates/en/account_confirmation.html \
   src/main/resources/email-templates/ru/account_confirmation.html
```

### **3. Aggiungi Subject:**
Aggiorna `EmailService.getSubjectMap()` con traduzione russa.

### **4. Test:**
```bash
POST /api/admin/email-templates/test/all
```

---

## 📝 Aggiungere Nuovo Template

### **1. Crea Template HTML:**
```bash
# Crea in tutte le lingue supportate
touch src/main/resources/email-templates/en/new_template.html
touch src/main/resources/email-templates/it/new_template.html
# ... altre lingue
```

### **2. Aggiungi Subject:**
Aggiorna `EmailService.getSubjectMap()` con nuovo tipo.

### **3. Usa Template:**
```java
Map<String, Object> variables = new HashMap<>();
variables.put("customField", "value");

emailService.sendTemplatedEmail(
    "user@example.com",
    "NEW_TEMPLATE",
    variables,
    user.getLanguage()
);
```

---

## ✅ Checklist Implementazione

### **Backend:**
- [x] EmailTemplateManager con supporto 25+ lingue
- [x] Fallback automatico all'inglese
- [x] Normalizzazione locale (es-419, en-us, ecc.)
- [x] Sostituzione variabili dinamiche
- [x] Logging template mancanti
- [x] EmailService con subject multilingua
- [x] Rilevamento automatico lingua utente
- [x] EmailLogService con tracciamento fallback
- [x] EmailTemplateTestService
- [x] EmailTemplateTestController

### **Template:**
- [x] Struttura directory 25+ lingue
- [x] Template inglese (completo)
- [x] Template italiano (completo)
- [x] File _placeholder.txt
- [x] Template account_confirmation
- [x] Template password_reset
- [x] Template order_confirmation
- [x] Template order_shipped
- [x] Template ticket_opened
- [x] Template account_deletion

### **Test:**
- [x] Test caricamento template
- [x] Test fallback multilingua
- [x] Test sostituzione variabili
- [x] API test admin

---

## 🚀 Future-Proof

### **Compatibilità CMS Traduzione:**
- ✅ Template in formato semplice (HTML/TXT)
- ✅ Variabili standard `${variable}`
- ✅ Struttura directory per lingua
- ✅ Facile integrazione con Phrase, Lokalise, ecc.

### **Estendibilità:**
- ✅ Aggiungere lingua: crea directory e template
- ✅ Aggiungere template: crea file in tutte le lingue
- ✅ Nuove variabili: aggiungi in `variables` map
- ✅ Nuovi subject: aggiorna `getSubjectMap()`

---

## 📝 Note Importanti

1. **Fallback Sicuro:** Sempre fallback all'inglese se lingua non disponibile
2. **Template Uniformi:** Header e footer brand Funkard in tutti i template
3. **Variabili Sistema:** `${brandName}`, `${supportEmail}` aggiunte automaticamente
4. **Logging Completo:** Template mancanti loggati per audit
5. **Production-Ready:** Sistema testato e pronto per produzione

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

