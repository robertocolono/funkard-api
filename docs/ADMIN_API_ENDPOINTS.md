# 📋 API Endpoint Pannello Admin Funkard

Documentazione completa degli endpoint disponibili per il pannello admin (`funkardadminreal`).

## 🔐 Autenticazione

Il backend supporta due sistemi di autenticazione:

1. **Nuovo sistema** (`com.funkard.adminauth`): Usa token da database `admin_users`
2. **Sistema legacy**: Usa `X-Admin-Token` header con valore da variabile d'ambiente `admin.token`

---

## 📍 1. `/api/admin/auth/**`

**Controller**: `AdminAuthController` (nuovo sistema `com.funkard.adminauth`)

### ✅ Endpoint Attivi

| Metodo | Endpoint | Autenticazione | Descrizione |
|--------|----------|----------------|-------------|
| `GET` | `/api/admin/auth/token/{token}` | Pubblico | Valida token e restituisce dati utente |
| `POST` | `/api/admin/auth/users/create` | `X-Admin-Token` (SUPER_ADMIN) | Crea nuovo utente admin |
| `PATCH` | `/api/admin/auth/users/{id}/regenerate-token` | `X-Admin-Token` (SUPER_ADMIN) | Rigenera token utente |
| `PATCH` | `/api/admin/auth/users/{id}/deactivate` | `X-Admin-Token` (SUPER_ADMIN) | Disattiva utente admin |

**Risposta GET `/token/{token}`:**
```json
{
  "status": "ok",
  "user": {
    "name": "Will",
    "email": "colonoroberto@gmail.com",
    "role": "SUPER_ADMIN"
  }
}
```

**Errore (401):**
```json
{
  "error": "Token non valido o utente inattivo"
}
```

---

## 📢 2. `/api/admin/notifications/**`

**Controller**: `AdminNotificationController`

### ⚠️ Autenticazione
**Nessuna autenticazione esplicita** - usa `Principal` (potrebbe essere vulnerabile)

### ✅ Endpoint Attivi

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/notifications` | Lista notifiche attive (con filtri opzionali: `?type=X&priority=Y&status=Z`) |
| `GET` | `/api/admin/notifications/{id}` | Dettaglio notifica |
| `GET` | `/api/admin/notifications/archive` | Notifiche archiviate (ultimi 30 giorni) |
| `GET` | `/api/admin/notifications/stream` | SSE per notifiche real-time |
| `POST` | `/api/admin/notifications/{id}/read` | Marca come letta |
| `POST` | `/api/admin/notifications/{id}/assign` | Prende in carico notifica |
| `POST` | `/api/admin/notifications/{id}/resolve` | Risolve notifica |
| `POST` | `/api/admin/notifications/{id}/archive` | Archivia notifica |
| `DELETE` | `/api/admin/notifications/cleanup` | Cleanup notifiche archiviate (param: `?days=30`) |
| `DELETE` | `/api/admin/notifications/delete/{id}` | Elimina notifica archiviata |

### ➕ Endpoint Aggiunti

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/notifications/unread-count` | Conteggio notifiche non lette |
| `GET` | `/api/admin/notifications/unread-latest` | Ultime notifiche non lette (max 10) |

---

## 🆘 3. `/api/admin/support/**`

**Controller**: `AdminSupportController`

### ✅ Autenticazione
**Richiede**: `X-Admin-Token` header (sistema legacy)

### ✅ Endpoint Attivi

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/support/tickets` | Lista tutti i ticket |
| `GET` | `/api/admin/support/stats` | Statistiche ultimi 30 giorni |
| `GET` | `/api/admin/support/new-messages-count` | Conteggio ticket con nuovi messaggi |
| `GET` | `/api/admin/support/assigned/{supportEmail}` | Ticket assegnati a support |
| `GET` | `/api/admin/support/assigned-count` | Conteggio ticket assegnati |
| `GET` | `/api/admin/support/stream` | SSE per notifiche real-time (con parametri `userId` e `role`) |
| `GET` | `/api/admin/support/stream/stats` | Statistiche connessioni SSE |
| `POST` | `/api/admin/support/reply/{id}` | Rispondi a ticket |
| `POST` | `/api/admin/support/resolve/{id}` | Risolvi ticket |
| `POST` | `/api/admin/support/close/{id}` | Chiudi ticket |
| `POST` | `/api/admin/support/reopen/{id}` | Riapri ticket |
| `POST` | `/api/admin/support/{id}/mark-read` | Marca messaggi come letti |
| `POST` | `/api/admin/support/{id}/assign` | Assegna ticket (param: `?supportEmail=X`) |
| `POST` | `/api/admin/support/{id}/release` | Rilascia ticket (unlock) |
| `DELETE` | `/api/admin/support/cleanup` | Cleanup messaggi (richiede `Authorization: Bearer FUNKARD_CRON_SECRET`) |

---

## 📊 4. `/api/admin/stats/**`

**Controller**: `AdminStatsController`

### ✅ Autenticazione
**Richiede**: `X-Admin-Token` header (sistema legacy)

### ✅ Endpoint Attivi

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/stats` | Statistiche generali (utenti, prodotti, carte, ecc.) |

---

## 📝 5. `/api/admin/logs/**`

**Controller**: `AdminActionLogController`

### ⚠️ Autenticazione
**Nessuna autenticazione esplicita**

### ✅ Endpoint Attivi

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/logs/{type}/{id}` | Storico azioni per entità (es: `/api/admin/logs/ticket/123`) |
| `DELETE` | `/api/admin/logs/cleanup` | Cleanup log vecchi (> 2 mesi) |

---

## 💰 6. `/api/admin/valuation/**`

**Controller**: `AdminValuationController` e `AdminController` (duplicato)

### ✅ Autenticazione
**Richiede**: `X-Admin-Token` header (sistema legacy)

### ✅ Endpoint Attivi

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/valuation/overview` | Overview mercato ultimi 7 giorni |
| `GET` | `/api/admin/valuation/pending` | Elementi in attesa di valutazione |
| `GET` | `/api/admin/valuation/check` | Verifica accesso admin |

---

## 📈 7. `/api/admin/dashboard/**`

**Controller**: `AdminDashboardController`

### ⚠️ Autenticazione
**Nessuna autenticazione esplicita**

### ✅ Endpoint Attivi

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/dashboard` | Dashboard aggregata (statistiche notifiche, mercato, grading, utenti, support) |
| `DELETE` | `/api/admin/dashboard/cleanup` | Cleanup notifiche archiviate (> 30 giorni) |

---

## 🔧 8. `/api/admin/system/**`

**Controller**: `SystemMaintenanceController`

### ⚠️ Autenticazione
**Nessuna autenticazione esplicita**

### ✅ Endpoint Attivi

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/system/cleanup/status` | Stato ultimo cleanup |
| `POST` | `/api/admin/system/cleanup/status` | Aggiorna stato cleanup |

---

## 🎯 Endpoint Aggiunti

Gli endpoint seguenti sono stati aggiunti per completare l'integrazione con il pannello admin:

### Notifiche

1. **GET `/api/admin/notifications/unread-count`**
   - Restituisce conteggio notifiche non lette
   - **Risposta**: `{ "count": 42 }`

2. **GET `/api/admin/notifications/unread-latest`**
   - Restituisce ultime 10 notifiche non lette
   - **Risposta**: Array di `AdminNotification`

---

## 🔒 Sicurezza

### Standard di Autenticazione

- **Nuovo sistema** (`/api/admin/auth/**`): Usa token da database `admin_users`
- **Sistema legacy**: Usa header `X-Admin-Token` con valore da `admin.token` (env var)
- **Endpoint senza autenticazione**: 
  - `/api/admin/notifications/**` (usa Principal)
  - `/api/admin/logs/**`
  - `/api/admin/dashboard/**`
  - `/api/admin/system/**`

### CORS

Configurato in `SecurityConfig.java` per:
- `https://funkard.com`
- `https://www.funkard.com`
- `http://localhost:3000`
- `http://localhost:3002`

---

## 📝 Note

1. **Duplicati**: Esistono due controller per `/api/admin/valuation`:
   - `AdminValuationController` (nuovo)
   - `AdminController` (legacy)

2. **SSE**: Endpoint `/api/admin/notifications/stream` e `/api/admin/support/stream` per notifiche real-time

3. **Cleanup**: Endpoint di cleanup protetti da `FUNKARD_CRON_SECRET` (solo Cloudflare Worker)

4. **Principal**: Alcuni endpoint usano `Principal` per identificare l'utente, ma non richiedono autenticazione esplicita

---

## ✅ Status Finale

- ✅ `/api/admin/auth/**` - Completo e funzionante
- ✅ `/api/admin/notifications/**` - Completo con endpoint aggiunti
- ✅ `/api/admin/support/**` - Completo
- ✅ `/api/admin/stats/**` - Completo
- ✅ `/api/admin/logs/**` - Completo
- ✅ `/api/admin/valuation/**` - Completo
- ✅ `/api/admin/dashboard/**` - Completo
- ✅ `/api/admin/system/**` - Completo

**Tutti gli endpoint necessari per il pannello admin sono disponibili e documentati.**

