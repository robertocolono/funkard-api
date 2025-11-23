# 🔍 Verifica Stato Locale e Currency - Funkard Backend

**Data Verifica:** 2025-01-15  
**Versione:** 1.0

---

## 📋 Riepilogo Verifica

### ✅ **1. MODELLO User**

**File:** `src/main/java/com/funkard/model/User.java`

#### **Campi Esistenti:**
- ✅ **`preferredCurrency`** (linea 58-59)
  - Tipo: `String`
  - Annotazione: `@Column(nullable = false, length = 3)`
  - Default: `"EUR"`
  - **Stato:** ✅ CORRETTO

- ✅ **`language`** (linea 52)
  - Tipo: `String`
  - Annotazione: ❌ **MANCA `@Column`**
  - Default: `null`
  - **Stato:** ⚠️ **DA CORREGGERE** (manca annotazione)

- ❌ **`locale`** 
  - **NON ESISTE** nel modello
  - **Nota:** Esiste solo `language` (equivalente a `locale`)

#### **Raccomandazione:**
1. Aggiungere `@Column(name = "language")` a `language`
2. Considerare se aggiungere anche `locale` come alias o mantenere solo `language`

---

### ✅ **2. CONTROLLER / SERVICE**

**File:** `src/main/java/com/funkard/controller/UserController.java`

#### **Endpoint Esistenti:**

- ✅ **`GET /api/user/me`** (linea 57)
  - Restituisce: `UserProfileDTO`
  - Include: `language` ✅ e `preferredCurrency` ✅
  - **Stato:** ✅ CORRETTO

- ✅ **`PUT /api/user/me`** (linea 79)
  - Aggiorna: `language` ✅ e `preferredCurrency` ✅
  - **Stato:** ✅ CORRETTO

- ✅ **`PUT /api/user/preferences`** (linea 339)
  - Gestisce: Cookie preferences (non locale/currency)
  - **Stato:** ✅ CORRETTO (ma non gestisce locale/currency)

#### **Endpoint Mancanti:**
- ❌ **`PATCH /api/users/preferences`** (specifico per locale/currency)
  - **Nota:** Esiste `PUT /api/user/me` che già gestisce questi campi

#### **Raccomandazione:**
- ✅ Gli endpoint esistenti sono sufficienti
- Considerare aggiungere `PATCH /api/user/preferences` per aggiornare solo locale/currency

---

### ⚠️ **3. DATABASE**

**Migration:** `V2__add_preferred_currency_to_users.sql`

#### **Colonne Esistenti:**
- ✅ **`preferred_currency`**
  - Tipo: `VARCHAR(3)`
  - Default: `'EUR'`
  - Nullable: `NOT NULL`
  - **Stato:** ✅ CORRETTO

- ❌ **`locale`**
  - **NON ESISTE** nel database
  - **Nota:** Esiste `language` nel modello ma non vedo migration per colonna DB

- ⚠️ **`language`**
  - Esiste nel modello Java
  - **NON VEDO MIGRATION** per colonna database
  - **Stato:** ⚠️ **DA VERIFICARE** (potrebbe esistere ma senza migration esplicita)

#### **Raccomandazione:**
1. Verificare se colonna `language` esiste nel DB (con `\d users` o query SQL)
2. Se non esiste → creare migration `V12__add_language_to_users.sql`
3. Se esiste → aggiungere annotazione `@Column` nel modello

---

### ❌ **4. JWT / LOGIN FLOW**

**File:** `src/main/java/com/funkard/controller/AuthController.java`

#### **Login Response (linea 99-102):**
```java
String token = jwtUtil.generateToken(user.getEmail());
Map<String, String> body = new HashMap<>();
body.put("token", token);
return ResponseEntity.ok(body);
```

**Stato:** ❌ **MANCA locale e currency nella response**

#### **JWT Token (JwtUtil.java):**
- Contiene solo: `email` (subject)
- **NON contiene:** locale, currency, o altri dati utente

#### **Raccomandazione:**
1. Aggiungere `locale` e `currency` nella response di `/login`
2. Opzionale: includere nel JWT payload (se necessario)

---

### ✅ **5. DTO**

**File:** `src/main/java/com/funkard/dto/UserProfileDTO.java`

#### **Campi Esistenti:**
- ✅ **`preferredCurrency`** (linea 18)
- ✅ **`language`** (linea 19)
- **Stato:** ✅ CORRETTO

---

## 📊 Tabella Riepilogo

| Componente | Campo | Stato | Azione Richiesta |
|------------|-------|-------|------------------|
| **User Model** | `preferredCurrency` | ✅ OK | Nessuna |
| **User Model** | `language` | ⚠️ WARN | Aggiungere `@Column` |
| **User Model** | `locale` | ❌ MISSING | Aggiungere (opzionale) |
| **Database** | `preferred_currency` | ✅ OK | Nessuna |
| **Database** | `language` | ⚠️ UNKNOWN | Verificare esistenza |
| **Database** | `locale` | ❌ MISSING | Aggiungere (opzionale) |
| **GET /api/user/me** | Restituisce locale/currency | ✅ OK | Nessuna |
| **PUT /api/user/me** | Aggiorna locale/currency | ✅ OK | Nessuna |
| **POST /api/auth/login** | Restituisce locale/currency | ❌ MISSING | Aggiungere |
| **JWT Token** | Contiene locale/currency | ❌ MISSING | Opzionale |

---

## 🔧 Azioni Consigliate

### **Priorità Alta:**
1. ✅ Verificare esistenza colonna `language` nel database
2. ✅ Aggiungere `@Column(name = "language")` a campo `language` nel modello
3. ✅ Aggiungere `locale` e `currency` nella response di `/login`

### **Priorità Media:**
4. ⚠️ Creare migration per colonna `language` se non esiste
5. ⚠️ Considerare aggiungere campo `locale` come alias di `language`

### **Priorità Bassa:**
6. 💡 Aggiungere `locale` e `currency` nel JWT payload (se necessario)
7. 💡 Creare endpoint dedicato `PATCH /api/user/preferences` per locale/currency

---

## 📝 Note

- Il campo `language` nel modello User è già utilizzato dal sistema email multilingua
- Il campo `preferredCurrency` è già completo e funzionante
- Gli endpoint GET/PUT `/api/user/me` già gestiscono correttamente questi campi
- Il problema principale è la mancanza di questi dati nella response di login

---

**Documento creato:** 2025-01-15  
**Versione:** 1.0

