# 🌍 Sistema Traduzione Automatica - Funkard Backend

**Data Implementazione:** 2025-01-15  
**Versione:** 1.0

---

## 📋 Panoramica

Sistema completo di traduzione automatica per:
- 💬 Chat tra utenti
- 🎧 Chat di supporto (utente ↔ admin)
- 📧 Email multilingua (già implementato)

Tutti i messaggi vengono tradotti automaticamente quando mittente e destinatario hanno lingue diverse, con logging completo per audit e privacy.

---

## ✅ Componenti Implementati

### **1. Modelli Database**

#### **SupportMessage (Aggiornato)**
- `originalLanguage` (VARCHAR 5) - Lingua originale (ISO 639-1)
- `translatedText` (TEXT) - Testo tradotto
- `targetLanguage` (VARCHAR 5) - Lingua destinazione
- `isTranslated` (BOOLEAN) - Flag traduzione

#### **ChatMessage (Nuovo)**
- Modello completo per chat tra utenti
- Campi traduzione identici a SupportMessage
- Relazioni con User (sender, recipient)

#### **TranslationLog (Nuovo)**
- Tabella audit per tutte le traduzioni
- Traccia provider, successo, errori
- Riferimenti opzionali a user, messageType, messageId

---

### **2. TranslationService**

**File:** `src/main/java/com/funkard/service/TranslationService.java`

**Funzionalità:**
- Supporta provider multipli (DeepL, Google Translate, interno)
- Fallback automatico se provider principale fallisce
- Normalizzazione codici lingua ISO 639-1
- Logging completo in `translation_logs`
- Gestione errori robusta (restituisce testo originale se fallisce)

**Metodi:**
- `translate(text, fromLang, toLang, userId, messageType, messageId)` - Traduce testo
- `normalizeLanguage(lang)` - Normalizza codice lingua
- `logTranslation(...)` - Logga in database

---

### **3. Chat Service**

**File:** `src/main/java/com/funkard/service/ChatService.java`

**Funzionalità:**
- Invia messaggi tra utenti con traduzione automatica
- Rileva lingue da `user.language`
- Traduce se lingue diverse
- Salva testo originale + tradotto

**Metodi:**
- `sendMessage(senderId, recipientId, text)` - Invia con traduzione
- `getConversation(userId1, userId2)` - Recupera conversazione
- `getUnreadMessages(userId)` - Messaggi non letti
- `markAsRead(messageId, userId)` - Marca come letto

---

### **4. Support Message Service (Aggiornato)**

**File:** `src/main/java/com/funkard/admin/service/SupportMessageService.java`

**Funzionalità:**
- Traduzione automatica tra utente e admin
- Rileva lingua utente da `user.language`
- Admin usa lingua default ("en")
- Salva campi traduzione nel database

---

### **5. Endpoint API**

#### **POST /api/chat/message**
**Request:**
```json
{
  "recipientId": 123,
  "text": "Ciao, come stai?"
}
```

**Response:**
```json
{
  "id": "uuid",
  "senderId": 1,
  "recipientId": 123,
  "originalText": "Ciao, come stai?",
  "translatedText": "Hi, how are you?",
  "originalLanguage": "it",
  "targetLanguage": "en",
  "isTranslated": true,
  "createdAt": "2025-01-15T10:30:00"
}
```

#### **POST /api/support/chat/{ticketId}/message**
**Request:**
```json
{
  "message": "Ho un problema",
  "sender": "user"
}
```

**Response:**
```json
{
  "success": true,
  "messageId": "uuid",
  "originalText": "Ho un problema",
  "translatedText": "I have a problem",
  "originalLanguage": "it",
  "targetLanguage": "en",
  "isTranslated": true,
  ...
}
```

#### **GET /api/support/chat/{ticketId}/messages**
**Response:**
```json
{
  "success": true,
  "messages": [
    {
      "id": "uuid",
      "sender": "user",
      "content": "Ho un problema",
      "originalText": "Ho un problema",
      "translatedText": "I have a problem",
      "originalLanguage": "it",
      "targetLanguage": "en",
      "isTranslated": true,
      "createdAt": "2025-01-15T10:30:00"
    }
  ]
}
```

---

## 📊 Database Schema

### **Migration V13: Support Messages**
```sql
ALTER TABLE support_messages 
ADD COLUMN original_language VARCHAR(5),
ADD COLUMN translated_text TEXT,
ADD COLUMN target_language VARCHAR(5),
ADD COLUMN is_translated BOOLEAN DEFAULT false;
```

### **Migration V14: Chat Messages**
```sql
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    sender_id BIGINT REFERENCES users(id),
    recipient_id BIGINT REFERENCES users(id),
    original_text TEXT NOT NULL,
    original_language VARCHAR(5),
    translated_text TEXT,
    target_language VARCHAR(5),
    is_translated BOOLEAN DEFAULT false,
    created_at TIMESTAMP,
    read_at TIMESTAMP
);
```

### **Migration V15: Translation Logs**
```sql
CREATE TABLE translation_logs (
    id UUID PRIMARY KEY,
    source_text TEXT NOT NULL,
    translated_text TEXT,
    source_language VARCHAR(5) NOT NULL,
    target_language VARCHAR(5) NOT NULL,
    translation_provider VARCHAR(50),
    success BOOLEAN DEFAULT true,
    error_message TEXT,
    created_at TIMESTAMP,
    user_id BIGINT REFERENCES users(id),
    message_type VARCHAR(50),
    message_id UUID
);
```

---

## 🔧 Configurazione

### **application.properties**
```properties
# Provider traduzione: deepl, google, internal
translation.provider=internal

# DeepL API (opzionale)
translation.deepl.api-key=your-deepl-api-key

# Google Translate API (opzionale)
translation.google.api-key=your-google-api-key
```

---

## 📝 Formato Response JSON

Tutti i messaggi includono sempre questi campi:

```json
{
  "originalText": "Testo originale",
  "translatedText": "Translated text",
  "originalLanguage": "it",
  "targetLanguage": "en",
  "isTranslated": true
}
```

**Note:**
- `originalLanguage` è sempre presente (ISO 639-1)
- `translatedText` è `null` se non tradotto
- `isTranslated` è `false` se lingue identiche o traduzione fallita
- Codici lingua sono ISO 639-1 (es. "it", "en", "es")

---

## 🔒 Privacy e GDPR

### **Logging Traduzioni:**
- Tutte le traduzioni sono loggate in `translation_logs`
- Include testo originale e tradotto
- Traccia provider, successo, errori
- Riferimenti opzionali a user e message

### **Termini e Condizioni:**
- Informare utenti che le chat possono essere tradotte automaticamente
- Testo originale sempre preservato
- Traduzione opzionale (fallback a originale se fallisce)

---

## 🧪 Test

### **Test Chat Utenti:**
```bash
POST /api/chat/message
{
  "recipientId": 123,
  "text": "Ciao, come stai?"
}
```

### **Test Support Chat:**
```bash
POST /api/support/chat/{ticketId}/message
{
  "message": "Ho un problema",
  "sender": "user"
}
```

### **Verifica Traduzione:**
```sql
SELECT * FROM translation_logs 
WHERE message_type = 'chat' 
ORDER BY created_at DESC;
```

---

## ✅ Checklist Implementazione

### **Database:**
- [x] Migration V13: Campi traduzione support_messages
- [x] Migration V14: Tabella chat_messages
- [x] Migration V15: Tabella translation_logs
- [x] Indici per performance

### **Modelli:**
- [x] SupportMessage aggiornato
- [x] ChatMessage creato
- [x] TranslationLog creato
- [x] DTO aggiornati (SupportMessageDTO, ChatMessageDTO)

### **Servizi:**
- [x] TranslationService con provider multipli
- [x] ChatService per chat utenti
- [x] SupportMessageService aggiornato

### **Controller:**
- [x] ChatController per chat utenti
- [x] SupportChatController aggiornato
- [x] Response JSON con campi traduzione

### **Email:**
- [x] Sistema email multilingua già implementato
- [x] Template per 25+ lingue
- [x] Fallback automatico all'inglese

---

## 🚀 Risultato Finale

✅ **Traduzione automatica funzionante per chat e supporto**  
✅ **Tutti i messaggi includono campi traduzione nel JSON**  
✅ **Logging completo per audit e privacy**  
✅ **Fallback robusto (testo originale se traduzione fallisce)**  
✅ **Supporto provider multipli (DeepL, Google, interno)**  
✅ **Email multilingua già implementata**

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

