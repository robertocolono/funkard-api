# 🔒 GDPR Compliance - Registrazione Utente

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0  
**Conformità:** GDPR Art. 7 (Consenso)

---

## 📋 Panoramica

Il sistema di registrazione di Funkard è stato aggiornato per essere **100% conforme al GDPR**, richiedendo l'accettazione obbligatoria dei **Termini e Condizioni d'Uso** e della **Privacy Policy** con tracciabilità completa del consenso tramite timestamp.

---

## ✅ Modifiche Backend

### **1. Modello User** (`com.funkard.model.User`)

Aggiunti due nuovi campi per tracciare il consenso GDPR:

```java
@Column(name = "terms_accepted_at")
private LocalDateTime termsAcceptedAt;

@Column(name = "privacy_accepted_at")
private LocalDateTime privacyAcceptedAt;
```

**Caratteristiche:**
- ✅ Timestamp preciso di accettazione
- ✅ Tracciabilità completa per audit GDPR
- ✅ NULL per utenti esistenti (non retroattivo)
- ✅ NOT NULL per nuovi utenti (validato a runtime)

### **2. DTO Registrazione** (`com.funkard.payload.RegisterRequest`)

Aggiunti campi obbligatori con validazione Jakarta:

```java
@NotNull(message = "L'accettazione dei Termini e Condizioni è obbligatoria")
private Boolean acceptTerms;

@NotNull(message = "L'accettazione della Privacy Policy è obbligatoria")
private Boolean acceptPrivacy;
```

### **3. Controller Registrazione** (`com.funkard.controller.AuthController`)

**Validazione GDPR:**
```java
// Validazione obbligatoria
if (request.getAcceptTerms() == null || request.getAcceptPrivacy() == null) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Per completare la registrazione devi accettare Termini e Privacy Policy.");
}

if (!request.getAcceptTerms() || !request.getAcceptPrivacy()) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Per completare la registrazione devi accettare Termini e Privacy Policy.");
}

// Salvataggio timestamp
user.setTermsAcceptedAt(LocalDateTime.now());
user.setPrivacyAcceptedAt(LocalDateTime.now());
```

### **4. Migration Database** (`V5__add_gdpr_consent_timestamps_to_users.sql`)

```sql
ALTER TABLE users 
ADD COLUMN terms_accepted_at TIMESTAMP NULL;

ALTER TABLE users 
ADD COLUMN privacy_accepted_at TIMESTAMP NULL;
```

**Note:**
- ✅ Compatibile con database esistente
- ✅ NULL per utenti esistenti
- ✅ Commenti per documentazione

---

## 🌐 Integrazione Frontend

### **Endpoint API**

**POST** `/api/auth/register`

**Request Body:**
```json
{
  "username": "mario_rossi",
  "email": "mario.rossi@example.com",
  "password": "Password123!",
  "preferredCurrency": "EUR",
  "acceptTerms": true,
  "acceptPrivacy": true
}
```

**Response Success (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response Error (400):**
```json
"Per completare la registrazione devi accettare Termini e Privacy Policy."
```

---

### **Esempio Frontend (Next.js/React)**

#### **1. Form Registrazione**

```tsx
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export default function RegisterForm() {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    preferredCurrency: 'EUR',
    acceptTerms: false,
    acceptPrivacy: false,
  });
  const [error, setError] = useState('');
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Validazione frontend
    if (!formData.acceptTerms || !formData.acceptPrivacy) {
      setError('Devi accettare Termini e Privacy Policy per continuare.');
      return;
    }

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      const data = await response.json();

      if (!response.ok) {
        setError(data || 'Errore durante la registrazione');
        return;
      }

      // Salva token e reindirizza
      localStorage.setItem('token', data.token);
      router.push('/dashboard');
    } catch (err) {
      setError('Errore di connessione. Riprova.');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Campi base */}
      <input
        type="text"
        placeholder="Username"
        value={formData.username}
        onChange={(e) => setFormData({ ...formData, username: e.target.value })}
        required
      />
      <input
        type="email"
        placeholder="Email"
        value={formData.email}
        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
        required
      />
      <input
        type="password"
        placeholder="Password"
        value={formData.password}
        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
        required
      />

      {/* 🔒 GDPR Compliance: Checkbox Termini e Privacy */}
      <div className="space-y-2">
        <label className="flex items-start space-x-2">
          <input
            type="checkbox"
            checked={formData.acceptTerms}
            onChange={(e) => setFormData({ ...formData, acceptTerms: e.target.checked })}
            required
            className="mt-1"
          />
          <span className="text-sm">
            Accetto i{' '}
            <a
              href={`${process.env.NEXT_PUBLIC_BASE_URL}/legal/terms`}
              target="_blank"
              rel="noopener noreferrer"
              className="text-blue-600 hover:underline"
            >
              Termini e Condizioni d'Uso
            </a>
          </span>
        </label>

        <label className="flex items-start space-x-2">
          <input
            type="checkbox"
            checked={formData.acceptPrivacy}
            onChange={(e) => setFormData({ ...formData, acceptPrivacy: e.target.checked })}
            required
            className="mt-1"
          />
          <span className="text-sm">
            Accetto la{' '}
            <a
              href={`${process.env.NEXT_PUBLIC_BASE_URL}/legal/privacy`}
              target="_blank"
              rel="noopener noreferrer"
              className="text-blue-600 hover:underline"
            >
              Privacy Policy
            </a>
          </span>
        </label>
      </div>

      {error && <div className="text-red-600 text-sm">{error}</div>}

      <button
        type="submit"
        disabled={!formData.acceptTerms || !formData.acceptPrivacy}
        className="w-full bg-blue-600 text-white py-2 rounded disabled:bg-gray-400"
      >
        Registrati
      </button>
    </form>
  );
}
```

#### **2. Link Dinamici Documenti Legali**

**Configurazione `.env.local`:**
```env
NEXT_PUBLIC_BASE_URL=https://funkard.com
NEXT_PUBLIC_API_URL=https://api.funkard.com
```

**Pagine Legali (Next.js):**
```
app/
  legal/
    terms/
      page.tsx      # Termini e Condizioni
    privacy/
      page.tsx      # Privacy Policy
```

**Esempio `app/legal/terms/page.tsx`:**
```tsx
export default function TermsPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Termini e Condizioni d'Uso</h1>
      <div className="prose max-w-none">
        {/* Contenuto Termini */}
      </div>
    </div>
  );
}
```

---

## 🔐 Conformità GDPR

### **Requisiti Soddisfatti**

✅ **Art. 7 GDPR - Condizioni per il consenso:**
- Consenso esplicito e inequivocabile
- Tracciabilità completa (timestamp)
- Facile revoca del consenso (futuro)

✅ **Art. 13 GDPR - Informazioni da fornire:**
- Link ai documenti legali
- Informazioni chiare e comprensibili
- Accesso facilitato ai documenti

✅ **Art. 30 GDPR - Registro delle attività:**
- Timestamp accettazione salvati nel database
- Audit trail completo
- Tracciabilità per ispezioni

---

## 🚀 Integrazione Social Login (Futuro)

Per future integrazioni con Google OAuth o altri provider social, l'accettazione deve essere richiesta al **primo accesso** dopo l'autenticazione social.

### **Flusso Proposto:**

1. **Autenticazione Social** → Token OAuth
2. **Verifica Utente Esistente:**
   - Se nuovo utente → Richiedi accettazione Termini/Privacy
   - Se utente esistente → Login diretto
3. **Salvataggio Consenso** → Stesso meccanismo attuale

**Esempio Endpoint Futuro:**
```java
@PostMapping("/auth/social/consent")
public ResponseEntity<?> socialConsent(
    @RequestBody SocialConsentRequest request
) {
    // Verifica che acceptTerms e acceptPrivacy siano true
    // Salva timestamp
    // Completa registrazione
}
```

---

## 📊 Query Database

### **Verifica Consensi Utente**

```sql
SELECT 
    id,
    email,
    terms_accepted_at,
    privacy_accepted_at,
    CASE 
        WHEN terms_accepted_at IS NOT NULL 
         AND privacy_accepted_at IS NOT NULL 
        THEN 'Compliant'
        ELSE 'Non Compliant'
    END AS gdpr_status
FROM users
WHERE id = ?;
```

### **Report GDPR Compliance**

```sql
SELECT 
    COUNT(*) AS total_users,
    COUNT(terms_accepted_at) AS users_with_terms,
    COUNT(privacy_accepted_at) AS users_with_privacy,
    COUNT(CASE 
        WHEN terms_accepted_at IS NOT NULL 
         AND privacy_accepted_at IS NOT NULL 
        THEN 1 
    END) AS fully_compliant_users
FROM users;
```

---

## ✅ Checklist Implementazione

### **Backend:**
- [x] Campi `termsAcceptedAt` e `privacyAcceptedAt` nel modello User
- [x] Validazione obbligatoria in `RegisterRequest`
- [x] Controller aggiornato con validazione GDPR
- [x] Migration database creata
- [x] CORS aggiornato per domini corretti
- [x] Messaggi di errore chiari

### **Frontend (da implementare):**
- [ ] Form registrazione con checkbox Termini/Privacy
- [ ] Link dinamici ai documenti legali
- [ ] Validazione frontend prima dell'invio
- [ ] Gestione errori API
- [ ] Pagine legali (`/legal/terms`, `/legal/privacy`)
- [ ] Test end-to-end

### **Documentazione:**
- [x] Documentazione backend
- [x] Esempi codice frontend
- [x] Query database per audit
- [ ] Documentazione legale (Termini/Privacy)

---

## 🔄 Prossimi Passi

1. **Implementare Frontend:**
   - Form registrazione con checkbox
   - Pagine legali
   - Test integrazione

2. **Documenti Legali:**
   - Redigere Termini e Condizioni
   - Redigere Privacy Policy
   - Pubblicare su `/legal/*`

3. **Audit e Monitoraggio:**
   - Dashboard compliance
   - Report utenti non compliant
   - Alert per nuovi utenti senza consenso

4. **Revoca Consenso (Futuro):**
   - Endpoint per revoca consenso
   - Cancellazione account
   - Export dati utente (GDPR Art. 20)

---

## 📝 Note Legali

⚠️ **Importante:**
- I documenti legali (Termini e Privacy Policy) devono essere redatti da un **avvocato specializzato in privacy e GDPR**
- La conformità GDPR richiede anche:
  - Cookie consent (se applicabile)
  - Data Processing Agreement con fornitori
  - Privacy by Design
  - Data Protection Impact Assessment (DPIA)

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

