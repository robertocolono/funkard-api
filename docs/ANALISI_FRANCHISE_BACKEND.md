# 🔍 ANALISI: Gestione Franchise nel Backend

**Data:** 2025-01-XX  
**Tipo:** Analisi Completa - Nessuna Implementazione  
**Obiettivo:** Capire ESATTAMENTE come `franchise` è gestita nel backend

---

## 1️⃣ DOMINIO / ENTITY

### 1.1 Card Entity

**File:** `src/main/java/com/funkard/model/Card.java`  
**Righe:** 31-32

**Definizione:**
```java
@Column(length = 100)
private String franchise; // es: "Pokémon", "Yu-Gi-Oh!", "Magic: The Gathering"
```

**Caratteristiche:**
- ✅ **Tipo:** `String` (non Enum)
- ✅ **Nullable:** SÌ (nessun `@NotNull`)
- ✅ **Lunghezza:** `VARCHAR(100)`

**Conferma:**
- ✅ `franchise` **ESISTE** su `Card`
- ✅ Campo **nullable** (può essere `null`)

---

### 1.2 Listing Entity

**File:** `src/main/java/com/funkard/model/Listing.java`  
**Righe:** 1-64

**Verifica:**
- ❌ **NON esiste** campo `franchise` su `Listing`
- ✅ Accesso tramite: `listing.getCard().getFranchise()`

**Conferma:**
- ❌ `franchise` **NON esiste** su `Listing`
- ✅ Accessibile solo tramite relazione `Listing.card.franchise`

---

### 1.3 Colonna Database

**File:** `src/main/resources/db/migration/V17__add_category_franchise_to_cards.sql`  
**Righe:** 4-6

**Definizione:**
```sql
ALTER TABLE cards 
ADD COLUMN IF NOT EXISTS category VARCHAR(100) NULL,
ADD COLUMN IF NOT EXISTS franchise VARCHAR(100) NULL,
ADD COLUMN IF NOT EXISTS language VARCHAR(50) NULL;
```

**Indice:**
```sql
CREATE INDEX IF NOT EXISTS idx_cards_franchise ON cards(franchise);
```

**Conferma:**
- ✅ **Tabella:** `cards`
- ✅ **Colonna:** `franchise` (`VARCHAR(100) NULL`)
- ✅ **Indice:** `idx_cards_franchise` esistente

---

## 2️⃣ CREAZIONE LISTING (SELL)

### 2.1 CreateListingRequest

**File:** `src/main/java/com/funkard/dto/CreateListingRequest.java`  
**Righe:** 45, 50

**Campi presenti:**
```java
private String franchise; // Valore Franchise selezionato
private String customFranchise; // Valore personalizzato se franchise = "Altro"
```

**Caratteristiche:**
- ✅ `franchise` **ESISTE** in `CreateListingRequest`
- ✅ Campo **opzionale** (nessun `@NotNull` o `@NotBlank`)
- ✅ `customFranchise` presente (per valori "Altro")

**Conferma:**
- ✅ `franchise` **presente** nella request
- ✅ Campo **opzionale**

---

### 2.2 Gestione durante Creazione Listing

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Righe:** 448-460

**Codice:**
```java
// Se Franchise è "Altro" e customFranchise è fornito, salva proposta
if ("Altro".equalsIgnoreCase(request.getFranchise()) && 
    request.getCustomFranchise() != null && !request.getCustomFranchise().trim().isEmpty()) {
    try {
        pendingValueService.submitPendingValue(
            PendingValue.ValueType.FRANCHISE,
            request.getCustomFranchise(),
            userId
        );
        log.info("✅ Proposta Franchise personalizzato salvata: {}", request.getCustomFranchise());
    } catch (Exception e) {
        log.warn("⚠️ Errore durante salvataggio proposta Franchise: {}", e.getMessage());
        // Non bloccare la creazione listing se la proposta fallisce
    }
}
```

**Comportamento:**
- ✅ `franchise` viene **letta** dalla request
- ❌ `franchise` **NON viene validata** (nessuna validazione valori)
- ❌ `franchise` **NON viene salvata** su `Card` o `Listing`
- ✅ Solo se `franchise = "Altro"` → salva `customFranchise` come `PendingValue` (proposta pending)

**Conferma:**
- ✅ `franchise` viene **letta** dalla request
- ❌ `franchise` **NON viene validata**
- ❌ `franchise` **NON viene salvata** su Card/Listing
- ⚠️ Solo gestione "Altro" → `PendingValue`

---

### 2.3 Creazione Card (Dopo Implementazione Category)

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Righe:** 379-386

**Codice attuale:**
```java
// Crea Card con category
Card card = new Card();
card.setCategory(category);
Card savedCard = cardRepository.save(card);
```

**Comportamento:**
- ❌ `card.setFranchise()` **NON viene chiamato**
- ❌ `franchise` **NON viene settata** su Card durante creazione

**Conferma:**
- ❌ `franchise` **IGNORATA** durante creazione Card dalla Sell

---

## 3️⃣ MARKETPLACE — FILTRI

### 3.1 Endpoint Marketplace

**File:** `src/main/java/com/funkard/controller/ListingController.java`  
**Righe:** 48-56

**Firma metodo:**
```java
@GetMapping
@Cacheable(...)
public ResponseEntity<?> getAllListings(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) List<String> type,
    @RequestParam(required = false) List<String> condition,
    @RequestParam(required = false) List<String> language,
    @RequestParam(required = false) List<String> franchise,  // ← List<String>
    @RequestParam(required = false) String search,
    @RequestParam(required = false) Boolean acceptTrades,
    Authentication authentication
)
```

**Conferma:**
- ✅ Endpoint: `GET /api/listings`
- ✅ Parametro: `franchise` (tipo `List<String>`, opzionale)
- ✅ Multiselect supportato

---

### 3.2 Query Repository

**File:** `src/main/java/com/funkard/repository/ListingRepository.java`  
**Righe:** 65-80

**Query JPQL:**
```java
@Query("""
    SELECT l FROM Listing l
    WHERE (:category IS NULL OR l.card.category = :category)
    AND (:type IS NULL OR l.card.type IN :type)
    AND (:condition IS NULL OR l.condition IN :condition)
    AND (:language IS NULL OR l.card.language IN :language)
    AND (:franchise IS NULL OR l.card.franchise IN :franchise)  // ← l.card.franchise
    AND (:search IS NULL OR (
        LOWER(COALESCE(l.title, '')) LIKE :search
        OR LOWER(COALESCE(l.description, '')) LIKE :search
        OR LOWER(COALESCE(l.card.name, '')) LIKE :search
        OR LOWER(COALESCE(l.card.setName, '')) LIKE :search
        OR LOWER(COALESCE(l.card.franchise, '')) LIKE :search  // ← anche in search
    ))
    AND (:acceptTrades IS NULL OR l.acceptTrades = :acceptTrades)
    """)
List<Listing> findByFilters(
    @Param("category") String category,
    @Param("type") List<String> type,
    @Param("condition") List<String> condition,
    @Param("language") List<String> language,
    @Param("franchise") List<String> franchise,  // ← List<String>
    @Param("search") String search,
    @Param("acceptTrades") Boolean acceptTrades
);
```

**Campo filtrato:**
- ✅ `l.card.franchise` (da `Card`, non da `Listing`)

**Tipo confronto:**
- ✅ `IN` (multiselect: `l.card.franchise IN :franchise`)

**Conferma:**
- ✅ Query: `WHERE (:franchise IS NULL OR l.card.franchise IN :franchise)`
- ✅ Campo: `l.card.franchise` (da `Card`)
- ✅ Confronto: `IN` (multiselect)

---

### 3.3 Normalizzazione Service

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Righe:** 338-350

**Codice:**
```java
// Normalizzazione franchise (multiselect): normalizza lista, rimuove duplicati, ordina
List<String> normalizedFranchise = null;
if (franchise != null && !franchise.isEmpty()) {
    normalizedFranchise = franchise.stream()
        .filter(f -> f != null && !f.trim().isEmpty())
        .map(f -> f.trim().toUpperCase())  // ← Normalizzazione uppercase
        .distinct()
        .sorted()
        .collect(Collectors.toList());
    // Se lista risultante è vuota dopo normalizzazione, trattare come null
    if (normalizedFranchise.isEmpty()) {
        normalizedFranchise = null;
    }
}
```

**Normalizzazione:**
- ✅ `trim()` - rimuove spazi
- ✅ `toUpperCase()` - converte a uppercase
- ✅ `distinct()` - rimuove duplicati
- ✅ `sorted()` - ordina

**Conferma:**
- ✅ Normalizzazione: `trim()` + `toUpperCase()`
- ✅ Nessuna validazione valori

---

## 4️⃣ VALORI ATTESI

### 4.1 Validazione

**Verifica codice:**
- ❌ **Nessuna validazione** valori `franchise`
- ❌ **Nessun enum** per `franchise`
- ❌ **Nessun constraint** database (CHECK o ENUM)
- ✅ Solo **normalizzazione** (`trim()` + `toUpperCase()`)

**Conferma:**
- ❌ **Nessuna validazione** valori
- ✅ Solo **normalizzazione** uppercase

---

### 4.2 Valori Liberi vs Normalizzati

**Comportamento:**
- ✅ **Valori liberi** accettati (qualsiasi stringa)
- ✅ **Normalizzazione** automatica (uppercase)
- ❌ **Nessuna whitelist** o enum

**Esempi:**
- `"Pokémon"` → normalizzato a `"POKÉMON"`
- `"Yu-Gi-Oh!"` → normalizzato a `"YU-GI-OH!"`
- `"Magic: The Gathering"` → normalizzato a `"MAGIC: THE GATHERING"`
- Qualsiasi altro valore → accettato e normalizzato

**Conferma:**
- ✅ **Valori liberi** (qualsiasi stringa)
- ✅ **Normalizzazione** automatica (uppercase)

---

### 4.3 Cosa Succede se Valore Non Valido

**Comportamento:**
- ✅ **Nessun errore** (non c'è validazione)
- ✅ Valore viene **normalizzato** (uppercase)
- ✅ Query restituisce **array vuoto** se non matcha

**Esempio:**
- Request: `GET /api/listings?franchise=INVALID`
- Normalizzazione: `"INVALID"` → `"INVALID"`
- Query: `WHERE l.card.franchise IN ('INVALID')`
- Risultato: **Array vuoto** (se nessuna Card ha `franchise = 'INVALID'`)

**Conferma:**
- ✅ **Nessun errore** (valore accettato)
- ✅ **Array vuoto** se non matcha

---

### 4.4 Tabelle Catalogo Franchise

**Tabelle esistenti:**
1. `franchise_catalog` (V18) - Catalogo franchise
2. `franchises` (V19) - Franchise ufficiali
3. `franchise_proposals` (V20) - Proposte pending

**Nota:**
- ⚠️ Tabelle catalogo **esistono** ma **NON vengono usate** per validazione
- ⚠️ `franchise` su `Card` è **indipendente** dal catalogo
- ⚠️ Nessun vincolo foreign key o validazione contro catalogo

**Conferma:**
- ⚠️ Tabelle catalogo **esistono** ma **non usate** per validazione
- ✅ `franchise` su `Card` è **stringa libera**

---

## 5️⃣ CONCLUSIONE

**"Nel backend, la franchise è persistita su `cards.franchise`, filtrata tramite `l.card.franchise IN :franchise` e i valori attesi sono stringhe libere (normalizzate uppercase)."**

**Dettagli:**
1. **Persistenza:** `cards.franchise` (`VARCHAR(100) NULL`)
2. **Filtri:** `l.card.franchise IN :franchise` (multiselect)
3. **Valori:** Stringhe libere (qualsiasi valore, normalizzato uppercase)
4. **Validazione:** Nessuna (valori accettati senza controllo)
5. **Creazione Sell:** `franchise` **NON viene salvata** su Card durante creazione listing

---

## 📋 RIEPILOGO

### ✅ Cosa Funziona

- ✅ `franchise` esiste su `Card` (nullable)
- ✅ Marketplace filtra per `franchise` (multiselect)
- ✅ Normalizzazione automatica (uppercase)
- ✅ Valori liberi accettati

### ❌ Cosa NON Funziona

- ❌ `franchise` **NON viene salvata** durante creazione listing dalla Sell
- ❌ Nessuna validazione valori
- ❌ Nessun collegamento con catalogo franchise

### ⚠️ Note

- ⚠️ Tabelle catalogo `franchise_catalog` e `franchises` esistono ma **non vengono usate** per validazione
- ⚠️ `franchise` su `Card` è **indipendente** dal catalogo
- ⚠️ Solo gestione "Altro" → `PendingValue` (proposta pending)

---

**Fine Analisi**
