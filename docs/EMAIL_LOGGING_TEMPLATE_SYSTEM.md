# 📧 Sistema Email Logging + Template Manager

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0  
**Conformità:** Audit, Debugging, Trasparenza

---

## 📋 Panoramica

Sistema completo di logging automatico delle email inviate e template manager modulare per gestire tutti i tipi di email (operative, transazionali, informative). Ogni email è tracciata, indicizzata e consultabile tramite API admin.

---

## ✅ Componenti Implementati

### **1. Database: Tabella `email_logs`**

**Campi:**
- `id` (UUID) - Identificativo univoco
- `recipient` (VARCHAR) - Email destinatario
- `sender` (VARCHAR) - Mittente usato
- `subject` (VARCHAR) - Oggetto email
- `type` (VARCHAR) - Tipo email (es. ACCOUNT_CONFIRMATION)
- `status` (ENUM) - SENT, FAILED, RETRIED
- `error_message` (TEXT) - Dettaglio errore
- `sent_at` (TIMESTAMP) - Data/ora invio
- `retry_count` (INT) - Numero tentativi
- `locale` (VARCHAR) - Lingua (it, en)
- `template_name` (VARCHAR) - Nome template usato
- `webhook_id` (VARCHAR) - ID webhook (futuro)

**Indici:**
- `recipient` - Query rapide per destinatario
- `type` - Filtro per tipo email
- `status` - Filtro per stato
- `sent_at` - Ordinamento temporale
- `(recipient, type)` - Query composite

### **2. EmailTemplateManager**

**Funzionalità:**
- Carica template da `/resources/email-templates/`
- Supporto multi-lingua (`.html` e `.txt`)
- Sostituzione variabili `${variable}`
- Fallback automatico se template non trovato

**Metodi:**
- `renderTemplate()` - Renderizza template con variabili
- `renderHtmlTemplate()` - Template HTML
- `renderTextTemplate()` - Template testo

### **3. EmailLogService**

**Funzionalità:**
- Registra log email inviate
- Registra log email fallite
- Aggiorna log con retry

**Metodi:**
- `logEmailSent()` - Registra email inviata
- `logEmailFailed()` - Registra email fallita
- `updateLogWithRetry()` - Aggiorna con retry
- `findById()` - Trova log per ID

### **4. EmailService Aggiornato**

**Nuove Funzionalità:**
- Integrazione logging automatico
- Supporto template manager
- Metodo `sendTemplatedEmail()` per email con template

**Metodo Principale:**
```java
public UUID sendEmail(String to, String subject, String bodyHtml, boolean isHtml,
                     String emailType, String locale, String templateName)
```

### **5. AdminEmailLogController**

**Endpoint:**
- `GET /api/admin/email-logs` - Elenco paginato con filtri
- `GET /api/admin/email-logs/{id}` - Dettaglio singolo log
- `GET /api/admin/email-logs/stats` - Statistiche

**Filtri Supportati:**
- `recipient` - Email destinatario
- `type` - Tipo email
- `status` - Stato (SENT, FAILED, RETRIED)
- `fromDate` - Data inizio (ISO format)
- `toDate` - Data fine (ISO format)
- `page` - Numero pagina (default 0)
- `size` - Dimensione pagina (default 20)

### **6. EmailLogCleanupScheduler**

**Funzionalità:**
- Job schedulato ogni giorno alle 3:00
- Rimuove log più vecchi di 90 giorni
- Logging completo operazioni

---

## 🔄 Flusso Completo

### **Invio Email con Template:**

```
1. EmailService.sendTemplatedEmail()
   ↓
2. EmailTemplateManager.renderHtmlTemplate()
   → Carica template da /resources/email-templates/
   → Sostituisce variabili ${variable}
   ↓
3. EmailService.sendEmail()
   → Tentativo invio con primary sender
   → Fallback su support@funkard.com se errore
   ↓
4. EmailLogService.logEmailSent() o logEmailFailed()
   → Registra log in database
   → Return UUID log
```

### **Audit Email:**

```
1. Admin chiama GET /api/admin/email-logs
   ↓
2. EmailLogRepository.findWithFilters()
   → Applica filtri (recipient, type, status, date)
   → Paginazione
   ↓
3. Restituisce Page<EmailLog>
```

---

## 📧 Template Email

### **Struttura Directory:**

```
src/main/resources/email-templates/
  ├── account_confirmation.html
  ├── account_confirmation.txt
  ├── account_confirmation_it.html
  ├── account_confirmation_en.html
  ├── password_reset.html
  ├── order_shipped.html
  └── ...
```

### **Formato Template:**

**HTML:**
```html
<!DOCTYPE html>
<html>
<body>
    <h1>Ciao ${userName}!</h1>
    <p>Data: ${date}</p>
    <a href="${verifyUrl}">Verifica</a>
</body>
</html>
```

**Variabili:**
- `${userName}` - Nome utente
- `${date}` - Data formattata
- `${verifyUrl}` - URL verifica
- `${token}` - Token (se necessario)

### **Template Disponibili:**

1. **account_confirmation** - Verifica account
2. **account_deletion** - Cancellazione account (futuro)
3. **password_reset** - Reset password (futuro)
4. **order_shipped** - Ordine spedito (futuro)

---

## 🌐 API Admin

### **GET /api/admin/email-logs**

**Query Params:**
```
?recipient=user@example.com
&type=ACCOUNT_CONFIRMATION
&status=SENT
&fromDate=2025-01-01T00:00:00
&toDate=2025-01-31T23:59:59
&page=0
&size=20
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "recipient": "user@example.com",
      "sender": "no-reply@funkard.com",
      "subject": "Verifica account",
      "type": "ACCOUNT_CONFIRMATION",
      "status": "SENT",
      "sentAt": "2025-01-15T10:30:00",
      "locale": "it",
      "templateName": "account_confirmation"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

### **GET /api/admin/email-logs/{id}**

**Response:**
```json
{
  "log": {
    "id": "uuid",
    "recipient": "user@example.com",
    "sender": "no-reply@funkard.com",
    "subject": "Verifica account",
    "type": "ACCOUNT_CONFIRMATION",
    "status": "SENT",
    "sentAt": "2025-01-15T10:30:00",
    "locale": "it",
    "templateName": "account_confirmation"
  },
  "renderedContent": "<html>...</html>",
  "templateName": "account_confirmation"
}
```

### **GET /api/admin/email-logs/stats**

**Response:**
```json
{
  "total": 1000,
  "sent": 950,
  "failed": 30,
  "retried": 20,
  "period": {
    "from": "2025-01-01T00:00:00",
    "to": "2025-01-31T23:59:59"
  }
}
```

---

## 🔒 Sicurezza e Privacy

### **Dati Salvati:**
- ✅ Metadati email (destinatario, oggetto, tipo)
- ✅ Template usato (non contenuto completo)
- ✅ Stato invio e errori
- ✅ Timestamp e locale

### **Dati NON Salvati:**
- ❌ Corpo completo email (solo template name)
- ❌ Password o token sensibili
- ❌ Dati personali completi

### **Conservazione:**
- ✅ Log conservati 90 giorni
- ✅ Cleanup automatico giornaliero
- ✅ Audit trail completo

---

## 🧹 Cleanup Automatico

### **Scheduler:**
- **Cron:** `0 0 3 * * *` (ogni giorno alle 3:00)
- **Retention:** 90 giorni
- **Logging:** Completo per audit

### **Log Output:**
```
INFO: 🧹 [CLEANUP] Inizio pulizia log email vecchi...
INFO: 🧹 [CLEANUP] Puliti 150 log email più vecchi di 90 giorni (cutoff: 2024-10-15T03:00:00)
```

---

## 📊 Tipi Email Supportati

### **Enum EmailType:**
- `ACCOUNT_CONFIRMATION` - Verifica account
- `PASSWORD_RESET` - Reset password
- `ORDER_SHIPPED` - Ordine spedito
- `ACCOUNT_DELETION` - Cancellazione account
- `GENERIC` - Generico

### **Stati Email:**
- `SENT` - Inviata con successo
- `FAILED` - Fallita
- `RETRIED` - Ritentata dopo errore

---

## ✅ Checklist Implementazione

### **Backend:**
- [x] Migration V11__create_email_logs_table.sql
- [x] Modello EmailLog
- [x] Repository EmailLogRepository
- [x] Service EmailLogService
- [x] EmailTemplateManager
- [x] EmailService aggiornato con logging
- [x] AdminEmailLogController
- [x] EmailLogCleanupScheduler
- [x] Template account_confirmation (HTML + TXT)

### **Sicurezza:**
- [x] Accesso solo SUPER_ADMIN e SUPERVISOR
- [x] Conservazione 90 giorni
- [x] Cleanup automatico
- [x] Nessun dato sensibile salvato

---

## 🚀 Utilizzo

### **Invio Email con Template:**
```java
@Autowired
private EmailService emailService;

Map<String, Object> variables = new HashMap<>();
variables.put("userName", "Mario");
variables.put("verifyUrl", "https://funkard.com/verify?token=123");

emailService.sendTemplatedEmail(
    "user@example.com",
    "ACCOUNT_CONFIRMATION",
    variables,
    Locale.ITALIAN
);
```

### **Query Log Email:**
```bash
# Tutte le email
GET /api/admin/email-logs

# Email per destinatario
GET /api/admin/email-logs?recipient=user@example.com

# Email fallite
GET /api/admin/email-logs?status=FAILED

# Email per tipo
GET /api/admin/email-logs?type=ACCOUNT_CONFIRMATION

# Range date
GET /api/admin/email-logs?fromDate=2025-01-01T00:00:00&toDate=2025-01-31T23:59:59
```

---

## 📝 Note Importanti

1. **Template Multi-lingua:** Supporto `_it` e `_en` suffix
2. **Fallback:** Se template non trovato, genera template fallback
3. **Logging Automatico:** Ogni email inviata viene loggata
4. **Retry:** Supporto retry con aggiornamento log
5. **Future-Proof:** Campo `webhook_id` per provider esterni

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

