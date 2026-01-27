# 📊 ANALISI: Stato Attuale `condition` nel Backend

**Data:** 2025-01-XX  
**Tipo:** Analisi Stato Attuale  
**Scope:** Verifica gestione `condition` in Sell e Marketplace

---

## 1️⃣ `condition` in `CreateListingRequest`

**Risposta:** ✅ **PRESENTE**

**File:** `src/main/java/com/funkard/dto/CreateListingRequest.java`  
**Riga:** 30

**Definizione:**
```java
private String condition;
```

**Tipo:** `String` (singolo valore, non `List<String>`)  
**Validazione:** ❌ **NESSUNA** (nessuna annotazione `@NotNull`, `@NotBlank`, o validazione custom)  
**Opzionalità:** ✅ **Opzionale** (campo nullable, nessun vincolo)

**Conferma:**
- Campo presente ma completamente opzionale
- Nessuna validazione applicata a livello di DTO

---

## 2️⃣ Flusso di creazione listing: `ListingService.create(...)`

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Metodo:** `create(Listing listing, CreateListingRequest request, Long userId)`  
**Righe:** 366-573

### 2.1 `condition` viene letto dalla request?

**Risposta:** ✅ **SÌ, ma indirettamente**

**File:** `src/main/java/com/funkard/controller/ListingController.java`  
**Riga:** 111

**Codice:**
```java
Listing listing = new Listing();
listing.setTitle(request.getTitle());
listing.setDescription(request.getDescription());
listing.setPrice(request.getPrice());
listing.setCondition(request.getCondition());  // ← Settato qui
```

**Conferma:**
- `condition` viene letto da `request.getCondition()` nel **controller** (riga 111)
- Viene settato direttamente su `listing.setCondition()` **prima** di chiamare `service.create()`
- Nel service `create()`, `condition` è già presente su `listing` (passato come parametro)

### 2.2 Dove viene settato (Listing o Card)?

**Risposta:** ✅ **SU `Listing`**

**File:** `src/main/java/com/funkard/controller/ListingController.java`  
**Riga:** 111

**Codice:**
```java
listing.setCondition(request.getCondition());
```

**Conferma:**
- `condition` viene settato su `Listing.condition` (non su `Card.condition`)
- Settato nel controller, prima della chiamata a `service.create()`

### 2.3 In quale punto del metodo `create()`?

**Risposta:** ❌ **NON viene settato nel metodo `create()`**

**Analisi:**
- `condition` è già presente su `listing` quando viene passato al metodo `create()`
- Nel metodo `create()`, `condition` viene **solo letto** per validazione cross-field (riga 443)
- **Nessuna chiamata a `listing.setCondition()`** nel metodo `create()`

**Uso nel metodo `create()` (righe 440-452):**
```java
// 🔒 Validazione cross-field: SEALED non valido con SINGLE_CARD
if (listing.getCard() != null && listing.getCard().getType() != null && 
    listing.getCondition() != null && !listing.getCondition().trim().isEmpty()) {
    String cardType = listing.getCard().getType().trim().toUpperCase();
    String listingCondition = listing.getCondition().trim().toUpperCase();
    if ("SEALED".equals(listingCondition) && "SINGLE_CARD".equals(cardType)) {
        throw new IllegalArgumentException(
            "SEALED non è valido per prodotti SINGLE_CARD. " +
            "SEALED può essere usato solo per prodotti sigillati (box, booster pack, ecc.)."
        );
    }
}
```

**Conferma:**
- `condition` viene **solo letto** per validazione cross-field
- **Nessuna normalizzazione** o **validazione** applicata nel metodo `create()`

### 2.4 Con quale normalizzazione (se esiste)?

**Risposta:** ❌ **NESSUNA NORMALIZZAZIONE**

**Analisi:**
- Nel controller (riga 111): `listing.setCondition(request.getCondition())` → **nessun trim/uppercase**
- Nel service `create()`: `condition` viene letto ma **non normalizzato**
- Solo per validazione cross-field viene applicato `trim().toUpperCase()` (riga 445), ma **non viene salvato**

**Conferma:**
- `condition` viene salvato **così com'è** dalla request (senza normalizzazione)
- Nessun `trim()`, `toUpperCase()`, o mapping applicato durante la creazione

---

## 3️⃣ Dominio: dove vive `condition`

### 3.1 Entity corretta (`Listing` o `Card`)?

**Risposta:** ✅ **SU ENTRAMBE, ma usato diversamente**

**File `Listing.java` (riga 24):**
```java
private String condition;
```

**File `Card.java` (riga 24):**
```java
private String condition; // es: MINT, NEAR_MINT, EXCELLENT, GOOD, FAIR, POOR
```

**Conferma:**
- `condition` esiste su **entrambe** le entity
- Nel flusso Sell: viene usato **solo `Listing.condition`**
- `Card.condition` esiste ma **non viene mai settato** durante la creazione listing dalla Sell

### 3.2 Tipo del campo

**Risposta:** `String` (non enum)

**File `Listing.java` (riga 24):**
```java
private String condition;
```

**File `Card.java` (riga 24):**
```java
private String condition;
```

**Conferma:**
- Tipo: `String` (non enum, non whitelist a livello di entity)

### 3.3 Nullable / non nullable

**Risposta:** ✅ **NULLABLE**

**File `Listing.java` (riga 24):**
```java
private String condition;  // Nessuna annotazione @NotNull o @Column(nullable = false)
```

**File `Card.java` (riga 24):**
```java
private String condition;  // Nessuna annotazione @NotNull o @Column(nullable = false)
```

**Conferma:**
- Entrambi i campi sono **nullable** (nessun vincolo a livello di entity o database)

### 3.4 Eventuale enum o whitelist supportata

**Risposta:** ✅ **WHITELIST ESISTE (ma non usata nel flusso Sell)**

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Righe:** 147-157

**Metodo `isValidCondition()`:**
```java
private boolean isValidCondition(String condition) {
    return "RAW".equals(condition) ||
           "MINT".equals(condition) ||
           "NEAR_MINT".equals(condition) ||
           "EXCELLENT".equals(condition) ||
           "VERY_GOOD".equals(condition) ||
           "GOOD".equals(condition) ||
           "FAIR".equals(condition) ||
           "POOR".equals(condition) ||
           "SEALED".equals(condition);
}
```

**Valori ammessi:**
- `RAW`
- `MINT`
- `NEAR_MINT`
- `EXCELLENT`
- `VERY_GOOD`
- `GOOD`
- `FAIR`
- `POOR`
- `SEALED`

**Conferma:**
- ✅ Whitelist esiste (9 valori validi)
- ❌ **NON viene usata** nel flusso Sell (`create()`)
- ✅ Viene usata solo nei metodi legacy di ricerca (`findByCondition()`, `findByCategoryAndCondition()`, ecc.)

---

## 4️⃣ Marketplace: endpoint e query

### 4.1 Parametro `condition` nel controller

**File:** `src/main/java/com/funkard/controller/ListingController.java`  
**Riga:** 51

**Definizione:**
```java
@GetMapping
public ResponseEntity<?> getAllListings(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) List<String> type,
        @RequestParam(required = false) List<String> condition,  // ← List<String>
        @RequestParam(required = false) List<String> language,
        @RequestParam(required = false) List<String> franchise,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean acceptTrades,
        Authentication authentication) {
```

**Tipo parametro:** `List<String>` (multiselect)  
**Opzionalità:** ✅ Opzionale (`required = false`)

**Conferma:**
- ✅ Endpoint accetta `condition` come `List<String>` (multiselect)
- ✅ Parametro opzionale

### 4.2 Query nel repository

**File:** `src/main/java/com/funkard/repository/ListingRepository.java`  
**Righe:** 65-89

**Query JPQL:**
```java
@Query("""
    SELECT l FROM Listing l
    WHERE (:category IS NULL OR l.card.category = :category)
    AND (:type IS NULL OR l.card.type IN :type)
    AND (:condition IS NULL OR l.condition IN :condition)  // ← l.condition
    AND (:language IS NULL OR l.card.language IN :language)
    AND (:franchise IS NULL OR l.card.franchise IN :franchise)
    ...
    """)
List<Listing> findByFilters(
    @Param("category") String category,
    @Param("type") List<String> type,
    @Param("condition") List<String> condition,  // ← List<String>
    ...
);
```

**Campo filtrato:** `l.condition` (da `Listing`, non da `Card`)  
**Confronto:** `IN :condition` (multiselect, lista valori)

**Conferma:**
- ✅ Query filtra su `l.condition` (da `Listing`)
- ✅ Usa `IN :condition` (multiselect)
- ✅ Parametro `condition` è `List<String>`

### 4.3 Normalizzazione applicata prima della query

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Righe:** 307-320

**Codice:**
```java
// Normalizzazione condition (multiselect): normalizza lista, rimuove duplicati, ordina
List<String> normalizedCondition = null;
if (condition != null && !condition.isEmpty()) {
    normalizedCondition = condition.stream()
        .filter(c -> c != null && !c.trim().isEmpty())
        .map(c -> c.trim().toUpperCase())  // ← trim() + toUpperCase()
        .distinct()
        .sorted()
        .collect(Collectors.toList());
    // Se lista risultante è vuota dopo normalizzazione, trattare come null
    if (normalizedCondition.isEmpty()) {
        normalizedCondition = null;
    }
}
```

**Normalizzazione applicata:**
- ✅ `trim()` su ogni elemento
- ✅ `toUpperCase()` su ogni elemento
- ✅ Rimozione duplicati (`distinct()`)
- ✅ Ordinamento (`sorted()`)

**Conferma:**
- ✅ Normalizzazione completa applicata prima della query
- ✅ Pattern: `trim()` + `toUpperCase()` (case-insensitive)

### 4.4 Comportamento quando `condition` è `null`

**Query JPQL:**
```java
AND (:condition IS NULL OR l.condition IN :condition)
```

**Comportamento JPQL/Hibernate:**
- Se `l.condition` è `null` e `:condition` è una lista non-null (es. `["MINT"]`):
  - `null IN ["MINT"]` → **`false`** (in JPQL, `null IN lista` restituisce `false`)
  - Listing **NON viene restituito**

**Conferma:**
- ❌ Listing con `Listing.condition = null` **NON** vengono restituiti quando viene applicato un filtro `condition`
- ✅ Solo listing con `Listing.condition` valorizzato e matchante vengono restituiti

**Esempio:**
- Listing creato dalla Sell: `Listing.condition = null`
- Filtro Marketplace: `?condition=MINT`
- Risultato: ❌ Listing **NON** viene restituito

---

## 5️⃣ Confronto Sell vs Marketplace

### 5.1 I listing creati dalla Sell hanno `condition` valorizzata?

**Risposta:** ⚠️ **DIPENDE DAL FRONTEND**

**Analisi:**
- `CreateListingRequest.condition` è **opzionale** (nessuna validazione)
- Nel controller, `listing.setCondition(request.getCondition())` viene chiamato (riga 111)
- Se il frontend invia `condition` → viene salvata
- Se il frontend **NON** invia `condition` → `Listing.condition = null`

**Conferma:**
- ✅ Se frontend invia `condition` → valorizzata
- ❌ Se frontend **NON** invia `condition` → `null`

### 5.2 Esistono casi in cui non vengono restituiti quando il filtro `condition` è attivo?

**Risposta:** ✅ **SÌ**

**Caso 1: Listing con `condition = null`**
- Listing creato dalla Sell senza `condition` → `Listing.condition = null`
- Filtro Marketplace: `?condition=MINT`
- Query: `null IN ["MINT"]` → `false`
- Risultato: ❌ **NON viene restituito**

**Caso 2: Listing con `condition` non normalizzata**
- Listing creato dalla Sell con `condition = "mint"` (lowercase)
- Filtro Marketplace: `?condition=MINT` (uppercase)
- Normalizzazione Marketplace: `["MINT"]` (uppercase)
- Query: `"mint" IN ["MINT"]` → `false` (case-sensitive in JPQL)
- Risultato: ❌ **NON viene restituito**

**Conferma:**
- ❌ Listing con `condition = null` non vengono restituiti quando filtro è attivo
- ❌ Listing con `condition` non normalizzata (case mismatch) non vengono restituiti

---

## 6️⃣ Conclusione secca

**Risposta:**

> **"I listing creati dalla Sell NON sono correttamente filtrabili per `condition` nel Marketplace"**

**Motivazione tecnica puntuale:**

1. **Asimmetria normalizzazione:**
   - **Sell:** `condition` viene salvato **così com'è** dalla request (nessuna normalizzazione)
   - **Marketplace:** Normalizza `condition` con `trim().toUpperCase()` prima della query
   - **Risultato:** Listing con `condition = "mint"` (lowercase) non matchano filtro `?condition=MINT` (uppercase)

2. **Valori null:**
   - `CreateListingRequest.condition` è **opzionale** (nessuna validazione)
   - Se frontend non invia `condition` → `Listing.condition = null`
   - Marketplace filtra su `l.condition IN :condition`
   - In JPQL, `null IN lista` restituisce `false`
   - **Risultato:** Listing con `condition = null` non vengono restituiti quando filtro è attivo

3. **Mancanza validazione:**
   - Whitelist `isValidCondition()` esiste (9 valori validi) ma **NON viene usata** nel flusso Sell
   - Valori non validi possono essere salvati (es. `"INVALID"`)
   - Marketplace normalizza ma non valida → valori non validi non matchano filtri

4. **Campo duplicato:**
   - `condition` esiste su **entrambe** `Listing` e `Card`
   - Nel flusso Sell, viene usato **solo `Listing.condition`**
   - Marketplace filtra su `l.condition` (da `Listing`)
   - **Nota:** Campo `Card.condition` esiste ma non viene mai settato durante creazione listing dalla Sell

---

## 📋 RIEPILOGO

### ✅ Stato Attuale

| Aspetto | Stato | Dettagli |
|---------|-------|----------|
| **CreateListingRequest.condition** | ✅ Presente | `String` opzionale (riga 30) |
| **Condition settato in Sell** | ✅ Sì | Su `Listing.condition` (controller riga 111) |
| **Normalizzazione in Sell** | ❌ No | Nessuna normalizzazione applicata |
| **Validazione in Sell** | ❌ No | Whitelist esiste ma non usata |
| **Dominio** | ✅ Listing | `String` nullable (riga 24) |
| **Marketplace endpoint** | ✅ Supportato | `List<String>` opzionale (riga 51) |
| **Marketplace query** | ✅ Supportato | `l.condition IN :condition` (riga 69) |
| **Normalizzazione Marketplace** | ✅ Completa | `trim()` + `toUpperCase()` (righe 310-315) |
| **Filtrabilità Sell listings** | ❌ No | Asimmetria normalizzazione + valori null |

### 🔍 Problema Identificato

**Asimmetria tra Sell e Marketplace:**
- **Sell:** `condition` viene salvato **così com'è** (senza normalizzazione)
- **Marketplace:** Normalizza `condition` con `trim().toUpperCase()` prima della query
- **Risultato:** Listing con `condition` non normalizzata (es. `"mint"` lowercase) non matchano filtri Marketplace (es. `?condition=MINT` uppercase)

**Valori null:**
- `condition` è opzionale → può essere `null`
- Marketplace filtra su `l.condition IN :condition`
- `null IN lista` → `false` → listing con `condition = null` non vengono restituiti

**Mancanza validazione:**
- Whitelist esiste ma non viene usata nel flusso Sell
- Valori non validi possono essere salvati

---

**Fine Analisi**
