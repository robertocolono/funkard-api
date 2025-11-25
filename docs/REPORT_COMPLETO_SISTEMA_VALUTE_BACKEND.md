# 📊 REPORT COMPLETO SISTEMA VALUTE - FUNKARD BACKEND

**Data Analisi:** 24 Novembre 2024  
**Versione Backend:** 0.0.1-SNAPSHOT  
**Java:** 17  
**Spring Boot:** 3.5.6

---

## 📋 INDICE

1. [Panoramica Generale](#panoramica-generale)
2. [Classi e Componenti](#classi-e-componenti)
3. [Entità con Campo Currency](#entità-con-campo-currency)
4. [Entità SENZA Campo Currency](#entità-senza-campo-currency)
5. [Validazione Valute](#validazione-valute)
6. [Default Valuta](#default-valuta)
7. [Conversione Valute](#conversione-valute)
8. [API Esterna Utilizzata](#api-esterna-utilizzata)
9. [Caching](#caching)
10. [Endpoint API](#endpoint-api)
11. [Database Schema](#database-schema)
12. [Problemi e Inconsistenze](#problemi-e-inconsistenze)
13. [Roadmap e Miglioramenti](#roadmap-e-miglioramenti)

---

## 1. 📌 PANORAMICA GENERALE

### **Stato Attuale**
Il backend Funkard implementa un sistema multi-valuta **parziale** con le seguenti caratteristiche:

- ✅ **Whitelist centralizzata** di 7 valute supportate
- ✅ **Validazione unificata** in tutti i punti di input
- ✅ **Campo currency** in Product, Listing, Transaction
- ✅ **Campo preferredCurrency** in User
- ✅ **Servizio conversione** con cache (TTL 1 ora)
- ✅ **Endpoint pubblico** per test conversione
- ❌ **Nessuna conversione automatica** nel marketplace
- ❌ **Nessuna formattazione** prezzi con simboli valuta
- ❌ **Inconsistenza default**: codice usa "USD", DB migration usa "EUR"

### **Valute Supportate**
7 valute ufficiali:
- `EUR` (Euro)
- `USD` (Dollaro USA) ⭐ **Default attuale nel codice**
- `GBP` (Sterlina britannica)
- `JPY` (Yen giapponese)
- `BRL` (Real brasiliano)
- `CAD` (Dollaro canadese)
- `AUD` (Dollaro australiano)

---

## 2. 🏗️ CLASSI E COMPONENTI

### **2.1 Configurazione**

#### **SupportedCurrencies.java**
**Percorso:** `src/main/java/com/funkard/config/SupportedCurrencies.java`

**Tipo:** Classe utility final (non istanziabile)

**Contenuto:**
- `SUPPORTED_CURRENCIES`: `Set<String>` con 7 valute
- `isValid(String currency)`: Metodo statico per validazione
- Normalizzazione automatica a uppercase

**Utilizzato da:**
- `CurrencyController` → Validazione query params
- `UserController` → Validazione `preferredCurrency`
- `UserService` → Validazione `preferredCurrency`
- `ProductService` → Validazione `currency`
- `ListingService` → Validazione `currency`
- `TransactionService` → Validazione `currency`
- `AuthController` → Validazione `preferredCurrency` alla registrazione

---

### **2.2 Servizio Conversione**

#### **CurrencyConversionService.java**
**Percorso:** `src/main/java/com/funkard/currency/CurrencyConversionService.java`

**Tipo:** `@Service` Spring

**Caratteristiche:**
- **Cache interna:** `ConcurrentHashMap<String, Map<String, Double>>`
- **TTL:** 3600_000 ms (1 ora)
- **API esterna:** `https://open.er-api.com/v6/latest/{base}`
- **Thread-safe:** Usa `ConcurrentHashMap`

**Metodi:**
- `isExpired(String baseCurrency)`: Verifica scadenza cache
- `fetchRates(String baseCurrency)`: Chiama API esterna
- `convert(double amount, String from, String to)`: Converte importo

**Comportamento:**
- Se cache valida → usa cache (nessuna chiamata API)
- Se cache scaduta → fetch da API e aggiorna cache
- Se API fallisce → usa cache scaduta (se disponibile)
- Se stessa valuta → ritorna importo originale

**Logging:**
- Info: Fetch API, aggiornamento cache
- Warn: Uso cache scaduta
- Error: Impossibile recuperare tassi

---

### **2.3 Controller**

#### **CurrencyController.java**
**Percorso:** `src/main/java/com/funkard/currency/CurrencyController.java`

**Tipo:** `@RestController`

**Endpoint:**
- `GET /api/currency/convert?from={currency}&to={currency}&amount={double}`

**Validazione:**
- Parametri obbligatori: `from`, `to`, `amount`
- `amount` deve essere positivo
- Valute validate con `SupportedCurrencies.isValid()`

**Response:**
```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100.0,
  "converted": 85.0,
  "rate": 0.85
}
```

**CORS:** Configurato per funkard.com e localhost

---

### **2.4 Servizi con Validazione Currency**

#### **ProductService.java**
**Percorso:** `src/main/java/com/funkard/market/service/ProductService.java`

**Metodo:** `createProduct(Product p)`

**Logica:**
- Se `currency` null/vuoto → default "USD"
- Se fornita → valida con `SupportedCurrencies.isValid()`
- Normalizza a uppercase
- Lancia `IllegalArgumentException` se non valida

**Linea:** 35-45

---

#### **ListingService.java**
**Percorso:** `src/main/java/com/funkard/service/ListingService.java`

**Metodi:**
1. `create(Listing listing, CreateListingRequest request, Long userId)`
   - Se `request.getCurrency()` null/vuoto → default "USD"
   - Valida con `SupportedCurrencies.isValid()`
   - Linea: 29-39

2. `create(Listing listing)` (legacy)
   - Se `listing.getCurrency()` null/vuoto → default "USD"
   - Valida con `SupportedCurrencies.isValid()`
   - Linea: 100-110

---

#### **TransactionService.java**
**Percorso:** `src/main/java/com/funkard/service/TransactionService.java`

**Metodo:** `create(Transaction t)`

**Logica:**
- Se `currency` null/vuoto → default "USD"
- Valida con `SupportedCurrencies.isValid()`
- Normalizza a uppercase
- Linea: 21-31

---

#### **UserService.java**
**Percorso:** `src/main/java/com/funkard/service/UserService.java`

**Metodo:** `updateUserProfile(User user, UserProfileDTO dto)`

**Logica:**
- Se `dto.getPreferredCurrency()` null/vuoto → fallback "USD" (linea 140-141)
- Valida con `isValidCurrency()` → chiama `SupportedCurrencies.isValid()`
- Linea: 139-146

**Metodo privato:**
- `isValidCurrency(String currency)`: Wrapper per `SupportedCurrencies.isValid()`
- Linea: 199-201

---

#### **AuthController.java**
**Percorso:** `src/main/java/com/funkard/controller/AuthController.java`

**Metodi:**
1. `register(RegisterRequest request)`
   - Default: "USD" (linea 61)
   - Valida con `SupportedCurrencies.isValid()` se fornita
   - Normalizza a uppercase
   - Linea: 60-69

2. `login(User request)`
   - Response include `preferredCurrency`
   - Fallback "USD" se null (linea 94, 123)

---

#### **UserController.java**
**Percorso:** `src/main/java/com/funkard/controller/UserController.java`

**Metodi:**
1. `updatePreferences(UserPreferencesDTO dto)`
   - Valida `preferredCurrency` con `SupportedCurrencies.isValid()`
   - Normalizza a uppercase
   - Linea: 421-437

2. `updateProfile(UserProfileDTO dto)`
   - Valida `preferredCurrency` con `LanguageWhitelist.isValid()` (solo per language)
   - Currency validata in `UserService.updateUserProfile()`

---

## 3. 💾 ENTITÀ CON CAMPO CURRENCY

### **3.1 User**
**Percorso:** `src/main/java/com/funkard/model/User.java`

**Campo:**
```java
@Column(name = "preferred_currency", nullable = false, length = 3)
private String preferredCurrency = "EUR";
```

**Database:**
- Tabella: `users`
- Colonna: `preferred_currency VARCHAR(3) NOT NULL DEFAULT 'EUR'`
- Migration: `V2__add_preferred_currency_to_users.sql`
- Indice: `idx_users_preferred_currency`

**Default:**
- **Entity:** "EUR" (hardcoded nel codice)
- **Database:** "EUR" (migration)
- **Servizi:** "USD" (logica applicativa) ⚠️ **INCONSISTENZA**

**Utilizzo:**
- Restituito in `GET /api/auth/login`
- Restituito in `GET /api/user/me`
- Aggiornabile in `PUT /api/user/me`
- Aggiornabile in `PATCH /api/user/preferences`

---

### **3.2 Product**
**Percorso:** `src/main/java/com/funkard/market/model/Product.java`

**Campo:**
```java
@Column(name = "currency", nullable = false, length = 3)
private String currency = "EUR";
```

**Database:**
- Tabella: `products`
- Colonna: `currency VARCHAR(3) NOT NULL DEFAULT 'EUR'`
- Migration: `V22__add_currency_to_products_listings_transactions.sql`
- Indice: `idx_products_currency`

**Default:**
- **Entity:** "EUR" (hardcoded nel codice)
- **Database:** "EUR" (migration)
- **Servizi:** "USD" (logica applicativa) ⚠️ **INCONSISTENZA**

**Validazione:**
- `ProductService.createProduct()` → valida e imposta default "USD"

---

### **3.3 Listing**
**Percorso:** `src/main/java/com/funkard/model/Listing.java`

**Campo:**
```java
@Column(name = "currency", nullable = false, length = 3)
private String currency = "EUR";
```

**Database:**
- Tabella: `listings`
- Colonna: `currency VARCHAR(3) NOT NULL DEFAULT 'EUR'`
- Migration: `V22__add_currency_to_products_listings_transactions.sql`
- Indice: `idx_listings_currency`

**Default:**
- **Entity:** "EUR" (hardcoded nel codice)
- **Database:** "EUR" (migration)
- **Servizi:** "USD" (logica applicativa) ⚠️ **INCONSISTENZA**

**Validazione:**
- `ListingService.create()` → valida e imposta default "USD" (entrambi i metodi)

**Getter/Setter:** Manuali (non usa Lombok)

---

### **3.4 Transaction**
**Percorso:** `src/main/java/com/funkard/model/Transaction.java`

**Campo:**
```java
@Column(name = "currency", nullable = false, length = 3)
private String currency = "EUR";
```

**Database:**
- Tabella: `transactions`
- Colonna: `currency VARCHAR(3) NOT NULL DEFAULT 'EUR'`
- Migration: `V22__add_currency_to_products_listings_transactions.sql`
- Indice: `idx_transactions_currency`

**Default:**
- **Entity:** "EUR" (hardcoded nel codice)
- **Database:** "EUR" (migration)
- **Servizi:** "USD" (logica applicativa) ⚠️ **INCONSISTENZA**

**Validazione:**
- `TransactionService.create()` → valida e imposta default "USD"

---

## 4. ❌ ENTITÀ SENZA CAMPO CURRENCY

### **4.1 MarketListing**
**Percorso:** `src/main/java/com/funkard/market/model/MarketListing.java`

**Campo prezzo:**
```java
@Column(name = "price_eur")
private double priceEUR;
```

**Problema:** Hardcoded in EUR, nessun campo currency

**Utilizzato da:**
- `TrendService.getLastSoldPrice()`
- `MarketValuationService` (calcolo avgPrice, lastSoldPrice)

**Impatto:** Impossibile gestire prezzi in altre valute per market listings

---

### **4.2 MarketValuation**
**Percorso:** `src/main/java/com/funkard/market/model/MarketValuation.java`

**Campi prezzo:**
```java
private Double avgPrice;
private Double lastSoldPrice;
```

**Problema:** Nessun campo currency, prezzi senza valuta associata

**Utilizzato da:**
- `MarketValuationService` (calcolo valutazioni)

---

### **4.3 UserCard**
**Percorso:** `src/main/java/com/funkard/model/UserCard.java`

**Campo valore:**
```java
private Double estimatedValue;
```

**Problema:** Nessun campo currency per `estimatedValue`

**Impatto:** Valore stimato senza valuta associata

---

### **4.4 Card**
**Percorso:** `src/main/java/com/funkard/model/Card.java`

**Campo valore:**
```java
private Double marketValue = 0.0;
```

**Problema:** Nessun campo currency per `marketValue`

**Impatto:** Valore di mercato senza valuta associata

---

### **4.5 GradeReport**
**Percorso:** `src/main/java/com/funkard/model/GradeReport.java`

**Campi valore:**
```java
private double valueLow;
private double valueMid;
private double valueHigh;
private String currency;
```

**Stato:** ✅ **HA campo currency** (String)

**Default:** Nessun default esplicito nel codice

**Utilizzato da:** Sistema grading

---

### **4.6 GradeResult.ValueEstimate**
**Percorso:** `src/main/java/com/funkard/gradelens/GradeResult.java`

**Campi valore:**
```java
public double low;
public double mid;
public double high;
public String currency = "EUR";
```

**Stato:** ✅ **HA campo currency** (String, default "EUR")

**Problema:** Default "EUR" non allineato con sistema (dovrebbe essere "USD")

---

### **4.7 Wishlist**
**Percorso:** `src/main/java/com/funkard/model/Wishlist.java`

**Stato:** ❌ Nessun campo prezzo o currency

**Impatto:** Wishlist non ha informazioni di prezzo

---

## 5. ✅ VALIDAZIONE VALUTE

### **5.1 Whitelist Centralizzata**

**Classe:** `SupportedCurrencies.java`

**Valute supportate:** 7 valute
- EUR, USD, GBP, JPY, BRL, CAD, AUD

**Metodo validazione:**
```java
public static boolean isValid(String currency)
```

**Comportamento:**
- Normalizza a uppercase
- Verifica null/empty
- Controlla presenza in `SUPPORTED_CURRENCIES`

---

### **5.2 Punti di Validazione**

#### **Registrazione Utente**
- **File:** `AuthController.register()`
- **Validazione:** `SupportedCurrencies.isValid()`
- **Default:** "USD" se null/vuoto
- **Errore:** 400 Bad Request con messaggio

#### **Aggiornamento Profilo**
- **File:** `UserService.updateUserProfile()`
- **Validazione:** `SupportedCurrencies.isValid()` (via `isValidCurrency()`)
- **Default:** "USD" se null/vuoto (fallback prima validazione)
- **Errore:** `IllegalArgumentException`

#### **Aggiornamento Preferenze**
- **File:** `UserController.updatePreferences()`
- **Validazione:** `SupportedCurrencies.isValid()`
- **Errore:** 400 Bad Request con Map

#### **Creazione Prodotto**
- **File:** `ProductService.createProduct()`
- **Validazione:** `SupportedCurrencies.isValid()`
- **Default:** "USD" se null/vuoto
- **Errore:** `IllegalArgumentException`

#### **Creazione Listing**
- **File:** `ListingService.create()` (entrambi i metodi)
- **Validazione:** `SupportedCurrencies.isValid()`
- **Default:** "USD" se null/vuoto
- **Errore:** `IllegalArgumentException`

#### **Creazione Transazione**
- **File:** `TransactionService.create()`
- **Validazione:** `SupportedCurrencies.isValid()`
- **Default:** "USD" se null/vuoto
- **Errore:** `IllegalArgumentException`

#### **Conversione Valute**
- **File:** `CurrencyController.convert()`
- **Validazione:** `SupportedCurrencies.isValid()` (from e to)
- **Errore:** 400 Bad Request con messaggio

---

### **5.3 Consistenza Validazione**

✅ **Tutti i punti di validazione usano `SupportedCurrencies.isValid()`**

✅ **Normalizzazione uniforme:** Tutti normalizzano a uppercase

✅ **Messaggi di errore:** Consistenti con lista valute supportate

---

## 6. 🔧 DEFAULT VALUTA

### **6.1 Inconsistenza Critica**

#### **Problema: Default Diversi tra Codice e Database**

| Componente | Default Codice | Default Database | Default Servizi |
|------------|----------------|------------------|-----------------|
| **User.preferredCurrency** | "EUR" (entity) | "EUR" (migration) | "USD" (AuthController, UserService) |
| **Product.currency** | "EUR" (entity) | "EUR" (migration) | "USD" (ProductService) |
| **Listing.currency** | "EUR" (entity) | "EUR" (migration) | "USD" (ListingService) |
| **Transaction.currency** | "EUR" (entity) | "EUR" (migration) | "USD" (TransactionService) |

**Impatto:**
- Nuovi record creati via servizi → default "USD"
- Record esistenti nel DB → default "EUR" (migration)
- Entity default → "EUR" (non usato se servizi impostano "USD")

**Rischio:**
- Inconsistenza tra record nuovi e vecchi
- Confusione per sviluppatori
- Possibili bug se entity viene salvata direttamente (bypass servizi)

---

### **6.2 Default per Componente**

#### **User.preferredCurrency**
- **Registrazione:** "USD" (AuthController linea 61)
- **Login response:** "USD" se null (AuthController linea 94, 123)
- **Update profile:** "USD" se null (UserService linea 141)
- **Entity default:** "EUR" (User.java linea 62) ⚠️ **NON USATO**

#### **Product.currency**
- **Creazione:** "USD" se null/vuoto (ProductService linea 37)
- **Entity default:** "EUR" (Product.java linea 28) ⚠️ **NON USATO**

#### **Listing.currency**
- **Creazione:** "USD" se null/vuoto (ListingService linea 38, 102)
- **Entity default:** "EUR" (Listing.java linea 22) ⚠️ **NON USATO**

#### **Transaction.currency**
- **Creazione:** "USD" se null/vuoto (TransactionService linea 23)
- **Entity default:** "EUR" (Transaction.java linea 28) ⚠️ **NON USATO**

---

## 7. 💱 CONVERSIONE VALUTE

### **7.1 Servizio Conversione**

#### **CurrencyConversionService**
**Percorso:** `src/main/java/com/funkard/currency/CurrencyConversionService.java`

**API Esterna:**
- URL: `https://open.er-api.com/v6/latest/{base}`
- Metodo: GET
- Formato risposta: JSON
- Endpoint pubblico (gratuito, no API key richiesta)

**Struttura risposta API:**
```json
{
  "result": "success",
  "base_code": "USD",
  "rates": {
    "EUR": 0.85,
    "GBP": 0.73,
    "JPY": 110.0,
    ...
  }
}
```

**Cache:**
- Tipo: `ConcurrentHashMap<String, Map<String, Double>>`
- TTL: 3600_000 ms (1 ora)
- Timestamp: `ConcurrentHashMap<String, Long>`
- Thread-safe: ✅ Sì

**Metodi:**
1. `isExpired(String baseCurrency)`: Verifica scadenza
2. `fetchRates(String baseCurrency)`: Chiama API
3. `convert(double amount, String from, String to)`: Converte importo

**Gestione Errori:**
- Se API fallisce → usa cache scaduta (se disponibile)
- Se cache non disponibile → `IllegalArgumentException`
- Logging completo (info, warn, error)

---

### **7.2 Endpoint Conversione**

#### **GET /api/currency/convert**
**Controller:** `CurrencyController.java`

**Query Parameters:**
- `from` (obbligatorio): Valuta di origine
- `to` (obbligatorio): Valuta di destinazione
- `amount` (obbligatorio): Importo da convertire

**Validazione:**
- Parametri obbligatori
- `amount` >= 0
- Valute validate con `SupportedCurrencies.isValid()`

**Response:**
```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100.0,
  "converted": 85.0,
  "rate": 0.85
}
```

**Errori:**
- 400: Parametri mancanti, valute non supportate, amount negativo
- 500: Errore interno conversione

---

### **7.3 Integrazione nel Marketplace**

**Stato Attuale:** ❌ **NESSUNA integrazione automatica**

**Punti dove potrebbe essere usata:**
- `ProductController.getAllProducts()` → Converti prezzi in `user.preferredCurrency`
- `ListingController.getAllListings()` → Converti prezzi in `user.preferredCurrency`
- `ProductController.getProduct()` → Converti prezzo in `user.preferredCurrency`
- `TransactionController.create()` → Converti prezzo se valute diverse

**Problema:** I prezzi vengono restituiti nella valuta originale senza conversione

---

## 8. 🌐 API ESTERNA UTILIZZATA

### **8.1 ExchangeRate-API**

**URL Base:** `https://open.er-api.com/v6/latest/{base}`

**Tipo:** API pubblica gratuita

**Endpoint utilizzato:**
- `GET https://open.er-api.com/v6/latest/USD`
- `GET https://open.er-api.com/v6/latest/EUR`
- `GET https://open.er-api.com/v6/latest/{base}` (qualsiasi valuta base)

**Formato risposta:**
```json
{
  "result": "success",
  "base_code": "USD",
  "rates": {
    "EUR": 0.85,
    "GBP": 0.73,
    "JPY": 110.0,
    "BRL": 5.2,
    "CAD": 1.25,
    "AUD": 1.35
  }
}
```

**Limitazioni:**
- Nessuna API key richiesta (piano gratuito)
- Rate limit: Non specificato nel codice
- Disponibilità: Dipende da servizio esterno

**Gestione Errori:**
- `RestClientException` → Usa cache scaduta (se disponibile)
- Cache non disponibile → `IllegalArgumentException`

---

### **8.2 Chiamate Esterne**

**Dove viene chiamata:**
- `CurrencyConversionService.fetchRates()` → Chiama API quando cache scaduta

**Frequenza:**
- Massimo 1 chiamata per valuta base ogni ora (TTL cache)
- Se stessa valuta base richiesta → usa cache (nessuna chiamata)

**RestTemplate:**
- Configurazione: `new RestTemplate()` (default)
- Timeout: Non configurato (usa default Spring)

---

## 9. 💾 CACHING

### **9.1 Cache Conversione Valute**

**Implementazione:** Cache interna in-memory

**Struttura:**
```java
// Cache tassi di cambio
Map<String, Map<String, Double>> ratesCache = new ConcurrentHashMap<>();
// Chiave: valuta base (es. "USD")
// Valore: mappa valuta -> tasso (es. {"EUR": 0.85, "GBP": 0.73})

// Timestamp cache
Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
// Chiave: valuta base (es. "USD")
// Valore: timestamp ultimo aggiornamento (ms)
```

**TTL:** 3600_000 ms (1 ora)

**Thread-safety:** ✅ `ConcurrentHashMap` (thread-safe)

**Strategia:**
- Cache per valuta base (es. "USD" → tutti i tassi relativi a USD)
- Se cache scaduta → fetch da API
- Se API fallisce → usa cache scaduta (se disponibile)

**Problemi:**
- Cache in-memory → persa al riavvio server
- Nessun limite dimensione → potrebbe crescere indefinitamente
- Nessuna cache distribuita → ogni istanza ha cache separata

---

### **9.2 Cache Marketplace**

**Caffeine Cache:**
- Configurato in `CacheConfig.java`
- TTL: 25 secondi
- Max size: 500 entries

**Endpoint cached:**
- `GET /api/franchises` → `FranchiseJsonService.getAllFranchises()`
- Altri endpoint read-only (da verificare)

**Nessuna cache per:**
- Prezzi prodotti/listings (non cached)
- Conversioni valute (cache interna separata)

---

## 10. 🔌 ENDPOINT API

### **10.1 Endpoint Currency**

#### **GET /api/currency/convert**
**Controller:** `CurrencyController`
**Autenticazione:** ❌ Pubblico
**Metodo:** GET
**Query params:**
- `from` (String, obbligatorio)
- `to` (String, obbligatorio)
- `amount` (Double, obbligatorio)

**Response:**
```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100.0,
  "converted": 85.0,
  "rate": 0.85
}
```

**Errori:**
- 400: Parametri mancanti, valute non supportate, amount negativo
- 500: Errore interno conversione

---

### **10.2 Endpoint che Restituiscono Currency**

#### **POST /api/auth/login**
**Response:** `LoginResponse`
```json
{
  "token": "...",
  "language": "en",
  "preferredCurrency": "USD"
}
```

#### **GET /api/user/me**
**Response:** `UserProfileDTO`
```json
{
  "id": 1,
  "preferredCurrency": "USD",
  "language": "en",
  ...
}
```

#### **GET /api/products**
**Response:** `List<Product>`
- Include `currency` field

#### **GET /api/listings**
**Response:** `List<Listing>`
- Include `currency` field (se mappato correttamente)

#### **GET /api/transactions**
**Response:** `List<Transaction>`
- Include `currency` field

---

### **10.3 Endpoint che Accettano Currency**

#### **POST /api/auth/register**
**Request:** `RegisterRequest`
- Campo: `preferredCurrency` (opzionale, default "USD")

#### **PUT /api/user/me**
**Request:** `UserProfileDTO`
- Campo: `preferredCurrency` (opzionale)

#### **PATCH /api/user/preferences**
**Request:** `UserPreferencesDTO`
- Campo: `preferredCurrency` (opzionale)

#### **POST /api/products**
**Request:** `Product` (JSON)
- Campo: `currency` (opzionale, default "USD")

#### **POST /api/listings**
**Request:** `CreateListingRequest`
- Campo: `currency` (opzionale, default "USD")

#### **POST /api/transactions**
**Request:** `Transaction` (JSON)
- Campo: `currency` (opzionale, default "USD")

---

## 11. 🗄️ DATABASE SCHEMA

### **11.1 Tabelle con Campo Currency**

#### **users**
```sql
preferred_currency VARCHAR(3) NOT NULL DEFAULT 'EUR'
```
- Migration: `V2__add_preferred_currency_to_users.sql`
- Indice: `idx_users_preferred_currency`
- Default DB: 'EUR' ⚠️ **Diverso da default codice ("USD")**

#### **products**
```sql
currency VARCHAR(3) NOT NULL DEFAULT 'EUR'
```
- Migration: `V22__add_currency_to_products_listings_transactions.sql`
- Indice: `idx_products_currency`
- Default DB: 'EUR' ⚠️ **Diverso da default codice ("USD")**

#### **listings**
```sql
currency VARCHAR(3) NOT NULL DEFAULT 'EUR'
```
- Migration: `V22__add_currency_to_products_listings_transactions.sql`
- Indice: `idx_listings_currency`
- Default DB: 'EUR' ⚠️ **Diverso da default codice ("USD")**

#### **transactions**
```sql
currency VARCHAR(3) NOT NULL DEFAULT 'EUR'
```
- Migration: `V22__add_currency_to_products_listings_transactions.sql`
- Indice: `idx_transactions_currency`
- Default DB: 'EUR' ⚠️ **Diverso da default codice ("USD")**

---

### **11.2 Tabelle SENZA Campo Currency**

#### **market_listings**
- Campo: `price_eur` (hardcoded EUR)
- Problema: Impossibile gestire altre valute

#### **market_valuation**
- Campi: `avg_price`, `last_sold_price` (senza currency)
- Problema: Prezzi senza valuta associata

#### **user_cards**
- Campo: `estimated_value` (senza currency)
- Problema: Valore stimato senza valuta

#### **cards**
- Campo: `market_value` (senza currency)
- Problema: Valore di mercato senza valuta

---

## 12. ⚠️ PROBLEMI E INCONSISTENZE

### **12.1 Inconsistenza Default Valuta**

**Problema Critico:** Default diversi tra codice e database

| Livello | Default | Impatto |
|---------|---------|---------|
| **Database (migration)** | "EUR" | Record esistenti e nuovi (se bypass servizi) |
| **Entity (hardcoded)** | "EUR" | Non usato (servizi sovrascrivono) |
| **Servizi (logica)** | "USD" | Record creati via servizi |

**Rischi:**
- Record creati direttamente nel DB → "EUR"
- Record creati via servizi → "USD"
- Inconsistenza dati
- Confusione sviluppatori

**Soluzione Consigliata:**
- Allineare default a "USD" ovunque (migration, entity, servizi)
- Oppure allineare a "EUR" ovunque

---

### **12.2 Entity Default Non Utilizzati**

**Problema:** Entity hanno default "EUR" ma servizi impostano sempre "USD"

**Impatto:**
- Default entity non ha effetto
- Se entity viene salvata direttamente (bypass servizi) → usa "EUR"
- Inconsistenza potenziale

**Soluzione Consigliata:**
- Rimuovere default da entity (lasciare null)
- Oppure allineare default entity a "USD"

---

### **12.3 MarketListing Hardcoded EUR**

**Problema:** `MarketListing.priceEUR` hardcoded in EUR

**Impatto:**
- Impossibile gestire prezzi in altre valute
- Limitazione per marketplace multi-valuta

**Soluzione Consigliata:**
- Aggiungere campo `currency` a `MarketListing`
- Oppure convertire `priceEUR` in `price` + `currency`

---

### **12.4 Nessuna Conversione Automatica**

**Problema:** Prezzi restituiti nella valuta originale senza conversione

**Impatto:**
- Utente vede prezzi in valute diverse dalla sua preferita
- Esperienza utente non ottimale

**Soluzione Consigliata:**
- Convertire prezzi in `user.preferredCurrency` nelle response
- Usare `CurrencyConversionService` per conversione

---

### **12.5 Nessuna Formattazione Prezzi**

**Problema:** Prezzi restituiti come numeri senza simbolo valuta

**Impatto:**
- Frontend deve formattare manualmente
- Inconsistenza formattazione

**Soluzione Consigliata:**
- Creare `PriceFormatter` per formattare con simbolo valuta
- Mappatura codici → simboli (EUR → €, USD → $, etc.)

---

### **12.6 Cache In-Memory**

**Problema:** Cache persa al riavvio server

**Impatto:**
- Primo fetch dopo riavvio → chiamata API
- Nessuna persistenza cache

**Soluzione Consigliata:**
- Cache distribuita (Redis) per multi-istanza
- Oppure accettare cache in-memory (OK per single instance)

---

### **12.7 GradeReport e GradeResult Default EUR**

**Problema:** `GradeResult.ValueEstimate.currency` default "EUR"

**Impatto:**
- Inconsistenza con default sistema ("USD")

**Soluzione Consigliata:**
- Allineare default a "USD"
- Oppure usare `user.preferredCurrency`

---

## 13. 🗺️ ROADMAP E MIGLIORAMENTI

### **13.1 Priorità Alta**

#### **1. Allineare Default Valuta**
- **Problema:** Inconsistenza tra DB ("EUR") e codice ("USD")
- **Soluzione:**
  - Opzione A: Cambiare default DB a "USD" (nuova migration)
  - Opzione B: Cambiare default servizi a "EUR"
  - **Raccomandazione:** Opzione A (USD è più internazionale)

#### **2. Rimuovere Default Entity**
- **Problema:** Default entity non usati, creano confusione
- **Soluzione:**
  - Rimuovere `= "EUR"` da entity
  - Lasciare null (servizi impostano sempre)

#### **3. Aggiungere Currency a MarketListing**
- **Problema:** `priceEUR` hardcoded
- **Soluzione:**
  - Aggiungere campo `currency` a `MarketListing`
  - Migration per aggiungere colonna
  - Aggiornare servizi che usano `MarketListing`

---

### **13.2 Priorità Media**

#### **4. Conversione Automatica Prezzi**
- **Problema:** Prezzi non convertiti in `user.preferredCurrency`
- **Soluzione:**
  - Intercettare response `ProductController`, `ListingController`
  - Convertire prezzi usando `CurrencyConversionService`
  - Aggiungere campo `convertedPrice` e `originalPrice` in response

#### **5. Formattazione Prezzi**
- **Problema:** Nessuna formattazione con simbolo valuta
- **Soluzione:**
  - Creare `PriceFormatter` utility
  - Mappatura codici → simboli
  - Metodo `format(double amount, String currency, String locale)`

#### **6. Cache Distribuita**
- **Problema:** Cache in-memory persa al riavvio
- **Soluzione:**
  - Integrare Redis per cache distribuita
  - Oppure accettare cache in-memory (OK per single instance)

---

### **13.3 Priorità Bassa**

#### **7. Aggiungere Currency a UserCard, Card**
- **Problema:** `estimatedValue`, `marketValue` senza currency
- **Soluzione:**
  - Aggiungere campo `currency` a entità
  - Migration per colonne

#### **8. Storico Tassi di Cambio**
- **Problema:** Nessun audit log conversioni
- **Soluzione:**
  - Tabella `exchange_rates_history`
  - Log ogni fetch da API

#### **9. Rate Limiting API**
- **Problema:** Nessun rate limiting configurato
- **Soluzione:**
  - Configurare timeout RestTemplate
  - Implementare retry con backoff

---

## 14. 📊 RIEPILOGO STATO ATTUALE

### **✅ Implementato**

| Componente | Stato | Dettagli |
|------------|-------|----------|
| **Whitelist centralizzata** | ✅ | `SupportedCurrencies` (7 valute) |
| **Validazione unificata** | ✅ | Tutti i servizi usano `SupportedCurrencies.isValid()` |
| **Campo currency in Product** | ✅ | Entity + DB + validazione |
| **Campo currency in Listing** | ✅ | Entity + DB + validazione |
| **Campo currency in Transaction** | ✅ | Entity + DB + validazione |
| **Campo preferredCurrency in User** | ✅ | Entity + DB + validazione |
| **Servizio conversione** | ✅ | `CurrencyConversionService` con cache |
| **Endpoint conversione** | ✅ | `GET /api/currency/convert` |
| **Cache conversione** | ✅ | In-memory, TTL 1 ora |
| **API esterna** | ✅ | ExchangeRate-API integrata |

---

### **❌ Non Implementato**

| Componente | Stato | Dettagli |
|------------|-------|----------|
| **Conversione automatica** | ❌ | Prezzi non convertiti in `user.preferredCurrency` |
| **Formattazione prezzi** | ❌ | Nessun formatter con simboli valuta |
| **Currency in MarketListing** | ❌ | Hardcoded EUR |
| **Currency in MarketValuation** | ❌ | Prezzi senza valuta |
| **Currency in UserCard** | ❌ | `estimatedValue` senza valuta |
| **Currency in Card** | ❌ | `marketValue` senza valuta |
| **Cache distribuita** | ❌ | Solo in-memory |
| **Storico tassi** | ❌ | Nessun audit log |

---

### **⚠️ Problemi Identificati**

| Problema | Gravità | Impatto |
|----------|---------|---------|
| **Default inconsistente (EUR vs USD)** | 🔴 Alta | Inconsistenza dati, confusione |
| **Entity default non usati** | 🟡 Media | Confusione sviluppatori |
| **MarketListing hardcoded EUR** | 🟡 Media | Limitazione multi-valuta |
| **Nessuna conversione automatica** | 🟡 Media | Esperienza utente non ottimale |
| **Nessuna formattazione** | 🟢 Bassa | Frontend deve formattare |
| **Cache in-memory** | 🟢 Bassa | OK per single instance |

---

## 15. 📝 CONCLUSIONI

### **Stato Generale: ⚠️ PARZIALE**

Il sistema multi-valuta è **parzialmente implementato**:

**Punti di Forza:**
- ✅ Whitelist centralizzata e validazione unificata
- ✅ Campo currency in entità principali (Product, Listing, Transaction, User)
- ✅ Servizio conversione funzionante con cache
- ✅ Endpoint pubblico per test

**Punti di Debolezza:**
- ❌ Inconsistenza default (EUR vs USD)
- ❌ Nessuna conversione automatica nel marketplace
- ❌ Nessuna formattazione prezzi
- ❌ Alcune entità senza campo currency (MarketListing, MarketValuation, UserCard, Card)

**Raccomandazioni Immediate:**
1. **Allineare default valuta** (scelta: USD ovunque)
2. **Rimuovere default entity** (lasciare null, servizi impostano)
3. **Aggiungere conversione automatica** nei controller marketplace
4. **Aggiungere formattazione prezzi** con simboli valuta

**Roadmap Completa:**
- STEP 4: Conversione automatica prezzi nel marketplace
- STEP 5: Formattazione prezzi con simboli valuta
- STEP 6: Aggiungere currency a MarketListing, MarketValuation
- STEP 7: Cache distribuita (Redis) per multi-istanza

---

**Report generato il:** 24 Novembre 2024  
**Analisi completa di:** 24 file Java, 2 migration SQL, 7 DTO

