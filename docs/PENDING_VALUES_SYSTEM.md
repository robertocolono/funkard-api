# ⏳ Sistema Validazione Valori Personalizzati "Altro" - Funkard Backend

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0

---

## 📋 Panoramica

Sistema di validazione per valori personalizzati "Altro" proposti dagli utenti durante la vendita (Sell). I valori TCG e Lingua personalizzati vengono salvati come proposte pending in attesa di approvazione admin prima di essere aggiunti alle liste ufficiali.

---

## ✅ Componenti Implementati

### **1. Modello PendingValue**

**File:** `src/main/java/com/funkard/model/PendingValue.java`

**Campi:**
- `id` (UUID) - Identificativo univoco
- `type` (ENUM: TCG, LANGUAGE) - Tipo valore
- `value` (TEXT) - Valore proposto
- `submittedBy` (User) - Utente che ha proposto
- `createdAt` (TIMESTAMP) - Data creazione
- `approved` (BOOLEAN) - Flag approvazione (default: false)
- `approvedBy` (User, nullable) - Admin che ha approvato
- `approvedAt` (TIMESTAMP, nullable) - Data approvazione

---

### **2. Migration SQL**

**File:** `src/main/resources/db/migration/V16__create_pending_values_table.sql`

**Tabella:**
```sql
CREATE TABLE pending_values (
    id UUID PRIMARY KEY,
    type VARCHAR(20) CHECK (type IN ('TCG', 'LANGUAGE')),
    value TEXT NOT NULL,
    submitted_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved BOOLEAN DEFAULT false,
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP
);
```

---

### **3. Repository**

**File:** `src/main/java/com/funkard/repository/PendingValueRepository.java`

**Metodi:**
- `findByApprovedFalseOrderByCreatedAtDesc()` - Proposte pending
- `findByTypeAndApprovedFalseOrderByCreatedAtDesc(type)` - Proposte per tipo
- `findBySubmittedByIdOrderByCreatedAtDesc(userId)` - Proposte utente
- `findByTypeAndValueIgnoreCase(type, value)` - Verifica duplicati
- `countByTypeAndApprovedFalse(type)` - Conta pending per tipo

---

### **4. Service**

**File:** `src/main/java/com/funkard/service/PendingValueService.java`

**Metodi:**
- `submitPendingValue(type, value, userId)` - Crea proposta
- `approvePendingValue(pendingValueId, adminId)` - Approva proposta
- `rejectPendingValue(pendingValueId, adminId)` - Rifiuta proposta
- `getPendingValues()` - Recupera tutte le pending
- `getPendingValuesByType(type)` - Recupera per tipo
- `getUserPendingValues(userId)` - Recupera per utente
- `countPendingByType(type)` - Conta pending per tipo

**Validazioni:**
- Verifica duplicati (stesso tipo + valore)
- Normalizzazione valore (trim, capitalize)
- Controllo approvazione già esistente

---

### **5. Controller**

**File:** `src/main/java/com/funkard/controller/PendingValueController.java`

**Endpoint:**
- `POST /api/pending-values/submit` - Invia proposta (utenti)
- `GET /api/pending-values` - Lista proposte pending (admin)
- `GET /api/pending-values/my` - Proposte utente corrente
- `POST /api/pending-values/{id}/approve` - Approva proposta (admin)
- `DELETE /api/pending-values/{id}` - Rifiuta proposta (admin)
- `GET /api/pending-values/stats` - Statistiche (admin)

---

### **6. Integrazione con Sell**

**File:** `src/main/java/com/funkard/service/ListingService.java`

**Funzionalità:**
- Quando utente seleziona "Altro" in Sell
- Se `customTcg` o `customLanguage` sono forniti
- Salva automaticamente come proposta pending
- Non blocca creazione listing se proposta fallisce

**File:** `src/main/java/com/funkard/controller/ListingController.java`

**Endpoint aggiornato:**
- `POST /api/listings` - Crea listing con gestione valori "Altro"

---

## 📝 DTO

### **SubmitPendingValueRequest**
```java
{
  "type": "TCG" | "LANGUAGE",
  "value": "Nome valore personalizzato"
}
```

### **PendingValueDTO**
```java
{
  "id": "uuid",
  "type": "TCG",
  "value": "Pokemon",
  "submittedById": 123,
  "submittedByEmail": "user@example.com",
  "createdAt": "2025-01-15T10:30:00",
  "approved": false,
  "approvedById": null,
  "approvedByEmail": null,
  "approvedAt": null
}
```

### **CreateListingRequest**
```java
{
  "title": "Carta Pokemon",
  "description": "...",
  "price": 99.99,
  "condition": "MINT",
  "tcg": "Altro",
  "customTcg": "Pokemon",
  "language": "Altro",
  "customLanguage": "Italiano"
}
```

---

## 🔄 Flusso Completo

### **1. Utente Crea Vendita con "Altro":**

```
1. Utente seleziona "Altro" in TCG o Lingua
2. Inserisce valore personalizzato (es. "Pokemon")
3. POST /api/listings con CreateListingRequest
   {
     "tcg": "Altro",
     "customTcg": "Pokemon",
     ...
   }
4. ListingService.create() rileva "Altro"
5. PendingValueService.submitPendingValue() salva proposta
6. Proposta salvata con approved = false
7. Listing creata normalmente
```

### **2. Admin Approva Proposta:**

```
1. Admin visualizza proposte pending
   GET /api/pending-values?type=TCG
2. Admin approva proposta
   POST /api/pending-values/{id}/approve
3. PendingValueService.approvePendingValue()
4. approved = true, approvedBy = adminId, approvedAt = now()
5. TODO: Aggiungere valore alle liste ufficiali TCG/Lingua
```

---

## 📊 Endpoint API

### **POST /api/pending-values/submit**
**Request:**
```json
{
  "type": "TCG",
  "value": "Pokemon"
}
```

**Response (201):**
```json
{
  "id": "uuid",
  "type": "TCG",
  "value": "Pokemon",
  "submittedById": 123,
  "submittedByEmail": "user@example.com",
  "createdAt": "2025-01-15T10:30:00",
  "approved": false
}
```

**Errori:**
- `400` - Valore vuoto o tipo non valido
- `409` - Proposta identica già esistente o già approvata

---

### **GET /api/pending-values**
**Query Params:**
- `type` (opzionale) - Filtra per tipo (TCG, LANGUAGE)

**Response:**
```json
[
  {
    "id": "uuid",
    "type": "TCG",
    "value": "Pokemon",
    "submittedById": 123,
    "submittedByEmail": "user@example.com",
    "createdAt": "2025-01-15T10:30:00",
    "approved": false
  }
]
```

**Accesso:** Solo ADMIN, SUPER_ADMIN, SUPERVISOR

---

### **GET /api/pending-values/my**
**Response:**
```json
[
  {
    "id": "uuid",
    "type": "TCG",
    "value": "Pokemon",
    "createdAt": "2025-01-15T10:30:00",
    "approved": false
  }
]
```

**Accesso:** Utente autenticato (solo proprie proposte)

---

### **POST /api/pending-values/{id}/approve**
**Response:**
```json
{
  "id": "uuid",
  "type": "TCG",
  "value": "Pokemon",
  "approved": true,
  "approvedById": 456,
  "approvedByEmail": "admin@funkard.com",
  "approvedAt": "2025-01-15T11:00:00"
}
```

**Accesso:** Solo ADMIN, SUPER_ADMIN, SUPERVISOR

---

### **DELETE /api/pending-values/{id}**
**Response:**
```json
{
  "success": true,
  "message": "Proposta rifiutata"
}
```

**Accesso:** Solo ADMIN, SUPER_ADMIN, SUPERVISOR

---

### **GET /api/pending-values/stats**
**Response:**
```json
{
  "pendingTcg": 5,
  "pendingLanguage": 3,
  "totalPending": 8
}
```

**Accesso:** Solo ADMIN, SUPER_ADMIN, SUPERVISOR

---

## 🔄 Integrazione con Sell

### **POST /api/listings**
**Request:**
```json
{
  "title": "Carta Pokemon",
  "description": "Carta rara",
  "price": 99.99,
  "condition": "MINT",
  "tcg": "Altro",
  "customTcg": "Pokemon",
  "language": "Altro",
  "customLanguage": "Italiano"
}
```

**Comportamento:**
1. Se `tcg = "Altro"` e `customTcg` è fornito → salva proposta TCG
2. Se `language = "Altro"` e `customLanguage` è fornito → salva proposta LANGUAGE
3. Crea listing normalmente
4. Se salvataggio proposta fallisce, non blocca creazione listing

---

## ✅ Checklist Implementazione

### **Database:**
- [x] Migration V16: Tabella pending_values
- [x] Indici per performance
- [x] Foreign keys e constraints

### **Modelli:**
- [x] PendingValue con enum ValueType
- [x] Relazioni con User (submittedBy, approvedBy)

### **Repository:**
- [x] PendingValueRepository con query custom
- [x] Metodi per filtri e statistiche

### **Service:**
- [x] PendingValueService con validazioni
- [x] Gestione duplicati
- [x] Normalizzazione valori
- [x] Integrazione con ListingService

### **Controller:**
- [x] PendingValueController con endpoint completi
- [x] ListingController aggiornato
- [x] Autenticazione e autorizzazione

### **DTO:**
- [x] PendingValueDTO
- [x] SubmitPendingValueRequest
- [x] CreateListingRequest

---

## 🚀 Risultato Finale

✅ **Tabella `pending_values` creata e funzionante**  
✅ **Valori "Altro" salvati automaticamente durante Sell**  
✅ **Sistema approvazione admin implementato**  
✅ **Endpoint API completi per gestione proposte**  
✅ **Integrazione con flusso vendita funzionante**  
✅ **Validazioni e controlli duplicati implementati**

---

## 📝 Note Future

- **TODO:** Aggiungere valore approvato alle liste ufficiali TCG/Lingua
- **TODO:** Notifica email utente quando proposta approvata/rifiutata
- **TODO:** Dashboard admin per gestione proposte
- **TODO:** Statistiche avanzate (proposte per utente, tasso approvazione, ecc.)

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

