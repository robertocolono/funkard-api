# 📡 Sistema Real-Time Backend Funkard

**Data:** 2025-01-15  
**Package:** `com.funkard.realtime`  
**Tecnologie:** SSE (Server-Sent Events) + WebSocket (fallback)

---

## 📋 Indice

1. [Panoramica](#panoramica)
2. [Architettura](#architettura)
3. [Componenti](#componenti)
4. [Endpoint](#endpoint)
5. [Eventi](#eventi)
6. [Integrazione](#integrazione)
7. [Sicurezza](#sicurezza)
8. [Fallback Polling](#fallback-polling)
9. [Esempi](#esempi)

---

## 🎯 Panoramica

Il sistema real-time di Funkard fornisce notifiche in tempo reale per:
- **Chat di supporto**: nuovi messaggi, aggiornamenti ticket
- **Notifiche admin**: nuovi ticket, messaggi utente, assegnazioni
- **Aggiornamenti stato**: cambi di stato ticket, risoluzioni, chiusure

**Tecnologie:**
- **SSE (Server-Sent Events)**: unidirezionale, server → client (primario)
- **WebSocket**: bidirezionale, server ↔ client (fallback automatico via SockJS)

---

## 🏗️ Architettura

```
┌─────────────────┐
│   Frontend      │
│  (Next.js)      │
└────────┬────────┘
         │
         │ SSE / WebSocket
         │
┌────────▼─────────────────────────────────┐
│  RealtimeConfig                          │
│  - Abilita SSE                           │
│  - Configura WebSocket                    │
└────────┬─────────────────────────────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌──▼────────┐
│Support│ │Admin      │
│Stream │ │Stream     │
│Ctrl   │ │Controller │
└───┬───┘ └──┬────────┘
    │        │
    │        │
┌───▼────────▼───┐
│  EventType     │
│  (Enum)        │
└────────────────┘
```

---

## 📦 Componenti

### **1. EventType.java**

Enum per tipi di eventi real-time:

```java
public enum EventType {
    NEW_TICKET,        // Nuovo ticket creato
    NEW_REPLY,        // Nuova risposta/messaggio
    TICKET_STATUS,    // Cambio stato ticket
    TICKET_ASSIGNED,  // Ticket assegnato
    TICKET_RESOLVED,  // Ticket risolto
    TICKET_CLOSED,    // Ticket chiuso
    TICKET_REOPENED,  // Ticket riaperto
    NOTIFICATION,     // Notifica generica
    PING,             // Keep-alive
    CONNECTED,        // Connessione stabilita
    ERROR             // Errore
}
```

### **2. RealtimeConfig.java**

Configurazione SSE e WebSocket:

```java
@Configuration
@EnableWebSocketMessageBroker
public class RealtimeConfig implements WebSocketMessageBrokerConfigurer {
    // Endpoint WebSocket: /ws
    // Destinazioni: /topic (broadcast), /app (client → server)
    // Fallback: SockJS automatico
}
```

### **3. SupportStreamController.java**

Controller SSE per utenti finali:

- **Endpoint:** `/api/support/stream?email={userEmail}`
- **Autenticazione:** JWT Bearer token (`@PreAuthorize("hasRole('USER')")`)
- **Gestione:** `ConcurrentHashMap<String, SseEmitter>` per connessioni attive
- **Keep-alive:** Automatico ogni 30 secondi

**Metodi principali:**
- `stream()` - Crea connessione SSE
- `sendEventToUser()` - Invia evento a utente specifico
- `sendTestEvent()` - Endpoint test (solo admin)

### **4. AdminStreamController.java**

Controller SSE per admin panel:

- **Endpoint:** `/api/admin/support/stream?userId={adminId}&role={role}`
- **Autenticazione:** JWT Bearer token (`@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")`)
- **Gestione:** Mappe separate per ruolo:
  - `superAdminEmitters` - SUPER_ADMIN
  - `adminEmitters` - ADMIN
  - `supportEmitters` - SUPPORT

**Metodi principali:**
- `stream()` - Crea connessione SSE
- `broadcastEvent()` - Broadcast a tutti i ruoli
- `sendToRole()` - Invia a ruolo specifico
- `sendToUser()` - Invia a utente specifico
- `sendTestEvent()` - Endpoint test

---

## 🔌 Endpoint

### **Utenti Finali**

#### **GET /api/support/stream**
Connessione SSE per utente.

**Query Parameters:**
- `email` (required) - Email dell'utente

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Response:** Stream SSE con eventi real-time

**Esempio:**
```http
GET /api/support/stream?email=user@example.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Eventi ricevuti:**
```
event: connected
data: {"type":"connected","data":{"message":"✅ Connessione attiva","email":"user@example.com"},"timestamp":1234567890}

event: new-reply
data: {"type":"new-reply","data":{"ticketId":"...","sender":"admin","messagePreview":"..."},"timestamp":1234567890}

event: ticket-status
data: {"type":"ticket-status","data":{"ticketId":"...","status":"resolved"},"timestamp":1234567890}
```

### **Admin Panel**

#### **GET /api/admin/support/stream**
Connessione SSE per admin.

**Query Parameters:**
- `userId` (required) - ID o email dell'admin
- `role` (required) - Ruolo: `SUPER_ADMIN`, `ADMIN`, `SUPPORT`

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Response:** Stream SSE con eventi real-time filtrati per ruolo

**Esempio:**
```http
GET /api/admin/support/stream?userId=admin@funkard.com&role=ADMIN
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Eventi ricevuti (per ADMIN):**
```
event: connected
data: {"type":"connected","data":{"message":"✅ Connessione attiva per ADMIN","role":"ADMIN","userId":"admin@funkard.com"},"timestamp":1234567890}

event: new-ticket
data: {"type":"new-ticket","data":{"ticketId":"...","email":"user@example.com","subject":"..."},"timestamp":1234567890}

event: new-reply
data: {"type":"new-reply","data":{"ticketId":"...","email":"user@example.com","messageId":"..."},"timestamp":1234567890}
```

### **Test/Development**

#### **POST /api/support/stream/events**
Invia evento di test a utente (solo admin).

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Body:**
```json
{
  "email": "user@example.com",
  "eventType": "NEW_REPLY",
  "data": {
    "ticketId": "...",
    "sender": "admin",
    "messagePreview": "Test message"
  }
}
```

#### **POST /api/admin/support/stream/events**
Invia evento di test a admin (solo admin).

**Body:**
```json
{
  "userId": "admin@funkard.com",
  "role": "ADMIN",
  "eventType": "NEW_TICKET",
  "data": {
    "ticketId": "...",
    "email": "user@example.com"
  }
}
```

**Broadcast a tutti:**
```json
{
  "userId": "*",
  "role": "ADMIN",
  "eventType": "NEW_TICKET",
  "data": {...}
}
```

### **Statistiche**

#### **GET /api/support/stream/stats**
Statistiche connessioni utenti (solo admin).

**Response:**
```json
{
  "connectedUsers": 5,
  "status": "active",
  "timestamp": 1234567890
}
```

#### **GET /api/admin/support/stream/stats**
Statistiche connessioni admin (solo admin).

**Response:**
```json
{
  "superAdminConnections": 1,
  "adminConnections": 3,
  "supportConnections": 2,
  "totalConnections": 6,
  "status": "active",
  "timestamp": 1234567890
}
```

---

## 📨 Eventi

### **Eventi Utente (SupportStreamController)**

| Evento | Tipo | Quando | Dati |
|--------|------|--------|------|
| `connected` | CONNECTED | Connessione stabilita | `{message, email, timestamp}` |
| `new-ticket` | NEW_TICKET | Ticket creato | `{ticketId, subject, status}` |
| `new-reply` | NEW_REPLY | Nuova risposta admin | `{ticketId, sender, messagePreview}` |
| `ticket-status` | TICKET_STATUS | Cambio stato | `{ticketId, status}` |
| `ticket-resolved` | TICKET_RESOLVED | Ticket risolto | `{ticketId, status}` |
| `ticket-closed` | TICKET_CLOSED | Ticket chiuso | `{ticketId}` |
| `ping` | PING | Keep-alive (30s) | `{message: "keep-alive"}` |

### **Eventi Admin (AdminStreamController)**

| Evento | Tipo | Ruoli | Quando | Dati |
|--------|------|-------|--------|------|
| `connected` | CONNECTED | Tutti | Connessione stabilita | `{message, role, userId, timestamp}` |
| `new-ticket` | NEW_TICKET | ADMIN, SUPER_ADMIN | Nuovo ticket | `{ticketId, email, subject, status, priority}` |
| `new-reply` | NEW_REPLY | ADMIN, SUPER_ADMIN | Nuovo messaggio utente | `{ticketId, email, messageId, sender, content}` |
| `ticket-status` | TICKET_STATUS | ADMIN, SUPER_ADMIN | Cambio stato | `{ticketId, email, oldStatus, newStatus}` |
| `ticket-assigned` | TICKET_ASSIGNED | SUPPORT (specifico), SUPER_ADMIN | Ticket assegnato | `{ticketId, email, assignedTo, status, locked}` |
| `ticket-resolved` | TICKET_RESOLVED | SUPER_ADMIN | Ticket risolto | `{ticketId, email, status}` |
| `ticket-closed` | TICKET_CLOSED | SUPER_ADMIN | Ticket chiuso | `{ticketId, email, status}` |
| `ping` | PING | Tutti | Keep-alive (30s) | `{message: "keep-alive"}` |

---

## 🔗 Integrazione

### **SupportTicketService**

Eventi emessi automaticamente:

#### **create() → NEW_TICKET**
```java
// Notifica admin
AdminStreamController.broadcastEvent(EventType.NEW_TICKET, eventData);

// Notifica utente
SupportStreamController.sendEventToUser(email, EventType.NEW_TICKET, data);
```

#### **addAdminReply() → NEW_REPLY**
```java
// Notifica admin
AdminStreamController.sendToRole("ADMIN", EventType.NEW_REPLY, messageData);
AdminStreamController.sendToRole("SUPER_ADMIN", EventType.NEW_REPLY, messageData);

// Notifica utente
SupportStreamController.sendEventToUser(userEmail, EventType.NEW_REPLY, data);
```

#### **updateStatus() → TICKET_STATUS**
```java
// Notifica admin
AdminStreamController.sendToRole("ADMIN", EventType.TICKET_STATUS, statusData);
AdminStreamController.sendToRole("SUPER_ADMIN", EventType.TICKET_STATUS, statusData);

// Notifica utente
SupportStreamController.sendEventToUser(userEmail, EventType.TICKET_STATUS, data);
```

#### **resolveTicket() → TICKET_RESOLVED**
```java
// Notifica admin
AdminStreamController.sendToRole("SUPER_ADMIN", EventType.TICKET_RESOLVED, resolvedData);

// Notifica utente
SupportStreamController.sendEventToUser(userEmail, EventType.TICKET_RESOLVED, data);
```

#### **closeTicket() → TICKET_CLOSED**
```java
// Notifica admin
AdminStreamController.sendToRole("SUPER_ADMIN", EventType.TICKET_CLOSED, closedData);

// Notifica utente
SupportStreamController.sendEventToUser(userEmail, EventType.TICKET_CLOSED, data);
```

#### **assignTicket() → TICKET_ASSIGNED**
```java
// Notifica support specifico
AdminStreamController.sendToUser(supportEmail, "SUPPORT", EventType.TICKET_ASSIGNED, eventData);

// Notifica super admin
AdminStreamController.sendToRole("SUPER_ADMIN", EventType.TICKET_ASSIGNED, eventData);
```

### **SupportMessageService**

#### **addMessage() → NEW_REPLY**
```java
// Se messaggio da utente, notifica admin
if (fromUser) {
    AdminStreamController.sendToRole("ADMIN", EventType.NEW_REPLY, eventData);
    AdminStreamController.sendToRole("SUPER_ADMIN", EventType.NEW_REPLY, eventData);
}
```

---

## 🔐 Sicurezza

### **Autenticazione**

Tutti gli endpoint real-time richiedono **JWT Bearer token**:

```
Authorization: Bearer {jwt_token}
```

**Ruoli supportati:**
- `ROLE_USER` - Utenti finali
- `ROLE_ADMIN` - Admin
- `ROLE_SUPER_ADMIN` - Super Admin
- `ROLE_SUPPORT` - Support staff

### **Autorizzazione**

- **SupportStreamController:**
  - `@PreAuthorize("hasRole('USER')")` per endpoint stream
  - `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` per endpoint test/stats

- **AdminStreamController:**
  - `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` per tutti gli endpoint

### **CORS**

Origini permesse:
- `https://funkard.com`
- `https://www.funkard.com`
- `https://admin.funkard.com`
- `http://localhost:3000`
- `http://localhost:3002`

**Rimosso:** `X-Admin-Token` (non più supportato)

---

## 🔄 Fallback Polling

Se l'utente non ha connessione SSE attiva, può usare polling HTTP.

### **GET /api/support/chat/{ticketId}/messages?lastMessageId={uuid}**

Restituisce solo i messaggi creati dopo `lastMessageId`.

**Esempio:**
```http
GET /api/support/chat/123e4567-e89b-12d3-a456-426614174000/messages?lastMessageId=789e0123-e89b-12d3-a456-426614174001
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "success": true,
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "messages": [
    {
      "id": "789e0123-e89b-12d3-a456-426614174002",
      "message": "Nuovo messaggio",
      "sender": "admin",
      "createdAt": "2025-01-15T10:30:00Z"
    }
  ],
  "count": 1,
  "hasNewMessages": true
}
```

**Frontend polling pattern:**
```javascript
let lastMessageId = null;

setInterval(async () => {
  const url = lastMessageId 
    ? `/api/support/chat/${ticketId}/messages?lastMessageId=${lastMessageId}`
    : `/api/support/chat/${ticketId}/messages`;
  
  const response = await fetch(url, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const data = await response.json();
  
  if (data.messages && data.messages.length > 0) {
    // Aggiorna UI con nuovi messaggi
    data.messages.forEach(msg => {
      displayMessage(msg);
      lastMessageId = msg.id;
    });
  }
}, 5000); // Poll ogni 5 secondi
```

---

## 💻 Esempi Frontend

### **React/Next.js - SSE Connection**

```typescript
// Hook per connessione SSE utente
function useSupportStream(email: string, token: string) {
  const [events, setEvents] = useState<any[]>([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const eventSource = new EventSource(
      `/api/support/stream?email=${encodeURIComponent(email)}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    eventSource.onopen = () => {
      setConnected(true);
      console.log('✅ SSE connesso');
    };

    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      setEvents(prev => [...prev, data]);
      
      // Gestisci eventi specifici
      if (data.type === 'new-reply') {
        showNotification('Nuova risposta dal supporto!');
      }
    };

    eventSource.onerror = (error) => {
      console.error('❌ Errore SSE:', error);
      setConnected(false);
    };

    return () => {
      eventSource.close();
    };
  }, [email, token]);

  return { events, connected };
}
```

### **React/Next.js - Admin SSE Connection**

```typescript
// Hook per connessione SSE admin
function useAdminSupportStream(userId: string, role: string, token: string) {
  const [events, setEvents] = useState<any[]>([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const eventSource = new EventSource(
      `/api/admin/support/stream?userId=${encodeURIComponent(userId)}&role=${role}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    eventSource.addEventListener('new-ticket', (event) => {
      const data = JSON.parse(event.data);
      setEvents(prev => [...prev, data]);
      showNotification(`Nuovo ticket da ${data.data.email}`);
    });

    eventSource.addEventListener('new-reply', (event) => {
      const data = JSON.parse(event.data);
      setEvents(prev => [...prev, data]);
      showNotification(`Nuovo messaggio nel ticket ${data.data.ticketId}`);
    });

    eventSource.onopen = () => setConnected(true);
    eventSource.onerror = () => setConnected(false);

    return () => eventSource.close();
  }, [userId, role, token]);

  return { events, connected };
}
```

### **WebSocket Fallback (SockJS)**

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const connectWebSocket = (token) => {
  const socket = new SockJS('/ws');
  const stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      'Authorization': `Bearer ${token}`
    },
    onConnect: () => {
      console.log('✅ WebSocket connesso');
      
      // Iscriviti a topic
      stompClient.subscribe('/topic/support/new-ticket', (message) => {
        const data = JSON.parse(message.body);
        handleNewTicket(data);
      });
    },
    onStompError: (error) => {
      console.error('❌ Errore WebSocket:', error);
    }
  });

  stompClient.activate();
  return stompClient;
};
```

---

## 📊 Gestione Connessioni

### **Keep-Alive**

Entrambi i controller inviano automaticamente ping ogni 30 secondi per:
- Mantenere connessioni attive
- Rilevare connessioni morte
- Rimuovere emitter non più validi

### **Cleanup Automatico**

Le connessioni vengono rimosse automaticamente quando:
- `emitter.onCompletion()` - Connessione chiusa dal client
- `emitter.onTimeout()` - Timeout connessione
- `emitter.onError()` - Errore connessione
- Errore durante invio evento

### **Statistiche**

Endpoint disponibili per monitorare connessioni:
- `GET /api/support/stream/stats` - Connessioni utenti
- `GET /api/admin/support/stream/stats` - Connessioni admin per ruolo

---

## 🧪 Testing

### **Test Manuale SSE**

```bash
# Connessione utente
curl -N -H "Authorization: Bearer {token}" \
  "http://localhost:8080/api/support/stream?email=user@example.com"

# Connessione admin
curl -N -H "Authorization: Bearer {token}" \
  "http://localhost:8080/api/admin/support/stream?userId=admin@funkard.com&role=ADMIN"

# Invia evento di test
curl -X POST -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "eventType": "NEW_REPLY",
    "data": {
      "ticketId": "123",
      "sender": "admin",
      "messagePreview": "Test message"
    }
  }' \
  "http://localhost:8080/api/support/stream/events"
```

### **Test Polling Fallback**

```bash
# Tutti i messaggi
curl -H "Authorization: Bearer {token}" \
  "http://localhost:8080/api/support/chat/{ticketId}/messages"

# Solo nuovi messaggi (dopo lastMessageId)
curl -H "Authorization: Bearer {token}" \
  "http://localhost:8080/api/support/chat/{ticketId}/messages?lastMessageId={uuid}"
```

---

## ✅ Checklist Implementazione

- [x] Package `com.funkard.realtime` creato
- [x] `EventType.java` enum definito
- [x] `RealtimeConfig.java` configurato
- [x] `SupportStreamController.java` implementato
- [x] `AdminStreamController.java` implementato
- [x] Integrazione eventi in `SupportTicketService`
- [x] Integrazione eventi in `SupportMessageService`
- [x] Fallback polling in `GET /api/support/chat/{ticketId}/messages`
- [x] Autenticazione JWT standardizzata
- [x] CORS aggiornato
- [x] Logging completo
- [x] Keep-alive automatico
- [x] Documentazione completa

---

## 🚀 Prossimi Passi

1. **Test end-to-end** con frontend Next.js
2. **Monitoraggio** connessioni attive
3. **Ottimizzazioni** per alta concorrenza
4. **Metriche** e alerting
5. **Rate limiting** per prevenire abusi

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15

