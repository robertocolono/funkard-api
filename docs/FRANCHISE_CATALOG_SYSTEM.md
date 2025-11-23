# 📚 Sistema Catalogo Franchise - Funkard Backend

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0

---

## 📋 Panoramica

Sistema completo per gestione franchise delle carte, con catalogo amministrabile e supporto per valori personalizzati "Altro". Ogni carta può avere `category`, `franchise` e `language` per una categorizzazione completa.

---

## ✅ Componenti Implementati

### **1. Modello Card Aggiornato**

**File:** `src/main/java/com/funkard/model/Card.java`

**Nuovi Campi:**
- `category` (VARCHAR 100) - Categoria carta (es. "TCG", "Anime", "TCG / Anime")
- `franchise` (VARCHAR 100) - Franchise carta (es. "Pokémon", "Yu-Gi-Oh!")
- `language` (VARCHAR 50) - Lingua carta (es. "Italiano", "Inglese")

**Esempio:**
```json
{
  "id": "uuid",
  "name": "Charizard Base Set",
  "category": "TCG / Anime",
  "franchise": "Pokémon",
  "language": "Italiano",
  "condition": "Near Mint"
}
```

---

### **2. Tabella Franchise Catalog**

**File:** `src/main/resources/db/migration/V18__create_franchise_catalog_table.sql`

**Campi:**
- `id` (BIGSERIAL) - ID univoco
- `category` (VARCHAR 100) - Categoria (es. "TCG", "Anime", "TCG / Anime")
- `name` (VARCHAR 100) - Nome franchise (es. "Pokémon", "MetaZoo")
- `active` (BOOLEAN) - Flag attivazione (default: true)
- `created_at` (TIMESTAMP) - Data creazione
- `updated_at` (TIMESTAMP) - Data aggiornamento

**Vincolo Unico:**
- `(category, name)` - Previene duplicati

**Seed Data:**
- 17 franchise iniziali (Pokémon, Yu-Gi-Oh!, Magic, One Piece, ecc.)

---

### **3. Modello FranchiseCatalog**

**File:** `src/main/java/com/funkard/model/FranchiseCatalog.java`

**Funzionalità:**
- Gestione catalogo franchise
- Flag `active` per disattivazione
- Timestamps per audit

---

### **4. Repository e Service**

**File:** `src/main/java/com/funkard/repository/FranchiseCatalogRepository.java`
**File:** `src/main/java/com/funkard/service/FranchiseCatalogService.java`

**Metodi:**
- `getActiveFranchises()` - Recupera franchise attivi
- `getFranchisesByCategory(category)` - Filtra per categoria
- `getFranchisesGroupedByCategory()` - Raggruppa per categoria
- `createFranchise(category, name)` - Crea nuovo (admin)
- `updateFranchise(id, ...)` - Aggiorna (admin)
- `deleteFranchise(id)` - Elimina (admin)
- `isFranchiseActive(category, name)` - Verifica attivazione
- `getStats()` - Statistiche

---

### **5. Controller API**

#### **FranchiseController (Pubblico)**
**File:** `src/main/java/com/funkard/controller/FranchiseController.java`

**Endpoint:**
- `GET /api/franchises` - Lista franchise attivi
  - Query: `?category=TCG` - Filtra per categoria
  - Query: `?grouped=true` - Raggruppa per categoria
- `GET /api/franchises/categories` - Lista categorie disponibili
- `GET /api/franchises/stats` - Statistiche pubbliche

#### **AdminFranchiseController (Admin)**
**File:** `src/main/java/com/funkard/admin/controller/AdminFranchiseController.java`

**Endpoint:**
- `GET /api/admin/franchises` - Lista tutti i franchise
- `POST /api/admin/franchises` - Crea nuovo franchise
- `PUT /api/admin/franchises/{id}` - Aggiorna franchise
- `DELETE /api/admin/franchises/{id}` - Elimina franchise
- `GET /api/admin/franchises/stats` - Statistiche admin

---

### **6. Integrazione con Pending Values**

**Aggiornato:**
- Enum `ValueType` include `FRANCHISE`
- `CreateListingRequest` supporta `franchise` e `customFranchise`
- `ListingService` salva proposte franchise come pending

---

## 📊 Endpoint API

### **GET /api/franchises**
**Query Params:**
- `category` (opzionale) - Filtra per categoria
- `grouped` (default: false) - Raggruppa per categoria

**Response (Lista):**
```json
[
  {
    "id": 1,
    "category": "TCG",
    "name": "Pokémon",
    "active": true
  },
  {
    "id": 2,
    "category": "TCG",
    "name": "Yu-Gi-Oh!",
    "active": true
  }
]
```

**Response (Raggruppato):**
```json
{
  "TCG": [
    {"id": 1, "category": "TCG", "name": "Pokémon", "active": true},
    {"id": 2, "category": "TCG", "name": "Yu-Gi-Oh!", "active": true}
  ],
  "Anime": [
    {"id": 9, "category": "Anime", "name": "Naruto", "active": true}
  ]
}
```

---

### **GET /api/franchises/categories**
**Response:**
```json
["TCG", "Anime", "TCG / Anime"]
```

---

### **POST /api/admin/franchises**
**Request:**
```json
{
  "category": "TCG",
  "name": "UFC Prizm"
}
```

**Response (201):**
```json
{
  "id": 18,
  "category": "TCG",
  "name": "UFC Prizm",
  "active": true
}
```

---

### **PUT /api/admin/franchises/{id}**
**Request:**
```json
{
  "category": "TCG",
  "name": "MetaZoo",
  "active": false
}
```

**Response:**
```json
{
  "id": 6,
  "category": "TCG",
  "name": "MetaZoo",
  "active": false
}
```

---

## 🔄 Flusso Completo

### **1. Utente Crea Carta con Franchise:**

```
1. Utente seleziona franchise da lista
   GET /api/franchises?category=TCG
   → ["Pokémon", "Yu-Gi-Oh!", "Magic: The Gathering", ...]
2. Utente crea carta
   POST /api/cards
   {
     "name": "Charizard",
     "category": "TCG / Anime",
     "franchise": "Pokémon",
     "language": "Italiano"
   }
3. Carta salvata con franchise
```

### **2. Utente Propone Franchise "Altro":**

```
1. Utente seleziona "Altro" in franchise
2. Inserisce valore personalizzato (es. "UFC Prizm")
3. POST /api/listings
   {
     "franchise": "Altro",
     "customFranchise": "UFC Prizm",
     ...
   }
4. Proposta salvata come pending (type: FRANCHISE)
5. Admin approva proposta
6. Franchise aggiunto al catalogo
```

### **3. Admin Gestisce Catalogo:**

```
1. Admin visualizza franchise
   GET /api/admin/franchises
2. Admin disattiva franchise
   PUT /api/admin/franchises/6
   {"active": false}
3. Franchise non appare più in GET /api/franchises
```

---

## 📝 Database Schema

### **Migration V17: Cards**
```sql
ALTER TABLE cards 
ADD COLUMN category VARCHAR(100),
ADD COLUMN franchise VARCHAR(100),
ADD COLUMN language VARCHAR(50);
```

### **Migration V18: Franchise Catalog**
```sql
CREATE TABLE franchise_catalog (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (category, name)
);
```

---

## ✅ Checklist Implementazione

### **Database:**
- [x] Migration V17: Campi category, franchise, language in cards
- [x] Migration V18: Tabella franchise_catalog
- [x] Seed data iniziale (17 franchise)
- [x] Indici per performance

### **Modelli:**
- [x] Card aggiornato con category, franchise, language
- [x] FranchiseCatalog creato
- [x] PendingValue enum aggiornato (FRANCHISE)

### **Repository:**
- [x] FranchiseCatalogRepository con query custom
- [x] Metodi per filtri e statistiche

### **Service:**
- [x] FranchiseCatalogService completo
- [x] Gestione attivazione/disattivazione
- [x] Validazione duplicati

### **Controller:**
- [x] FranchiseController (pubblico)
- [x] AdminFranchiseController (admin)
- [x] Integrazione con pending_values

### **DTO:**
- [x] FranchiseDTO
- [x] CreateListingRequest aggiornato

---

## 🚀 Risultato Finale

✅ **Campi category, franchise, language aggiunti a Card**  
✅ **Tabella franchise_catalog creata e funzionante**  
✅ **Endpoint GET /api/franchises per liste disponibili**  
✅ **Gestione admin per attivazione/disattivazione franchise**  
✅ **Integrazione con sistema pending_values per "Altro"**  
✅ **Seed data iniziale con 17 franchise comuni**

---

## 📝 Note Future

- **TODO:** Caricare franchise da file JSON seed
- **TODO:** Notifica admin quando nuovo franchise proposto
- **TODO:** Dashboard admin per gestione catalogo
- **TODO:** Statistiche avanzate (franchise più usati, ecc.)

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

