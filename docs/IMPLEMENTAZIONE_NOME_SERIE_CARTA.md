# ✅ IMPLEMENTAZIONE: Supporto Nome Carta e Serie nella Sell

**Data:** 2025-01-XX  
**Tipo:** Implementazione Completa  
**Scope:** `Card.name` e `Card.setName` durante creazione listing

---

## 📋 CHECKLIST IMPLEMENTAZIONE

### ✅ Implementazione Completata

**File Modificati:**
1. `src/main/java/com/funkard/dto/CreateListingRequest.java`
   - Aggiunto campo `cardName` (obbligatorio)
   - Aggiunto campo `series` (opzionale)

2. `src/main/java/com/funkard/service/ListingService.java`
   - Aggiunta logica per settare `card.setName()`
   - Aggiunta logica per settare `card.setSetName()`

**Posizione:**
- Righe 383-393 (dopo `card.setCategory()`, prima di `cardRepository.save()`)

**Codice Aggiunto:**
```java
// 📝 Imposta nome carta (obbligatorio)
if (request.getCardName() != null && !request.getCardName().trim().isEmpty()) {
    card.setName(request.getCardName().trim());
    log.debug("✅ Nome carta impostato: {}", request.getCardName().trim());
}

// 📚 Imposta serie/espansione se presente (opzionale)
if (request.getSeries() != null && !request.getSeries().trim().isEmpty()) {
    card.setSetName(request.getSeries().trim());
    log.debug("✅ Serie impostata: {}", request.getSeries().trim());
}
```

**Conferma:**
- ✅ Logica inserita nella posizione corretta
- ✅ Normalizzazione: solo `trim()` (no uppercase)
- ✅ Gestione null: `cardName` obbligatorio, `series` opzionale

---

## 1️⃣ COMPORTAMENTO

### 1.1 Se `cardName` è Presente

**Request:**
```json
{
  "title": "Test Listing",
  "price": 100.00,
  "category": "TCG",
  "cardName": "Charizard Base Set"
}
```

**Comportamento:**
- ✅ `cardName` viene letto dalla request
- ✅ Normalizzato: `trim()` → `"Charizard Base Set"`
- ✅ Salvato su `Card.name`
- ✅ Marketplace può cercare per nome carta

**Conferma:**
- ✅ Nome carta salvato e normalizzato

---

### 1.2 Se `cardName` è Null o Vuoto

**Request:**
```json
{
  "title": "Test Listing",
  "price": 100.00,
  "category": "TCG"
}
```

**Comportamento:**
- ❌ **400 Bad Request** (Bean Validation)
- ✅ Messaggio: "Il nome della carta è obbligatorio"

**Conferma:**
- ✅ Validazione obbligatoria attiva

---

### 1.3 Se `series` è Presente

**Request:**
```json
{
  "title": "Test Listing",
  "price": 100.00,
  "category": "TCG",
  "cardName": "Charizard",
  "series": "Base Set"
}
```

**Comportamento:**
- ✅ `series` viene letto dalla request
- ✅ Normalizzato: `trim()` → `"Base Set"`
- ✅ Salvato su `Card.setName`
- ✅ Marketplace può cercare per serie

**Conferma:**
- ✅ Serie salvata e normalizzata

---

### 1.4 Se `series` è Null o Vuoto

**Request:**
```json
{
  "title": "Test Listing",
  "price": 100.00,
  "category": "TCG",
  "cardName": "Charizard"
}
```

**Comportamento:**
- ✅ `series` non viene settato
- ✅ `Card.setName` rimane `null`
- ✅ Creazione listing prosegue normalmente

**Conferma:**
- ✅ Gestione null corretta (opzionale)

---

## 2️⃣ NORMALIZZAZIONE

### 2.1 Pattern Applicato

**Normalizzazione:**
- ✅ Solo `trim()` (rimuove spazi iniziali/finali)
- ❌ **NON** `toUpperCase()` (preserva case originale)

**Motivazione:**
- ✅ Nome carta e serie sono stringhe libere
- ✅ Case originale deve essere preservato
- ✅ Diverso da `franchise` che usa uppercase per normalizzazione

**Conferma:**
- ✅ Normalizzazione corretta (solo `trim()`)

---

### 2.2 Esempi Normalizzazione

**Input → Output:**
- `"  Charizard  "` → `"Charizard"`
- `"Base Set"` → `"Base Set"`
- `"  Yu-Gi-Oh!  "` → `"Yu-Gi-Oh!"`

**Conferma:**
- ✅ Normalizzazione corretta (solo trim, case preservato)

---

## 3️⃣ BACKWARD-COMPATIBILITY

### 3.1 Compatibilità Request

**Request senza `cardName`:**
- ❌ **400 Bad Request** (campo obbligatorio)
- ✅ Validazione Bean Validation attiva

**Request senza `series`:**
- ✅ Funziona (campo opzionale)
- ✅ `Card.setName` rimane `null`
- ✅ Nessun errore

**Conferma:**
- ⚠️ **Breaking change** per `cardName` (ora obbligatorio)
- ✅ **Backward-compatible** per `series` (opzionale)

---

### 3.2 Compatibilità Database

**Colonne:**
- `cards.name VARCHAR` (nullable)
- `cards.set_name VARCHAR` (nullable)

**Comportamento:**
- ✅ Se `cardName` assente → validazione fallisce (non arriva al DB)
- ✅ Se `series` assente → `NULL` (comportamento attuale)
- ✅ Se `cardName` presente → valore normalizzato (nuovo comportamento)
- ✅ Se `series` presente → valore normalizzato (nuovo comportamento)
- ✅ Nessun vincolo violato

**Conferma:**
- ✅ **Compatibile** con schema database

---

### 3.3 Compatibilità Marketplace

**Query Marketplace:**
```java
OR LOWER(COALESCE(l.card.name, '')) LIKE :search
OR LOWER(COALESCE(l.card.setName, '')) LIKE :search
```

**Comportamento:**
- ✅ Query **non modificata**
- ✅ Se `Card.name` è `null` → non matcha search (comportamento attuale)
- ✅ Se `Card.name` è valorizzato → matcha search (nuovo comportamento)
- ✅ Stesso per `Card.setName`

**Conferma:**
- ✅ **Nessuna modifica** a Marketplace
- ✅ **Compatibile** con search esistenti

---

## 4️⃣ VERIFICA IMPLEMENTAZIONE

### 4.1 Checklist Finale

**✅ CardName letto dalla request:**
- ✅ `request.getCardName()` chiamato

**✅ Normalizzazione corretta:**
- ✅ `trim()` applicato

**✅ Salvato su Card:**
- ✅ `card.setName(cardName.trim())` chiamato

**✅ Series letto dalla request:**
- ✅ `request.getSeries()` chiamato (se presente)

**✅ Normalizzazione corretta:**
- ✅ `trim()` applicato

**✅ Salvato su Card:**
- ✅ `card.setSetName(series.trim())` chiamato (se presente)

**✅ Gestione null:**
- ✅ `cardName` obbligatorio (validazione Bean)
- ✅ `series` opzionale (se null, non settato)

**✅ Nessuna modifica a:**
- ✅ Marketplace (query invariata)
- ✅ Listing legacy (metodo non modificato)
- ✅ `Listing.title` (rimane invariato)

**Conferma:**
- ✅ **Tutti i requisiti soddisfatti**

---

## 5️⃣ TEST CONSIGLIATO

### Test 1: Creazione con Nome Carta e Serie

**Request:**
```json
POST /api/listings
{
  "title": "Test Listing",
  "price": 100.00,
  "category": "TCG",
  "cardName": "Charizard",
  "series": "Base Set"
}
```

**Expected:**
- ✅ 201 Created
- ✅ Card creata con `name = "Charizard"` e `setName = "Base Set"`
- ✅ Marketplace search: `GET /api/listings?search=Charizard` → trova listing

---

### Test 2: Creazione senza Serie

**Request:**
```json
POST /api/listings
{
  "title": "Test Listing",
  "price": 100.00,
  "category": "TCG",
  "cardName": "Charizard"
}
```

**Expected:**
- ✅ 201 Created
- ✅ Card creata con `name = "Charizard"` e `setName = null`
- ✅ Marketplace search funziona su nome

---

### Test 3: Creazione senza Nome Carta

**Request:**
```json
POST /api/listings
{
  "title": "Test Listing",
  "price": 100.00,
  "category": "TCG"
}
```

**Expected:**
- ✅ 400 Bad Request
- ✅ Messaggio: "Il nome della carta è obbligatorio"

---

### Test 4: Marketplace Search Nome Carta

**Request:**
```
GET /api/listings?search=Charizard
```

**Expected:**
- ✅ 200 OK
- ✅ Lista listing con `Card.name` contenente "Charizard"
- ✅ Include listing creati dalla Sell con nome carta

---

## 6️⃣ RIEPILOGO

### ✅ Implementazione

**File Modificati:**
1. `src/main/java/com/funkard/dto/CreateListingRequest.java`
   - Aggiunto `cardName` (obbligatorio)
   - Aggiunto `series` (opzionale)

2. `src/main/java/com/funkard/service/ListingService.java`
   - Aggiunta logica `card.setName()` (righe 383-387)
   - Aggiunta logica `card.setSetName()` (righe 389-393)

**Logica:**
- ✅ Lettura `request.getCardName()` e `request.getSeries()`
- ✅ Normalizzazione `trim()` (no uppercase)
- ✅ Salvataggio su `Card.name` e `Card.setName`
- ✅ Gestione null (`cardName` obbligatorio, `series` opzionale)

**Conferma:**
- ✅ **Implementazione completa**
- ✅ **Allineata a logica Marketplace**
- ⚠️ **Breaking change** per `cardName` (ora obbligatorio)

---

### ✅ Compatibilità

**Breaking Change:**
- ⚠️ `cardName` ora obbligatorio (validazione Bean)

**Backward-Compatible:**
- ✅ `series` opzionale (se null, non settato)
- ✅ Database nullable → compatibile
- ✅ Marketplace query → invariata

**Nessuna Modifica:**
- ✅ Marketplace
- ✅ Listing legacy
- ✅ `Listing.title`

**Conferma:**
- ⚠️ **1 breaking change** (`cardName` obbligatorio)
- ✅ **Resto backward-compatible**

---

## ✅ IMPLEMENTAZIONE COMPLETA

**Status:** ✅ **COMPLETATA**

**Conferme:**
- ✅ Nome carta letto e normalizzato (solo `trim()`)
- ✅ Serie letta e normalizzata (solo `trim()`, opzionale)
- ✅ Salvato su `Card.name` e `Card.setName`
- ✅ Marketplace search funziona su nome carta e serie
- ⚠️ Breaking change: `cardName` ora obbligatorio

**Pronto per:**
- ✅ Test manuale
- ✅ Deploy (con aggiornamento frontend per `cardName` obbligatorio)

---

**Fine Implementazione**
