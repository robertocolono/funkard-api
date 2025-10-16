# 🔔 Sistema Notifiche Admin Funkard - Documentazione Completa

## 📋 Panoramica

Il sistema notifiche admin Funkard è un sistema enterprise-grade completo per la gestione delle notifiche interne, con archiviazione automatica, pulizia schedulata e esportazione dati.

## 🏗️ Architettura

### Modelli Principali
- `AdminNotification` - Notifiche attive
- `AdminNotificationArchive` - Notifiche archiviate
- `UserNotification` - Notifiche utente
- `SupportTicket` - Sistema supporto

### Servizi Core
- `AdminNotificationService` - Gestione notifiche
- `AdminNotificationArchiveService` - Gestione archivio
- `NotificationEventService` - Eventi automatici
- `NotificationOptimizationService` - Ottimizzazioni

## 🚀 Endpoint API Completi

### 📱 Notifiche Attive
```
GET    /api/admin/notifications                    # Lista tutte
GET    /api/admin/notifications/unread            # Non lette
GET    /api/admin/notifications/unresolved        # Non risolte
GET    /api/admin/notifications/type/{type}      # Per tipo
GET    /api/admin/notifications/reference/{type}/{id}  # Per riferimento
POST   /api/admin/notifications                  # Crea notifica
POST   /api/admin/notifications/with-reference    # Crea con riferimento
PATCH  /api/admin/notifications/{id}/read        # Segna come letta
PATCH  /api/admin/notifications/{id}/resolve     # Segna come risolta
POST   /api/admin/notifications/{id}/archive     # Archivia
POST   /api/admin/notifications/{id}/resolve-and-archive  # Risolvi e archivia
DELETE /api/admin/notifications/{id}             # Elimina
GET    /api/admin/notifications/stats            # Statistiche
```

### 📁 Archivio
```
GET    /api/admin/notifications/archive                    # Archivio completo
GET    /api/admin/notifications/archive/type/{type}        # Per tipo
GET    /api/admin/notifications/archive/reference/{type}/{id}  # Per riferimento
GET    /api/admin/notifications/archive/recent            # Recenti (default: 7 giorni)
```

### 🔍 Archivio Avanzato
```
GET    /api/admin/notifications/archive/advanced/filter   # Filtri avanzati
GET    /api/admin/notifications/archive/advanced/analytics # Analytics
POST   /api/admin/notifications/archive/advanced/cleanup   # Pulizia manuale
GET    /api/admin/notifications/archive/advanced/trends    # Trend analysis
```

### 📊 Esportazione
```
GET    /api/admin/notifications/export/csv       # Esporta CSV
GET    /api/admin/notifications/export/json      # Esporta JSON
GET    /api/admin/notifications/export/report    # Report statistico
```

### ⚡ Ottimizzazioni
```
GET    /api/admin/notifications/optimization/stats        # Statistiche ottimizzate
POST   /api/admin/notifications/optimization/refresh-cache  # Refresh cache
POST   /api/admin/notifications/optimization/clear-cache    # Clear cache
POST   /api/admin/notifications/optimization/batch-mark-read # Batch lettura
POST   /api/admin/notifications/optimization/batch-resolve-archive # Batch risoluzione
GET    /api/admin/notifications/optimization/dashboard    # Dashboard dati
```

### 👥 Notifiche Utente
```
GET    /api/user/notifications/{userId}          # Per utente
GET    /api/user/notifications/{userId}/unread   # Non lette utente
GET    /api/user/notifications/{userId}/unresolved # Non risolte utente
POST   /api/user/notifications/for-user         # Crea per utente
PATCH  /api/user/notifications/{userId}/mark-all-read # Segna tutte come lette
GET    /api/user/notifications/{userId}/stats    # Statistiche utente
```

## 🔄 Flusso Operativo

### 1. Generazione Automatica
```java
// Eventi automatici
eventService.notifyNewCard("Charizard EX", "bobbojr", cardId);
eventService.notifyCardReported("Charizard EX", "prezzo anomalo", cardId);
eventService.notifyValuationRequested("bobbojr", "Charizard EX", cardId);
```

### 2. Gestione e Risoluzione
- Visualizzazione in tempo reale via SSE
- Lettura: `PATCH /api/admin/notifications/{id}/read`
- Risoluzione: `PATCH /api/admin/notifications/{id}/resolve`
- Archiviazione: `POST /api/admin/notifications/{id}/resolve-and-archive`

### 3. Pulizia Automatica
```java
@Scheduled(cron = "0 0 2 * * *") // Ogni giorno alle 2:00
public void cleanupOldArchives() {
    // Rimuove notifiche archiviate da +30 giorni
}
```

## 🎯 Tipi di Notifiche

| Tipo | Descrizione | Esempi |
|------|-------------|--------|
| `INFO` | Informazioni generali | Nuova carta caricata, valutazione completata |
| `WARNING` | Avvisi importanti | Carta segnalata, prezzo anomalo |
| `ERROR` | Errori critici | Transazione fallita, errore sistema |
| `SUPPORT` | Supporto utenti | Nuovo ticket, richiesta assistenza |

## 🔗 Sistema Riferimenti

Le notifiche sono collegate agli oggetti tramite:
- `referenceType`: "CARD", "USER", "TRANSACTION", "TICKET", "SYSTEM"
- `referenceId`: ID dell'oggetto specifico

## ⚡ Ottimizzazioni

- **Cache in memoria**: `ConcurrentHashMap` per performance
- **Batch operations**: Operazioni multiple in transazione
- **SSE streaming**: Notifiche real-time
- **Paginazione**: Per grandi dataset
- **Indici database**: Su campi critici

## 🧹 Pulizia Automatica

- **Scheduled cleanup**: Ogni giorno alle 2:00 AM
- **Retention policy**: 30 giorni per archivio
- **Auto-archiviazione**: Notifiche risolte spostate automaticamente
- **Cleanup manuale**: Endpoint per pulizia on-demand

## 📈 Monitoring e Analytics

- **Statistiche real-time**: Contatori per tipo e stato
- **Trend analysis**: Analisi andamenti nel tempo
- **Dashboard dati**: Overview completo sistema
- **Export capabilities**: CSV, JSON, report statistici

## 🚀 Pronto per Produzione

Il sistema è completamente funzionale e include:
- ✅ Gestione completa ciclo di vita
- ✅ Archiviazione automatica
- ✅ Pulizia schedulata
- ✅ Eventi automatici integrati
- ✅ Ottimizzazioni performance
- ✅ Export e reporting
- ✅ API RESTful complete
- ✅ Real-time streaming

## 🔧 Configurazione

### Variabili Ambiente
```properties
admin.email=${ADMIN_EMAIL:}
admin.token=${ADMIN_TOKEN:FUNKARD_ADMIN_TOKEN_2025}
```

### Database
Le tabelle vengono create automaticamente:
- `admin_notifications`
- `admin_notification_archive`
- `user_notifications`
- `support_tickets`

## 📞 Supporto

Per domande o problemi con il sistema notifiche, contattare il team di sviluppo.

---
*Sistema Notifiche Funkard v1.0 - Enterprise Ready* 🚀
