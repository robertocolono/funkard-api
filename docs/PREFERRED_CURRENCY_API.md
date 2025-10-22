# 💱 API Preferred Currency - Funkard

## 🎯 Panoramica

Sistema per gestione valuta preferita degli utenti, integrato nel flusso di registrazione e profilo utente.

## 🔧 Modifiche Implementate

### **1. 📝 User Entity**
```java
@Column(nullable = false, length = 3)
private String preferredCurrency = "EUR";
```

### **2. 📦 UserDTO**
```java
private String preferredCurrency; // Nuovo campo
```

### **3. 📋 RegisterRequest**
```java
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String preferredCurrency; // 👈 aggiunto
}
```

## 📡 Endpoint API

### **🔐 Registrazione Utente**
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "mario_rossi",
  "email": "mario@example.com",
  "password": "password123",
  "preferredCurrency": "USD"
}
```

**Risposta:**
```json
{
  "token": "jwt_token_here"
}
```

### **👤 Profilo Utente**
```http
GET /api/users/{id}
```

**Risposta:**
```json
{
  "id": 1,
  "username": "mario_rossi",
  "email": "mario@example.com",
  "name": "Mario Rossi",
  "role": "USER",
  "preferredCurrency": "USD"
}
```

## 🗄️ Database Schema

### **Tabella: `users`**
```sql
-- Nuovo campo aggiunto
ALTER TABLE users 
ADD COLUMN preferred_currency VARCHAR(3) NOT NULL DEFAULT 'EUR';

-- Indice per performance
CREATE INDEX idx_users_preferred_currency ON users(preferred_currency);
```

## 🔄 Compatibilità

### **✅ Retrocompatibilità**
- ✅ **Utenti esistenti**: Automaticamente impostati su "EUR"
- ✅ **API esistenti**: Continuano a funzionare senza modifiche
- ✅ **Frontend**: Può ignorare il campo se non supportato

### **✅ Valori Supportati**
```javascript
const SUPPORTED_CURRENCIES = [
  'EUR', // Euro (default)
  'USD', // Dollaro USA
  'GBP', // Sterlina britannica
  'JPY', // Yen giapponese
  'CAD', // Dollaro canadese
  'AUD', // Dollaro australiano
  'CHF', // Franco svizzero
  'CNY'  // Yuan cinese
];
```

## 🧪 Test di Integrazione

### **Test Registrazione**
```bash
# Test con valuta specifica
curl -X POST /api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "password": "password123",
    "preferredCurrency": "USD"
  }'

# Test senza valuta (default EUR)
curl -X POST /api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user2",
    "email": "test2@example.com",
    "password": "password123"
  }'
```

### **Test Profilo Utente**
```bash
curl -X GET /api/users/1 \
  -H "Authorization: Bearer jwt_token"
```

## 🔮 Utilizzo Frontend

### **React Hook Example**
```javascript
const useUserProfile = () => {
  const [user, setUser] = useState(null);
  const [currency, setCurrency] = useState('EUR');

  const updateCurrency = async (newCurrency) => {
    try {
      await fetch(`/api/users/${user.id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ preferredCurrency: newCurrency })
      });
      setCurrency(newCurrency);
    } catch (error) {
      console.error('Errore aggiornamento valuta:', error);
    }
  };

  return { user, currency, updateCurrency };
};
```

### **Componente Selezione Valuta**
```jsx
const CurrencySelector = ({ currentCurrency, onCurrencyChange }) => {
  const currencies = [
    { code: 'EUR', name: 'Euro', symbol: '€' },
    { code: 'USD', name: 'Dollaro USA', symbol: '$' },
    { code: 'GBP', name: 'Sterlina', symbol: '£' },
    { code: 'JPY', name: 'Yen', symbol: '¥' }
  ];

  return (
    <select 
      value={currentCurrency} 
      onChange={(e) => onCurrencyChange(e.target.value)}
    >
      {currencies.map(currency => (
        <option key={currency.code} value={currency.code}>
          {currency.symbol} {currency.name}
        </option>
      ))}
    </select>
  );
};
```

## 📊 Statistiche Utilizzo

### **Query Database**
```sql
-- Distribuzione valute utenti
SELECT preferred_currency, COUNT(*) as user_count
FROM users 
GROUP BY preferred_currency 
ORDER BY user_count DESC;

-- Utenti per valuta e paese
SELECT u.paese, u.preferred_currency, COUNT(*) as count
FROM users u 
GROUP BY u.paese, u.preferred_currency
ORDER BY count DESC;
```

## 🚀 Deployment

### **1. Database Migration**
```bash
# Applica migrazione automaticamente
./mvnw flyway:migrate
```

### **2. Verifica Deployment**
```bash
# Test endpoint registrazione
curl -X POST https://api.funkard.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"test123","preferredCurrency":"USD"}'
```

## ✅ Checklist Implementazione

- ✅ **User.java**: Campo `preferredCurrency` aggiunto
- ✅ **UserDTO.java**: Campo incluso nel DTO
- ✅ **RegisterRequest.java**: Payload registrazione aggiornato
- ✅ **AuthController.java**: Logica registrazione aggiornata
- ✅ **UserService.java**: Mapping DTO aggiornato
- ✅ **Migration SQL**: Script database creato
- ✅ **Documentazione**: API documentata
- ✅ **Test**: Esempi di test forniti

---

**🎉 Sistema preferred currency implementato con successo!**
