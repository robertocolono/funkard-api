# 👤 API Profilo Utente - Funkard Backend

## 📋 **Panoramica**

Sistema completo per la gestione del profilo utente e degli indirizzi di spedizione nel backend Funkard.

---

## 🎯 **Endpoint Principali**

### **👤 Profilo Utente**

#### `GET /api/user/me`
Ottieni il profilo dell'utente corrente.

**Headers:**
```
X-User-Id: 123
```

**Response 200:**
```json
{
  "id": 123,
  "name": "Mario Rossi",
  "email": "mario@example.com",
  "username": "mario_rossi",
  "preferredCurrency": "EUR",
  "language": "it",
  "theme": "light",
  "avatarUrl": "https://example.com/avatar.jpg",
  "createdAt": "2025-01-17T10:30:00",
  "lastLoginAt": "2025-01-17T15:45:00"
}
```

#### `PUT /api/user/me`
Aggiorna il profilo dell'utente corrente.

**Headers:**
```
X-User-Id: 123
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Mario Rossi",
  "preferredCurrency": "USD",
  "language": "en",
  "theme": "dark",
  "avatarUrl": "https://example.com/new-avatar.jpg"
}
```

**Response 200:**
```json
{
  "id": 123,
  "name": "Mario Rossi",
  "email": "mario@example.com",
  "username": "mario_rossi",
  "preferredCurrency": "USD",
  "language": "en",
  "theme": "dark",
  "avatarUrl": "https://example.com/new-avatar.jpg",
  "createdAt": "2025-01-17T10:30:00",
  "lastLoginAt": "2025-01-17T15:45:00"
}
```

---

### **🏠 Gestione Indirizzi**

#### `GET /api/user/address`
Ottieni tutti gli indirizzi dell'utente.

**Headers:**
```
X-User-Id: 123
```

**Response 200:**
```json
[
  {
    "id": 1,
    "fullName": "Mario Rossi",
    "street": "Via Roma 123",
    "city": "Milano",
    "state": "Lombardia",
    "postalCode": "20100",
    "country": "Italia",
    "phone": "+39 123 456 7890",
    "addressLabel": "Casa",
    "isDefault": true,
    "createdAt": "2025-01-17T10:30:00",
    "updatedAt": "2025-01-17T10:30:00"
  }
]
```

#### `POST /api/user/address`
Aggiungi un nuovo indirizzo.

**Headers:**
```
X-User-Id: 123
Content-Type: application/json
```

**Request Body:**
```json
{
  "fullName": "Mario Rossi",
  "street": "Via Milano 456",
  "city": "Roma",
  "state": "Lazio",
  "postalCode": "00100",
  "country": "Italia",
  "phone": "+39 987 654 3210",
  "addressLabel": "Ufficio",
  "isDefault": false
}
```

**Response 201:**
```json
{
  "id": 2,
  "fullName": "Mario Rossi",
  "street": "Via Milano 456",
  "city": "Roma",
  "state": "Lazio",
  "postalCode": "00100",
  "country": "Italia",
  "phone": "+39 987 654 3210",
  "addressLabel": "Ufficio",
  "isDefault": false,
  "createdAt": "2025-01-17T16:00:00",
  "updatedAt": "2025-01-17T16:00:00"
}
```

#### `PUT /api/user/address/{id}`
Aggiorna un indirizzo esistente.

**Headers:**
```
X-User-Id: 123
Content-Type: application/json
```

**Request Body:**
```json
{
  "fullName": "Mario Rossi",
  "street": "Via Milano 456 - Piano 2",
  "city": "Roma",
  "state": "Lazio",
  "postalCode": "00100",
  "country": "Italia",
  "phone": "+39 987 654 3210",
  "addressLabel": "Ufficio Principale",
  "isDefault": true
}
```

#### `DELETE /api/user/address/{id}`
Elimina un indirizzo.

**Headers:**
```
X-User-Id: 123
```

**Response 204:** No Content

#### `PATCH /api/user/address/{id}/default`
Imposta un indirizzo come predefinito.

**Headers:**
```
X-User-Id: 123
```

**Response 200:**
```json
{
  "id": 2,
  "fullName": "Mario Rossi",
  "street": "Via Milano 456",
  "city": "Roma",
  "state": "Lazio",
  "postalCode": "00100",
  "country": "Italia",
  "phone": "+39 987 654 3210",
  "addressLabel": "Ufficio",
  "isDefault": true,
  "createdAt": "2025-01-17T16:00:00",
  "updatedAt": "2025-01-17T16:15:00"
}
```

#### `GET /api/user/address/default`
Ottieni l'indirizzo predefinito dell'utente.

**Headers:**
```
X-User-Id: 123
```

**Response 200:**
```json
{
  "id": 1,
  "fullName": "Mario Rossi",
  "street": "Via Roma 123",
  "city": "Milano",
  "state": "Lombardia",
  "postalCode": "20100",
  "country": "Italia",
  "phone": "+39 123 456 7890",
  "addressLabel": "Casa",
  "isDefault": true,
  "createdAt": "2025-01-17T10:30:00",
  "updatedAt": "2025-01-17T10:30:00"
}
```

---

## ⚠️ **Gestione Errori**

### **400 Bad Request**
```json
{
  "error": "Il nome non può essere vuoto"
}
```

### **404 Not Found**
```json
{
  "error": "Utente non trovato"
}
```

### **409 Conflict**
```json
{
  "error": "Limite massimo di 10 indirizzi raggiunto"
}
```

### **500 Internal Server Error**
```json
{
  "error": "Errore interno del server"
}
```

---

## 🔒 **Sicurezza**

- **Autenticazione:** Header `X-User-Id` obbligatorio
- **Autorizzazione:** Gli utenti possono accedere solo ai propri dati
- **Validazione:** Input validati con Jakarta Validation
- **CORS:** Configurato per frontend Funkard

---

## 📊 **Limitazioni**

- **Indirizzi per utente:** Massimo 10 indirizzi
- **Indirizzo predefinito:** Solo uno per utente
- **Valute supportate:** EUR, USD, GBP
- **Lunghezza campi:** Definita nelle validazioni

---

## 🚀 **Utilizzo Frontend**

### **JavaScript Example:**
```javascript
// Ottieni profilo utente
const response = await fetch('/api/user/me', {
  headers: {
    'X-User-Id': userId
  }
});
const profile = await response.json();

// Aggiungi nuovo indirizzo
const newAddress = await fetch('/api/user/address', {
  method: 'POST',
  headers: {
    'X-User-Id': userId,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    fullName: 'Mario Rossi',
    street: 'Via Roma 123',
    city: 'Milano',
    state: 'Lombardia',
    postalCode: '20100',
    country: 'Italia',
    phone: '+39 123 456 7890',
    addressLabel: 'Casa',
    isDefault: true
  })
});
```

---

## 📝 **Note Tecniche**

- **Database:** PostgreSQL con migrazione V3
- **ORM:** JPA/Hibernate con validazioni
- **Transazioni:** Gestite automaticamente
- **Logging:** Completo per debugging
- **Performance:** Index ottimizzati per query frequenti
