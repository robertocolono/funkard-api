# 📧 Email Service con Fallback Automatico

**Data Implementazione:** 2025-01-15  
**Versione:** 2.0  
**Affidabilità:** Alta (fallback automatico)

---

## 📋 Panoramica

Sistema di invio email con fallback automatico su account secondario in caso di errore SMTP. Garantisce alta affidabilità e tracciabilità completa tramite logging dettagliato.

---

## ✅ Componenti Implementati

### **1. EmailConfig**
- Configurazione due `JavaMailSender`:
  - **Primary:** `no-reply@funkard.com` (principale)
  - **Fallback:** `support@funkard.com` (backup)
- Configurazione dinamica da variabili ambiente
- Supporto provider multipli (Register, Gmail, SendGrid, ecc.)

### **2. EmailService Aggiornato**

#### **Metodo Generico: `sendEmail()`**
- Tentativo con mittente principale
- Fallback automatico su support@funkard.com in caso di errore
- Logging dettagliato con timestamp
- Return boolean per successo/fallimento

#### **Metodo Privato: `sendUsing()`**
- Astrae logica SMTP
- Gestione UTF-8
- Supporto HTML e testo

### **3. Metodi Esistenti Aggiornati**
- `sendVerificationEmail()` - Usa nuovo sistema
- `sendSimple()` - Usa nuovo sistema
- `sendAccountDeletionCompletedEmail()` - Usa nuovo sistema

---

## 🔄 Flusso Fallback

```
1. Tentativo invio con no-reply@funkard.com
   ↓
2. Se successo → Log ✅ e return true
   ↓
3. Se errore → Log ⚠️ e tentativo fallback
   ↓
4. Tentativo invio con support@funkard.com
   ↓
5. Se successo → Log ✅ (fallback) e return true
   ↓
6. Se errore → Log ❌ e alert interno
   ↓
7. Return false
```

---

## ⚙️ Configurazione

### **Variabili Ambiente Richieste:**

#### **Primary (no-reply@funkard.com):**
```bash
MAIL_HOST=smtp.register.it
MAIL_PORT=587
MAIL_USERNAME=no-reply@funkard.com
MAIL_PASSWORD=********
MAIL_FROM=no-reply@funkard.com
MAIL_FROM_NAME=Funkard
```

#### **Fallback (support@funkard.com):**
```bash
MAIL_FALLBACK=support@funkard.com
MAIL_FALLBACK_HOST=smtp.register.it  # Opzionale, default = MAIL_HOST
MAIL_FALLBACK_PORT=587               # Opzionale, default = MAIL_PORT
MAIL_FALLBACK_USERNAME=support@funkard.com
MAIL_FALLBACK_PASSWORD=********
```

#### **Alert Interno:**
```bash
ADMIN_EMAIL=legal@funkard.com  # Per alert critici
```

### **application.properties:**
```properties
# === 📧 Email Configuration (Primary: no-reply@funkard.com)
MAIL_HOST=${MAIL_HOST:smtp.register.it}
MAIL_PORT=${MAIL_PORT:587}
MAIL_USERNAME=${MAIL_USERNAME:no-reply@funkard.com}
MAIL_PASSWORD=${MAIL_PASSWORD:}
MAIL_FROM=${MAIL_FROM:no-reply@funkard.com}
MAIL_FROM_NAME=${MAIL_FROM_NAME:Funkard}

# === 📧 Email Configuration (Fallback: support@funkard.com)
MAIL_FALLBACK=${MAIL_FALLBACK:support@funkard.com}
MAIL_FALLBACK_HOST=${MAIL_FALLBACK_HOST:${MAIL_HOST:smtp.register.it}}
MAIL_FALLBACK_PORT=${MAIL_FALLBACK_PORT:${MAIL_PORT:587}}
MAIL_FALLBACK_USERNAME=${MAIL_FALLBACK_USERNAME:support@funkard.com}
MAIL_FALLBACK_PASSWORD=${MAIL_FALLBACK_PASSWORD:}
```

---

## 📊 Logging

### **Successo Primary:**
```
INFO: ✅ [2025-01-15 10:30:00] Email inviata da no-reply@funkard.com a user@example.com - Oggetto: Verifica account
```

### **Fallback Attivato:**
```
WARN: ⚠️ [2025-01-15 10:30:00] Errore invio da no-reply@funkard.com a user@example.com: Connection timeout - Tentativo con fallback...
INFO: ✅ [2025-01-15 10:30:01] Email inviata da support@funkard.com (fallback) a user@example.com - Oggetto: Verifica account
```

### **Fallimento Completo:**
```
WARN: ⚠️ [2025-01-15 10:30:00] Errore invio da no-reply@funkard.com a user@example.com: Connection timeout - Tentativo con fallback...
ERROR: ❌ [2025-01-15 10:30:01] Errore invio anche da fallback support@funkard.com a user@example.com: Connection timeout
ERROR: 🚨 [2025-01-15 10:30:01] ALERT: Invio email fallito per destinatario user@example.com - Controllare configurazione SMTP
```

---

## 🔧 Utilizzo

### **Metodo Generico:**
```java
@Autowired
private EmailService emailService;

// Invio email HTML
boolean sent = emailService.sendEmail(
    "user@example.com",
    "Oggetto email",
    "<h1>Corpo HTML</h1>",
    true
);

// Invio email testo
boolean sent = emailService.sendEmail(
    "user@example.com",
    "Oggetto email",
    "Corpo testo",
    false
);
```

### **Metodi Specifici:**
```java
// Email verifica account
emailService.sendVerificationEmail("user@example.com", "token123");

// Email semplice
emailService.sendSimple("user@example.com", "Oggetto", "Corpo");

// Email cancellazione account
emailService.sendAccountDeletionCompletedEmail("user@example.com", "IT", "Mario");
```

---

## 🚀 Provider Supportati

### **Register.it (Attuale):**
```bash
MAIL_HOST=smtp.register.it
MAIL_PORT=587
```

### **Gmail (Alternativa):**
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
```

### **SendGrid (Futuro):**
```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
```

**Nota:** Il sistema è provider-agnostic, tutte le credenziali vengono lette da env.

---

## ✅ Vantaggi

1. **Alta Affidabilità:** Fallback automatico garantisce invio anche in caso di problemi SMTP
2. **Tracciabilità:** Logging dettagliato con timestamp per audit
3. **Flessibilità:** Supporto provider multipli senza hardcoding
4. **Sicurezza:** Credenziali sempre da env, mai nel codice
5. **Professionalità:** Email sempre inviate, anche da account fallback

---

## 🔒 Sicurezza

- ✅ Credenziali sempre da variabili ambiente
- ✅ Nessuna password hardcoded
- ✅ Supporto TLS/STARTTLS
- ✅ Logging senza dati sensibili
- ✅ Alert interno per errori critici

---

## 📝 Checklist Implementazione

### **Backend:**
- [x] EmailConfig creato con due JavaMailSender
- [x] EmailService aggiornato con metodo generico sendEmail()
- [x] Metodo privato sendUsing() per astrazione SMTP
- [x] Fallback automatico implementato
- [x] Logging dettagliato con timestamp
- [x] Alert interno per errori critici
- [x] Configurazione env completa
- [x] Metodi esistenti aggiornati

### **Configurazione:**
- [x] application.properties aggiornato
- [x] application-prod.yml aggiornato
- [x] Variabili ambiente documentate
- [x] Supporto provider multipli

---

## 🧪 Test

### **Test Unitario (Mock):**
```java
@Test
void testSendEmailWithFallback() {
    // Mock primaryMailSender per fallire
    // Mock fallbackMailSender per successo
    // Verifica chiamata fallback
    // Verifica logging
}
```

### **Test Integrazione:**
```java
@Test
void testRealEmailWithFallback() {
    // Test con account reali Register
    // Simula errore primary
    // Verifica invio fallback
}
```

---

## 📝 Note Importanti

1. **Fallback Trasparente:** L'utente riceve l'email anche se inviata da support@funkard.com
2. **Logging Completo:** Ogni tentativo viene loggato per audit
3. **Alert Interno:** Se entrambi falliscono, viene generato alert a legal@funkard.com
4. **Provider Agnostic:** Funziona con qualsiasi provider SMTP
5. **Future-Proof:** Facile aggiungere provider aggiuntivi

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 2.0

