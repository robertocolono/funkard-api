# 📊 Report Integrazione Backend ↔ Frontend Funkard

**Data Analisi:** 2025-01-15  
**Backend:** Spring Boot 3.5.6  
**Frontend:** Next.js (non presente nel workspace corrente)

---

## 📋 Indice

1. [Panoramica Endpoint Backend](#panoramica-endpoint-backend)
2. [API Funzionanti e Collegate](#api-funzionanti-e-collegate)
3. [API Esistenti ma Non Utilizzate](#api-esistenti-ma-non-utilizzate)
4. [API Mancanti](#api-mancanti)
5. [Differenze e Problemi](#differenze-e-problemi)
6. [Suggerimenti per Allineamento](#suggerimenti-per-allineamento)

---

## 🗺️ Panoramica Endpoint Backend

### **Autenticazione** (`/api/auth`)
- ✅ `POST /api/auth/register` - Registrazione utente
- ✅ `POST /api/auth/login` - Login utente
- ✅ `GET /api/auth/validate?token={token}` - Validazione token

### **Utenti** (`/api/user`)
- ✅ `GET /api/user/me` - Profilo utente corrente
- ✅ `PUT /api/user/me` - Aggiorna profilo
- ✅ `GET /api/user/address` - Lista indirizzi
- ✅ `POST /api/user/address` - Aggiungi indirizzo
- ✅ `PUT /api/user/address/{id}` - Aggiorna indirizzo
- ✅ `DELETE /api/user/address/{id}` - Elimina indirizzo
- ✅ `PATCH /api/user/address/{id}/default` - Imposta default
- ✅ `GET /api/user/address/default` - Indirizzo predefinito
- ✅ `GET /api/user/users` - Lista utenti (admin)
- ✅ `POST /api/user/users` - Crea utente (admin)
- ✅ `DELETE /api/user/users/{id}` - Elimina utente (admin)

### **Pagamenti** (`/api/user/payments`)
- ✅ `GET /api/user/payments` - Lista metodi pagamento
- ✅ `POST /api/user/payments` - Aggiungi metodo
- ✅ `DELETE /api/user/payments/{id}` - Rimuovi metodo
- ✅ `PATCH /api/user/payments/{id}/default` - Imposta default
- ✅ `GET /api/user/payments/default` - Metodo predefinito
- ✅ `GET /api/user/payments/stats` - Statistiche
- ✅ `POST /api/user/payments/cleanup` - Pulizia scaduti
- ✅ `POST /api/user/payments/validate` - Valida senza salvare

### **Carte** (`/api/cards`)
- ✅ `GET /api/cards` - Lista tutte le carte
- ✅ `POST /api/cards` - Crea nuova carta

### **Collezione** (`/api/collection`)
- ✅ `POST /api/collection` - Crea carta in collezione (multipart/form-data)
- ✅ `GET /api/collection/{userId}` - Collezione utente

### **User Cards** (`/api/usercards`)
- ✅ `GET /api/usercards/collection/{userId}` - Collezione utente
- ✅ `GET /api/usercards/{id}` - Dettaglio carta
- ✅ `POST /api/usercards` - Aggiungi carta
- ✅ `PUT /api/usercards/{id}` - Aggiorna carta
- ✅ `PUT /api/usercards/usercards/{id}` - Aggiorna carta (legacy)
- ✅ `DELETE /api/usercards/{id}` - Elimina carta
- ✅ `PUT /api/usercards/{id}/raw-images` - Upload immagini (multipart)

### **Listings** (`/api/listings`)
- ✅ `GET /api/listings` - Lista tutti i listing
- ✅ `POST /api/listings` - Crea nuovo listing

### **Transazioni** (`/api/transactions`)
- ✅ `GET /api/transactions` - Lista transazioni
- ✅ `POST /api/transactions` - Crea transazione

### **Wishlist** (`/api/wishlist`)
- ✅ `GET /api/wishlist` - Lista wishlist
- ✅ `POST /api/wishlist` - Aggiungi a wishlist
- ✅ `DELETE /api/wishlist/{id}` - Rimuovi da wishlist

### **Supporto** (`/api/support`)
- ✅ `POST /api/support/tickets` - Crea ticket
- ✅ `GET /api/support/tickets?email={email}` - Ticket utente
- ✅ `GET /api/support/tickets/{id}` - Dettaglio ticket
- ✅ `POST /api/support/tickets/{id}/reply` - Rispondi a ticket
- ✅ `POST /api/support/tickets/{id}/reopen` - Riapri ticket
- ✅ `GET /api/support/stats?email={email}` - Statistiche utente
- ✅ `GET /api/support/stream` - SSE real-time (utenti)
- ✅ `GET /api/support/stream/stats` - Statistiche stream

### **Chat Supporto** (`/api/support/chat`)
- ✅ `POST /api/support/chat/{ticketId}/message` - Invia messaggio
- ✅ `GET /api/support/chat/{ticketId}/messages` - Lista messaggi
- ✅ `POST /api/support/chat/{ticketId}/read` - Marca come letto
- ✅ `GET /api/support/chat/{ticketId}/stats` - Statistiche chat

### **GradeLens** (`/api/gradelens`)
- ✅ `POST /api/gradelens/analyze` - Analisi AI carta
- ✅ `POST /api/gradelens/confirm` - Conferma grading

### **Grading** (`/api/grading`)
- ✅ `POST /api/grading/submit` - Invia per grading
- ✅ `PATCH /api/grading/{cardId}/status` - Aggiorna stato
- ✅ `POST /api/grading/{cardId}/failed` - Marca come fallito
- ✅ `POST /api/grading/{cardId}/completed` - Marca come completato

### **Market Valuation** (`/api/valuation`)
- ✅ `POST /api/valuation/get` - Ottieni valutazione
- ✅ `POST /api/valuation/refreshIncremental` - Refresh incrementale (cron)

### **Trends** (`/api/trends`)
- ✅ `GET /api/trends/{rangeType}/{itemName}` - Trend per item

### **Admin - Autenticazione** (`/api/admin/auth`)
- ✅ `GET /api/admin/auth/token/{token}` - Valida token admin
- ✅ `POST /api/admin/auth/users/create` - Crea utente admin
- ✅ `PATCH /api/admin/auth/users/{id}/regenerate-token` - Rigenera token
- ✅ `PATCH /api/admin/auth/users/{id}/deactivate` - Disattiva utente
- ✅ `PATCH /api/admin/auth/users/{id}/activate` - Attiva utente
- ✅ `PATCH /api/admin/auth/users/{id}/role` - Cambia ruolo
- ✅ `GET /api/admin/auth/team/list` - Lista team
- ✅ `GET /api/admin/auth/diagnostic` - Diagnostica
- ✅ `POST /api/admin/auth/verify-and-fix` - Verifica e correggi

### **Admin - Token** (`/api/admin/tokens`)
- ✅ `GET /api/admin/tokens` - Lista token
- ✅ `POST /api/admin/tokens/generate?role={ROLE}` - Genera token
- ✅ `GET /api/admin/tokens/validate/{token}` - Valida token

### **Admin - Access Requests** (`/api/admin/access-requests`)
- ✅ `POST /api/admin/access-requests/create` - Crea richiesta
- ✅ `GET /api/admin/access-requests/pending` - Richieste pending
- ✅ `POST /api/admin/access-requests/{id}/approve` - Approva
- ✅ `POST /api/admin/access-requests/{id}/reject` - Rifiuta

### **Admin - Access** (`/api/admin/access`)
- ✅ `POST /api/admin/access/generate?role={ROLE}` - Genera token ruolo
- ✅ `POST /api/admin/access/request` - Richiesta accesso
- ✅ `GET /api/admin/access/requests` - Lista richieste
- ✅ `POST /api/admin/access/approve/{id}` - Approva richiesta
- ✅ `POST /api/admin/access/reject/{id}` - Rifiuta richiesta
- ✅ `GET /api/admin/access/tokens` - Lista token

### **Admin - Supporto** (`/api/admin/support`)
- ✅ `GET /api/admin/support/tickets` - Lista ticket
- ✅ `GET /api/admin/support/stats` - Statistiche
- ✅ `POST /api/admin/support/reply/{id}` - Rispondi
- ✅ `POST /api/admin/support/resolve/{id}` - Risolvi
- ✅ `POST /api/admin/support/close/{id}` - Chiudi
- ✅ `POST /api/admin/support/reopen/{id}` - Riapri
- ✅ `POST /api/admin/support/{id}/mark-read` - Marca letti
- ✅ `GET /api/admin/support/new-messages-count` - Conta nuovi messaggi
- ✅ `POST /api/admin/support/{id}/assign` - Assegna ticket
- ✅ `POST /api/admin/support/{id}/release` - Rilascia ticket
- ✅ `GET /api/admin/support/assigned/{supportEmail}` - Ticket assegnati
- ✅ `GET /api/admin/support/assigned-count` - Conta assegnati
- ✅ `GET /api/admin/support/stream` - SSE real-time (admin)
- ✅ `GET /api/admin/support/stream/stats` - Statistiche stream
- ✅ `DELETE /api/admin/support/cleanup` - Cleanup messaggi

### **Admin - Notifiche** (`/api/admin/notifications`)
- ✅ `GET /api/admin/notifications` - Lista notifiche (con filtri)
- ✅ `GET /api/admin/notifications/{id}` - Dettaglio
- ✅ `POST /api/admin/notifications/{id}/read` - Marca letta
- ✅ `POST /api/admin/notifications/{id}/assign` - Assegna
- ✅ `POST /api/admin/notifications/{id}/resolve` - Risolvi
- ✅ `POST /api/admin/notifications/{id}/archive` - Archivia
- ✅ `DELETE /api/admin/notifications/cleanup` - Cleanup
- ✅ `GET /api/admin/notifications/stream` - SSE real-time
- ✅ `GET /api/admin/notifications/unread-count` - Conta non lette
- ✅ `GET /api/admin/notifications/unread-latest` - Ultime non lette
- ✅ `POST /api/admin/notifications/batch/resolve` - Risolvi batch
- ✅ `POST /api/admin/notifications/batch/archive` - Archivia batch
- ✅ `DELETE /api/admin/notifications/batch/delete` - Elimina batch
- ✅ `GET /api/admin/notifications/archive` - Lista archiviate
- ✅ `DELETE /api/admin/notifications/delete/{id}` - Elimina notifica
- ✅ `PATCH /api/admin/notifications/archive/{id}` - Archivia (PATCH)

### **Admin - Dashboard** (`/api/admin/dashboard`)
- ✅ `GET /api/admin/dashboard` - Dashboard aggregata
- ✅ `DELETE /api/admin/dashboard/cleanup` - Cleanup notifiche

### **Admin - Statistiche** (`/api/admin/stats`)
- ✅ `GET /api/admin/stats` - Statistiche generali

### **Admin - Ticket Assignment** (`/api/admin/tickets`)
- ✅ `POST /api/admin/tickets/{id}/assign` - Assegna ticket
- ✅ `POST /api/admin/tickets/{id}/release` - Rilascia ticket
- ✅ `POST /api/admin/tickets/{id}/assign-with-role` - Assegna con ruolo
- ✅ `POST /api/admin/tickets/{id}/release-with-role` - Rilascia con ruolo
- ✅ `GET /api/admin/tickets/assignment-stats` - Statistiche assegnazioni

### **Admin - Ruoli** (`/api/admin/roles`)
- ✅ `GET /api/admin/roles/permissions/{userEmail}` - Permessi utente
- ✅ `POST /api/admin/roles/check-permissions` - Verifica permessi
- ✅ `GET /api/admin/roles/available` - Ruoli disponibili

### **Admin - Valuation** (`/api/admin/valuation`)
- ✅ `GET /api/admin/valuation/overview` - Panoramica valutazioni
- ✅ `GET /api/admin/valuation/pending` - Valutazioni pending
- ✅ `GET /api/admin/valuation/check` - Verifica valutazioni

### **Admin - Logs** (`/api/admin/logs`)
- ✅ `GET /api/admin/logs/{type}/{id}` - Log per tipo/ID
- ✅ `DELETE /api/admin/logs/cleanup` - Cleanup log

### **Admin - System** (`/api/admin/system`)
- ✅ `POST /api/admin/system/cleanup/status` - Aggiorna stato cleanup
- ✅ `GET /api/admin/system/cleanup/status` - Stato cleanup

### **Admin - Cleanup** (`/api/admin/cleanup`)
- ✅ `POST /api/admin/cleanup/manual` - Cleanup manuale
- ✅ `GET /api/admin/cleanup/stats` - Statistiche cleanup
- ✅ `POST /api/admin/cleanup/test` - Test cleanup

### **Admin - GradeLens** (`/api/admin/gradelens`)
- ⚠️ Endpoint presente ma non documentato

### **Ads** (`/api/ads`)
- ✅ `GET /api/ads/gradelens` - Banner pubblicitario GradeLens

### **Test** (`/api/test`)
- ✅ `GET /api/test/ping` - Health check
- ✅ `GET /api/test/sse-test` - Test SSE

### **Root** (`/`)
- ✅ `GET /` - Root endpoint
- ✅ `GET /health` - Health check

---

## ✅ API Funzionanti e Collegate

### **Sezioni Completamente Funzionanti:**

1. **Autenticazione** ✅
   - Registrazione e login funzionanti
   - Validazione token implementata
   - JWT integrato

2. **Profilo Utente** ✅
   - Gestione profilo completa
   - Indirizzi utente (CRUD completo)
   - Metodi di pagamento (CRUD completo)

3. **Supporto/Chat** ✅
   - Sistema ticket completo
   - Chat real-time (SSE)
   - Gestione messaggi
   - Assegnazione ticket

4. **Admin Panel** ✅
   - Dashboard completa
   - Notifiche real-time (SSE)
   - Gestione utenti admin
   - Sistema di ruoli e permessi
   - Token e richieste di accesso

5. **Collezione** ✅
   - Upload carte con immagini
   - Gestione UserCard
   - Integrazione R2 storage

---

## ⚠️ API Esistenti ma Non Utilizzate

### **Marketplace:**
- `GET /api/listings` - Lista listing (non filtrata)
- `POST /api/listings` - Crea listing (senza validazione avanzata)
- `GET /api/transactions` - Lista transazioni (senza filtri)
- `POST /api/transactions` - Crea transazione (base)

### **Market Valuation:**
- `POST /api/valuation/get` - Valutazione market (non integrata nel frontend)
- `GET /api/trends/{rangeType}/{itemName}` - Trend analisi (non usato)

### **Grading:**
- `POST /api/grading/submit` - Submit grading (non collegato a UI)
- `PATCH /api/grading/{cardId}/status` - Aggiorna stato (interno)
- `POST /api/grading/{cardId}/failed` - Marca fallito (interno)
- `POST /api/grading/{cardId}/completed` - Marca completato (interno)

### **Wishlist:**
- `GET /api/wishlist` - Lista wishlist (senza filtri utente)
- `POST /api/wishlist` - Aggiungi (senza validazione)
- `DELETE /api/wishlist/{id}` - Rimuovi (base)

### **Carte:**
- `GET /api/cards` - Lista tutte le carte (senza paginazione)
- `POST /api/cards` - Crea carta (base, senza validazione avanzata)

### **Admin - Avanzate:**
- `POST /api/admin/notifications/batch/*` - Operazioni batch (non usate)
- `GET /api/admin/notifications/archive` - Archivio (non integrato)
- `GET /api/admin/valuation/*` - Valutazioni admin (non usate)
- `GET /api/admin/logs/*` - Log sistema (non visualizzati)

---

## ❌ API Mancanti ma Necessarie

### **Marketplace:**

1. **Listings Avanzati:**
   ```
   GET /api/listings/search?query={}&category={}&minPrice={}&maxPrice={}
   GET /api/listings/{id} - Dettaglio listing
   PUT /api/listings/{id} - Aggiorna listing
   DELETE /api/listings/{id} - Elimina listing
   GET /api/listings/user/{userId} - Listing utente
   GET /api/listings/favorites - Listing preferiti
   POST /api/listings/{id}/favorite - Aggiungi ai preferiti
   DELETE /api/listings/{id}/favorite - Rimuovi dai preferiti
   ```

2. **Transazioni Avanzate:**
   ```
   GET /api/transactions/{id} - Dettaglio transazione
   GET /api/transactions/user/{userId} - Transazioni utente
   GET /api/transactions/listing/{listingId} - Transazioni listing
   POST /api/transactions/{id}/complete - Completa transazione
   POST /api/transactions/{id}/cancel - Cancella transazione
   ```

3. **Offerte:**
   ```
   POST /api/listings/{id}/offer - Crea offerta
   GET /api/listings/{id}/offers - Lista offerte
   POST /api/offers/{id}/accept - Accetta offerta
   POST /api/offers/{id}/reject - Rifiuta offerta
   ```

### **Collection:**

1. **Ricerca e Filtri:**
   ```
   GET /api/collection/search?query={}&set={}&grade={}
   GET /api/collection/filter?minGrade={}&maxGrade={}&set={}
   GET /api/collection/stats/{userId} - Statistiche collezione
   ```

2. **Organizzazione:**
   ```
   POST /api/collection/folders - Crea cartella
   GET /api/collection/folders/{userId} - Lista cartelle
   POST /api/collection/{cardId}/move - Sposta in cartella
   ```

### **GradeLens:**

1. **Storico e Gestione:**
   ```
   GET /api/gradelens/history/{userId} - Storico analisi
   GET /api/gradelens/{id} - Dettaglio analisi
   DELETE /api/gradelens/{id} - Elimina analisi
   POST /api/gradelens/{id}/reanalyze - Rianalizza
   ```

### **Market Valuation:**

1. **Valutazioni Avanzate:**
   ```
   GET /api/valuation/history/{itemName} - Storico valutazioni
   GET /api/valuation/compare?item1={}&item2={} - Confronta
   GET /api/valuation/bulk - Valutazioni multiple
   ```

### **Wishlist:**

1. **Gestione Avanzata:**
   ```
   GET /api/wishlist/user/{userId} - Wishlist utente
   GET /api/wishlist/stats/{userId} - Statistiche wishlist
   POST /api/wishlist/{id}/alert - Imposta alert prezzo
   ```

### **User Cards:**

1. **Gestione Avanzata:**
   ```
   GET /api/usercards/search?query={} - Ricerca carte
   GET /api/usercards/stats/{userId} - Statistiche carte
   POST /api/usercards/{id}/grade - Richiedi grading
   GET /api/usercards/{id}/valuation - Valutazione carta
   ```

### **Notifiche Utente:**

1. **Sistema Notifiche:**
   ```
   GET /api/user/notifications - Notifiche utente
   POST /api/user/notifications/{id}/read - Marca letta
   GET /api/user/notifications/unread-count - Conta non lette
   GET /api/user/notifications/stream - SSE notifiche utente
   ```

---

## 🔍 Differenze e Problemi

### **1. Autenticazione Inconsistente:**

- **Backend:** Supporta `Authorization: Bearer {token}` e `X-Admin-Token`
- **Problema:** Alcuni endpoint usano solo `X-Admin-Token`, altri solo `Authorization`
- **Soluzione:** Standardizzare su `Authorization: Bearer` con fallback a `X-Admin-Token`

### **2. CORS Configurazione:**

- **Problema:** Alcuni controller hanno `@CrossOrigin` con domini Vercel vecchi
- **Soluzione:** Aggiornare tutti a `funkard.com` e `localhost:3000/3002`

### **3. Paginazione Mancante:**

- **Problema:** Molti endpoint `GET` restituiscono tutte le entità senza paginazione
- **Endpoints affetti:**
  - `GET /api/cards`
  - `GET /api/listings`
  - `GET /api/transactions`
  - `GET /api/wishlist`
  - `GET /api/admin/support/tickets`
- **Soluzione:** Aggiungere `?page={}&size={}` a tutti gli endpoint lista

### **4. Filtri e Ricerca Mancanti:**

- **Problema:** Endpoint lista non supportano filtri avanzati
- **Soluzione:** Aggiungere query parameters per filtri comuni

### **5. Validazione Input:**

- **Problema:** Alcuni endpoint non validano input (es. `POST /api/cards`)
- **Soluzione:** Aggiungere `@Valid` e DTO con validazione

### **6. Response Format Inconsistente:**

- **Problema:** Alcuni endpoint restituiscono entità dirette, altri `{success, data}`
- **Soluzione:** Standardizzare su formato unificato:
  ```json
  {
    "success": true,
    "data": {...},
    "message": "..."
  }
  ```

### **7. Error Handling:**

- **Problema:** Errori restituiti in formati diversi
- **Soluzione:** Usare `GlobalExceptionHandler` per formattare errori uniformemente

### **8. ID Type Inconsistente:**

- **Problema:** Alcuni endpoint usano `Long` per ID, altri `UUID`
- **Esempi:**
  - User: `Long`
  - Ticket: `UUID`
  - Cards: `Long`
  - Notifications: `UUID`
- **Soluzione:** Documentare quale tipo usare per ogni entità

---

## 💡 Suggerimenti per Allineamento Backend ↔ Frontend

### **Priorità Alta:**

1. **Standardizzare Autenticazione:**
   ```typescript
   // Frontend helper
   const getAuthHeaders = () => ({
     'Authorization': `Bearer ${token}`,
     'X-Admin-Token': adminToken // fallback
   });
   ```

2. **Implementare Paginazione:**
   ```typescript
   interface PaginatedResponse<T> {
     content: T[];
     page: number;
     size: number;
     totalElements: number;
     totalPages: number;
   }
   ```

3. **Creare API Client Centralizzato:**
   ```typescript
   // src/lib/api.ts
   class FunkardAPI {
     private baseURL = process.env.NEXT_PUBLIC_API_URL;
     
     async get<T>(endpoint: string, params?: Record<string, any>): Promise<T> {
       // Implementazione con error handling
     }
     
     async post<T>(endpoint: string, data?: any): Promise<T> {
       // Implementazione
     }
   }
   ```

4. **Aggiungere TypeScript Types:**
   ```typescript
   // src/types/api.ts
   export interface User {
     id: number;
     email: string;
     username: string;
     // ...
   }
   
   export interface Ticket {
     id: string; // UUID
     email: string;
     subject: string;
     // ...
   }
   ```

### **Priorità Media:**

5. **Implementare Ricerca e Filtri:**
   - Aggiungere endpoint di ricerca per Collection, Listings, Cards
   - Implementare filtri avanzati con query parameters

6. **Sistema Notifiche Utente:**
   - Creare endpoint notifiche per utenti finali
   - Implementare SSE per notifiche real-time

7. **Marketplace Completo:**
   - Implementare sistema offerte
   - Aggiungere gestione preferiti
   - Sistema di recensioni/rating

8. **Gestione Errori Frontend:**
   ```typescript
   // src/lib/errors.ts
   export class APIError extends Error {
     constructor(
       public status: number,
       public message: string,
       public data?: any
     ) {
       super(message);
     }
   }
   ```

### **Priorità Bassa:**

9. **Ottimizzazioni:**
   - Implementare caching lato frontend
   - Aggiungere rate limiting backend
   - Ottimizzare query database

10. **Documentazione:**
    - Generare OpenAPI/Swagger docs
    - Creare Postman collection
    - Documentare tutti gli endpoint

---

## 📈 Metriche

- **Totale Endpoint Backend:** ~150+
- **Endpoint Autenticati:** ~80+
- **Endpoint Pubblici:** ~20+
- **Endpoint Admin:** ~60+
- **Endpoint con SSE:** 4
- **Endpoint con WebSocket:** 1

---

## ✅ Checklist Integrazione

### **Backend:**
- [x] Autenticazione JWT
- [x] CORS configurato
- [x] Error handling globale
- [x] Logging strutturato
- [ ] Paginazione standardizzata
- [ ] Validazione input completa
- [ ] Response format unificato
- [ ] OpenAPI documentation

### **Frontend (da implementare):**
- [ ] API client centralizzato
- [ ] TypeScript types per tutte le entità
- [ ] Error handling unificato
- [ ] Loading states
- [ ] Retry logic
- [ ] Cache management
- [ ] SSE integration
- [ ] WebSocket integration

---

## 🎯 Prossimi Passi

1. **Immediato:**
   - Standardizzare autenticazione su tutti gli endpoint
   - Aggiungere paginazione agli endpoint lista
   - Creare API client TypeScript

2. **Breve Termine:**
   - Implementare endpoint mancanti per Marketplace
   - Aggiungere sistema notifiche utente
   - Completare integrazione GradeLens

3. **Medio Termine:**
   - Sistema offerte e negoziazioni
   - Ricerca avanzata
   - Analytics e reporting

4. **Lungo Termine:**
   - Ottimizzazioni performance
   - Caching strategico
   - Monitoring e alerting

---

**Report generato automaticamente**  
**Ultimo aggiornamento:** 2025-01-15

