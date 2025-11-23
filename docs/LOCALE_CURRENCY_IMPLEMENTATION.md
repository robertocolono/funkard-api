# 🌍 Implementazione Locale e Currency - Funkard Backend

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0

---

## ✅ Implementazione Completata

### **1. Modello User.java**

**File:** `src/main/java/com/funkard/model/User.java`

#### **Campi Aggiornati:**
```java
@Column(name = "language", length = 5)
private String language = "en";

@Column(name = "preferred_currency", nullable = false, length = 3)
private String preferredCurrency = "EUR";
```

**Stato:** ✅ **COMPLETATO**
- Annotazioni `@Column` corrette
- Valori di default impostati
- Compatibilità con database garantita

---

### **2. Migration SQL**

**File:** `src/main/resources/db/migration/V12__add_language_to_users.sql`

#### **Contenuto:**
- Aggiunge colonna `language VARCHAR(5) DEFAULT 'en'`
- Verifica e aggiunge `preferred_currency` se non esiste (retrocompatibilità)
- Aggiorna utenti esistenti con valori di default
- Crea indici per performance
- Aggiunge commenti per documentazione

**Stato:** ✅ **COMPLETATO**

---

### **3. Login Response**

**File:** `src/main/java/com/funkard/controller/AuthController.java`

#### **DTO Creato:**
```java
@Data
public class LoginResponse {
    private String token;
    private String language;
    private String preferredCurrency;
}
```

#### **Response Login:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "language": "en",
  "preferredCurrency": "EUR"
}
```

**Stato:** ✅ **COMPLETATO**
- Response include `language` e `preferredCurrency`
- Valori di default se null ("en", "EUR")
- Aggiornato anche endpoint `/register`

---

### **4. DTO Aggiornati**

#### **UserProfileDTO:**
```java
private String language;
private String preferredCurrency;
```

**Stato:** ✅ **GIÀ PRESENTE** (verificato)

#### **UserPreferencesDTO (NUOVO):**
```java
@Data
public class UserPreferencesDTO {
    private String language;
    private String preferredCurrency;
}
```

**Stato:** ✅ **COMPLETATO**

#### **RegisterRequest:**
```java
private String preferredCurrency;
private String language; // NUOVO
```

**Stato:** ✅ **COMPLETATO**

---

### **5. Endpoint Preferenze**

#### **GET /api/user/me**
- ✅ Restituisce `language` e `preferredCurrency`
- ✅ Già implementato e funzionante

#### **PUT /api/user/me**
- ✅ Permette aggiornamento `language` e `preferredCurrency`
- ✅ Già implementato e funzionante

#### **PATCH /api/user/preferences** (NUOVO)
- ✅ Endpoint dedicato per aggiornare solo `language` e `preferredCurrency`
- ✅ Validazione formato lingua (max 5 caratteri)
- ✅ Validazione valuta supportata (27 valute)
- ✅ Aggiorna solo i campi forniti nel payload
- ✅ Restituisce valori aggiornati

**Stato:** ✅ **COMPLETATO**

---

## 📋 Endpoint API

### **POST /api/auth/login**
**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "language": "en",
  "preferredCurrency": "EUR"
}
```

---

### **POST /api/auth/register**
**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "username": "username",
  "preferredCurrency": "USD",
  "language": "en",
  "acceptTerms": true,
  "acceptPrivacy": true
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "language": "en",
  "preferredCurrency": "USD"
}
```

---

### **GET /api/user/me**
**Headers:**
```
Authorization: Bearer {token}
X-User-Id: {userId}
```

**Response:**
```json
{
  "id": 1,
  "name": "Mario Rossi",
  "email": "user@example.com",
  "username": "username",
  "preferredCurrency": "EUR",
  "language": "it",
  "theme": "light",
  "avatarUrl": null,
  "createdAt": "2025-01-15T10:30:00",
  "lastLoginAt": "2025-01-15T12:00:00"
}
```

---

### **PUT /api/user/me**
**Headers:**
```
Authorization: Bearer {token}
X-User-Id: {userId}
```

**Request:**
```json
{
  "name": "Mario Rossi",
  "preferredCurrency": "USD",
  "language": "en"
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Mario Rossi",
  "preferredCurrency": "USD",
  "language": "en",
  ...
}
```

---

### **PATCH /api/user/preferences** (NUOVO)
**Headers:**
```
Authorization: Bearer {token}
X-User-Id: {userId}
```

**Request:**
```json
{
  "language": "it",
  "preferredCurrency": "EUR"
}
```

**Response:**
```json
{
  "language": "it",
  "preferredCurrency": "EUR"
}
```

**Note:**
- Aggiorna solo i campi forniti
- Valuta deve essere supportata (27 valute)
- Lingua max 5 caratteri

---

## 🔍 Valute Supportate

**27 Valute Supportate:**
- EUR, USD, GBP, JPY, CNY, CAD, AUD, CHF, SEK, NOK, DKK
- PLN, CZK, HUF, RON, BGN, HRK, RUB, TRY, BRL, MXN
- ZAR, INR, KRW, SGD, HKD, NZD

---

## 🧪 Test Post-Deploy

### **1. Test Aggiornamento Preferenze**

```bash
# Aggiorna lingua e valuta
curl -X PUT http://localhost:8080/api/user/me \
  -H "Authorization: Bearer {token}" \
  -H "X-User-Id: {userId}" \
  -H "Content-Type: application/json" \
  -d '{
    "language": "it",
    "preferredCurrency": "EUR"
  }'
```

**Verifica:**
- ✅ Response include valori aggiornati
- ✅ Database aggiornato correttamente

---

### **2. Test Recupero Profilo**

```bash
# Recupera profilo
curl -X GET http://localhost:8080/api/user/me \
  -H "Authorization: Bearer {token}" \
  -H "X-User-Id: {userId}"
```

**Verifica:**
- ✅ Response include `language` e `preferredCurrency`
- ✅ Valori corrispondono al database

---

### **3. Test Login Response**

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Verifica:**
- ✅ Response include `token`, `language`, `preferredCurrency`
- ✅ Valori corrispondono al database

---

### **4. Test PATCH Preferences**

```bash
# Aggiorna solo preferenze
curl -X PATCH http://localhost:8080/api/user/preferences \
  -H "Authorization: Bearer {token}" \
  -H "X-User-Id: {userId}" \
  -H "Content-Type: application/json" \
  -d '{
    "language": "en",
    "preferredCurrency": "USD"
  }'
```

**Verifica:**
- ✅ Response include valori aggiornati
- ✅ Solo i campi forniti vengono aggiornati
- ✅ Validazione valuta funzionante

---

## 📊 Database Schema

### **Tabella users:**
```sql
CREATE TABLE users (
    ...
    language VARCHAR(5) DEFAULT 'en',
    preferred_currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    ...
);

CREATE INDEX idx_users_language ON users(language);
CREATE INDEX idx_users_preferred_currency ON users(preferred_currency);
```

---

## ✅ Checklist Implementazione

### **Backend:**
- [x] Modello User con annotazioni `@Column`
- [x] Migration SQL per colonna `language`
- [x] DTO LoginResponse creato
- [x] AuthController aggiornato (login + register)
- [x] UserController con endpoint PATCH /preferences
- [x] Validazione valute supportate
- [x] Validazione formato lingua
- [x] RegisterRequest con campo `language`

### **Database:**
- [x] Colonna `language` con default 'en'
- [x] Colonna `preferred_currency` verificata
- [x] Indici per performance
- [x] Commenti per documentazione

### **API:**
- [x] GET /api/user/me restituisce language/currency
- [x] PUT /api/user/me aggiorna language/currency
- [x] PATCH /api/user/preferences (nuovo endpoint)
- [x] POST /api/auth/login restituisce language/currency
- [x] POST /api/auth/register restituisce language/currency

---

## 🚀 Risultato Finale

✅ **Colonne `language` e `preferred_currency` presenti e persistenti nel DB**  
✅ **Valori aggiornabili via API e visibili nel profilo**  
✅ **Il frontend riceve `language` e `preferredCurrency` già al login**  
✅ **Pronto per futura estensione multilingua e multivaluta globale**

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

