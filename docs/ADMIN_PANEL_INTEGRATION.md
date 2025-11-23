# 🔗 Integrazione Pannello Admin - Frontend Funkard

## 📋 Panoramica

Questo documento descrive gli endpoint disponibili per collegare il pannello admin di Funkard con il frontend principale per la gestione di:
- 💬 Chat di supporto con gli utenti
- 🔄 Sincronizzazione stato ticket/task
- 🔔 Aggiornamenti quasi real-time delle notifiche

## 🔐 Autenticazione

Tutti gli endpoint admin supportano **due metodi di autenticazione**:

1. **Authorization: Bearer {token}** (preferito)
   ```
   Authorization: Bearer {ADMIN_TOKEN}
   ```

2. **X-Admin-Token** (legacy, mantenuto per compatibilità)
   ```
   X-Admin-Token: {ADMIN_TOKEN}
   ```

## 💬 Endpoint Chat Supporto

### Base Path: `/api/support/chat`

#### 1. Invia Messaggio
```http
POST /api/support/chat/{ticketId}/message
Content-Type: application/json
Authorization: Bearer {token} (opzionale per utenti)
X-Admin-Token: {token} (per admin)

Body:
{
  "message": "Testo del messaggio",
  "sender": "user@example.com" o "admin"
}
```

**Risposta:**
```json
{
  "success": true,
  "message": "Messaggio inviato con successo",
  "messageId": "uuid",
  "ticketId": "uuid",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

#### 2. Ottieni Messaggi
```http
GET /api/support/chat/{ticketId}/messages
Authorization: Bearer {token} (opzionale)
X-Admin-Token: {token} (per admin)
```

**Risposta:**
```json
{
  "success": true,
  "ticketId": "uuid",
  "messages": [
    {
      "id": "uuid",
      "message": "Testo",
      "sender": "user@example.com",
      "createdAt": "2025-01-15T10:30:00Z"
    }
  ],
  "count": 5
}
```

#### 3. Marca come Letto (Admin)
```http
POST /api/support/chat/{ticketId}/read
X-Admin-Token: {token}
```

#### 4. Statistiche Chat
```http
GET /api/support/chat/{ticketId}/stats
```

**Risposta:**
```json
{
  "success": true,
  "ticketId": "uuid",
  "stats": {
    "totalMessages": 10,
    "userMessages": 5,
    "adminMessages": 5,
    "hasNewMessages": true,
    "lastMessageAt": "2025-01-15T10:30:00Z"
  }
}
```

## 🎫 Endpoint Supporto Admin

### Base Path: `/api/admin/support`

#### 1. Lista Tutti i Ticket
```http
GET /api/admin/support/tickets
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 2. Statistiche
```http
GET /api/admin/support/stats
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 3. Rispondi a Ticket
```http
POST /api/admin/support/reply/{id}
Authorization: Bearer {token}
X-Admin-Token: {token}
Content-Type: application/json

Body:
{
  "sender": "admin@funkard.com",
  "content": "Risposta admin"
}
```

#### 4. Risolvi Ticket
```http
POST /api/admin/support/resolve/{id}
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 5. Chiudi Ticket
```http
POST /api/admin/support/close/{id}
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 6. Riapri Ticket
```http
POST /api/admin/support/reopen/{id}
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 7. Marca Messaggi come Letti
```http
POST /api/admin/support/{id}/mark-read
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 8. Conta Nuovi Messaggi
```http
GET /api/admin/support/new-messages-count
Authorization: Bearer {token}
X-Admin-Token: {token}
```

**Risposta:**
```json
{
  "count": 5
}
```

#### 9. Assegna Ticket
```http
POST /api/admin/support/{id}/assign?supportEmail=support@funkard.com
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 10. Rilascia Ticket
```http
POST /api/admin/support/{id}/release
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 11. Ticket Assegnati
```http
GET /api/admin/support/assigned/{supportEmail}
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 12. Conta Ticket Assegnati
```http
GET /api/admin/support/assigned-count
Authorization: Bearer {token}
X-Admin-Token: {token}
```

## 🔔 Endpoint Notifiche Admin

### Base Path: `/api/admin/notifications`

#### 1. Lista Notifiche
```http
GET /api/admin/notifications
GET /api/admin/notifications?type=support_ticket&priority=high&status=unread
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 2. Dettaglio Notifica
```http
GET /api/admin/notifications/{id}
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 3. Marca come Letta
```http
POST /api/admin/notifications/{id}/read
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 4. Assegna Notifica
```http
POST /api/admin/notifications/{id}/assign
Authorization: Bearer {token}
X-Admin-Token: {token}
```

#### 5. Risolvi Notifica
```http
POST /api/admin/notifications/{id}/resolve
Authorization: Bearer {token}
X-Admin-Token: {token}
Content-Type: application/json

Body (opzionale):
{
  "note": "Nota di risoluzione"
}
```

#### 6. Archivia Notifica
```http
POST /api/admin/notifications/{id}/archive
Authorization: Bearer {token}
X-Admin-Token: {token}
Content-Type: application/json

Body (opzionale):
{
  "note": "Nota di archiviazione"
}
```

#### 7. Stream Notifiche Real-Time (SSE)
```http
GET /api/admin/notifications/stream
Authorization: Bearer {token}
X-Admin-Token: {token}
Accept: text/event-stream
```

**Eventi SSE:**
```
event: notification
data: {"id":"uuid","type":"support_ticket","message":"Nuovo ticket","priority":"high"}

event: notification
data: {"id":"uuid","type":"support_message","message":"Nuovo messaggio","priority":"normal"}
```

#### 8. Conta Non Lette
```http
GET /api/admin/notifications/unread-count
Authorization: Bearer {token}
X-Admin-Token: {token}
```

**Risposta:**
```json
{
  "count": 10
}
```

#### 9. Ultime Non Lette
```http
GET /api/admin/notifications/unread-latest
Authorization: Bearer {token}
X-Admin-Token: {token}
```

## 🔄 Sincronizzazione Real-Time

### Server-Sent Events (SSE)

Il backend supporta SSE per aggiornamenti real-time:

1. **Notifiche Admin**: `/api/admin/notifications/stream`
2. **Supporto Admin**: `/api/admin/support/stream` (se disponibile)
3. **Supporto Utente**: `/api/support/stream` (per utenti)

### WebSocket (se configurato)

Alcuni endpoint possono utilizzare WebSocket per comunicazione bidirezionale.

## 📝 Esempi di Integrazione Frontend

### React/Next.js - Chat Supporto

```typescript
// Invia messaggio
const sendMessage = async (ticketId: string, message: string, sender: string) => {
  const response = await fetch(`/api/support/chat/${ticketId}/message`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ message, sender })
  });
  return response.json();
};

// Ottieni messaggi
const getMessages = async (ticketId: string) => {
  const response = await fetch(`/api/support/chat/${ticketId}/messages`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};

// SSE per aggiornamenti real-time
const eventSource = new EventSource('/api/admin/notifications/stream', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

eventSource.onmessage = (event) => {
  const notification = JSON.parse(event.data);
  // Aggiorna UI
};
```

### Vue.js - Gestione Ticket

```javascript
// Risolvi ticket
async function resolveTicket(ticketId) {
  const response = await fetch(`/api/admin/support/resolve/${ticketId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    }
  });
  return response.json();
}

// Assegna ticket
async function assignTicket(ticketId, supportEmail) {
  const response = await fetch(`/api/admin/support/${ticketId}/assign?supportEmail=${supportEmail}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${adminToken}`
    }
  });
  return response.json();
}
```

## ✅ Checklist Integrazione

- [x] Endpoint chat supporto (`/api/support/chat/*`)
- [x] Endpoint supporto admin (`/api/admin/support/*`)
- [x] Endpoint notifiche admin (`/api/admin/notifications/*`)
- [x] Supporto autenticazione `Authorization: Bearer`
- [x] Supporto autenticazione `X-Admin-Token` (legacy)
- [x] SSE per notifiche real-time
- [x] CORS configurato per frontend
- [x] Gestione errori e validazione

## 🚀 Prossimi Passi

1. Testare tutti gli endpoint con Postman/curl
2. Implementare frontend React/Vue per chat
3. Configurare polling o SSE per aggiornamenti real-time
4. Aggiungere gestione errori lato frontend
5. Implementare notifiche push (opzionale)

