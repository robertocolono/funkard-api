# 🗑️ GDPR Account Deletion - Diritto all'Oblio (Art. 17)

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0  
**Conformità:** GDPR Art. 17 (Diritto alla cancellazione)

---

## 📋 Panoramica

Sistema automatico di cancellazione account con periodo di grazia di 7 giorni, conforme al GDPR Art. 17 (Diritto all'oblio). Garantisce tracciabilità completa e rimozione sicura di tutti i dati personali.

---

## ✅ Componenti Implementati

### **1. Modello UserDeletion**
- `userId` (Long) - ID utente
- `email` (String) - Email (salvata per log)
- `requestedAt` (LocalDateTime) - Data richiesta
- `scheduledDeletionAt` (LocalDateTime) - Data programmata (+7 giorni)
- `status` (Enum) - PENDING, COMPLETED, FAILED
- `reason` (String, opzionale) - Motivo cancellazione
- `completedAt` (LocalDateTime) - Data completamento

### **2. Modello User Aggiornato**
- `deletionPending` (Boolean) - Flag account in cancellazione
- `deletionRequestedAt` (LocalDateTime) - Data richiesta

### **3. UserAccountDeletionService**
- `requestAccountDeletion()` - Registra richiesta
- `hasPendingDeletionRequest()` - Verifica richiesta pending
- `isAccountPendingDeletion()` - Verifica flag account

### **4. UserDeletionService**
- `permanentlyDeleteUser()` - Cancellazione definitiva di:
  - UserCards + file R2
  - Wishlist
  - UserAddresses
  - UserPreferences
  - CookieConsentLogs
  - Transactions
  - Listings
  - SupportTickets
  - User (ultimo)

### **5. UserDeletionScheduler**
- Job schedulato ogni ora (`@Scheduled(cron = "0 0 * * * *")`)
- Processa richieste con `scheduledDeletionAt <= now()`
- Gestione errori con retry automatico
- Logging completo

### **6. JwtFilter Aggiornato**
- Blocca accesso per utenti con `deletionPending = true`
- Restituisce 403 Forbidden

---

## 🌐 API Endpoints

### **DELETE /api/user/delete-account**

Richiedi cancellazione account.

**⚠️ RICHIEDE AUTENTICAZIONE OBBLIGATORIA (solo JWT)**

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body (opzionale):**
```json
{
  "reason": "Motivo cancellazione (opzionale)"
}
```

**Response Success (200):**
```json
{
  "message": "Richiesta di cancellazione account registrata con successo",
  "scheduledDeletionAt": "2025-01-22T10:30:00",
  "note": "Il tuo account verrà cancellato definitivamente dopo 7 giorni. Durante questo periodo non potrai accedere al sistema."
}
```

**Response Conflict (409):**
```json
{
  "error": "Richiesta di cancellazione già presente per questo account"
}
```

**Response Unauthorized (401):**
```json
{
  "error": "Autenticazione richiesta per cancellare l'account"
}
```

---

## 🔄 Flusso Completo

### **1. Richiesta Cancellazione (Utente)**
```
POST /api/user/delete-account
  → UserAccountDeletionService.requestAccountDeletion()
  → Marca user.deletionPending = true
  → Crea UserDeletion con scheduledDeletionAt = now() + 7 giorni
  → Disabilita accesso immediato (JwtFilter blocca)
```

### **2. Periodo di Grazia (7 giorni)**
```
- Utente NON può accedere (JwtFilter blocca)
- Dati ancora presenti nel database
- Possibilità di annullare (futuro)
```

### **3. Cancellazione Automatica (Scheduler)**
```
UserDeletionScheduler.processPendingDeletions() (ogni ora)
  → Trova richieste con scheduledDeletionAt <= now()
  → UserDeletionService.permanentlyDeleteUser()
    → Cancella UserCards + file R2
    → Cancella Wishlist
    → Cancella UserAddresses
    → Cancella UserPreferences
    → Cancella CookieConsentLogs
    → Cancella Transactions
    → Cancella Listings
    → Cancella SupportTickets
    → Cancella User
  → Aggiorna UserDeletion.status = COMPLETED
  → Log: "User [id/email] permanently deleted on [date]"
```

---

## 🔐 Sicurezza

### **Blocco Accesso Immediato**

Dopo la richiesta di cancellazione:
- ✅ `user.deletionPending = true`
- ✅ `JwtFilter` blocca tutti gli accessi (403 Forbidden)
- ✅ Login disabilitato
- ✅ API disabilitate

### **Cancellazione Definitiva**

Dopo 7 giorni:
- ✅ Tutti i dati rimossi dal database
- ✅ File R2 cancellati
- ✅ UserDeletion.status = COMPLETED
- ✅ Log permanente per audit

---

## 📊 Dati Cancellati

### **Database:**
1. ✅ `user_cards` - Tutte le carte utente
2. ✅ `wishlist` - Wishlist utente
3. ✅ `user_addresses` - Indirizzi utente
4. ✅ `user_preferences` - Preferenze utente
5. ✅ `cookie_consent_logs` - Log consenso cookie
6. ✅ `transactions` - Transazioni utente (come buyer)
7. ✅ `listings` - Listings utente (come seller)
8. ✅ `support_tickets` - Ticket supporto utente
9. ✅ `users` - Record utente (ultimo)

### **Storage R2:**
- ✅ Tutte le immagini UserCard (front, back, corners, edges)
- ✅ Immagini Listings (se presenti)

### **Dati Conservati (per audit):**
- ✅ `user_deletions` - Record cancellazione (userId, email, date)
- ✅ Log applicazione (userId, data cancellazione)

---

## 📋 Query Database

### **Verifica Richieste Pending**

```sql
SELECT 
    id,
    user_id,
    email,
    requested_at,
    scheduled_deletion_at,
    status,
    reason
FROM user_deletions
WHERE status = 'PENDING'
ORDER BY scheduled_deletion_at ASC;
```

### **Report Cancellazioni**

```sql
SELECT 
    status,
    COUNT(*) AS total,
    MIN(requested_at) AS first_request,
    MAX(requested_at) AS last_request
FROM user_deletions
GROUP BY status;
```

### **Utenti in Cancellazione**

```sql
SELECT 
    id,
    email,
    deletion_pending,
    deletion_requested_at
FROM users
WHERE deletion_pending = true;
```

---

## 🔒 Conformità GDPR

### **Requisiti Soddisfatti**

✅ **Art. 17 GDPR - Diritto alla cancellazione:**
- Cancellazione completa di tutti i dati personali
- Periodo di grazia di 7 giorni
- Tracciabilità richiesta e completamento

✅ **Art. 30 GDPR - Registro:**
- Log completo in `user_deletions`
- Timestamp richiesta e completamento
- Audit trail per ispezioni

✅ **Principio di Minimizzazione:**
- Solo dati necessari conservati (userId, email, date)
- Nessun dato personale dopo cancellazione

✅ **Sicurezza:**
- Accesso bloccato durante periodo di grazia
- Cancellazione non reversibile
- File storage puliti

---

## ⚙️ Configurazione Scheduler

**Cron Expression:** `0 0 * * * *`
- Eseguito ogni ora allo scoccare del minuto 0
- Timezone: `Europe/Rome`

**Modifica frequenza:**
```java
@Scheduled(cron = "0 0 * * * *")  // Ogni ora
@Scheduled(cron = "0 0 3 * * *")  // Ogni giorno alle 3:00
@Scheduled(fixedRate = 3600000)   // Ogni ora (millisecondi)
```

---

## 📝 Logging

### **Log Richiesta Cancellazione:**
```
INFO: 📝 Richiesta cancellazione account per utente: 123 (user@example.com)
INFO: ✅ Richiesta cancellazione registrata per utente: 123 - Cancellazione programmata per: 2025-01-22T10:30:00
```

### **Log Scheduler:**
```
INFO: 🗑️ [SCHEDULER] Inizio processo cancellazione account in scadenza...
INFO: 🗑️ [SCHEDULER] Trovate 2 richieste di cancellazione da processare
INFO: 🗑️ [SCHEDULER] Processando cancellazione per utente: 123 (user@example.com)
INFO: ✅ [SCHEDULER] Utente 123 (user@example.com) cancellato definitivamente il 2025-01-15T10:30:00
INFO: 🗑️ [SCHEDULER] Processo completato: 2 successi, 0 fallimenti
```

### **Log Cancellazione:**
```
INFO: 🗑️ Inizio cancellazione definitiva utente: 123 (user@example.com)
DEBUG: Trovate 5 UserCards per utente 123
DEBUG: ✅ Cancellate 5 UserCards per utente 123
DEBUG: ✅ Cancellate 3 Wishlist per utente 123
DEBUG: ✅ Cancellati 2 UserAddresses per utente 123
DEBUG: ✅ Cancellate UserPreferences per utente 123
DEBUG: ✅ Cancellati CookieConsentLogs per utente 123
DEBUG: ✅ Cancellate 1 Transactions per utente 123
DEBUG: ✅ Cancellati 0 Listings per utente 123
DEBUG: ✅ Cancellati 2 SupportTickets per utente 123
DEBUG: ✅ Cancellato User 123
INFO: ✅ Cancellazione definitiva completata per utente: 123 (user@example.com)
```

---

## ✅ Checklist Implementazione

### **Backend:**
- [x] Modello `UserDeletion` creato
- [x] Modello `User` aggiornato con flag cancellazione
- [x] Repository `UserDeletionRepository` creato
- [x] Service `UserAccountDeletionService` creato
- [x] Service `UserDeletionService` creato
- [x] Scheduler `UserDeletionScheduler` creato
- [x] Endpoint `DELETE /api/user/delete-account` creato
- [x] `JwtFilter` aggiornato per bloccare accesso
- [x] Migration `V9__create_user_deletions_table.sql`
- [x] Migration `V10__add_deletion_fields_to_users.sql`
- [x] Cancellazione file R2 implementata
- [x] Logging completo

### **Sicurezza:**
- [x] Accesso bloccato durante periodo di grazia
- [x] Validazione autenticazione obbligatoria
- [x] Prevenzione richieste duplicate
- [x] Cancellazione non reversibile

---

## 🚀 Prossimi Passi (Opzionali)

1. **Annullamento Richiesta:**
   - Endpoint per annullare richiesta durante periodo di grazia
   - Verifica identità aggiuntiva

2. **Notifica Email:**
   - Email di conferma richiesta
   - Email di conferma cancellazione (se possibile)

3. **Export Dati Prima Cancellazione:**
   - Endpoint per export completo dati (GDPR Art. 20)
   - Formato JSON/PDF

4. **Dashboard Admin:**
   - Visualizzazione richieste pending
   - Statistiche cancellazioni

---

## 📝 Note Importanti

1. **Periodo di Grazia:** 7 giorni configurabili nel codice
2. **Irreversibilità:** Cancellazione definitiva, non annullabile
3. **File R2:** Cancellazione file potrebbe fallire silenziosamente (loggato)
4. **Transazioni:** Potrebbero richiedere anonimizzazione invece di cancellazione (valutare caso d'uso)
5. **Support Tickets:** Cancellati completamente (valutare se mantenere anonimizzati)

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

