# 🔍 AUDIT COMPLETO SISTEMA MULTI-VALUTA - FUNKARD BACKEND

**Data Analisi:** 24 Novembre 2024  
**Versione Backend:** 0.0.1-SNAPSHOT  
**Spring Boot:** 3.5.6  
**Java:** 17

---

## 📋 INDICE

1. [Panoramica Generale](#panoramica-generale)
2. [File e Componenti Coinvolti](#file-e-componenti-coinvolti)
3. [Analisi Dettagliata per File](#analisi-dettagliata-per-file)
4. [Entity con Campo Currency](#entity-con-campo-currency)
5. [Endpoint API che Gestiscono Currency](#endpoint-api-che-gestiscono-currency)
6. [Analisi API Esterna ExchangeRate-API](#analisi-api-esterna-exchangerate-api)
7. [Conflitti con Nuovo Sistema (38 Valute)](#conflitti-con-nuovo-sistema-38-valute)
8. [Mappa Flussi Conversione Attuali](#mappa-flussi-conversione-attuali)
9. [Rischi e Criticità](#rischi-e-criticità)
10. [Livello di Prontezza](#livello-di-prontezza)
11. [Raccomandazioni per Integrazione](#raccomandazioni-per-integrazione)

---

## 1. 📌 PANORAMICA GENERALE

### **1.1 Stato Attuale**

Il sistema multi-valuta attuale è **parzialmente implementato** con le seguenti caratteristiche:

- ✅ **7 valute supportate:** EUR, USD, GBP, JPY, BRL, CAD, AUD
- ✅ **Whitelist centralizzata:** `SupportedCurrencies.java`
- ✅ **Validazione unificata:** Tutti i servizi usano `SupportedCurrencies.isValid()`
- ✅ **Campo currency in entità principali:** Product, Listing, Transaction, User
- ✅ **Servizio conversione:** `CurrencyConversionService` con cache (TTL 1 ora)
- ✅ **Endpoint pubblico:** `GET /api/currency/convert`
- ❌ **Nessuna conversione automatica** nel marketplace
- ❌ **Nessuna formattazione prezzi** con simboli valuta
- ⚠️ **Inconsistenza default:** Codice usa "USD", DB migration usa "EUR"

### **1.2 Nuovo Sistema Previsto**

Il nuovo sistema dovrà supportare:
- **38 valute** (vs 7 attuali)
- **USD come valuta pivot** (tutti i tassi relativi a USD)
- **1 chiamata API ogni ora** (vs chiamate on-demand attuali)
- **Cache centralizzata** con mappa completa (USD → tutte le valute)
- **Conversione automatica** nel marketplace
- **Compatibilità Stripe** (presentment currencies)

---

## 2. 📁 FILE E COMPONENTI COINVOLTI

### **2.1 File Totali Coinvolti: 24 file**

#### **Core Currency System (2 file)**
1. `src/main/java/com/funkard/currency/CurrencyConversionService.java`
2. `src/main/java/com/funkard/currency/CurrencyController.java`

#### **Configurazione (1 file)**
3. `src/main/java/com/funkard/config/SupportedCurrencies.java`

#### **Entity con Currency (6 file)**
4. `src/main/java/com/funkard/model/User.java`
5. `src/main/java/com/funkard/market/model/Product.java`
6. `src/main/java/com/funkard/model/Listing.java`
7. `src/main/java/com/funkard/model/Transaction.java`
8. `src/main/java/com/funkard/model/GradeReport.java`
9. `src/main/java/com/funkard/gradelens/GradeResult.java`

#### **Entity SENZA Currency (3 file)**
10. `src/main/java/com/funkard/market/model/MarketListing.java` ⚠️ Hardcoded EUR
11. `src/main/java/com/funkard/market/model/MarketValuation.java` ⚠️ Nessun campo currency
12. `src/main/java/com/funkard/model/UserPreferences.java` ⚠️ Nessun campo currency

#### **DTO (7 file)**
13. `src/main/java/com/funkard/dto/UserProfileDTO.java`
14. `src/main/java/com/funkard/dto/UserPreferencesDTO.java`
15. `src/main/java/com/funkard/dto/LoginResponse.java`
16. `src/main/java/com/funkard/dto/CreateListingRequest.java`
17. `src/main/java/com/funkard/dto/ListingDTO.java`
18. `src/main/java/com/funkard/dto/TransactionDTO.java`
19. `src/main/java/com/funkard/payload/RegisterRequest.java`

#### **Service (4 file)**
20. `src/main/java/com/funkard/market/service/ProductService.java`
21. `src/main/java/com/funkard/service/ListingService.java`
22. `src/main/java/com/funkard/service/TransactionService.java`
23. `src/main/java/com/funkard/service/UserService.java`

#### **Controller (2 file)**
24. `src/main/java/com/funkard/controller/AuthController.java`
25. `src/main/java/com/funkard/controller/UserController.java`

#### **Database Migrations (2 file)**
26. `src/main/resources/db/migration/V2__add_preferred_currency_to_users.sql`
27. `src/main/resources/db/migration/V22__add_currency_to_products_listings_transactions.sql`

---

## 3. 🔬 ANALISI DETTAGLIATA PER FILE

### **3.1 CurrencyConversionService.java**

**Percorso:** `src/main/java/com/funkard/currency/CurrencyConversionService.java`  
**Tipo:** `@Service` Spring  
**Righe:** 166

#### **Cosa Fa Attualmente:**
- Gestisce conversione tra valute supportate (7 valute)
- Utilizza API esterna: `https://open.er-api.com/v6/latest/{base}`
- Cache interna in-memory (ConcurrentHashMap)
- TTL cache: 3600_000 ms (1 ora)
- Chiamate API **on-demand** quando cache scaduta

#### **Supporto Multi-Valuta:**
- ✅ Supporta qualsiasi valuta base (non solo USD)
- ✅ Cache per valuta base (chiave: valuta base, valore: mappa tassi)
- ⚠️ **Problema:** Chiama API per ogni valuta base diversa
- ⚠️ **Problema:** Se 38 valute → potenzialmente 38 chiamate API

#### **Chiamate Esterne:**
- **URL:** `https://open.er-api.com/v6/latest/{base}`
- **Metodo:** GET
- **API Key:** ❌ **NON richiesta** (piano gratuito)
- **Formato risposta:** JSON con `rates` (mappa valuta → tasso)
- **Limitazioni:** Non documentate nel codice

#### **Caching:**
- **Tipo:** `ConcurrentHashMap<String, Map<String, Double>>`
- **TTL:** 3600_000 ms (1 ora)
- **Thread-safe:** ✅ Sì (ConcurrentHashMap)
- **Persistenza:** ❌ No (persa al riavvio server)
- **Distribuzione:** ❌ No (ogni istanza ha cache separata)

#### **Fallback:**
- ✅ Se API fallisce → usa cache scaduta (se disponibile)
- ✅ Se cache non disponibile → `IllegalArgumentException`
- ⚠️ **Problema:** Nessun fallback a tassi statici o default

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Metodo `convert()` può essere esteso
- ✅ **SICURO:** Cache può essere sostituita con cache centralizzata
- ⚠️ **RISCHIOSO:** Logica `isExpired()` assume chiamate on-demand
- ⚠️ **RISCHIOSO:** `fetchRates()` chiama API per ogni valuta base

#### **Compatibilità Nuovo Sistema:**
- ❌ **INCOMPATIBILE:** Assume chiamate on-demand
- ❌ **INCOMPATIBILE:** Non usa USD come pivot fisso
- ✅ **COMPATIBILE:** Struttura cache può essere riutilizzata
- ✅ **COMPATIBILE:** Metodo `convert()` può essere adattato

---

### **3.2 CurrencyController.java**

**Percorso:** `src/main/java/com/funkard/currency/CurrencyController.java`  
**Tipo:** `@RestController`  
**Righe:** 134

#### **Cosa Fa Attualmente:**
- Espone endpoint pubblico: `GET /api/currency/convert`
- Valida valute con `SupportedCurrencies.isValid()`
- Chiama `CurrencyConversionService.convert()`
- Restituisce JSON con `from`, `to`, `amount`, `converted`, `rate`

#### **Supporto Multi-Valuta:**
- ✅ Valida valute contro whitelist (7 valute)
- ⚠️ **Problema:** Whitelist hardcoded nel messaggio errore
- ⚠️ **Problema:** Non supporta 38 valute

#### **Chiamate Esterne:**
- ❌ Nessuna chiamata diretta (usa `CurrencyConversionService`)

#### **Caching:**
- ❌ Nessuna cache a livello controller
- ✅ Usa cache interna di `CurrencyConversionService`

#### **Fallback:**
- ✅ Gestisce `IllegalArgumentException` da `CurrencyConversionService`
- ✅ Restituisce 400 Bad Request con messaggio errore

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Endpoint può essere esteso
- ⚠️ **RISCHIOSO:** Messaggio errore hardcoded con lista 7 valute
- ⚠️ **RISCHIOSO:** Validazione usa `SupportedCurrencies` (7 valute)

#### **Compatibilità Nuovo Sistema:**
- ⚠️ **PARZIALMENTE COMPATIBILE:** Endpoint può rimanere
- ❌ **INCOMPATIBILE:** Validazione deve supportare 38 valute
- ❌ **INCOMPATIBILE:** Messaggio errore deve essere dinamico

---

### **3.3 SupportedCurrencies.java**

**Percorso:** `src/main/java/com/funkard/config/SupportedCurrencies.java`  
**Tipo:** Classe utility final (non istanziabile)  
**Righe:** 43

#### **Cosa Fa Attualmente:**
- Definisce whitelist centralizzata: 7 valute
- Metodo `isValid(String currency)` per validazione
- Normalizza a uppercase

#### **Supporto Multi-Valuta:**
- ❌ **LIMITATO:** Solo 7 valute (EUR, USD, GBP, JPY, BRL, CAD, AUD)
- ✅ **ESTENDIBILE:** Set può essere espanso a 38 valute

#### **Chiamate Esterne:**
- ❌ Nessuna

#### **Caching:**
- ❌ Nessuna cache (Set statico)

#### **Fallback:**
- ❌ Nessun fallback (validazione binaria)

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Classe utility, facile da estendere
- ✅ **SICURO:** Nessuna dipendenza esterna
- ✅ **SICURO:** Nessuna logica complessa

#### **Compatibilità Nuovo Sistema:**
- ✅ **COMPATIBILE:** Può essere esteso a 38 valute
- ✅ **COMPATIBILE:** Metodo `isValid()` può rimanere invariato
- ⚠️ **ATTENZIONE:** Tutti i file che usano questa classe devono essere aggiornati

---

### **3.4 Product.java**

**Percorso:** `src/main/java/com/funkard/market/model/Product.java`  
**Tipo:** `@Entity` JPA  
**Righe:** 64

#### **Cosa Fa Attualmente:**
- Entity per prodotti marketplace
- Campo `currency` (VARCHAR(3), NOT NULL, default "EUR")
- Default hardcoded: "EUR" (non usato, servizi impostano "USD")

#### **Supporto Multi-Valuta:**
- ✅ Campo `currency` presente
- ⚠️ **Problema:** Default "EUR" inconsistente con servizi ("USD")
- ✅ Supporta qualsiasi codice ISO 4217 (3 caratteri)

#### **Database:**
- **Colonna:** `currency VARCHAR(3) NOT NULL DEFAULT 'EUR'`
- **Indice:** `idx_products_currency`
- **Migration:** `V22__add_currency_to_products_listings_transactions.sql`

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Campo può supportare 38 valute senza modifiche
- ⚠️ **RISCHIOSO:** Default "EUR" deve essere allineato

#### **Compatibilità Nuovo Sistema:**
- ✅ **COMPATIBILE:** Campo currency supporta 38 valute
- ⚠️ **ATTENZIONE:** Default deve essere allineato a "USD"

---

### **3.5 Listing.java**

**Percorso:** `src/main/java/com/funkard/model/Listing.java`  
**Tipo:** `@Entity` JPA  
**Righe:** 56

#### **Cosa Fa Attualmente:**
- Entity per listings/vendite
- Campo `currency` (VARCHAR(3), NOT NULL, default "EUR")
- Default hardcoded: "EUR" (non usato, servizi impostano "USD")
- Getter/Setter manuali (non usa Lombok)

#### **Supporto Multi-Valuta:**
- ✅ Campo `currency` presente
- ⚠️ **Problema:** Default "EUR" inconsistente con servizi ("USD")
- ✅ Supporta qualsiasi codice ISO 4217 (3 caratteri)

#### **Database:**
- **Colonna:** `currency VARCHAR(3) NOT NULL DEFAULT 'EUR'`
- **Indice:** `idx_listings_currency`
- **Migration:** `V22__add_currency_to_products_listings_transactions.sql`

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Campo può supportare 38 valute senza modifiche
- ⚠️ **RISCHIOSO:** Default "EUR" deve essere allineato

#### **Compatibilità Nuovo Sistema:**
- ✅ **COMPATIBILE:** Campo currency supporta 38 valute
- ⚠️ **ATTENZIONE:** Default deve essere allineato a "USD"

---

### **3.6 Transaction.java**

**Percorso:** `src/main/java/com/funkard/model/Transaction.java`  
**Tipo:** `@Entity` JPA  
**Righe:** 33

#### **Cosa Fa Attualmente:**
- Entity per transazioni
- Campo `currency` (VARCHAR(3), NOT NULL, default "EUR")
- Default hardcoded: "EUR" (non usato, servizi impostano "USD")
- Usa Lombok `@Data`

#### **Supporto Multi-Valuta:**
- ✅ Campo `currency` presente
- ⚠️ **Problema:** Default "EUR" inconsistente con servizi ("USD")
- ✅ Supporta qualsiasi codice ISO 4217 (3 caratteri)

#### **Database:**
- **Colonna:** `currency VARCHAR(3) NOT NULL DEFAULT 'EUR'`
- **Indice:** `idx_transactions_currency`
- **Migration:** `V22__add_currency_to_products_listings_transactions.sql`

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Campo può supportare 38 valute senza modifiche
- ⚠️ **RISCHIOSO:** Default "EUR" deve essere allineato

#### **Compatibilità Nuovo Sistema:**
- ✅ **COMPATIBILE:** Campo currency supporta 38 valute
- ⚠️ **ATTENZIONE:** Default deve essere allineato a "USD"

---

### **3.7 User.java**

**Percorso:** `src/main/java/com/funkard/model/User.java`  
**Tipo:** `@Entity` JPA  
**Righe:** 92

#### **Cosa Fa Attualmente:**
- Entity per utenti
- Campo `preferredCurrency` (VARCHAR(3), NOT NULL, default "EUR")
- Default hardcoded: "EUR" (non usato, servizi impostano "USD")

#### **Supporto Multi-Valuta:**
- ✅ Campo `preferredCurrency` presente
- ⚠️ **Problema:** Default "EUR" inconsistente con servizi ("USD")
- ✅ Supporta qualsiasi codice ISO 4217 (3 caratteri)

#### **Database:**
- **Colonna:** `preferred_currency VARCHAR(3) NOT NULL DEFAULT 'EUR'`
- **Indice:** `idx_users_preferred_currency`
- **Migration:** `V2__add_preferred_currency_to_users.sql`

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Campo può supportare 38 valute senza modifiche
- ⚠️ **RISCHIOSO:** Default "EUR" deve essere allineato

#### **Compatibilità Nuovo Sistema:**
- ✅ **COMPATIBILE:** Campo preferredCurrency supporta 38 valute
- ⚠️ **ATTENZIONE:** Default deve essere allineato a "USD"

---

### **3.8 GradeReport.java**

**Percorso:** `src/main/java/com/funkard/model/GradeReport.java`  
**Tipo:** `@Entity` JPA  
**Righe:** 40

#### **Cosa Fa Attualmente:**
- Entity per report grading
- Campo `currency` (String, nullable, nessun default)
- Usato per valori stimati (valueLow, valueMid, valueHigh)

#### **Supporto Multi-Valuta:**
- ✅ Campo `currency` presente
- ⚠️ **Problema:** Nessun default, può essere null
- ✅ Supporta qualsiasi codice ISO 4217

#### **Database:**
- **Colonna:** `currency VARCHAR(3)` (nullable, nessun default esplicito)

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Campo può supportare 38 valute senza modifiche
- ⚠️ **RISCHIOSO:** Default dovrebbe essere "USD" per consistenza

#### **Compatibilità Nuovo Sistema:**
- ✅ **COMPATIBILE:** Campo currency supporta 38 valute
- ⚠️ **ATTENZIONE:** Default dovrebbe essere "USD"

---

### **3.9 GradeResult.java**

**Percorso:** `src/main/java/com/funkard/gradelens/GradeResult.java`  
**Tipo:** Classe POJO (non entity)  
**Righe:** 18

#### **Cosa Fa Attualmente:**
- Classe per risultati grading
- Campo `currency` in `ValueEstimate` (default "EUR")
- Default hardcoded: "EUR"

#### **Supporto Multi-Valuta:**
- ✅ Campo `currency` presente
- ⚠️ **Problema:** Default "EUR" inconsistente con sistema ("USD")
- ✅ Supporta qualsiasi codice ISO 4217

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Campo può supportare 38 valute senza modifiche
- ⚠️ **RISCHIOSO:** Default "EUR" deve essere allineato a "USD"

#### **Compatibilità Nuovo Sistema:**
- ✅ **COMPATIBILE:** Campo currency supporta 38 valute
- ⚠️ **ATTENZIONE:** Default deve essere allineato a "USD"

---

### **3.10 MarketListing.java** ⚠️ **PROBLEMA CRITICO**

**Percorso:** `src/main/java/com/funkard/market/model/MarketListing.java`  
**Tipo:** `@Entity` JPA  
**Righe:** 78

#### **Cosa Fa Attualmente:**
- Entity per market listings (prezzi di mercato)
- Campo `priceEUR` (double, hardcoded EUR)
- **Nessun campo currency**

#### **Supporto Multi-Valuta:**
- ❌ **NON SUPPORTATO:** Hardcoded EUR
- ❌ **NON SUPPORTATO:** Impossibile gestire altre valute
- ⚠️ **PROBLEMA CRITICO:** Blocca sistema multi-valuta per market data

#### **Database:**
- **Colonna:** `price_eur DOUBLE` (hardcoded EUR)

#### **Sicurezza da Estendere:**
- ❌ **NON SICURO:** Richiede refactoring completo
- ❌ **RISCHIOSO:** Cambio breaking per servizi che usano `priceEUR`

#### **Compatibilità Nuovo Sistema:**
- ❌ **INCOMPATIBILE:** Deve essere refactorato
- ❌ **RICHIESTO:** Aggiungere campo `currency`
- ❌ **RICHIESTO:** Rinominare `priceEUR` in `price` o aggiungere `price` + `currency`

---

### **3.11 MarketValuation.java** ⚠️ **PROBLEMA**

**Percorso:** `src/main/java/com/funkard/market/model/MarketValuation.java`  
**Tipo:** `@Entity` JPA  
**Righe:** 57

#### **Cosa Fa Attualmente:**
- Entity per valutazioni mercato
- Campi `avgPrice`, `lastSoldPrice` (Double, senza currency)
- **Nessun campo currency**

#### **Supporto Multi-Valuta:**
- ❌ **NON SUPPORTATO:** Prezzi senza valuta associata
- ⚠️ **PROBLEMA:** Impossibile sapere in quale valuta sono i prezzi

#### **Database:**
- **Colonne:** `avg_price DOUBLE`, `last_sold_price DOUBLE` (senza currency)

#### **Sicurezza da Estendere:**
- ❌ **NON SICURO:** Richiede refactoring
- ❌ **RISCHIOSO:** Cambio breaking per servizi che usano questi campi

#### **Compatibilità Nuovo Sistema:**
- ❌ **INCOMPATIBILE:** Deve essere refactorato
- ❌ **RICHIESTO:** Aggiungere campo `currency`

---

### **3.12 ProductService.java**

**Percorso:** `src/main/java/com/funkard/market/service/ProductService.java`  
**Tipo:** `@Service` Spring  
**Righe:** 221

#### **Cosa Fa Attualmente:**
- Gestisce creazione prodotti
- Valida currency con `SupportedCurrencies.isValid()`
- Default "USD" se currency null/vuoto
- Normalizza a uppercase

#### **Supporto Multi-Valuta:**
- ✅ Valida currency contro whitelist (7 valute)
- ⚠️ **Problema:** Whitelist limitata a 7 valute
- ✅ Default "USD" (allineato con nuovo sistema)

#### **Chiamate Esterne:**
- ❌ Nessuna (solo validazione)

#### **Caching:**
- ❌ Nessuna cache

#### **Fallback:**
- ✅ Default "USD" se currency null/vuoto
- ✅ `IllegalArgumentException` se currency non valida

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Logica validazione può essere estesa
- ⚠️ **RISCHIOSO:** Messaggio errore hardcoded con lista 7 valute

#### **Compatibilità Nuovo Sistema:**
- ⚠️ **PARZIALMENTE COMPATIBILE:** Deve supportare 38 valute
- ✅ **COMPATIBILE:** Default "USD" già corretto
- ⚠️ **ATTENZIONE:** Messaggio errore deve essere dinamico

---

### **3.13 ListingService.java**

**Percorso:** `src/main/java/com/funkard/service/ListingService.java`  
**Tipo:** `@Service` Spring  
**Righe:** 114

#### **Cosa Fa Attualmente:**
- Gestisce creazione listings
- Valida currency con `SupportedCurrencies.isValid()`
- Default "USD" se currency null/vuoto
- Normalizza a uppercase
- Due metodi: `create(Listing, CreateListingRequest, Long)` e `create(Listing)` (legacy)

#### **Supporto Multi-Valuta:**
- ✅ Valida currency contro whitelist (7 valute)
- ⚠️ **Problema:** Whitelist limitata a 7 valute
- ✅ Default "USD" (allineato con nuovo sistema)

#### **Chiamate Esterne:**
- ❌ Nessuna (solo validazione)

#### **Caching:**
- ❌ Nessuna cache

#### **Fallback:**
- ✅ Default "USD" se currency null/vuoto
- ✅ `IllegalArgumentException` se currency non valida

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Logica validazione può essere estesa
- ⚠️ **RISCHIOSO:** Messaggio errore hardcoded con lista 7 valute

#### **Compatibilità Nuovo Sistema:**
- ⚠️ **PARZIALMENTE COMPATIBILE:** Deve supportare 38 valute
- ✅ **COMPATIBILE:** Default "USD" già corretto
- ⚠️ **ATTENZIONE:** Messaggio errore deve essere dinamico

---

### **3.14 TransactionService.java**

**Percorso:** `src/main/java/com/funkard/service/TransactionService.java`  
**Tipo:** `@Service` Spring  
**Righe:** 35

#### **Cosa Fa Attualmente:**
- Gestisce creazione transazioni
- Valida currency con `SupportedCurrencies.isValid()`
- Default "USD" se currency null/vuoto
- Normalizza a uppercase

#### **Supporto Multi-Valuta:**
- ✅ Valida currency contro whitelist (7 valute)
- ⚠️ **Problema:** Whitelist limitata a 7 valute
- ✅ Default "USD" (allineato con nuovo sistema)

#### **Chiamate Esterne:**
- ❌ Nessuna (solo validazione)

#### **Caching:**
- ❌ Nessuna cache

#### **Fallback:**
- ✅ Default "USD" se currency null/vuoto
- ✅ `IllegalArgumentException` se currency non valida

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Logica validazione può essere estesa
- ⚠️ **RISCHIOSO:** Messaggio errore hardcoded con lista 7 valute

#### **Compatibilità Nuovo Sistema:**
- ⚠️ **PARZIALMENTE COMPATIBILE:** Deve supportare 38 valute
- ✅ **COMPATIBILE:** Default "USD" già corretto
- ⚠️ **ATTENZIONE:** Messaggio errore deve essere dinamico

---

### **3.15 UserService.java**

**Percorso:** `src/main/java/com/funkard/service/UserService.java`  
**Tipo:** `@Service` Spring  
**Righe:** ~236

#### **Cosa Fa Attualmente:**
- Gestisce aggiornamento profilo utente
- Valida currency con `isValidCurrency()` → `SupportedCurrencies.isValid()`
- Default "USD" se currency null/vuoto (fallback prima validazione)
- Normalizza a uppercase

#### **Supporto Multi-Valuta:**
- ✅ Valida currency contro whitelist (7 valute)
- ⚠️ **Problema:** Whitelist limitata a 7 valute
- ✅ Default "USD" (allineato con nuovo sistema)

#### **Chiamate Esterne:**
- ❌ Nessuna (solo validazione)

#### **Caching:**
- ❌ Nessuna cache

#### **Fallback:**
- ✅ Default "USD" se currency null/vuoto
- ✅ `IllegalArgumentException` se currency non valida

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Logica validazione può essere estesa
- ⚠️ **RISCHIOSO:** Metodo `isValidCurrency()` usa whitelist limitata

#### **Compatibilità Nuovo Sistema:**
- ⚠️ **PARZIALMENTE COMPATIBILE:** Deve supportare 38 valute
- ✅ **COMPATIBILE:** Default "USD" già corretto
- ⚠️ **ATTENZIONE:** Metodo `isValidCurrency()` deve usare nuova whitelist

---

### **3.16 AuthController.java**

**Percorso:** `src/main/java/com/funkard/controller/AuthController.java`  
**Tipo:** `@RestController`  
**Righe:** 139

#### **Cosa Fa Attualmente:**
- Gestisce registrazione e login
- Valida currency con `SupportedCurrencies.isValid()`
- Default "USD" se currency null/vuoto
- Restituisce `preferredCurrency` in `LoginResponse`

#### **Supporto Multi-Valuta:**
- ✅ Valida currency contro whitelist (7 valute)
- ⚠️ **Problema:** Whitelist limitata a 7 valute
- ✅ Default "USD" (allineato con nuovo sistema)
- ⚠️ **Problema:** Messaggio errore hardcoded con lista 7 valute

#### **Chiamate Esterne:**
- ❌ Nessuna (solo validazione)

#### **Caching:**
- ❌ Nessuna cache

#### **Fallback:**
- ✅ Default "USD" se currency null/vuoto
- ✅ 400 Bad Request se currency non valida

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Logica validazione può essere estesa
- ⚠️ **RISCHIOSO:** Messaggio errore hardcoded con lista 7 valute

#### **Compatibilità Nuovo Sistema:**
- ⚠️ **PARZIALMENTE COMPATIBILE:** Deve supportare 38 valute
- ✅ **COMPATIBILE:** Default "USD" già corretto
- ⚠️ **ATTENZIONE:** Messaggio errore deve essere dinamico

---

### **3.17 UserController.java**

**Percorso:** `src/main/java/com/funkard/controller/UserController.java`  
**Tipo:** `@RestController`  
**Righe:** ~714

#### **Cosa Fa Attualmente:**
- Gestisce aggiornamento preferenze utente
- Valida currency con `SupportedCurrencies.isValid()`
- Normalizza a uppercase
- Restituisce `preferredCurrency` in response

#### **Supporto Multi-Valuta:**
- ✅ Valida currency contro whitelist (7 valute)
- ⚠️ **Problema:** Whitelist limitata a 7 valute
- ✅ Validazione formato (3 caratteri)

#### **Chiamate Esterne:**
- ❌ Nessuna (solo validazione)

#### **Caching:**
- ❌ Nessuna cache

#### **Fallback:**
- ✅ 400 Bad Request se currency non valida
- ✅ Validazione formato valuta

#### **Sicurezza da Estendere:**
- ✅ **SICURO:** Logica validazione può essere estesa
- ⚠️ **RISCHIOSO:** Validazione usa whitelist limitata

#### **Compatibilità Nuovo Sistema:**
- ⚠️ **PARZIALMENTE COMPATIBILE:** Deve supportare 38 valute
- ✅ **COMPATIBILE:** Validazione formato già corretta
- ⚠️ **ATTENZIONE:** Validazione whitelist deve supportare 38 valute

---

## 4. 💾 ENTITY CON CAMPO CURRENCY

### **4.1 Entity con Currency (6 entity)**

| Entity | Campo | Tipo | Default | Nullable | Indice | Migration |
|--------|-------|------|---------|----------|--------|-----------|
| **User** | `preferredCurrency` | VARCHAR(3) | 'EUR' (DB) / "EUR" (entity) | NOT NULL | ✅ `idx_users_preferred_currency` | V2 |
| **Product** | `currency` | VARCHAR(3) | 'EUR' (DB) / "EUR" (entity) | NOT NULL | ✅ `idx_products_currency` | V22 |
| **Listing** | `currency` | VARCHAR(3) | 'EUR' (DB) / "EUR" (entity) | NOT NULL | ✅ `idx_listings_currency` | V22 |
| **Transaction** | `currency` | VARCHAR(3) | 'EUR' (DB) / "EUR" (entity) | NOT NULL | ✅ `idx_transactions_currency` | V22 |
| **GradeReport** | `currency` | VARCHAR(3) | NULL | ✅ Nullable | ❌ Nessuno | - |
| **GradeResult.ValueEstimate** | `currency` | String | "EUR" | N/A (POJO) | N/A | - |

### **4.2 Entity SENZA Currency (3 entity)** ⚠️ **PROBLEMI**

| Entity | Campo Prezzo | Problema | Impatto |
|--------|--------------|----------|---------|
| **MarketListing** | `priceEUR` (double) | Hardcoded EUR, nessun campo currency | ❌ Blocca sistema multi-valuta |
| **MarketValuation** | `avgPrice`, `lastSoldPrice` (Double) | Nessun campo currency | ❌ Prezzi senza valuta |
| **UserPreferences** | N/A | Nessun campo currency | ⚠️ Opzionale (non critico) |

### **4.3 Analisi Default**

**Problema Critico:** Inconsistenza default tra DB, entity e servizi

| Livello | Default | Utilizzato | Allineato |
|---------|---------|------------|-----------|
| **Database (migration)** | 'EUR' | Record esistenti | ❌ No |
| **Entity (hardcoded)** | "EUR" | Non usato (servizi sovrascrivono) | ❌ No |
| **Servizi (logica)** | "USD" | Record creati via servizi | ✅ Sì |

**Rischio:**
- Record creati direttamente nel DB → "EUR"
- Record creati via servizi → "USD"
- Inconsistenza dati

---

## 5. 🔌 ENDPOINT API CHE GESTISCONO CURRENCY

### **5.1 Endpoint che Accettano Currency in Input**

| Metodo | Endpoint | Campo | Validazione | Default |
|--------|----------|-------|-------------|---------|
| `POST` | `/api/auth/register` | `preferredCurrency` | `SupportedCurrencies.isValid()` | "USD" |
| `PUT` | `/api/user/me` | `preferredCurrency` | `SupportedCurrencies.isValid()` | "USD" (fallback) |
| `PATCH` | `/api/user/preferences` | `preferredCurrency` | `SupportedCurrencies.isValid()` | Nessuno |
| `POST` | `/api/products` | `currency` | `SupportedCurrencies.isValid()` | "USD" |
| `POST` | `/api/listings` | `currency` | `SupportedCurrencies.isValid()` | "USD" |
| `POST` | `/api/transactions` | `currency` | `SupportedCurrencies.isValid()` | "USD" |
| `GET` | `/api/currency/convert` | `from`, `to` (query params) | `SupportedCurrencies.isValid()` | Nessuno |

**Totale:** 7 endpoint

### **5.2 Endpoint che Restituiscono Currency in Output**

| Metodo | Endpoint | Campo | DTO/Entity |
|--------|----------|-------|------------|
| `POST` | `/api/auth/login` | `preferredCurrency` | `LoginResponse` |
| `GET` | `/api/user/me` | `preferredCurrency` | `UserProfileDTO` |
| `GET` | `/api/products` | `currency` | `Product` |
| `GET` | `/api/products/{id}` | `currency` | `Product` |
| `GET` | `/api/listings` | `currency` | `Listing` |
| `GET` | `/api/transactions` | `currency` | `Transaction` |
| `GET` | `/api/currency/convert` | `from`, `to`, `converted`, `rate` | Map |

**Totale:** 7 endpoint

### **5.3 Endpoint che Eseguono Conversioni**

| Metodo | Endpoint | Conversione | Servizio |
|--------|----------|-------------|----------|
| `GET` | `/api/currency/convert` | ✅ Sì | `CurrencyConversionService.convert()` |

**Totale:** 1 endpoint

**⚠️ PROBLEMA:** Nessuna conversione automatica nel marketplace

### **5.4 Endpoint che Validano Currency**

| Metodo | Endpoint | Validazione | Metodo |
|--------|----------|-------------|--------|
| `POST` | `/api/auth/register` | ✅ Sì | `SupportedCurrencies.isValid()` |
| `PUT` | `/api/user/me` | ✅ Sì | `SupportedCurrencies.isValid()` |
| `PATCH` | `/api/user/preferences` | ✅ Sì | `SupportedCurrencies.isValid()` |
| `POST` | `/api/products` | ✅ Sì | `SupportedCurrencies.isValid()` |
| `POST` | `/api/listings` | ✅ Sì | `SupportedCurrencies.isValid()` |
| `POST` | `/api/transactions` | ✅ Sì | `SupportedCurrencies.isValid()` |
| `GET` | `/api/currency/convert` | ✅ Sì | `SupportedCurrencies.isValid()` |

**Totale:** 7 endpoint

---

## 6. 🌐 ANALISI API ESTERNA EXCHANGERATE-API

### **6.1 Endpoint Chiamato**

**URL Base:** `https://open.er-api.com/v6/latest/{base}`

**Esempio:**
- `https://open.er-api.com/v6/latest/USD`
- `https://open.er-api.com/v6/latest/EUR`

**Metodo:** GET  
**Autenticazione:** ❌ **NON richiesta** (piano gratuito)

### **6.2 Formato Risposta**

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
    "AUD": 1.35,
    ...
  }
}
```

**Caratteristiche:**
- ✅ Restituisce **TUTTE le valute** in un'unica risposta
- ✅ Valuta base specificata in `base_code`
- ✅ Mappa completa `rates` con tutti i tassi relativi alla base

### **6.3 Gestione API Key**

**Stato Attuale:**
- ❌ **Nessuna API key** configurata nel codice
- ❌ **Nessuna variabile d'ambiente** per API key
- ✅ Piano gratuito (no API key richiesta)

**Rischi:**
- ⚠️ Rate limits non documentati
- ⚠️ Disponibilità dipende da servizio esterno
- ⚠️ Nessun controllo su limiti chiamate

### **6.4 Limitazioni Piano Gratuito**

**Non Documentate nel Codice:**
- ⚠️ Rate limits sconosciuti
- ⚠️ Numero massimo chiamate/giorno sconosciuto
- ⚠️ Disponibilità 24/7 non garantita
- ⚠️ SLA non garantito

**Rischi:**
- ❌ Possibile blocco se troppe chiamate
- ❌ Possibile downtime non previsto
- ❌ Nessun fallback a servizio alternativo

### **6.5 Analisi Formato Risposta**

**Struttura:**
- `result`: String ("success" o errore)
- `base_code`: String (valuta base, es. "USD")
- `rates`: Map<String, Double> (valuta → tasso)

**Esempio Risposta Completa:**
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
    "AUD": 1.35,
    "CHF": 0.92,
    "CNY": 7.2,
    "INR": 83.0,
    "MXN": 17.5,
    ... (tutte le valute supportate)
  }
}
```

**Caratteristiche:**
- ✅ **Restituisce TUTTE le valute** in un'unica risposta
- ✅ **Supporta 38+ valute** (verificare documentazione API)
- ✅ **Formato compatibile** con nuovo sistema (USD pivot)

### **6.6 Fallback ed Error Handling**

**Stato Attuale:**
- ✅ Se API fallisce → usa cache scaduta (se disponibile)
- ✅ Se cache non disponibile → `IllegalArgumentException`
- ❌ Nessun fallback a tassi statici
- ❌ Nessun fallback a servizio alternativo

**Rischi:**
- ❌ Se API down e cache vuota → conversione fallisce
- ❌ Nessun meccanismo di retry
- ❌ Nessun logging dettagliato errori API

### **6.7 Valutazione Adeguatezza per 38 Valute**

**✅ ADEGUATA:**
- ✅ Restituisce tutte le valute in un'unica risposta
- ✅ Supporta USD come base (compatibile con pivot USD)
- ✅ Formato risposta compatibile con nuovo sistema

**⚠️ LIMITAZIONI:**
- ⚠️ Rate limits non documentati
- ⚠️ Nessuna garanzia disponibilità
- ⚠️ Piano gratuito può avere limiti

**❌ RISCHI:**
- ❌ Possibile blocco se troppe chiamate
- ❌ Nessun SLA garantito
- ❌ Dipendenza da servizio esterno

### **6.8 Raccomandazioni API Esterna**

**Opzione 1: Mantenere come Fallback**
- ✅ Mantenere ExchangeRate-API come fallback
- ✅ Implementare servizio primario più affidabile
- ✅ Fallback automatico se primario fallisce

**Opzione 2: Sostituire**
- ❌ Sostituire con servizio più affidabile (es. Fixer.io, CurrencyLayer)
- ❌ Richiede API key e piano a pagamento
- ✅ Maggiore affidabilità e SLA

**Opzione 3: Isolare in Livello Astrazione**
- ✅ Creare interfaccia `ExchangeRateProvider`
- ✅ Implementare multiple provider (ExchangeRate-API, Fixer.io, etc.)
- ✅ Fallback automatico tra provider
- ✅ Facilita cambio provider futuro

**Raccomandazione:** **Opzione 3** (Isolare in livello astrazione)

---

## 7. ⚠️ CONFLITTI CON NUOVO SISTEMA (38 VALUTE)

### **7.1 Conflitti Identificati**

#### **1. Whitelist Limitata a 7 Valute**

**File Coinvolti:**
- `SupportedCurrencies.java` → Set hardcoded (7 valute)
- Tutti i servizi che usano `SupportedCurrencies.isValid()`
- Tutti i controller che validano currency

**Conflitto:**
- ❌ Nuovo sistema richiede 38 valute
- ❌ Whitelist attuale supporta solo 7 valute

**Impatto:**
- ❌ Validazione fallirà per 31 nuove valute
- ❌ Endpoint rifiuteranno valute non in whitelist

**Soluzione:**
- ✅ Estendere `SupportedCurrencies.SUPPORTED_CURRENCIES` a 38 valute
- ✅ Aggiornare tutti i file che usano questa classe

---

#### **2. Chiamate API On-Demand vs 1 Chiamata/Ora**

**File Coinvolti:**
- `CurrencyConversionService.java` → Logica `isExpired()` e `fetchRates()`

**Conflitto:**
- ❌ Sistema attuale chiama API quando cache scaduta (on-demand)
- ❌ Nuovo sistema richiede 1 chiamata ogni ora (scheduled)

**Impatto:**
- ❌ Potenzialmente 38 chiamate API se 38 valute base diverse
- ❌ Non rispetta requisito "1 chiamata ogni ora"

**Soluzione:**
- ✅ Implementare scheduled job (cron ogni ora)
- ✅ Chiamare API solo per USD (pivot)
- ✅ Salvare mappa completa (USD → tutte le valute)
- ✅ Rimuovere logica on-demand

---

#### **3. Valuta Base Variabile vs USD Pivot**

**File Coinvolti:**
- `CurrencyConversionService.java` → Metodo `convert()` accetta qualsiasi valuta base

**Conflitto:**
- ❌ Sistema attuale supporta qualsiasi valuta base (USD, EUR, GBP, etc.)
- ❌ Nuovo sistema richiede USD come pivot fisso

**Impatto:**
- ❌ Cache attuale supporta multiple valute base
- ❌ Nuovo sistema deve usare solo USD come base

**Soluzione:**
- ✅ Modificare `convert()` per usare sempre USD come base
- ✅ Convertire `fromCurrency → USD → toCurrency`
- ✅ Semplificare cache (solo USD → tutte le valute)

---

#### **4. Messaggi Errore Hardcoded**

**File Coinvolti:**
- `CurrencyController.java` → Messaggio errore con lista 7 valute
- `ProductService.java` → Messaggio errore con lista 7 valute
- `ListingService.java` → Messaggio errore con lista 7 valute
- `TransactionService.java` → Messaggio errore con lista 7 valute
- `AuthController.java` → Messaggio errore con lista 7 valute

**Conflitto:**
- ❌ Messaggi errore hardcoded con lista 7 valute
- ❌ Nuovo sistema richiede 38 valute

**Impatto:**
- ❌ Messaggi errore non aggiornati
- ❌ Confusione utenti

**Soluzione:**
- ✅ Rendere messaggi errore dinamici
- ✅ Usare `SupportedCurrencies.ORDERED` per lista valute

---

#### **5. MarketListing Hardcoded EUR**

**File Coinvolti:**
- `MarketListing.java` → Campo `priceEUR` hardcoded

**Conflitto:**
- ❌ MarketListing non supporta multi-valuta
- ❌ Nuovo sistema richiede supporto 38 valute

**Impatto:**
- ❌ Impossibile gestire market data in altre valute
- ❌ Blocca sistema multi-valuta per market listings

**Soluzione:**
- ✅ Refactoring completo `MarketListing`
- ✅ Aggiungere campo `currency`
- ✅ Rinominare `priceEUR` in `price` o aggiungere `price` + `currency`

---

#### **6. MarketValuation Senza Currency**

**File Coinvolti:**
- `MarketValuation.java` → Campi `avgPrice`, `lastSoldPrice` senza currency

**Conflitto:**
- ❌ MarketValuation non supporta multi-valuta
- ❌ Nuovo sistema richiede supporto 38 valute

**Impatto:**
- ❌ Impossibile sapere in quale valuta sono i prezzi
- ❌ Blocca sistema multi-valuta per valutazioni

**Soluzione:**
- ✅ Refactoring completo `MarketValuation`
- ✅ Aggiungere campo `currency`

---

#### **7. Nessuna Conversione Automatica**

**File Coinvolti:**
- `ProductController.java` → Restituisce prezzi nella valuta originale
- `ListingController.java` → Restituisce prezzi nella valuta originale
- Tutti i controller marketplace

**Conflitto:**
- ❌ Prezzi restituiti nella valuta originale
- ❌ Nuovo sistema richiede conversione automatica in `user.preferredCurrency`

**Impatto:**
- ❌ Utente vede prezzi in valute diverse dalla sua preferita
- ❌ Esperienza utente non ottimale

**Soluzione:**
- ✅ Implementare conversione automatica nei controller
- ✅ Usare `CurrencyConversionService` per conversione
- ✅ Aggiungere campo `convertedPrice` in response

---

#### **8. Inconsistenza Default Valuta**

**File Coinvolti:**
- Tutte le entity (User, Product, Listing, Transaction)
- Tutte le migration SQL
- Tutti i servizi

**Conflitto:**
- ❌ Default DB: "EUR"
- ❌ Default Entity: "EUR"
- ❌ Default Servizi: "USD"

**Impatto:**
- ❌ Inconsistenza tra record nuovi e vecchi
- ❌ Confusione sviluppatori

**Soluzione:**
- ✅ Allineare default a "USD" ovunque
- ✅ Aggiornare migration SQL
- ✅ Aggiornare entity default

---

## 8. 🗺️ MAPPA FLUSSI CONVERSIONE ATTUALI

### **8.1 Flusso Conversione Attuale**

```
1. Richiesta conversione: GET /api/currency/convert?from=USD&to=EUR&amount=100
   └─> CurrencyController.convert()
       ├─> Validazione valute (SupportedCurrencies.isValid())
       └─> CurrencyConversionService.convert(amount, from, to)
           ├─> Normalizza valute a uppercase
           ├─> Se stessa valuta → return amount
           ├─> Verifica cache scaduta (isExpired(from))
           │   ├─> Se scaduta:
           │   │   └─> fetchRates(from) → ExchangeRate-API
           │   │       └─> Aggiorna cache (ratesCache.put(from, rates))
           │   └─> Se non scaduta:
           │       └─> Usa cache esistente
           ├─> Recupera tassi dalla cache (ratesCache.get(from))
           ├─> Cerca tasso per valuta destinazione (rates.get(to))
           └─> Calcola conversione: amount * rate
               └─> Return converted amount
```

**Caratteristiche:**
- ✅ Chiamata API on-demand quando cache scaduta
- ✅ Cache per valuta base (chiave: valuta base)
- ⚠️ Potenzialmente multiple chiamate API (una per valuta base)

### **8.2 Flusso Nuovo Sistema Previsto**

```
1. Scheduled Job (ogni ora, cron: 0 0 * * * *)
   └─> CurrencyRateUpdateService.updateRates()
       └─> fetchRates("USD") → ExchangeRate-API
           └─> Salva mappa completa (USD → tutte le valute)
               └─> Cache centralizzata o database

2. Richiesta conversione: GET /api/currency/convert?from=EUR&to=GBP&amount=100
   └─> CurrencyController.convert()
       ├─> Validazione valute (SupportedCurrencies.isValid() - 38 valute)
       └─> CurrencyConversionService.convert(amount, from, to)
           ├─> Normalizza valute a uppercase
           ├─> Se stessa valuta → return amount
           ├─> Se from != USD:
           │   └─> Converti from → USD (usando cache USD)
           ├─> Se to != USD:
           │   └─> Converti USD → to (usando cache USD)
           └─> Calcola conversione finale
               └─> Return converted amount
```

**Caratteristiche:**
- ✅ 1 chiamata API ogni ora (scheduled)
- ✅ USD come pivot fisso
- ✅ Cache centralizzata (USD → tutte le valute)
- ✅ Nessuna chiamata API on-demand

### **8.3 Differenze Chiave**

| Aspetto | Sistema Attuale | Nuovo Sistema |
|---------|-----------------|---------------|
| **Chiamate API** | On-demand (quando cache scaduta) | Scheduled (1 ogni ora) |
| **Valuta Base** | Variabile (qualsiasi valuta) | USD fisso (pivot) |
| **Cache** | Per valuta base (multiple cache) | USD → tutte le valute (single cache) |
| **Numero Chiamate** | Potenzialmente multiple | 1 ogni ora |
| **Trigger** | Richiesta utente | Scheduled job |

---

## 9. ⚠️ RISCHI E CRITICITÀ

### **9.1 Rischi Tecnici**

#### **1. Rate Limits API Esterna**
- **Rischio:** 🔴 **ALTO**
- **Descrizione:** ExchangeRate-API piano gratuito può avere rate limits non documentati
- **Impatto:** Possibile blocco se troppe chiamate
- **Mitigazione:** Implementare scheduled job (1 chiamata/ora) invece di on-demand

#### **2. Disponibilità API Esterna**
- **Rischio:** 🟡 **MEDIO**
- **Descrizione:** Nessun SLA garantito per piano gratuito
- **Impatto:** Possibile downtime non previsto
- **Mitigazione:** Implementare fallback a cache scaduta o tassi statici

#### **3. Cache In-Memory**
- **Rischio:** 🟡 **MEDIO**
- **Descrizione:** Cache persa al riavvio server
- **Impatto:** Primo fetch dopo riavvio → chiamata API
- **Mitigazione:** Considerare cache distribuita (Redis) o persistenza

#### **4. Inconsistenza Default Valuta**
- **Rischio:** 🟡 **MEDIO**
- **Descrizione:** Default diversi tra DB ("EUR") e servizi ("USD")
- **Impatto:** Inconsistenza dati, confusione sviluppatori
- **Mitigazione:** Allineare default a "USD" ovunque

#### **5. MarketListing Hardcoded EUR**
- **Rischio:** 🔴 **ALTO**
- **Descrizione:** MarketListing non supporta multi-valuta
- **Impatto:** Blocca sistema multi-valuta per market data
- **Mitigazione:** Refactoring completo `MarketListing`

#### **6. Nessuna Conversione Automatica**
- **Rischio:** 🟡 **MEDIO**
- **Descrizione:** Prezzi restituiti nella valuta originale
- **Impatto:** Esperienza utente non ottimale
- **Mitigazione:** Implementare conversione automatica nei controller

### **9.2 Rischi Architetturali**

#### **1. Assunzione Chiamate On-Demand**
- **Rischio:** 🔴 **ALTO**
- **Descrizione:** `CurrencyConversionService` assume chiamate on-demand
- **Impatto:** Incompatibile con nuovo sistema (1 chiamata/ora)
- **Mitigazione:** Refactoring completo logica conversione

#### **2. Valuta Base Variabile**
- **Rischio:** 🟡 **MEDIO**
- **Descrizione:** Sistema attuale supporta qualsiasi valuta base
- **Impatto:** Incompatibile con nuovo sistema (USD pivot)
- **Mitigazione:** Modificare `convert()` per usare sempre USD come base

#### **3. Whitelist Limitata**
- **Rischio:** 🟡 **MEDIO**
- **Descrizione:** Whitelist supporta solo 7 valute
- **Impatto:** Validazione fallirà per 31 nuove valute
- **Mitigazione:** Estendere whitelist a 38 valute

### **9.3 Rischi Operativi**

#### **1. Scalabilità**
- **Rischio:** 🟡 **MEDIO**
- **Descrizione:** Sistema attuale non scalabile per 38 valute
- **Impatto:** Performance degradate con aumento valute
- **Mitigazione:** Implementare cache centralizzata e scheduled job

#### **2. Manutenibilità**
- **Rischio:** 🟢 **BASSO**
- **Descrizione:** Codice ben strutturato, facile da estendere
- **Impatto:** Minimo
- **Mitigazione:** Nessuna (codice già ben strutturato)

---

## 10. 📊 LIVELLO DI PRONTEZZA

### **10.1 Componenti Pronti (✅)**

| Componente | Prontezza | Note |
|------------|-----------|------|
| **Entity con Currency** | ✅ **PRONTO** | Campi currency supportano 38 valute |
| **DTO con Currency** | ✅ **PRONTO** | Campi currency supportano 38 valute |
| **Database Schema** | ✅ **PRONTO** | Colonne VARCHAR(3) supportano 38 valute |
| **Validazione Formato** | ✅ **PRONTO** | Validazione formato (3 caratteri) già corretta |
| **Default USD** | ✅ **PRONTO** | Servizi già usano "USD" come default |

### **10.2 Componenti da Estendere (⚠️)**

| Componente | Prontezza | Azione Richiesta |
|------------|-----------|------------------|
| **SupportedCurrencies** | ⚠️ **DA ESTENDERE** | Espandere Set a 38 valute |
| **CurrencyConversionService** | ⚠️ **DA RIFATTORARE** | Implementare USD pivot, scheduled job |
| **CurrencyController** | ⚠️ **DA AGGIORNARE** | Messaggio errore dinamico |
| **ProductService** | ⚠️ **DA AGGIORNARE** | Messaggio errore dinamico |
| **ListingService** | ⚠️ **DA AGGIORNARE** | Messaggio errore dinamico |
| **TransactionService** | ⚠️ **DA AGGIORNARE** | Messaggio errore dinamico |
| **UserService** | ⚠️ **DA AGGIORNARE** | Usare nuova whitelist |
| **AuthController** | ⚠️ **DA AGGIORNARE** | Messaggio errore dinamico |
| **UserController** | ⚠️ **DA AGGIORNARE** | Usare nuova whitelist |

### **10.3 Componenti da Sostituire (❌)**

| Componente | Prontezza | Azione Richiesta |
|------------|-----------|------------------|
| **Logica On-Demand** | ❌ **DA SOSTITUIRE** | Sostituire con scheduled job |
| **Cache Multi-Base** | ❌ **DA SOSTITUIRE** | Sostituire con cache USD pivot |
| **MarketListing** | ❌ **DA REFATTORARE** | Aggiungere campo currency |
| **MarketValuation** | ❌ **DA REFATTORARE** | Aggiungere campo currency |

### **10.4 Componenti da Creare (🆕)**

| Componente | Prontezza | Azione Richiesta |
|------------|-----------|------------------|
| **Scheduled Job** | 🆕 **DA CREARE** | Job per aggiornamento tassi ogni ora |
| **CurrencyRateUpdateService** | 🆕 **DA CREARE** | Servizio per aggiornamento tassi |
| **Conversione Automatica** | 🆕 **DA CREARE** | Logica conversione automatica nei controller |
| **Formattazione Prezzi** | 🆕 **DA CREARE** | Utility per formattazione prezzi con simboli |

---

## 11. 📝 RACCOMANDAZIONI PER INTEGRAZIONE

### **11.1 Cosa Può Rimanere Invariato**

#### **✅ Entity e DTO**
- ✅ Tutte le entity con campo `currency` (User, Product, Listing, Transaction, GradeReport)
- ✅ Tutti i DTO con campo `currency` (UserProfileDTO, ListingDTO, TransactionDTO, etc.)
- ✅ Database schema (colonne VARCHAR(3) supportano 38 valute)

#### **✅ Validazione Formato**
- ✅ Validazione formato valuta (3 caratteri, uppercase)
- ✅ Normalizzazione a uppercase

#### **✅ Default USD**
- ✅ Default "USD" nei servizi (già allineato con nuovo sistema)

#### **✅ Struttura Cache**
- ✅ Struttura `ConcurrentHashMap` può essere riutilizzata
- ✅ TTL 1 ora può essere mantenuto

---

### **11.2 Cosa Deve Essere Esteso**

#### **⚠️ SupportedCurrencies**
- ⚠️ Espandere `SUPPORTED_CURRENCIES` da 7 a 38 valute
- ⚠️ Aggiungere lista ordinata per messaggi errore

#### **⚠️ Validazione Whitelist**
- ⚠️ Aggiornare tutti i servizi che usano `SupportedCurrencies.isValid()`
- ⚠️ Rendere messaggi errore dinamici (usare `SupportedCurrencies.ORDERED`)

#### **⚠️ CurrencyConversionService**
- ⚠️ Modificare `convert()` per usare sempre USD come base
- ⚠️ Implementare conversione `fromCurrency → USD → toCurrency`
- ⚠️ Semplificare cache (solo USD → tutte le valute)

#### **⚠️ Controller**
- ⚠️ Aggiornare messaggi errore per essere dinamici
- ⚠️ Implementare conversione automatica nei controller marketplace

---

### **11.3 Cosa Deve Essere Sostituito**

#### **❌ Logica On-Demand**
- ❌ Rimuovere logica `isExpired()` che triggera chiamate on-demand
- ❌ Sostituire con scheduled job (cron ogni ora)

#### **❌ Cache Multi-Base**
- ❌ Rimuovere cache per multiple valute base
- ❌ Sostituire con cache single (USD → tutte le valute)

#### **❌ MarketListing**
- ❌ Refactoring completo: aggiungere campo `currency`
- ❌ Rinominare `priceEUR` in `price` o aggiungere `price` + `currency`

#### **❌ MarketValuation**
- ❌ Refactoring completo: aggiungere campo `currency`

---

### **11.4 File che Dovranno Essere Aggiornati**

#### **🔧 File da Modificare (15 file)**

1. `src/main/java/com/funkard/config/SupportedCurrencies.java` → Espandere a 38 valute
2. `src/main/java/com/funkard/currency/CurrencyConversionService.java` → USD pivot, scheduled job
3. `src/main/java/com/funkard/currency/CurrencyController.java` → Messaggio errore dinamico
4. `src/main/java/com/funkard/market/service/ProductService.java` → Messaggio errore dinamico
5. `src/main/java/com/funkard/service/ListingService.java` → Messaggio errore dinamico
6. `src/main/java/com/funkard/service/TransactionService.java` → Messaggio errore dinamico
7. `src/main/java/com/funkard/service/UserService.java` → Usare nuova whitelist
8. `src/main/java/com/funkard/controller/AuthController.java` → Messaggio errore dinamico
9. `src/main/java/com/funkard/controller/UserController.java` → Usare nuova whitelist
10. `src/main/java/com/funkard/market/model/MarketListing.java` → Aggiungere campo currency
11. `src/main/java/com/funkard/market/model/MarketValuation.java` → Aggiungere campo currency
12. `src/main/java/com/funkard/model/User.java` → Allineare default a "USD"
13. `src/main/java/com/funkard/market/model/Product.java` → Allineare default a "USD"
14. `src/main/java/com/funkard/model/Listing.java` → Allineare default a "USD"
15. `src/main/java/com/funkard/model/Transaction.java` → Allineare default a "USD"

#### **🆕 File da Creare (4 file)**

1. `src/main/java/com/funkard/currency/CurrencyRateUpdateService.java` → Servizio aggiornamento tassi
2. `src/main/java/com/funkard/scheduler/CurrencyRateUpdateScheduler.java` → Scheduled job (cron ogni ora)
3. `src/main/java/com/funkard/currency/PriceFormatter.java` → Utility formattazione prezzi
4. `src/main/java/com/funkard/currency/ExchangeRateProvider.java` → Interfaccia provider (astrazione)

#### **📝 Migration da Creare (2 file)**

1. `src/main/resources/db/migration/V23__update_currency_defaults_to_usd.sql` → Allineare default a "USD"
2. `src/main/resources/db/migration/V24__add_currency_to_market_listings_valuations.sql` → Aggiungere currency a MarketListing e MarketValuation

---

### **11.5 File che NON Devono Essere Tocati**

#### **✅ File Sicuri (NON modificare)**

1. `src/main/java/com/funkard/model/GradeReport.java` → Campo currency già presente, nullable OK
2. `src/main/java/com/funkard/gradelens/GradeResult.java` → POJO, default può essere aggiornato ma non critico
3. `src/main/java/com/funkard/dto/*.java` → DTO già supportano currency, nessuna modifica necessaria
4. `src/main/java/com/funkard/payload/RegisterRequest.java` → Campo currency già presente
5. `src/main/java/com/funkard/repository/*.java` → Repository non toccano logica currency
6. `src/main/java/com/funkard/config/CacheConfig.java` → Configurazione cache può rimanere invariata

---

### **11.6 Ordine di Implementazione Consigliato**

#### **Fase 1: Preparazione (Basso Rischio)**
1. ✅ Estendere `SupportedCurrencies` a 38 valute
2. ✅ Allineare default entity a "USD"
3. ✅ Creare migration per allineare default DB a "USD"

#### **Fase 2: Refactoring Core (Medio Rischio)**
4. ✅ Creare `CurrencyRateUpdateService` (scheduled job)
5. ✅ Modificare `CurrencyConversionService` per USD pivot
6. ✅ Sostituire cache multi-base con cache USD

#### **Fase 3: Aggiornamento Servizi (Basso Rischio)**
7. ✅ Aggiornare messaggi errore in tutti i servizi
8. ✅ Aggiornare validazione in tutti i controller

#### **Fase 4: Refactoring Market (Alto Rischio)**
9. ✅ Refactoring `MarketListing` (aggiungere currency)
10. ✅ Refactoring `MarketValuation` (aggiungere currency)

#### **Fase 5: Funzionalità Avanzate (Medio Rischio)**
11. ✅ Implementare conversione automatica nei controller
12. ✅ Creare `PriceFormatter` per formattazione prezzi

---

## 12. 📊 RIEPILOGO FINALE

### **12.1 Stato Attuale**

- ✅ **Infrastruttura base:** Pronta (entity, DTO, database)
- ⚠️ **Validazione:** Limitata a 7 valute
- ❌ **Conversione:** On-demand, non scheduled
- ❌ **Cache:** Multi-base, non USD pivot
- ❌ **Market data:** Non supporta multi-valuta

### **12.2 Prontezza per Nuovo Sistema**

- ✅ **Pronto:** 40% (infrastruttura base)
- ⚠️ **Da Estendere:** 35% (validazione, servizi)
- ❌ **Da Sostituire:** 20% (logica conversione, cache)
- ❌ **Da Creare:** 5% (scheduled job, formattazione)

### **12.3 Rischi Principali**

1. 🔴 **ALTO:** MarketListing hardcoded EUR
2. 🔴 **ALTO:** Logica on-demand incompatibile
3. 🟡 **MEDIO:** Rate limits API esterna
4. 🟡 **MEDIO:** Inconsistenza default valuta

### **12.4 Raccomandazioni Finali**

1. ✅ **Mantenere:** Entity, DTO, database schema
2. ⚠️ **Estendere:** Whitelist, validazione, messaggi errore
3. ❌ **Sostituire:** Logica on-demand, cache multi-base
4. 🆕 **Creare:** Scheduled job, formattazione prezzi

---

**Report generato il:** 24 Novembre 2024  
**Analisi completa di:** 24 file Java, 2 migration SQL, 7 endpoint API  
**Pronto per integrazione:** ⚠️ **PARZIALMENTE PRONTO** (40% pronto, 60% da modificare)

