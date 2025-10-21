# 📡 Formato Eventi SSE Funkard

## 🎯 **Struttura Standard Eventi**

Tutti gli eventi SSE seguono questo formato standard per garantire il filtraggio lato frontend:

```json
{
  "type": "ticket-reply",
  "ticketId": "1234abcd",
  "email": "utente@dominio.it",
  "timestamp": 1697567890123
}
```

## 🔔 **Eventi Utenti Finali**

### **💬 Nuova Risposta Admin**
```json
{
  "type": "ticket-reply",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "agentName": "support@funkard.com",
  "messagePreview": "Grazie per averci contattato. Stiamo lavorando...",
  "timestamp": 1697567890123
}
```

### **✅ Ticket Risolto**
```json
{
  "type": "ticket-resolved",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "status": "resolved",
  "timestamp": 1697567890123
}
```

### **🔒 Ticket Chiuso**
```json
{
  "type": "ticket-closed",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "timestamp": 1697567890123
}
```

### **🎫 Ticket Creato**
```json
{
  "type": "ticket-created",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "subject": "Problema con il pagamento",
  "timestamp": 1697567890123
}
```

### **🔄 Aggiornamento Stato**
```json
{
  "type": "status-update",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "oldStatus": "open",
  "newStatus": "in_progress",
  "timestamp": 1697567890123
}
```

## 🎧 **Eventi Admin Panel**

### **🎫 Nuovo Ticket (Admin/Super Admin)**
```json
{
  "type": "new-ticket",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "subject": "Problema con il pagamento",
  "status": "open",
  "priority": "normal",
  "createdAt": "2023-10-17T15:30:00",
  "timestamp": 1697567890123
}
```

### **💬 Nuovo Messaggio (Admin/Super Admin)**
```json
{
  "type": "new-message",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "messageId": "456e7890-e89b-12d3-a456-426614174000",
  "sender": "user",
  "content": "Grazie per la risposta, il problema persiste...",
  "timestamp": 1697567890123
}
```

### **🎯 Ticket Assegnato (Support specifico + Super Admin)**
```json
{
  "type": "ticket-assigned",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "subject": "Problema con il pagamento",
  "assignedTo": "support@funkard.com",
  "status": "in_progress",
  "locked": true,
  "timestamp": 1697567890123
}
```

### **✅ Ticket Risolto (Super Admin)**
```json
{
  "type": "ticket-resolved",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "status": "resolved",
  "timestamp": 1697567890123
}
```

### **🔒 Ticket Chiuso (Super Admin)**
```json
{
  "type": "ticket-closed",
  "ticketId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "status": "closed",
  "timestamp": 1697567890123
}
```

## 🎮 **Filtraggio Frontend**

### **JavaScript Example**
```javascript
class SSEEventFilter {
    constructor(userEmail) {
        this.userEmail = userEmail;
    }

    handleEvent(event) {
        const data = JSON.parse(event.data);
        
        // Filtra per email utente
        if (data.email !== this.userEmail) {
            return; // Ignora eventi per altri utenti
        }

        // Processa evento basato sul tipo
        switch (data.type) {
            case 'ticket-reply':
                this.handleTicketReply(data);
                break;
            case 'ticket-resolved':
                this.handleTicketResolved(data);
                break;
            case 'ticket-closed':
                this.handleTicketClosed(data);
                break;
            case 'ticket-created':
                this.handleTicketCreated(data);
                break;
            case 'status-update':
                this.handleStatusUpdate(data);
                break;
        }
    }

    handleTicketReply(data) {
        this.showToast(
            '💬 Nuova risposta',
            `Da: ${data.agentName} - ${data.messagePreview}`,
            'info'
        );
        this.refreshTicketList();
    }

    handleTicketResolved(data) {
        this.showToast(
            '✅ Ticket risolto',
            `Ticket ${data.ticketId} risolto!`,
            'success'
        );
        this.updateTicketStatus(data.ticketId, 'resolved');
    }

    handleTicketClosed(data) {
        this.showToast(
            '🔒 Ticket chiuso',
            `Ticket ${data.ticketId} chiuso`,
            'warning'
        );
        this.updateTicketStatus(data.ticketId, 'closed');
    }

    handleTicketCreated(data) {
        this.showToast(
            '🎫 Ticket creato',
            `Ticket "${data.subject}" creato con successo`,
            'success'
        );
    }

    handleStatusUpdate(data) {
        this.showToast(
            '🔄 Stato aggiornato',
            `Da ${data.oldStatus} a ${data.newStatus}`,
            'info'
        );
        this.updateTicketStatus(data.ticketId, data.newStatus);
    }
}
```

## 📊 **Endpoint SSE**

### **Utenti Finali**
```bash
GET /api/support/stream?email=user@example.com
```

### **Admin Panel**
```bash
GET /api/admin/support/stream?userId=123&role=admin
```

## 🔧 **Configurazione CORS**

Gli endpoint SSE supportano le seguenti origini:
- `https://funkard.vercel.app`
- `https://funkardnew.vercel.app`
- `https://funkard-admin.vercel.app`
- `http://localhost:3000`

## 📈 **Benefici del Formato Standardizzato**

1. **Filtraggio Efficiente**: L'email permette filtraggio lato frontend
2. **Compatibilità**: Formato uniforme per tutti gli eventi
3. **Debugging**: Struttura chiara per troubleshooting
4. **Scalabilità**: Facile estensione per nuovi tipi di evento
5. **Performance**: Riduzione traffico con filtraggio intelligente
