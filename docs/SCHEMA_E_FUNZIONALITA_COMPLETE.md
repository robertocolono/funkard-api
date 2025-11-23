# 📚 Schema Database e Funzionalità Complete - Funkard API

**Data:** 2025-01-15  
**Versione:** 2.0  
**Framework:** Spring Boot 3.5.6  
**Java:** 21

---

## 📋 Indice

1. [Schema Database Completo](#schema-database-completo)
2. [Funzionalità Implementate](#funzionalità-implementate)
3. [Sistema di Traduzione](#sistema-di-traduzione)
4. [Generazione Automatica nameEn](#generazione-automatica-nameen)
5. [Endpoint API Completi](#endpoint-api-completi)
6. [Architettura Servizi](#architettura-servizi)

---

## 🗄️ Schema Database Completo

### **Tabella: users**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    handle VARCHAR(255) UNIQUE,
    nome VARCHAR(255),
    paese VARCHAR(255),
    tipo_utente VARCHAR(50), -- PRIVATO o BUSINESS
    indirizzo VARCHAR(255),
    citta VARCHAR(255),
    cap VARCHAR(10),
    telefono VARCHAR(50),
    metodo_pagamento VARCHAR(50),
    
    -- GDPR Compliance
    terms_accepted_at TIMESTAMP,
    privacy_accepted_at TIMESTAMP,
    deletion_pending BOOLEAN DEFAULT FALSE,
    deletion_requested_at TIMESTAMP,
    
    -- Profilo
    username VARCHAR(255),
    avatar_url VARCHAR(500),
    role VARCHAR(50) DEFAULT 'USER',
    language VARCHAR(5) DEFAULT 'en',
    preferred_currency VARCHAR(3) DEFAULT 'EUR',
    theme VARCHAR(50),
    
    -- Bio venditore (traduzione dinamica)
    description_original TEXT, -- Max 500 caratteri
    description_language VARCHAR(5), -- ISO 639-1
    
    verified BOOLEAN DEFAULT FALSE,
    flagged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP
);
```

### **Tabella: products**
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2),
    estimated_value DECIMAL(10,2),
    user_id VARCHAR(255),
    
    -- Traduzione dinamica
    description_original TEXT,
    description_language VARCHAR(5), -- ISO 639-1
    name_en VARCHAR(255), -- Nome inglese globale generato automaticamente
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_products_description_language ON products(description_language);
CREATE INDEX idx_products_name_en ON products(name_en);
```

### **Tabella: cards**
```sql
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    category VARCHAR(100), -- TCG, Anime, ecc.
    franchise VARCHAR(100), -- Pokémon, Yu-Gi-Oh!, ecc.
    language VARCHAR(50),
    card_type VARCHAR(50),
    card_source VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

### **Tabella: user_cards**
```sql
CREATE TABLE user_cards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    card_id BIGINT REFERENCES cards(id),
    condition VARCHAR(50),
    grade DECIMAL(3,1),
    graded_by VARCHAR(100),
    image_url VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

### **Tabella: listings**
```sql
CREATE TABLE listings (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    condition VARCHAR(50),
    seller_id BIGINT REFERENCES users(id),
    card_id BIGINT REFERENCES cards(id),
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, SOLD, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

### **Tabella: transactions**
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT REFERENCES users(id),
    seller_id BIGINT REFERENCES users(id),
    listing_id BIGINT REFERENCES listings(id),
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, COMPLETED, CANCELLED
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);
```

### **Tabella: user_addresses**
```sql
CREATE TABLE user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    full_name VARCHAR(255) NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    address_label VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

### **Tabella: user_preferences**
```sql
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) UNIQUE,
    cookies_accepted BOOLEAN DEFAULT FALSE,
    cookies_preferences JSONB,
    cookies_accepted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

### **Tabella: cookie_consent_logs**
```sql
CREATE TABLE cookie_consent_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(50) NOT NULL, -- ACCEPTED, UPDATED, REJECTED
    old_preferences JSONB,
    new_preferences JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cookie_logs_user_id ON cookie_consent_logs(user_id);
CREATE INDEX idx_cookie_logs_created_at ON cookie_consent_logs(created_at);
```

### **Tabella: user_deletions**
```sql
CREATE TABLE user_deletions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT REFERENCES users(id),
    email VARCHAR(255) NOT NULL,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scheduled_deletion_at TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, COMPLETED
    reason TEXT,
    completed_at TIMESTAMP
);

CREATE INDEX idx_user_deletions_status ON user_deletions(status);
CREATE INDEX idx_user_deletions_scheduled ON user_deletions(scheduled_deletion_at);
```

### **Tabella: email_logs**
```sql
CREATE TABLE email_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient VARCHAR(255) NOT NULL,
    sender VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    type VARCHAR(100),
    status VARCHAR(50) NOT NULL, -- SENT, FAILED, RETRIED
    error_message TEXT,
    sent_at TIMESTAMP,
    retry_count INTEGER DEFAULT 0,
    locale VARCHAR(10),
    template_name VARCHAR(255),
    webhook_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_logs_recipient ON email_logs(recipient);
CREATE INDEX idx_email_logs_status ON email_logs(status);
CREATE INDEX idx_email_logs_created_at ON email_logs(created_at);
```

### **Tabella: chat_messages**
```sql
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id BIGINT REFERENCES users(id) NOT NULL,
    recipient_id BIGINT REFERENCES users(id) NOT NULL,
    original_text TEXT NOT NULL,
    translated_text TEXT,
    original_language VARCHAR(5),
    target_language VARCHAR(5),
    is_translated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

CREATE INDEX idx_chat_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_recipient ON chat_messages(recipient_id);
CREATE INDEX idx_chat_created_at ON chat_messages(created_at);
```

### **Tabella: translation_logs**
```sql
CREATE TABLE translation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_text TEXT NOT NULL,
    translated_text TEXT,
    source_language VARCHAR(5),
    target_language VARCHAR(5),
    translation_provider VARCHAR(50), -- GPT, DeepL, internal
    success BOOLEAN DEFAULT FALSE,
    error_message TEXT,
    user_id BIGINT REFERENCES users(id),
    message_type VARCHAR(50), -- CHAT, SUPPORT, PRODUCT
    message_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_translation_logs_user ON translation_logs(user_id);
CREATE INDEX idx_translation_logs_provider ON translation_logs(translation_provider);
```

### **Tabella: pending_values**
```sql
CREATE TABLE pending_values (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL, -- TCG, LANGUAGE, FRANCHISE
    value TEXT NOT NULL,
    submitted_by BIGINT REFERENCES users(id),
    approved BOOLEAN DEFAULT FALSE,
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pending_values_type ON pending_values(type);
CREATE INDEX idx_pending_values_approved ON pending_values(approved);
```

### **Tabella: franchises**
```sql
CREATE TABLE franchises (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, DISABLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_franchises_category ON franchises(category);
CREATE INDEX idx_franchises_status ON franchises(status);
```

### **Tabella: franchise_proposals**
```sql
CREATE TABLE franchise_proposals (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    franchise VARCHAR(255) NOT NULL,
    user_email VARCHAR(255),
    user_id BIGINT REFERENCES users(id),
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    processed_by BIGINT REFERENCES users(id),
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_franchise_proposals_status ON franchise_proposals(status);
```

### **Tabella: support_tickets**
```sql
CREATE TABLE support_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT REFERENCES users(id),
    user_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    priority VARCHAR(50) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    category VARCHAR(100),
    assigned_to VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_support_tickets_user ON support_tickets(user_id);
CREATE INDEX idx_support_tickets_status ON support_tickets(status);
```

### **Tabella: support_messages**
```sql
CREATE TABLE support_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID REFERENCES support_tickets(id) NOT NULL,
    sender VARCHAR(255) NOT NULL, -- user email o admin username
    message TEXT NOT NULL,
    original_text TEXT,
    translated_text TEXT,
    original_language VARCHAR(5),
    target_language VARCHAR(5),
    is_translated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_support_messages_ticket ON support_messages(ticket_id);
```

### **Tabella: admin_notifications**
```sql
CREATE TABLE admin_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    priority VARCHAR(50) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    read BOOLEAN DEFAULT FALSE,
    archived BOOLEAN DEFAULT FALSE,
    read_by VARCHAR(255),
    read_at TIMESTAMP,
    archived_at TIMESTAMP,
    assigned_to VARCHAR(255),
    assigned_at TIMESTAMP,
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(255),
    history JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_admin_notifications_read ON admin_notifications(read);
CREATE INDEX idx_admin_notifications_archived ON admin_notifications(archived);
CREATE INDEX idx_admin_notifications_type ON admin_notifications(type);
```

---

## ✅ Funzionalità Implementate

### **1. 🌍 Sistema di Traduzione Unificato**

**Componenti:**
- `UnifiedTranslationService` - Servizio centrale con fallback automatico
- `OpenAiTranslateService` - Provider GPT-4o-mini (primario)
- `DeepLTranslateService` - Provider DeepL (fallback)
- `TranslationProvider` - Interfaccia comune
- `TranslationException` - Eccezione per errori traduzione

**Flusso:**
1. Prova GPT-4o-mini
2. Se fallisce → prova DeepL
3. Se fallisce anche DeepL → restituisce testo originale

**Endpoint:**
- `POST /api/translate` - Traduzione generica
  - Request: `{"text": "...", "targetLanguage": "it"}`
  - Response: `{"translated": "..."}`
  - Lingue supportate: `["en","it","es","fr","de","pt","ru","ja","zh"]`

**Validazioni:**
- Testo non vuoto
- Lingua target tra quelle supportate
- Normalizzazione automatica codici lingua (ISO 639-1)

---

### **2. 🎴 Generazione Automatica nameEn per Prodotti**

**Componenti:**
- `ProductService.generateGlobalEnglishName()` - Genera nome inglese globale
- `ProductService.buildCardNameNormalizationPrompt()` - Costruisce prompt avanzato GPT
- `OpenAiTranslateService.executeWithCustomPrompt()` - Esegue prompt personalizzato

**Flusso:**
1. Alla creazione prodotto (`POST /api/products`), se `nameEn` è null/vuoto:
2. Valida nome (non vuoto, non troppo corto)
3. Costruisce prompt specializzato per riconoscere nome ufficiale collezionistico
4. Chiama GPT-4o-mini con prompt personalizzato
5. Se fallisce → fallback DeepL (traduzione semplice)
6. Se fallisce anche DeepL → usa nome originale
7. Salva `nameEn` nel database

**Prompt GPT:**
```
"You are an expert in trading cards (TCG/CCG). Given a card name written by a user in any language or any non-standard format, your task is to output ONLY the official collectible card name in English, exactly as used in the global marketplace.

Do NOT translate literally.
Do NOT output explanations.
Normalize accents, edition markers, variant identifiers, rarity abbreviations, set codes, and special editions.
If the user provided incomplete or unclear names, infer the most likely official collectible name.
Output ONLY the corrected English collectible name."
```

**Validazioni:**
- Nome vuoto → `nameEn = null`
- Nome ≤3 caratteri o generico ("a", "aaa", "???", "card") → usa nome originale
- Se `nameEn` già fornito → non viene sovrascritto

**Logging:**
- Info: quando GPT genera con successo
- Warning: quando GPT fallisce e si usa DeepL
- Warning: quando DeepL genera (fallback)
- Error: quando entrambi i provider falliscono

---

### **3. 👤 Validazione Bio Venditore**

**Componenti:**
- `User.descriptionOriginal` - Campo bio venditore (max 500 caratteri)
- `UserProfileDTO.descriptionOriginal` - DTO con validazione
- `UserService.updateUserProfile()` - Validazione esplicita

**Validazioni:**
- `@Size(max = 500)` su `User.descriptionOriginal`
- `@Size(max = 500)` su `UserProfileDTO.descriptionOriginal`
- Validazione esplicita in `UserService`: se > 500 caratteri → errore 400
- Messaggio errore: "La bio del venditore non può superare 500 caratteri."

**Endpoint:**
- `PUT /api/user/me` - Aggiorna profilo (include bio venditore)

---

### **4. 🌍 Traduzione Dinamica Contenuti**

**Campi aggiunti:**

**Product:**
- `descriptionOriginal` (TEXT) - Descrizione originale
- `descriptionLanguage` (VARCHAR(5)) - Lingua originale (ISO 639-1)
- `nameEn` (VARCHAR(255)) - Nome inglese globale (generato automaticamente)

**User (Seller Profile):**
- `descriptionOriginal` (TEXT, max 500) - Bio venditore originale
- `descriptionLanguage` (VARCHAR(5)) - Lingua originale (ISO 639-1)

**Migration:**
- `V21__add_translation_fields_to_products_and_users.sql`

**Indici:**
- `idx_products_description_language`
- `idx_products_name_en`
- `idx_users_description_language`

---

### **5. 🔐 Autenticazione e Autorizzazione**

**Endpoint:**
- `POST /api/auth/register` - Registrazione (GDPR compliant)
- `POST /api/auth/login` - Login (restituisce token, language, preferredCurrency)
- `GET /api/auth/validate` - Valida token

**GDPR Compliance:**
- `termsAcceptedAt` - Timestamp accettazione Termini
- `privacyAcceptedAt` - Timestamp accettazione Privacy
- Validazione obbligatoria durante registrazione

---

### **6. 👤 Gestione Utenti**

**Endpoint:**
- `GET /api/user/me` - Profilo utente (include language, preferredCurrency, descriptionOriginal)
- `PUT /api/user/me` - Aggiorna profilo (con validazione bio max 500 caratteri)
- `PATCH /api/user/preferences` - Aggiorna preferenze (language, currency)
- `GET /api/user/address` - Lista indirizzi
- `POST /api/user/address` - Crea indirizzo
- `PUT /api/user/address/{id}` - Aggiorna indirizzo
- `DELETE /api/user/address/{id}` - Elimina indirizzo
- `POST /api/user/preferences/cookies` - Salva preferenze cookie
- `GET /api/user/preferences/cookies` - Ottieni preferenze cookie
- `GET /api/user/preferences/cookies/export` - Export log cookie (JSON/PDF)
- `DELETE /api/user/delete-account` - Richiedi cancellazione account

---

### **7. 🛒 Marketplace**

**Endpoint:**
- `GET /api/products` - Lista prodotti
- `GET /api/products/{id}` - Dettaglio prodotto
- `POST /api/products` - Crea prodotto (genera automaticamente nameEn)
- `GET /api/listings` - Lista annunci
- `POST /api/listings` - Crea annuncio
- `GET /api/transactions` - Lista transazioni
- `POST /api/transactions` - Crea transazione

**Funzionalità:**
- Generazione automatica `nameEn` alla creazione prodotto
- Supporto traduzione dinamica descrizioni
- Validazione e notifiche admin

---

### **8. 💬 Chat e Messaggistica**

**Endpoint:**
- `POST /api/chat/message` - Invia messaggio (con traduzione automatica)
- `GET /api/chat/conversation/{userId}` - Conversazione con utente
- `GET /api/chat/unread` - Conta messaggi non letti
- `PUT /api/chat/message/{messageId}/read` - Marca come letto

**Funzionalità:**
- Traduzione automatica messaggi tra utenti
- Salvataggio testo originale e tradotto
- Log traduzioni

---

### **9. 🎫 Sistema Supporto**

**Endpoint:**
- `POST /api/support/tickets` - Crea ticket supporto
- `GET /api/support/tickets` - Lista ticket utente
- `GET /api/support/tickets/{id}` - Dettaglio ticket
- `POST /api/support/chat/{ticketId}/message` - Invia messaggio (con traduzione)
- `GET /api/support/chat/{ticketId}/messages` - Messaggi ticket

**Funzionalità:**
- Traduzione automatica messaggi supporto
- Notifiche real-time (SSE)
- Assegnazione ticket admin

---

### **10. 📚 Gestione Franchise**

**Endpoint:**
- `GET /api/franchises` - Lista franchise (pubblico, da JSON)
- `POST /api/franchises/propose` - Proponi nuovo franchise
- `GET /api/admin/franchises` - Lista franchise e proposte (admin)
- `POST /api/admin/franchises/approve/{proposalId}` - Approva proposta
- `POST /api/admin/franchises/reject/{proposalId}` - Rifiuta proposta
- `PATCH /api/admin/franchises/{id}/disable` - Disabilita franchise
- `PATCH /api/admin/franchises/{id}/enable` - Riabilita franchise
- `POST /api/admin/franchises/add` - Crea franchise manualmente

**Funzionalità:**
- Sincronizzazione automatica DB ↔ JSON
- Proposte utenti
- Approvazione/rifiuto admin

---

### **11. ⏰ Scheduler**

**Jobs:**
- `UserDeletionScheduler` - Cancellazione account dopo 7 giorni
- `EmailLogCleanupScheduler` - Pulizia log email (90 giorni)
- `GradeCleanupScheduler` - Pulizia report grading

---

### **12. 📧 Sistema Email**

**Funzionalità:**
- Invio email multi-lingua (25+ lingue)
- Template email modulari
- Fallback sender automatico
- Logging email completo
- Retry automatico (3 tentativi)

**Template disponibili:**
- `account_confirmation_{locale}.html`
- `account_deletion_completed_{locale}.html`
- E altri...

---

## 🔧 Architettura Servizi

### **Sistema Traduzione**

```
TranslateController
    ↓
UnifiedTranslationService (servizio centrale)
    ├── OpenAiTranslateService (provider primario)
    │   ├── translate(text, targetLang) - Traduzione standard
    │   └── executeWithCustomPrompt(prompt) - Prompt personalizzato
    └── DeepLTranslateService (provider secondario)
        └── translate(text, targetLang) - Traduzione fallback
```

### **Generazione nameEn**

```
ProductController.createProduct()
    ↓
ProductService.createProduct()
    ↓
ProductService.generateGlobalEnglishName()
    ├── buildCardNameNormalizationPrompt()
    ├── OpenAiTranslateService.executeWithCustomPrompt()
    ├── DeepLTranslateService.translate() (fallback)
    └── product.setNameEn()
```

---

## 📊 Statistiche Progetto

- **File Java:** ~250 file
- **Controller:** 42 file (21 pubblici + 21 admin)
- **Service:** 32 file
- **Repository:** 20 file
- **Modelli:** 22 file
- **DTO:** 17 file
- **Migration:** 21 file
- **Endpoint API:** ~160+ endpoint

---

## 🔄 Flussi Principali

### **1. Creazione Prodotto con nameEn Automatico**
```
POST /api/products
  → Validazione input
  → Se nameEn null/vuoto:
    → generateGlobalEnglishName()
      → buildCardNameNormalizationPrompt()
      → GPT-4o-mini (prompt personalizzato)
      → Se fallisce → DeepL (traduzione semplice)
      → Se fallisce → nome originale
  → Salvataggio prodotto con nameEn
  → Notifiche admin
  → Ritorno prodotto creato
```

### **2. Traduzione Testo**
```
POST /api/translate
  → Validazione (testo non vuoto, lingua supportata)
  → UnifiedTranslationService.translate()
    → OpenAiTranslateService.translate()
    → Se fallisce → DeepLTranslateService.translate()
    → Se fallisce → testo originale
  → Ritorno testo tradotto
```

### **3. Aggiornamento Profilo con Bio Venditore**
```
PUT /api/user/me
  → Validazione bio (max 500 caratteri)
  → UserService.updateUserProfile()
  → Aggiornamento campi (inclusi descriptionOriginal, descriptionLanguage)
  → Salvataggio
  → Ritorno profilo aggiornato
```

---

## 📝 Note Finali

### **Punti di Forza**
- ✅ Sistema traduzione unificato con fallback automatico
- ✅ Generazione automatica nameEn per prodotti
- ✅ Validazione bio venditore (GDPR compliant)
- ✅ Traduzione dinamica contenuti utente
- ✅ Architettura modulare e scalabile
- ✅ Logging completo e tracciabilità

### **Configurazione Richiesta**
- `OPENAI_API_KEY` - Chiave API OpenAI (per GPT-4o-mini)
- `DEEPL_API_KEY` - Chiave API DeepL (per fallback)
- `DEEPL_API_URL` - URL API DeepL (default: https://api-free.deepl.com/v2/translate)

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 2.0

