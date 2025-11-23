# 📚 STRUTTURA COMPLETA CODESPACE E FUNZIONI - Funkard Backend

**Data:** 2025-01-XX  
**Versione:** Spring Boot 3.5.6 + Java 17  
**Database:** PostgreSQL (Neon)  
**Deploy:** Render.com

---

## 🌐 COLLEGAMENTI E URL

### **Frontend Funkard**
- **Produzione:** https://funkard.com
- **Produzione (www):** https://www.funkard.com
- **Sviluppo Locale:** http://localhost:3000
- **Sviluppo Locale (Alternativo):** http://localhost:3002

### **Pannello Admin**
- **Produzione:** https://admin.funkard.com
- **Sviluppo Locale:** http://localhost:3002

### **Backend API**
- **Produzione:** https://funkard-api.onrender.com (o URL Render configurato)
- **Sviluppo Locale:** http://localhost:8080
- **Health Check:** https://funkard-api.onrender.com/health

### **URL Legacy (Vercel - Deprecati)**
- https://funkard.vercel.app
- https://funkardnew.vercel.app
- https://funkard-admin.vercel.app

### **Email**
- **Primary:** no-reply@funkard.com
- **Fallback:** support@funkard.com
- **Legal:** legal@funkard.com

### **Storage**
- **Cloudflare R2:** Configurato via `R2_PUBLIC_BASE_URL` (env variable)

---

## 📁 STRUTTURA COMPLETA CODESPACE

```
funkard-api/
├── backend/
│   └── sql/
│       └── migrations/
│           └── 2025-10-15_add_grading_columns.sql
├── docs/                          # 📚 Documentazione completa (30+ file)
│   ├── ADMIN_API_ENDPOINTS.md
│   ├── ADMIN_PANEL_INTEGRATION.md
│   ├── AUDIT_GESTIONE_LINGUA_BACKEND.md
│   ├── BACKEND_FRONTEND_INTEGRATION_REPORT.md
│   ├── GDPR_*.md                  # GDPR compliance docs
│   ├── EMAIL_*.md                 # Email system docs
│   ├── TRANSLATION_SYSTEM.md
│   └── ... (30+ documenti)
├── logs/
│   └── funkard-api.log
├── src/
│   ├── main/
│   │   ├── java/com/funkard/
│   │   │   ├── admin/             # 🔐 Modulo Admin (62 file)
│   │   │   │   ├── controller/     # 21 controller admin
│   │   │   │   ├── dto/            # 10 DTO admin
│   │   │   │   ├── log/            # 4 file logging
│   │   │   │   ├── model/          # 6 model admin
│   │   │   │   ├── notification/   # Sistema notifiche
│   │   │   │   ├── repository/     # 4 repository admin
│   │   │   │   ├── service/        # 12 service admin
│   │   │   │   ├── system/         # 1 file system
│   │   │   │   └── util/           # 1 utility
│   │   │   ├── adminaccess/        # 🔑 Accesso Admin (6 file)
│   │   │   ├── adminauth/          # 🔒 Autenticazione Admin (18 file)
│   │   │   ├── common/             # 🌐 Common (1 file)
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── config/             # ⚙️ Configurazione (5 file)
│   │   │   │   ├── CacheConfig.java
│   │   │   │   ├── EmailConfig.java
│   │   │   │   ├── R2Config.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebSocketConfig.java
│   │   │   ├── controller/         # 🌍 Controller Pubblici (22 file)
│   │   │   ├── dto/                # 📦 DTO (19 file)
│   │   │   ├── gradelens/          # 🔍 GradeLens AI (8 file)
│   │   │   ├── grading/            # 📊 Grading (4 file)
│   │   │   ├── market/             # 🛒 Marketplace (16 file)
│   │   │   │   ├── controller/     # 3 controller
│   │   │   │   ├── model/          # 3 model
│   │   │   │   ├── repository/     # 3 repository
│   │   │   │   ├── service/        # 4 service
│   │   │   │   └── trend/         # 3 file trend
│   │   │   ├── model/              # 📋 Model/Entity (23 file)
│   │   │   ├── payload/            # 📝 Payload (1 file)
│   │   │   ├── realtime/           # ⚡ Real-time (4 file)
│   │   │   ├── repository/         # 💾 Repository (20 file)
│   │   │   ├── scheduler/          # ⏰ Scheduler (3 file)
│   │   │   ├── security/           # 🔐 Security (2 file)
│   │   │   ├── service/            # 🔧 Service (32 file)
│   │   │   ├── storage/            # 📦 Storage (1 file)
│   │   │   ├── support/            # 💬 Support
│   │   │   └── user/               # 👤 User (6 file)
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.yml
│   │       ├── application-test.properties
│   │       ├── data/
│   │       │   └── franchises.json
│   │       ├── db/
│   │       │   ├── migration/      # 21 migration SQL
│   │       │   └── *.sql           # Script SQL legacy
│   │       ├── email-templates/    # 📧 Template email (25+ lingue)
│   │       │   ├── en/             # Template inglese
│   │       │   ├── it/             # Template italiano
│   │       │   └── ... (25+ lingue)
│   │       └── static/
│   │           └── admin-notifications-example.html
│   └── test/
│       └── java/com/funkard/
│           └── FunkardApiApplicationTests.java
├── target/                         # Build output
├── Dockerfile
├── Makefile
├── mvnw / mvnw.cmd
├── pom.xml
├── render.yaml
└── spring-boot.log
```

---

## 🎯 CONTROLLER E ENDPOINT API

### **CONTROLLER PUBBLICI/UTENTE** (22 file)

#### **1. AuthController** (`/api/auth`)
- `POST /api/auth/register` - Registrazione utente (GDPR compliant)
- `POST /api/auth/login` - Login (restituisce token, language, preferredCurrency)
- `GET /api/auth/validate?token=...` - Valida token JWT

#### **2. UserController** (`/api/user`)
- `GET /api/user/me` - Profilo utente corrente
- `PUT /api/user/me` - Aggiorna profilo utente
- `PATCH /api/user/preferences` - Aggiorna preferenze (language, currency)
- `GET /api/user/address` - Lista indirizzi utente
- `POST /api/user/address` - Crea nuovo indirizzo
- `PUT /api/user/address/{id}` - Aggiorna indirizzo
- `DELETE /api/user/address/{id}` - Elimina indirizzo
- `PATCH /api/user/address/{id}/default` - Imposta indirizzo predefinito
- `GET /api/user/address/default` - Ottieni indirizzo predefinito
- `GET /api/user/preferences` - Ottieni preferenze (cookie + altre)
- `PUT /api/user/preferences` - Salva preferenze
- `GET /api/user/preferences/cookies` - Ottieni preferenze cookie
- `POST /api/user/preferences/cookies` - Salva preferenze cookie
- `GET /api/user/preferences/cookies/export?format=json|pdf` - Export log cookie (GDPR)
- `DELETE /api/user/delete-account` - Richiedi cancellazione account (GDPR)

#### **3. CardController** (`/api/cards`)
- `GET /api/cards` - Lista tutte le carte
- `POST /api/cards` - Crea carta (admin)

#### **4. UserCardController** (`/api/usercards`)
- `GET /api/usercards/collection/{userId}` - Collezione utente
- `GET /api/usercards/{id}` - Dettaglio carta utente
- `POST /api/usercards` - Aggiungi carta a collezione
- `PUT /api/usercards/{id}` - Aggiorna carta
- `PUT /api/usercards/usercards/{id}` - Aggiorna carta (legacy)
- `DELETE /api/usercards/{id}` - Elimina carta
- `PUT /api/usercards/{id}/raw-images` - Upload immagini carta (multipart)

#### **5. ListingController** (`/api/listings`)
- `GET /api/listings` - Lista annunci
- `GET /api/listings/{id}` - Dettaglio annuncio
- `POST /api/listings` - Crea annuncio (gestisce custom TCG/Language/Franchise)
- `POST /api/listings/legacy` - Crea annuncio (legacy)

#### **6. ProductController** (`/api/products`)
- `GET /api/products` - Lista prodotti
- `GET /api/products/{id}` - Dettaglio prodotto
- `POST /api/products` - Crea prodotto (genera automaticamente nameEn)

#### **7. TransactionController** (`/api/transactions`)
- `GET /api/transactions` - Lista transazioni utente
- `POST /api/transactions` - Crea transazione

#### **8. WishlistController** (`/api/wishlist`)
- `GET /api/wishlist` - Lista wishlist
- `POST /api/wishlist` - Aggiungi a wishlist
- `DELETE /api/wishlist/{id}` - Rimuovi da wishlist

#### **9. FranchiseController** (`/api/franchises`)
- `GET /api/franchises` - Lista franchise (pubblico, da JSON)
- `GET /api/franchises/database` - Lista franchise da database
- `GET /api/franchises/categories` - Lista categorie
- `GET /api/franchises/category/{category}` - Franchise per categoria
- `POST /api/franchises/propose` - Proponi nuovo franchise
- `GET /api/franchises/stats` - Statistiche franchise

#### **10. PendingValueController** (`/api/pending-values`)
- `POST /api/pending-values/submit` - Invia proposta valore custom (TCG/Language)
- `GET /api/pending-values` - Lista proposte (admin)
- `GET /api/pending-values/my` - Le mie proposte
- `POST /api/pending-values/{id}/approve` - Approva proposta (admin)
- `DELETE /api/pending-values/{id}` - Elimina proposta (admin)
- `GET /api/pending-values/stats` - Statistiche proposte

#### **11. ChatController** (`/api/chat`)
- `POST /api/chat/message` - Invia messaggio (con traduzione automatica GPT+DeepL)
- `GET /api/chat/conversation/{userId}` - Conversazione con utente
- `GET /api/chat/unread` - Conta messaggi non letti
- `PUT /api/chat/message/{messageId}/read` - Marca messaggio come letto

#### **12. SupportController** (`/api/support`)
- `POST /api/support/tickets` - Crea ticket supporto
- `GET /api/support/tickets` - Lista ticket utente
- `GET /api/support/tickets/{id}` - Dettaglio ticket
- `POST /api/support/tickets/{id}/reply` - Rispondi a ticket
- `POST /api/support/tickets/{id}/reopen` - Riapri ticket
- `GET /api/support/stats` - Statistiche supporto

#### **13. SupportChatController** (`/api/support/chat`)
- `POST /api/support/chat/{ticketId}/message` - Invia messaggio (con traduzione)
- `GET /api/support/chat/{ticketId}/messages` - Messaggi ticket
- `POST /api/support/chat/{ticketId}/read` - Marca come letto
- `GET /api/support/chat/{ticketId}/stats` - Statistiche chat

#### **14. SupportSseController** (`/api/support/stream`)
- `GET /api/support/stream` - SSE per notifiche real-time supporto
- `GET /api/support/stream/stats` - Statistiche stream

#### **15. AdminSupportSseController** (`/api/admin/support`)
- `GET /api/admin/support/stream` - SSE per admin (notifiche supporto)
- `GET /api/admin/support/stream/stats` - Statistiche stream admin

#### **16. AdminTicketAssignmentController** (`/api/admin/tickets`)
- `POST /api/admin/tickets/{id}/assign` - Assegna ticket
- `POST /api/admin/tickets/{id}/release` - Rilascia ticket
- `POST /api/admin/tickets/{id}/assign-with-role` - Assegna con ruolo
- `POST /api/admin/tickets/{id}/release-with-role` - Rilascia con ruolo
- `GET /api/admin/tickets/assignment-stats` - Statistiche assegnazioni

#### **17. CollectionController** (`/api/collection`)
- `POST /api/collection` - Upload collezione (multipart)
- `GET /api/collection/{userId}` - Collezione utente

#### **18. GradeLensAdminController** (`/api/admin/gradelens`)
- `GET /api/admin/gradelens/metrics` - Metriche GradeLens
- `POST /api/admin/gradelens/purge` - Pulisci risultati vecchi

#### **19. TranslateController** (`/api/translate`)
- `POST /api/translate` - Traduce testo (GPT-4o-mini + DeepL fallback)

#### **20. RootController** (`/`)
- `GET /` - Health check
- `GET /health` - Health check dettagliato

#### **21. TestController** (`/api/test`)
- `GET /api/test/ping` - Test ping
- `GET /api/test/sse-test` - Test SSE

#### **22. AdsController** (`/api/ads`)
- `GET /api/ads/gradelens` - Annunci GradeLens

---

### **CONTROLLER ADMIN** (21 file)

#### **1. AdminDashboardController** (`/api/admin/dashboard`)
- `GET /api/admin/dashboard` - Dashboard admin con statistiche
- `DELETE /api/admin/dashboard/cleanup` - Pulizia dashboard

#### **2. AdminStatsController** (`/api/admin/stats`)
- `GET /api/admin/stats` - Statistiche complete

#### **3. AdminSupportController** (`/api/admin/support`)
- `GET /api/admin/support/tickets` - Lista ticket
- `GET /api/admin/support/stats` - Statistiche supporto
- `POST /api/admin/support/reply/{id}` - Rispondi a ticket
- `POST /api/admin/support/resolve/{id}` - Risolvi ticket
- `POST /api/admin/support/close/{id}` - Chiudi ticket
- `POST /api/admin/support/reopen/{id}` - Riapri ticket
- `POST /api/admin/support/{id}/mark-read` - Marca come letto
- `GET /api/admin/support/new-messages-count` - Conta nuovi messaggi
- `POST /api/admin/support/{id}/assign` - Assegna ticket
- `POST /api/admin/support/{id}/release` - Rilascia ticket
- `GET /api/admin/support/assigned/{supportEmail}` - Ticket assegnati
- `GET /api/admin/support/assigned-count` - Conta ticket assegnati

#### **4. AdminNotificationController** (`/api/admin/notifications`)
- `GET /api/admin/notifications` - Lista notifiche
- `GET /api/admin/notifications/{id}` - Dettaglio notifica
- `POST /api/admin/notifications/{id}/read` - Marca come letta
- `POST /api/admin/notifications/{id}/assign` - Assegna notifica
- `POST /api/admin/notifications/{id}/resolve` - Risolvi notifica
- `POST /api/admin/notifications/{id}/archive` - Archivia notifica
- `DELETE /api/admin/notifications/cleanup` - Pulizia notifiche
- `GET /api/admin/notifications/stream` - SSE stream notifiche
- `GET /api/admin/notifications/unread-count` - Conta non lette
- `GET /api/admin/notifications/unread-latest` - Ultime non lette

#### **5. AdminNotificationStreamController** (`/api/admin/notifications`)
- `GET /api/admin/notifications/stream` - SSE stream notifiche
- `GET /api/admin/notifications/test` - Test stream

#### **6. AdminNotificationBatchController** (`/api/admin/notifications`)
- `POST /api/admin/notifications/batch/resolve` - Risolvi batch
- `POST /api/admin/notifications/batch/archive` - Archivia batch
- `DELETE /api/admin/notifications/batch/delete` - Elimina batch

#### **7. AdminNotificationArchiveController** (`/api/admin/notifications`)
- `GET /api/admin/notifications/archive` - Lista archiviate
- `DELETE /api/admin/notifications/delete/{id}` - Elimina archiviata

#### **8. AdminNotificationActionController** (`/api/admin/notifications`)
- `PATCH /api/admin/notifications/archive/{id}` - Archivia notifica

#### **9. AdminNotificationCleanupController** (`/api/admin/notifications/cleanup`)
- `POST /api/admin/notifications/cleanup/manual` - Pulizia manuale
- `GET /api/admin/notifications/cleanup/stats` - Statistiche pulizia
- `POST /api/admin/notifications/cleanup/test` - Test pulizia
- `GET /api/admin/notifications/cleanup/info` - Info pulizia

#### **10. FranchiseAdminController** (`/api/admin/franchises`)
- `GET /api/admin/franchises` - Lista franchise (con proposte)
- `POST /api/admin/franchises/approve/{proposalId}` - Approva proposta
- `POST /api/admin/franchises/reject/{proposalId}` - Rifiuta proposta
- `PATCH /api/admin/franchises/{id}/disable` - Disabilita franchise
- `PATCH /api/admin/franchises/{id}/enable` - Abilita franchise
- `POST /api/admin/franchises/add` - Aggiungi franchise manualmente

#### **11. AdminFranchiseController** (`/api/admin/franchises`)
- `GET /api/admin/franchises` - Lista franchise
- `POST /api/admin/franchises` - Crea franchise
- `PUT /api/admin/franchises/{id}` - Aggiorna franchise
- `DELETE /api/admin/franchises/{id}` - Elimina franchise
- `GET /api/admin/franchises/stats` - Statistiche franchise

#### **12. AdminPendingValueController** (`/api/admin/pending-values`)
- `GET /api/admin/pending-values` - Lista proposte
- `POST /api/admin/pending-values/{id}/approve` - Approva proposta
- `DELETE /api/admin/pending-values/{id}` - Elimina proposta
- `GET /api/admin/pending-values/stats` - Statistiche proposte

#### **13. AdminEmailLogController** (`/api/admin/email-logs`)
- `GET /api/admin/email-logs` - Lista log email
- `GET /api/admin/email-logs/{id}` - Dettaglio log email
- `GET /api/admin/email-logs/stats` - Statistiche email

#### **14. CookieLogAdminController** (`/api/admin/cookies`)
- `GET /api/admin/cookies/logs` - Lista log cookie
- `GET /api/admin/cookies/logs/export` - Export log cookie (admin)

#### **15. EmailTemplateTestController** (`/api/admin/email-templates/test`)
- `POST /api/admin/email-templates/test/all` - Test tutti i template
- `POST /api/admin/email-templates/test/variables` - Test variabili template

#### **16. AdminSupportCleanupController** (`/api/admin/support`)
- `DELETE /api/admin/support/cleanup` - Pulizia supporto

#### **17. AdminCleanupController** (`/api/admin/cleanup`)
- `POST /api/admin/cleanup/manual` - Pulizia manuale
- `GET /api/admin/cleanup/stats` - Statistiche pulizia
- `POST /api/admin/cleanup/test` - Test pulizia

#### **18. AdminValuationController** (`/api/admin/valuation`)
- `GET /api/admin/valuation/overview` - Overview valutazioni

#### **19. RolePermissionController** (`/api/admin/roles`)
- `GET /api/admin/roles/permissions/{userEmail}` - Permessi utente
- `POST /api/admin/roles/check-permissions` - Verifica permessi
- `GET /api/admin/roles/available` - Ruoli disponibili

#### **20. AdminLegacyAuthController** (`/api/admin`)
- `GET /api/admin/ping` - Ping admin

#### **21. SupportTicketController** (`/api/support`)
- `POST /api/support` - Crea ticket (admin)
- `GET /api/support` - Lista ticket (admin)
- `GET /api/support/{id}` - Dettaglio ticket (admin)
- `POST /api/support/{id}/status` - Aggiorna status
- `POST /api/support/{id}/message` - Aggiungi messaggio

---

### **CONTROLLER AUTHENTICATION** (4 file)

#### **1. AdminAuthController** (`/api/admin/auth`)
- Endpoint autenticazione admin

#### **2. AdminTokenController** (`/api/admin/token`)
- Gestione token admin

#### **3. AccessRequestController** (`/api/admin/access-request`)
- Richieste accesso admin

#### **4. AdminAccessController** (`/api/admin/access`)
- Gestione accesso admin

---

## 🔧 SERVICE (32 file)

### **User & Profile Services**
1. **UserService** - Gestione utenti e profili
2. **UserAddressService** - Gestione indirizzi utente
3. **UserPreferencesService** - Gestione preferenze utente
4. **UserAccountDeletionService** - Gestione cancellazione account (GDPR)
5. **UserDeletionService** - Servizio cancellazione account

### **Marketplace Services**
6. **ProductService** - Gestione prodotti (genera nameEn automaticamente)
7. **ListingService** - Gestione annunci
8. **TransactionService** - Gestione transazioni
9. **WishlistService** - Gestione wishlist
10. **CardService** - Gestione carte

### **Franchise Services**
11. **FranchiseCatalogService** - Gestione catalogo franchise
12. **FranchiseJsonService** - Gestione franchise da JSON
13. **FranchiseAdminService** - Gestione admin franchise
14. **PendingValueService** - Gestione proposte valori custom

### **Translation Services**
15. **UnifiedTranslationService** - Servizio traduzione unificato (GPT + DeepL)
16. **OpenAiTranslateService** - Traduzione OpenAI GPT-4o-mini
17. **DeepLTranslateService** - Traduzione DeepL (fallback)
18. **TranslationService** - Servizio traduzione legacy
19. **TranslationProvider** - Interfaccia provider traduzione

### **Chat & Support Services**
20. **ChatService** - Gestione chat utenti (con traduzione)
21. **SupportMessageService** - Gestione messaggi supporto (con traduzione)

### **Email Services**
22. **EmailService** - Servizio email principale (primary + fallback)
23. **EmailTemplateManager** - Gestione template email multilingua (25+ lingue)
24. **EmailLocaleHelper** - Helper locale email
25. **EmailLogService** - Gestione log email
26. **EmailTemplateTestService** - Test template email

### **Grading Services**
27. **GradeLensService** - Grading AI carte
28. **GradeLensCleanupService** - Pulizia risultati grading
29. **GradeReportLookupService** - Lookup report grading
30. **GradeCalculator** - Calcolo grading

### **Storage Services**
31. **R2Service** - Cloudflare R2 storage (S3-compatible)

### **Cookie & GDPR Services**
32. **CookieConsentLogService** - Gestione log consenso cookie
33. **CookieLogExportService** - Export log cookie (JSON/PDF)

### **Admin Services**
34. **AdminNotifier** - Notifiche admin
35. **AdminNotificationService** - Gestione notifiche admin
36. **AdminStatsService** - Statistiche admin

---

## 💾 REPOSITORY (20 file)

1. **UserRepository** - Repository utenti
2. **UserCardRepository** - Repository carte utente
3. **UserAddressRepository** - Repository indirizzi
4. **UserPreferencesRepository** - Repository preferenze
5. **UserDeletionRepository** - Repository cancellazioni account
6. **CardRepository** - Repository carte
7. **ListingRepository** - Repository annunci
8. **TransactionRepository** - Repository transazioni
9. **WishlistRepository** - Repository wishlist
10. **ChatMessageRepository** - Repository messaggi chat
11. **FranchiseRepository** - Repository franchise
12. **FranchiseCatalogRepository** - Repository catalogo franchise
13. **FranchiseProposalRepository** - Repository proposte franchise
14. **PendingValueRepository** - Repository proposte valori
15. **GradeLensRepository** - Repository risultati grading
16. **GradeReportRepository** - Repository report grading
17. **EmailLogRepository** - Repository log email
18. **CookieConsentLogRepository** - Repository log cookie
19. **TranslationLogRepository** - Repository log traduzioni
20. **VerificationTokenRepository** - Repository token verifica

---

## 📋 MODEL/ENTITY (23 file)

1. **User** - Utente (con language, preferredCurrency, GDPR fields)
2. **UserCard** - Carta utente
3. **UserAddress** - Indirizzo utente
4. **UserPreferences** - Preferenze utente
5. **UserDeletion** - Cancellazione account
6. **Card** - Carta
7. **Listing** - Annuncio
8. **Product** - Prodotto (con descriptionOriginal, descriptionLanguage, nameEn)
9. **Transaction** - Transazione
10. **Wishlist** - Wishlist
11. **ChatMessage** - Messaggio chat (con traduzione)
12. **Franchise** - Franchise
13. **FranchiseCatalog** - Catalogo franchise
14. **FranchiseProposal** - Proposta franchise
15. **PendingValue** - Proposta valore custom
16. **GradeLensResult** - Risultato grading AI
17. **GradeReport** - Report grading
18. **EmailLog** - Log email
19. **CookieConsentLog** - Log consenso cookie
20. **TranslationLog** - Log traduzione
21. **VerificationToken** - Token verifica
22. **CardSource** - Fonte carta
23. **CardType** - Tipo carta

---

## 📦 DTO (19 file)

1. **UserDTO** - DTO utente
2. **UserProfileDTO** - DTO profilo utente
3. **UserPreferencesDTO** - DTO preferenze utente
4. **CookiePreferencesDTO** - DTO preferenze cookie
5. **CardDTO** - DTO carta
6. **ListingDTO** - DTO annuncio
7. **CreateListingRequest** - Request creazione annuncio
8. **TransactionDTO** - DTO transazione
9. **WishlistDTO** - DTO wishlist
10. **ChatMessageDTO** - DTO messaggio chat
11. **FranchiseDTO** - DTO franchise
12. **FranchiseProposalDTO** - DTO proposta franchise
13. **CreateFranchiseRequest** - Request creazione franchise
14. **ProposeFranchiseRequest** - Request proposta franchise
15. **PendingValueDTO** - DTO proposta valore
16. **SubmitPendingValueRequest** - Request proposta valore
17. **TranslateRequest** - Request traduzione
18. **TranslateResponse** - Response traduzione
19. **LoginResponse** - Response login (token, language, preferredCurrency)

---

## ⏰ SCHEDULER (3 file)

1. **UserDeletionScheduler** - Cancellazione account dopo 7 giorni (GDPR)
2. **EmailLogCleanupScheduler** - Pulizia log email vecchi
3. **GradeCleanupScheduler** - Pulizia risultati grading vecchi

---

## 🔐 SECURITY (2 file)

1. **JwtUtil** - Utility JWT
2. **JwtFilter** - Filter JWT

---

## ⚙️ CONFIG (5 file)

1. **SecurityConfig** - Configurazione sicurezza (CORS, JWT)
2. **CacheConfig** - Configurazione cache Caffeine
3. **EmailConfig** - Configurazione email (primary + fallback)
4. **R2Config** - Configurazione Cloudflare R2
5. **WebSocketConfig** - Configurazione WebSocket

---

## 📊 STATISTICHE PROGETTO

| Categoria | Quantità |
|-----------|----------|
| **Controller** | 58 file |
| **Service** | 32 file |
| **Repository** | 20 file |
| **Model/Entity** | 23 file |
| **DTO** | 19 file |
| **Config** | 5 file |
| **Scheduler** | 3 file |
| **Security** | 2 file |
| **Migration SQL** | 21 file |
| **Email Templates** | 25+ lingue |
| **Documentazione** | 30+ file |

---

## 🚀 DEPLOYMENT

### **Render.com**
- **File:** `render.yaml`
- **Build:** `./mvnw clean package -DskipTests`
- **Start:** `java -jar target/funkard-api-0.0.1-SNAPSHOT.jar`
- **Port:** Configurato via `PORT` env variable (default: 10000)

### **Database**
- **Provider:** Neon PostgreSQL
- **Connection Pool:** HikariCP (max 5 connections)
- **Migrations:** Flyway (disabilitato in dev, abilitato in prod)

### **Environment Variables**
- `SPRING_DATASOURCE_URL` - URL database
- `SPRING_DATASOURCE_USERNAME` - Username database
- `SPRING_DATASOURCE_PASSWORD` - Password database
- `MAIL_HOST` - SMTP host
- `MAIL_PORT` - SMTP port
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `OPENAI_API_KEY` - OpenAI API key
- `DEEPL_API_KEY` - DeepL API key
- `ADMIN_TOKEN` - Admin token
- `R2_PUBLIC_BASE_URL` - Cloudflare R2 public URL

---

## 📝 NOTE IMPORTANTI

1. **CORS:** Configurato per `funkard.com`, `www.funkard.com`, `admin.funkard.com`, `localhost:3000`, `localhost:3002`
2. **GDPR Compliance:** Sistema completo per consenso, cancellazione account, export dati
3. **Traduzione:** Sistema unificato GPT-4o-mini + DeepL fallback
4. **Email:** Sistema multilingua con 25+ lingue supportate
5. **Cache:** Caffeine cache per endpoint pubblici read-only
6. **Real-time:** SSE e WebSocket per notifiche admin e supporto

---

**Fine Documento**

