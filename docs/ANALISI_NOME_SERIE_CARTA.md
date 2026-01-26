# 🔍 ANALISI: Gestione Nome Carta e Serie/Espansione

**Data:** 2025-01-XX  
**Tipo:** Analisi Completa - Nessuna Implementazione  
**Obiettivo:** Capire ESATTAMENTE come sono gestiti nome carta e serie/espansione

---

## 1️⃣ NOME DELLA CARTA

### 1.1 Entity Card

**File:** `src/main/java/com/funkard/model/Card.java`  
**Righe:** 15

**Definizione:**
```java
private String name;
```

**Caratteristiche:**
- ✅ **Esiste** `Card.name`
- ✅ **Tipo:** `String` (nullable, nessun `@NotNull`)
- ✅ **Nessuna annotazione** particolare

**Conferma:**
- ✅ `Card.name` **ESISTE** nell'entity

---

### 1.2 Entity Listing

**File:** `src/main/java/com/funkard/model/Listing.java`  
**Righe:** 14

**Definizione:**
```java
private String title;
```

**Caratteristiche:**
- ✅ **Esiste** `Listing.title`
- ✅ **Tipo:** `String` (nullable)

**Conferma:**
- ✅ `Listing.title` **ESISTE** nell'entity

---

### 1.3 CreateListingRequest

**File:** `src/main/java/com/funkard/dto/CreateListingRequest.java`  
**Righe:** 16-17

**Campi presenti:**
```java
@NotBlank(message = "Il titolo è obbligatorio")
private String title;
```

**Campi NON presenti:**
- ❌ `cardName` - **NON esiste**
- ❌ `name` - **NON esiste**

**Conferma:**
- ✅ `title` **ESISTE** in `CreateListingRequest` (obbligatorio)
- ❌ `cardName` / `name` **NON esistono** in `CreateListingRequest`

---

### 1.4 Creazione Listing - Controller

**File:** `src/main/java/com/funkard/controller/ListingController.java`  
**Righe:** 106-114

**Codice:**
```java
// Crea listing entity
Listing listing = new Listing();
listing.setTitle(request.getTitle());  // ← title viene settato
listing.setDescription(request.getDescription());
listing.setPrice(request.getPrice());
listing.setCondition(request.getCondition());

// Imposta seller da userId
listing.setSeller(userId.toString());
```

**Comportamento:**
- ✅ `listing.setTitle(request.getTitle())` viene chiamato
- ❌ **NON viene creato** `Card` nel controller
- ❌ **NON viene settato** `Card.name`

**Conferma:**
- ✅ `title` viene settato su `Listing.title`
- ❌ `Card.name` **NON viene settato** nel controller

---

### 1.5 Creazione Listing - Service

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Righe:** 379-391

**Codice:**
```java
// Crea Card con category
Card card = new Card();
card.setCategory(category);

// 🎮 Imposta franchise se presente (opzionale, normalizzato uppercase)
if (request.getFranchise() != null && !request.getFranchise().trim().isEmpty()) {
    String franchise = request.getFranchise().trim().toUpperCase();
    card.setFranchise(franchise);
    log.debug("✅ Franchise impostato: {}", franchise);
}

Card savedCard = cardRepository.save(card);
log.debug("✅ Card creata con category: {}", category);
```

**Comportamento:**
- ✅ `Card` viene creata
- ✅ `card.setCategory()` viene chiamato
- ✅ `card.setFranchise()` viene chiamato (se presente)
- ❌ `card.setName()` **NON viene chiamato**
- ❌ `card.setSetName()` **NON viene chiamato**

**Conferma:**
- ❌ `Card.name` **NON viene settato** durante creazione listing
- ❌ `Card.setName` **NON viene settato** durante creazione listing

---

### 1.6 Query Marketplace - Search

**File:** `src/main/java/com/funkard/repository/ListingRepository.java`  
**Righe:** 72-78

**Query JPQL:**
```java
AND (:search IS NULL OR (
    LOWER(COALESCE(l.title, '')) LIKE :search           // ← Listing.title
    OR LOWER(COALESCE(l.description, '')) LIKE :search
    OR LOWER(COALESCE(l.card.name, '')) LIKE :search     // ← Card.name
    OR LOWER(COALESCE(l.card.setName, '')) LIKE :search // ← Card.setName
    OR LOWER(COALESCE(l.card.franchise, '')) LIKE :search
))
```

**Comportamento:**
- ✅ Search cerca in `l.title` (Listing.title)
- ✅ Search cerca in `l.card.name` (Card.name)
- ⚠️ Se `listing.card` è `null` → `l.card.name` è `null` → non matcha
- ⚠️ Se `Card.name` è `null` → non matcha

**Conferma:**
- ✅ Search usa sia `Listing.title` che `Card.name`
- ⚠️ `Card.name` è sempre `null` per listing creati dalla Sell → search non funziona su nome carta

---

### 1.7 Conclusione: Nome Carta

**Dove finisce il nome carta:**
- ✅ **`Listing.title`** - Viene settato da `request.getTitle()`
- ❌ **`Card.name`** - **NON viene settato** (rimane `null`)

**File coinvolti:**
- `ListingController.java` (riga 108) - `listing.setTitle(request.getTitle())`
- `ListingService.java` (righe 379-391) - `Card.name` **NON viene settato**

**Conferma:**
- ✅ Nome carta finisce su **`Listing.title`**
- ❌ Nome carta **NON finisce** su `Card.name`
- ⚠️ `Card.name` rimane sempre `null` per listing creati dalla Sell

---

## 2️⃣ SERIE / ESPANSIONE

### 2.1 Entity Card

**File:** `src/main/java/com/funkard/model/Card.java`  
**Righe:** 16

**Definizione:**
```java
private String setName;
```

**Caratteristiche:**
- ✅ **Esiste** `Card.setName`
- ✅ **Tipo:** `String` (nullable, nessun `@NotNull`)
- ✅ **Nessuna annotazione** particolare

**Conferma:**
- ✅ `Card.setName` **ESISTE** nell'entity

---

### 2.2 CreateListingRequest

**File:** `src/main/java/com/funkard/dto/CreateListingRequest.java`  
**Righe:** 14-51

**Campi presenti:**
- `title`, `description`, `price`, `currency`, `condition`, `cardId`, `category`, `tcg`, `language`, `franchise`, `customTcg`, `customLanguage`, `customFranchise`

**Campi NON presenti:**
- ❌ `series` - **NON esiste**
- ❌ `expansion` - **NON esiste**
- ❌ `setName` - **NON esiste**

**Conferma:**
- ❌ `series` / `expansion` / `setName` **NON esistono** in `CreateListingRequest`

---

### 2.3 Creazione Listing - Service

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Righe:** 379-391

**Codice:**
```java
// Crea Card con category
Card card = new Card();
card.setCategory(category);

// 🎮 Imposta franchise se presente (opzionale, normalizzato uppercase)
if (request.getFranchise() != null && !request.getFranchise().trim().isEmpty()) {
    String franchise = request.getFranchise().trim().toUpperCase();
    card.setFranchise(franchise);
    log.debug("✅ Franchise impostato: {}", franchise);
}

Card savedCard = cardRepository.save(card);
```

**Comportamento:**
- ❌ `card.setSetName()` **NON viene chiamato**
- ❌ Serie/Espansione **NON viene settata**

**Conferma:**
- ❌ `Card.setName` **NON viene settato** durante creazione listing
- ❌ Serie/Espansione **IGNORATA** (non presente in request, non settata)

---

### 2.4 Query Marketplace - Search

**File:** `src/main/java/com/funkard/repository/ListingRepository.java`  
**Righe:** 72-78

**Query JPQL:**
```java
AND (:search IS NULL OR (
    LOWER(COALESCE(l.title, '')) LIKE :search
    OR LOWER(COALESCE(l.description, '')) LIKE :search
    OR LOWER(COALESCE(l.card.name, '')) LIKE :search
    OR LOWER(COALESCE(l.card.setName, '')) LIKE :search  // ← Card.setName
    OR LOWER(COALESCE(l.card.franchise, '')) LIKE :search
))
```

**Comportamento:**
- ✅ Search cerca in `l.card.setName` (Card.setName)
- ⚠️ Se `listing.card` è `null` → `l.card.setName` è `null` → non matcha
- ⚠️ Se `Card.setName` è `null` → non matcha

**Conferma:**
- ✅ Search usa `Card.setName`
- ⚠️ `Card.setName` è sempre `null` per listing creati dalla Sell → search non funziona su serie

---

### 2.5 Conclusione: Serie/Espansione

**Come viene gestita:**
- ❌ **IGNORATA** - Non presente in `CreateListingRequest`
- ❌ **NON viene salvata** - `Card.setName` non viene settato
- ❌ **NON viene mappata** - Nessun mapping su altri campi

**File coinvolti:**
- `Card.java` (riga 16) - `Card.setName` esiste ma non viene usato
- `CreateListingRequest.java` - Campo `series`/`expansion` **NON esiste**
- `ListingService.java` (righe 379-391) - `Card.setName` **NON viene settato**

**Conferma:**
- ❌ Serie/Espansione **NON viene salvata**
- ❌ Serie/Espansione **IGNORATA** durante creazione listing
- ⚠️ `Card.setName` rimane sempre `null` per listing creati dalla Sell

---

## 3️⃣ RIEPILOGO

### 3.1 Nome Carta

**Entity:**
- ✅ `Card.name` esiste (riga 15 di `Card.java`)
- ✅ `Listing.title` esiste (riga 14 di `Listing.java`)

**Creazione Listing:**
- ✅ `Listing.title` viene settato da `request.getTitle()` (riga 108 di `ListingController.java`)
- ❌ `Card.name` **NON viene settato** (righe 379-391 di `ListingService.java`)

**Search Marketplace:**
- ✅ Cerca in `l.title` (Listing.title) - riga 73 di `ListingRepository.java`
- ✅ Cerca in `l.card.name` (Card.name) - riga 75 di `ListingRepository.java`
- ⚠️ `Card.name` è sempre `null` → search non funziona su nome carta

**Conferma:**
- ✅ Nome carta finisce su **`Listing.title`**
- ❌ Nome carta **NON finisce** su `Card.name`

---

### 3.2 Serie/Espansione

**Entity:**
- ✅ `Card.setName` esiste (riga 16 di `Card.java`)

**Creazione Listing:**
- ❌ Campo `series`/`expansion` **NON esiste** in `CreateListingRequest`
- ❌ `Card.setName` **NON viene settato** (righe 379-391 di `ListingService.java`)

**Search Marketplace:**
- ✅ Cerca in `l.card.setName` (Card.setName) - riga 76 di `ListingRepository.java`
- ⚠️ `Card.setName` è sempre `null` → search non funziona su serie

**Conferma:**
- ❌ Serie/Espansione **NON viene salvata**
- ❌ Serie/Espansione **IGNORATA** durante creazione listing

---

## 4️⃣ CONCLUSIONE FINALE

### 4.1 Nome Carta

**"Il nome carta finisce su `Listing.title` e NON su `Card.name`."**

**Dettagli:**
- File: `ListingController.java` (riga 108) - `listing.setTitle(request.getTitle())`
- File: `ListingService.java` (righe 379-391) - `Card.name` **NON viene settato**
- Risultato: `Card.name` rimane sempre `null` per listing creati dalla Sell

---

### 4.2 Serie/Espansione

**"Serie/Espansione viene IGNORATA: non esiste in `CreateListingRequest` e `Card.setName` non viene settato."**

**Dettagli:**
- File: `CreateListingRequest.java` - Campo `series`/`expansion` **NON esiste**
- File: `ListingService.java` (righe 379-391) - `Card.setName` **NON viene settato**
- Risultato: `Card.setName` rimane sempre `null` per listing creati dalla Sell

---

**Fine Analisi**
