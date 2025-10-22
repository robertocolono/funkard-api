# 💳 API Metodi di Pagamento - Funkard

## 🎯 Panoramica

Sistema completo per gestione metodi di pagamento utenti con sicurezza integrata e validazione avanzata.

## 🔒 Sicurezza

- ✅ **Mai salva numeri di carta completi** - Solo versioni mascherate
- ✅ **Validazione algoritmo Luhn** per numeri di carta
- ✅ **Controllo scadenze** automatico
- ✅ **Limite 5 metodi** per utente
- ✅ **Autenticazione JWT** richiesta

## 📡 Endpoint API

### **1. 📋 Ottieni Metodi di Pagamento**
```http
GET /api/user/payments
Headers:
  X-User-Id: {userId}
```

**Risposta:**
```json
[
  {
    "id": "uuid-123",
    "cardHolder": "Mario Rossi",
    "cardNumberMasked": "**** **** **** 1234",
    "expiryDate": "12/25",
    "brand": "VISA",
    "isDefault": true,
    "createdAt": "2024-01-15T10:30:00",
    "displayName": "VISA •••• 1234",
    "isExpired": false,
    "lastFourDigits": "1234"
  }
]
```

### **2. ➕ Aggiungi Metodo di Pagamento**
```http
POST /api/user/payments
Headers:
  X-User-Id: {userId}
  Content-Type: application/json

Body:
{
  "cardHolder": "Mario Rossi",
  "cardNumber": "4111 1111 1111 1111",
  "expiryDate": "12/25",
  "brand": "VISA",
  "cvv": "123",
  "setAsDefault": true
}
```

**Validazioni:**
- ✅ Numero carta: Algoritmo Luhn
- ✅ Data scadenza: Formato MM/YY
- ✅ Brand: VISA, MASTERCARD, AMEX, DISCOVER
- ✅ CVV: 3-4 cifre
- ✅ Limite: Max 5 metodi per utente

### **3. 🗑️ Elimina Metodo di Pagamento**
```http
DELETE /api/user/payments/{id}
Headers:
  X-User-Id: {userId}
```

### **4. 🎯 Imposta Metodo Predefinito**
```http
PATCH /api/user/payments/{id}/default
Headers:
  X-User-Id: {userId}
```

### **5. 🔍 Ottieni Metodo Predefinito**
```http
GET /api/user/payments/default
Headers:
  X-User-Id: {userId}
```

### **6. 📊 Statistiche Metodi**
```http
GET /api/user/payments/stats
Headers:
  X-User-Id: {userId}
```

**Risposta:**
```json
{
  "totalMethods": 3,
  "expiredMethods": 1,
  "hasDefaultMethod": true
}
```

### **7. 🧹 Pulizia Metodi Scaduti**
```http
POST /api/user/payments/cleanup
Headers:
  X-User-Id: {userId}
```

### **8. ❓ Valida Metodo (Senza Salvare)**
```http
POST /api/user/payments/validate
Content-Type: application/json

Body:
{
  "cardHolder": "Mario Rossi",
  "cardNumber": "4111 1111 1111 1111",
  "expiryDate": "12/25",
  "brand": "VISA",
  "cvv": "123"
}
```

**Risposta:**
```json
{
  "isValid": true,
  "cardValid": true,
  "expiryValid": true,
  "errors": []
}
```

## 🚨 Codici di Errore

| Codice | Descrizione |
|--------|-------------|
| `400` | Richiesta non valida (validazione fallita) |
| `401` | Non autenticato |
| `403` | Accesso negato |
| `404` | Metodo di pagamento non trovato |
| `409` | Limite metodi raggiunto |
| `500` | Errore interno del server |

## 🔧 Integrazione Frontend

### **React Hook Example:**
```javascript
const usePaymentMethods = (userId) => {
  const [methods, setMethods] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchMethods = async () => {
    setLoading(true);
    try {
      const response = await fetch('/api/user/payments', {
        headers: { 'X-User-Id': userId }
      });
      const data = await response.json();
      setMethods(data);
    } catch (error) {
      console.error('Errore nel caricamento metodi:', error);
    } finally {
      setLoading(false);
    }
  };

  const addMethod = async (methodData) => {
    try {
      const response = await fetch('/api/user/payments', {
        method: 'POST',
        headers: {
          'X-User-Id': userId,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(methodData)
      });
      
      if (response.ok) {
        await fetchMethods(); // Ricarica lista
      }
    } catch (error) {
      console.error('Errore nell\'aggiunta metodo:', error);
    }
  };

  return { methods, loading, fetchMethods, addMethod };
};
```

## 🗄️ Database Schema

### **Tabella: `user_payment_methods`**
```sql
CREATE TABLE user_payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    card_holder VARCHAR(100) NOT NULL,
    card_number_masked VARCHAR(20) NOT NULL,
    expiry_date VARCHAR(5) NOT NULL,
    brand VARCHAR(20) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indici per performance
CREATE INDEX idx_user_payment_methods_user_id ON user_payment_methods(user_id);
CREATE INDEX idx_user_payment_methods_default ON user_payment_methods(user_id, is_default);
```

## 🔮 Roadmap Futura

- ✅ **Integrazione Stripe** - Payment Intent e Setup Intent
- ✅ **Webhook Stripe** - Gestione eventi di pagamento
- ✅ **Criptografia** - Campi sensibili crittografati
- ✅ **Audit Log** - Tracciamento modifiche metodi
- ✅ **Notifiche** - Alert scadenze e modifiche
- ✅ **Analytics** - Statistiche utilizzo metodi

## 🧪 Test

### **Test di Validazione:**
```bash
# Test numero carta valido
curl -X POST /api/user/payments/validate \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":"4111111111111111","expiryDate":"12/25","brand":"VISA"}'

# Test numero carta non valido
curl -X POST /api/user/payments/validate \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":"1234567890123456","expiryDate":"12/25","brand":"VISA"}'
```

---

**🎉 Sistema di gestione metodi di pagamento implementato con successo!**
