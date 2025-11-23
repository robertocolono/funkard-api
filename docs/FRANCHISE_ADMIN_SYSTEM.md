# 📚 Sistema Completo Gestione Franchise - Funkard Backend

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0

---

## 📋 Panoramica

Sistema completo per gestione franchise e proposte, con sincronizzazione automatica tra database e file JSON. Gli admin possono approvare/rifiutare proposte, abilitare/disabilitare franchise e creare nuovi franchise manualmente.

---

## ✅ Componenti Implementati

### **1. Modelli Database**

#### **Franchise**
**File:** `src/main/java/com/funkard/model/Franchise.java`

**Campi:**
- `id` (Long, PK)
- `category` (String, not null)
- `name` (String, unique)
- `status` (enum: ACTIVE, DISABLED)
- `createdAt`, `updatedAt`

#### **FranchiseProposal**
**File:** `src/main/java/com/funkard/model/FranchiseProposal.java`

**Campi:**
- `id` (Long, PK)
- `category` (String)
- `franchise` (String)
- `userEmail` (String, opzionale)
- `userId` (User, opzionale)
- `status` (enum: PENDING, APPROVED, REJECTED)
- `processedBy` (User, opzionale)
- `processedAt` (LocalDateTime, opzionale)
- `createdAt`

---

### **2. Migration SQL**

#### **V19: Tabella franchises**
```sql
CREATE TABLE franchises (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **V20: Tabella franchise_proposals**
```sql
CREATE TABLE franchise_proposals (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    franchise VARCHAR(100) NOT NULL,
    user_email VARCHAR(255) NULL,
    user_id BIGINT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by BIGINT NULL REFERENCES users(id),
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

### **3. Repository**

- `FranchiseRepository` - Query per franchise
- `FranchiseProposalRepository` - Query per proposte

---

### **4. Service**

**File:** `src/main/java/com/funkard/service/FranchiseAdminService.java`

**Metodi:**
- `getAllFranchisesAndProposals(status)` - Recupera tutto con filtri
- `approveProposal(proposalId, adminId)` - Approva proposta
- `rejectProposal(proposalId, adminId)` - Rifiuta proposta
- `disableFranchise(franchiseId, adminId)` - Disabilita franchise
- `enableFranchise(franchiseId, adminId)` - Riabilita franchise
- `createFranchise(category, name, adminId)` - Crea manualmente
- `createProposal(category, franchise, userEmail, userId)` - Crea proposta

**Sincronizzazione JSON:**
- Aggiorna cache JSON automaticamente
- Aggiunge/rimuove franchise in base allo stato
- Ricarica cache dopo ogni modifica

---

### **5. Controller Admin**

**File:** `src/main/java/com/funkard/admin/controller/FranchiseAdminController.java`

**Endpoint:**
- `GET /api/admin/franchises` - Lista proposte e franchise
- `POST /api/admin/franchises/approve/{proposalId}` - Approva proposta
- `POST /api/admin/franchises/reject/{proposalId}` - Rifiuta proposta
- `PATCH /api/admin/franchises/{id}/disable` - Disabilita franchise
- `PATCH /api/admin/franchises/{id}/enable` - Riabilita franchise
- `POST /api/admin/franchises/add` - Crea franchise manualmente

**Sicurezza:**
- `@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR', 'ADMIN')")`
- Autenticazione JWT obbligatoria

---

## 📊 Endpoint API

### **GET /api/admin/franchises**
**Query Params:**
- `status` (opzionale) - Filtra per stato (pending, active, disabled)

**Response:**
```json
{
  "proposals": [
    {
      "id": 1,
      "category": "Sportive",
      "franchise": "NBA Prizm",
      "userEmail": "user@example.com",
      "status": "PENDING",
      "createdAt": "2025-01-15T10:30:00"
    }
  ],
  "franchises": [
    {
      "id": 1,
      "category": "TCG",
      "name": "Pokémon",
      "active": true
    }
  ],
  "stats": {
    "totalProposals": 5,
    "pendingProposals": 3,
    "totalFranchises": 20,
    "activeFranchises": 18,
    "disabledFranchises": 2
  }
}
```

---

### **POST /api/admin/franchises/approve/{proposalId}**
**Comportamento:**
1. Cambia status proposta a APPROVED
2. Crea nuovo record in Franchise (status: ACTIVE)
3. Aggiorna cache JSON (aggiunge franchise)
4. Invia notifica admin

**Response:**
```json
{
  "success": true,
  "message": "Franchise 'NBA Prizm' approvato e aggiunto al catalogo",
  "franchise": {
    "id": 21,
    "category": "Sportive",
    "name": "NBA Prizm",
    "active": true
  }
}
```

---

### **POST /api/admin/franchises/reject/{proposalId}**
**Comportamento:**
1. Imposta status=REJECTED
2. Invia notifica admin
3. TODO: Invia email utente (se userEmail presente)

**Response:**
```json
{
  "success": true,
  "message": "Proposta rifiutata"
}
```

---

### **PATCH /api/admin/franchises/{id}/disable**
**Comportamento:**
1. Aggiorna status=DISABLED
2. Rimuove da cache JSON (non visibile pubblicamente)
3. Invia notifica admin

**Response:**
```json
{
  "success": true,
  "message": "Franchise 'MetaZoo' disabilitato",
  "franchise": {
    "id": 6,
    "category": "TCG",
    "name": "MetaZoo",
    "active": false
  }
}
```

---

### **PATCH /api/admin/franchises/{id}/enable**
**Comportamento:**
1. Aggiorna status=ACTIVE
2. Aggiunge a cache JSON (visibile pubblicamente)
3. Invia notifica admin

**Response:**
```json
{
  "success": true,
  "message": "Franchise 'MetaZoo' riabilitato",
  "franchise": {
    "id": 6,
    "category": "TCG",
    "name": "MetaZoo",
    "active": true
  }
}
```

---

### **POST /api/admin/franchises/add**
**Request:**
```json
{
  "category": "TCG / Anime",
  "name": "Bleach Card Game"
}
```

**Comportamento:**
1. Crea franchise nel DB (status: ACTIVE)
2. Aggiorna cache JSON
3. Invia notifica admin

**Response (201):**
```json
{
  "success": true,
  "message": "Franchise 'Bleach Card Game' creato con successo",
  "franchise": {
    "id": 22,
    "category": "TCG / Anime",
    "name": "Bleach Card Game",
    "active": true
  }
}
```

---

## 🔄 Flusso Completo

### **1. Utente Propone Franchise:**

```
1. POST /api/franchises/propose
   {
     "category": "Sportive",
     "franchise": "NBA Prizm"
   }
2. FranchiseAdminService.createProposal()
3. Proposta salvata con status=PENDING
4. Notifica admin inviata
```

### **2. Admin Approva Proposta:**

```
1. GET /api/admin/franchises?status=pending
   → Vede proposta "NBA Prizm"
2. POST /api/admin/franchises/approve/1
3. Proposta → status=APPROVED
4. Franchise creato → status=ACTIVE
5. Cache JSON aggiornata (aggiunto "NBA Prizm" in "Sportive")
6. GET /api/franchises → include "NBA Prizm"
```

### **3. Admin Disabilita Franchise:**

```
1. PATCH /api/admin/franchises/6/disable
2. Franchise → status=DISABLED
3. Cache JSON aggiornata (rimosso "MetaZoo")
4. GET /api/franchises → non include "MetaZoo"
```

### **4. Admin Riabilita Franchise:**

```
1. PATCH /api/admin/franchises/6/enable
2. Franchise → status=ACTIVE
3. Cache JSON aggiornata (aggiunto "MetaZoo")
4. GET /api/franchises → include "MetaZoo"
```

---

## 🔄 Sincronizzazione JSON

### **Meccanismo:**
- Cache in memoria (`FranchiseJsonService.cachedFranchises`)
- Aggiornata automaticamente dopo ogni modifica
- `GET /api/franchises` legge dalla cache (veloce)
- File fisico non modificato (solo in produzione con percorso scrivibile)

### **Operazioni:**
- **Approvazione** → Aggiunge a cache JSON
- **Disabilitazione** → Rimuove da cache JSON
- **Riabilitazione** → Aggiunge a cache JSON
- **Creazione manuale** → Aggiunge a cache JSON

---

## 🔐 Sicurezza

### **Autorizzazione:**
- Tutti gli endpoint `/api/admin/franchises/**` richiedono:
  - `SUPER_ADMIN` o
  - `SUPERVISOR` o
  - `ADMIN`

### **Autenticazione:**
- JWT Bearer token obbligatorio
- Verifica ruolo tramite `@PreAuthorize`

---

## 📝 Logging e Notifiche

### **Notifiche Admin:**
- "Franchise approvato" - Quando proposta approvata
- "Proposta franchise rifiutata" - Quando proposta rifiutata
- "Franchise disabilitato" - Quando franchise disabilitato
- "Franchise riabilitato" - Quando franchise riabilitato
- "Franchise creato manualmente" - Quando creato da admin
- "Nuova proposta franchise" - Quando utente propone

### **Logging:**
- Ogni operazione loggata con dettagli
- Errori loggati con stack trace
- Operazioni JSON loggate

---

## ✅ Checklist Implementazione

### **Database:**
- [x] Migration V19: Tabella franchises
- [x] Migration V20: Tabella franchise_proposals
- [x] Indici per performance
- [x] Foreign keys e constraints

### **Modelli:**
- [x] Franchise con enum FranchiseStatus
- [x] FranchiseProposal con enum ProposalStatus
- [x] Relazioni con User

### **Repository:**
- [x] FranchiseRepository con query custom
- [x] FranchiseProposalRepository con query custom

### **Service:**
- [x] FranchiseAdminService completo
- [x] Sincronizzazione cache JSON
- [x] Validazioni e controlli duplicati
- [x] Notifiche admin integrate

### **Controller:**
- [x] FranchiseAdminController con tutti gli endpoint
- [x] Autenticazione e autorizzazione
- [x] Gestione errori completa
- [x] DTO per response

### **Integrazione:**
- [x] FranchiseController aggiornato (usa FranchiseAdminService)
- [x] Sincronizzazione JSON automatica
- [x] Notifiche admin funzionanti

---

## 🚀 Risultato Finale

✅ **Sistema completo gestione franchise e proposte**  
✅ **Sincronizzazione automatica DB ↔ JSON**  
✅ **Endpoint admin completi per approvazione/rifiuto**  
✅ **Abilitazione/disabilitazione franchise**  
✅ **Creazione manuale franchise da admin**  
✅ **Notifiche admin per tutte le operazioni**  
✅ **Sicurezza e autorizzazione implementate**  
✅ **Logging completo per audit**

---

## 📝 Note Future

- **TODO:** Salvare JSON in percorso scrivibile in produzione
- **TODO:** Email utente quando proposta rifiutata
- **TODO:** Dashboard admin per visualizzazione proposte
- **TODO:** Statistiche avanzate (tasso approvazione, ecc.)
- **TODO:** Export/import franchise da file JSON

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

