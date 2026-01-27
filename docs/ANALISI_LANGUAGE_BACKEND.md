# 📊 ANALISI: Stato Attuale `language` nel Backend

**Data:** 2025-01-XX  
**Tipo:** Analisi Stato Attuale  
**Scope:** Verifica gestione `language` in Sell e Marketplace

---

## 1️⃣ `language` in `CreateListingRequest`

**Risposta:** ✅ **PRESENTE**

**File:** `src/main/java/com/funkard/dto/CreateListingRequest.java`  
**Riga:** 64

**Definizione:**
```java
private String language; // Valore Lingua selezionato
```

**Tipo:** `String` (singolo valore, non `List<String>`)  
**Validazione:** ❌ Nessuna annotazione `@NotNull` o `@NotBlank`  
**Opzionalità:** ✅ Opzionale (campo nullable)

**Note:**
- Esiste anche `customLanguage` (riga 69) per valori personalizzati "Altro"
- Campo presente ma non obbligatorio

---

## 2️⃣ Flusso Sell: `Card.language` durante creazione listing

**Risposta:** ❌ **NON VIENE MAI SETTATO**

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Metodo:** `create(Listing listing, CreateListingRequest request, Long userId)`  
**Righe:** 366-573

**Analisi codice:**

### 2.1 Creazione Card (righe 390-415)

**Codice:**
```java
// Crea Card con category
Card card = new Card();
card.setCategory(category);
card.setType(normalizedType);
// ... altri set (name, setName, franchise)
Card savedCard = cardRepository.save(card);
```

**Conferma:**
- ❌ **Nessuna chiamata a `card.setLanguage()`**
- ❌ **`request.getLanguage()` non viene mai letto per settare `Card.language`**
- ✅ Card viene creata e salvata senza `language`

### 2.2 Gestione "Altro" (righe 465-479)

**Codice:**
```java
// Se Lingua è "Altro" e customLanguage è fornito, salva proposta
if ("Altro".equalsIgnoreCase(request.getLanguage()) && 
    request.getCustomLanguage() != null && !request.getCustomLanguage().trim().isEmpty()) {
    try {
        pendingValueService.submitPendingValue(
            PendingValue.ValueType.LANGUAGE,
            request.getCustomLanguage(),
            userId
        );
        log.info("✅ Proposta Lingua personalizzata salvata: {}", request.getCustomLanguage());
    } catch (Exception e) {
        log.warn("⚠️ Errore durante salvataggio proposta Lingua: {}", e.getMessage());
        // Non bloccare la creazione listing se la proposta fallisce
    }
}
```

**Conferma:**
- ✅ `request.getLanguage()` viene letto **SOLO** per verificare se è "Altro"
- ✅ Se "Altro", viene salvata una proposta `PendingValue` (non su `Card.language`)
- ❌ **Se `language` non è "Altro", viene completamente ignorato**
- ❌ **Nessuna normalizzazione o validazione per `language` standard**

### 2.3 Normalizzazione

**Risposta:** ❌ **NESSUNA NORMALIZZAZIONE**

**Conferma:**
- ❌ Nessun `trim()` applicato a `request.getLanguage()`
- ❌ Nessun `toUpperCase()` applicato a `request.getLanguage()`
- ❌ Nessun mapping applicato a `request.getLanguage()`
- ✅ Esiste metodo `normalizeLanguageCode()` (riga 528) ma **NON viene usato** durante creazione listing

### 2.4 Validazione

**Risposta:** ❌ **NESSUNA VALIDAZIONE**

**Conferma:**
- ❌ Nessun enum per valori validi
- ❌ Nessuna whitelist
- ❌ Nessuna chiamata a `normalizeLanguageCode()` per validare/mappare
- ✅ Metodo `normalizeLanguageCode()` esiste ma **NON viene usato** durante creazione

**Conclusione:**
- `Card.language` rimane sempre `null` per listing creati dalla Sell
- `request.getLanguage()` viene letto solo per gestire "Altro" → `PendingValue`
- Nessuna logica per salvare `language` standard su `Card.language`

---

## 3️⃣ Dominio: dove vive `language`

**Risposta:** ✅ **SU `Card`**

**File:** `src/main/java/com/funkard/model/Card.java`  
**Riga:** 35

**Definizione:**
```java
@Column(length = 50)
private String language; // es: "Italiano", "Inglese", "Giapponese"
```

**Tipo:** `String` (non enum)  
**Nullable:** ✅ **SÌ** (campo nullable, nessuna annotazione `@NotNull`)  
**Colonna DB:** `cards.language VARCHAR(50) NULL`

**Conferma:**
- ❌ `language` **NON** esiste su `Listing`
- ✅ `language` esiste **SOLO** su `Card`
- ✅ Campo nullable (può essere `null`)

---

## 4️⃣ Marketplace: endpoint e query

### 4.1 Endpoint

**File:** `src/main/java/com/funkard/controller/ListingController.java`  
**Riga:** 52

**Definizione:**
```java
@GetMapping
public ResponseEntity<?> getAllListings(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) List<String> type,
        @RequestParam(required = false) List<String> condition,
        @RequestParam(required = false) List<String> language,  // ← List<String>
        @RequestParam(required = false) List<String> franchise,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean acceptTrades
```

**Tipo parametro:** `List<String>` (multiselect)  
**Opzionalità:** ✅ Opzionale (`required = false`)

**Conferma:**
- ✅ Endpoint accetta `language` come `List<String>` (multiselect)
- ✅ Parametro opzionale

### 4.2 Query Repository

**File:** `src/main/java/com/funkard/repository/ListingRepository.java`  
**Righe:** 65-89

**Query JPQL:**
```java
@Query("""
    SELECT l FROM Listing l
    WHERE (:category IS NULL OR l.card.category = :category)
    AND (:type IS NULL OR l.card.type IN :type)
    AND (:condition IS NULL OR l.condition IN :condition)
    AND (:language IS NULL OR l.card.language IN :language)  // ← l.card.language
    AND (:franchise IS NULL OR l.card.franchise IN :franchise)
    ...
    """)
List<Listing> findByFilters(
    @Param("category") String category,
    @Param("type") List<String> type,
    @Param("condition") List<String> condition,
    @Param("language") List<String> language,  // ← List<String>
    ...
);
```

**Campo filtrato:** `l.card.language` (da `Card`, non da `Listing`)  
**Confronto:** `IN :language` (multiselect, lista valori)

**Conferma:**
- ✅ Query filtra su `l.card.language`
- ✅ Usa `IN :language` (multiselect)
- ✅ Parametro `language` è `List<String>`

### 4.3 Normalizzazione prima della query

**File:** `src/main/java/com/funkard/service/ListingService.java`  
**Righe:** 322-336

**Codice:**
```java
// Normalizzazione language (multiselect): normalizza lista, rimuove duplicati, ordina
List<String> normalizedLanguage = null;
if (language != null && !language.isEmpty()) {
    normalizedLanguage = language.stream()
        .filter(l -> l != null && !l.trim().isEmpty())
        .map(l -> normalizeLanguageCode(l.trim().toUpperCase()))
        .distinct()
        .sorted()
        .collect(Collectors.toList());
    // Se lista risultante è vuota dopo normalizzazione, trattare come null
    if (normalizedLanguage.isEmpty()) {
        normalizedLanguage = null;
    }
}
```

**Normalizzazione applicata:**
- ✅ `trim()` su ogni elemento
- ✅ `toUpperCase()` su ogni elemento
- ✅ `normalizeLanguageCode()` per mapping (es. "CHINESE (SIMPLIFIED)" → "CHINESE_SIMPLIFIED")
- ✅ Rimozione duplicati (`distinct()`)
- ✅ Ordinamento (`sorted()`)

**Metodo `normalizeLanguageCode()` (righe 528-573):**

**Mapping supportati:**
- `ENGLISH` → `ENGLISH`
- `JAPANESE` → `JAPANESE`
- `KOREAN` → `KOREAN`
- `CHINESE (SIMPLIFIED)`, `CHINESE_SIMPLIFIED`, `CHINESE SIMPLIFIED` → `CHINESE_SIMPLIFIED`
- `CHINESE (TRADITIONAL)`, `CHINESE_TRADITIONAL`, `CHINESE TRADITIONAL` → `CHINESE_TRADITIONAL`
- `ITALIAN` → `ITALIAN`
- `FRENCH` → `FRENCH`
- `GERMAN` → `GERMAN`
- `SPANISH` → `SPANISH`
- `PORTUGUESE` → `PORTUGUESE`
- `RUSSIAN` → `RUSSIAN`
- `INDONESIAN` → `INDONESIAN`
- `THAI` → `THAI`
- Default: restituisce valore uppercase così com'è (per retrocompatibilità)

**Conferma:**
- ✅ Normalizzazione completa applicata prima della query
- ✅ Mapping da nomi completi a codici normalizzati
- ✅ Gestione valori custom (restituiti uppercase così com'è)

---

## 5️⃣ Caso `Card.language = null` con filtro applicato

**Risposta:** ❌ **NON VIENE RESTITUITO**

**Query JPQL:**
```java
AND (:language IS NULL OR l.card.language IN :language)
```

**Comportamento JPQL/Hibernate:**
- Se `l.card.language` è `null` e `:language` è una lista non-null (es. `["ENGLISH"]`):
  - `null IN ["ENGLISH"]` → **`false`** (in JPQL, `null IN lista` restituisce `false`)
  - Listing **NON viene restituito**

**Conferma:**
- ❌ Listing con `Card.language = null` **NON** vengono restituiti quando viene applicato un filtro `language`
- ✅ Solo listing con `Card.language` valorizzato e matchante vengono restituiti

**Esempio:**
- Listing creato dalla Sell: `Card.language = null`
- Filtro Marketplace: `?language=ENGLISH`
- Risultato: ❌ Listing **NON** viene restituito

---

## 6️⃣ Conclusione secca

**Risposta:**

> **"I listing creati dalla Sell NON sono filtrabili correttamente per `language` nel Marketplace"**

**Motivazione:**
1. `Card.language` non viene mai settato durante creazione listing dalla Sell
2. `Card.language` rimane sempre `null` per listing creati dalla Sell
3. Marketplace filtra su `l.card.language IN :language`
4. In JPQL, `null IN lista` restituisce `false`
5. Listing con `Card.language = null` non vengono restituiti quando viene applicato un filtro `language`

---

## 📋 RIEPILOGO

### ✅ Stato Attuale

| Aspetto | Stato | Dettagli |
|---------|-------|----------|
| **CreateListingRequest.language** | ✅ Presente | `String` opzionale (riga 64) |
| **Card.language settato in Sell** | ❌ No | Mai settato durante creazione |
| **Normalizzazione in Sell** | ❌ No | Nessuna normalizzazione applicata |
| **Validazione in Sell** | ❌ No | Nessuna validazione applicata |
| **Dominio** | ✅ Card | `String` nullable (riga 35) |
| **Marketplace endpoint** | ✅ Supportato | `List<String>` opzionale (riga 52) |
| **Marketplace query** | ✅ Supportato | `l.card.language IN :language` (riga 70) |
| **Normalizzazione Marketplace** | ✅ Completa | `trim()` + `toUpperCase()` + `normalizeLanguageCode()` |
| **Filtrabilità Sell listings** | ❌ No | `Card.language = null` → non matcha filtri |

### 🔍 Problema Identificato

**Asimmetria tra Sell e Marketplace:**
- **Sell:** `request.getLanguage()` viene letto solo per gestire "Altro" → `PendingValue`, ma **NON viene salvato** su `Card.language`
- **Marketplace:** Filtra su `l.card.language IN :language`, ma listing creati dalla Sell hanno sempre `Card.language = null`

**Risultato:**
- Listing creati dalla Sell **NON sono filtrabili** per `language` nel Marketplace

---

**Fine Analisi**
