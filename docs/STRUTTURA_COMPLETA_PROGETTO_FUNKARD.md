# 📁 STRUTTURA COMPLETA PROGETTO FUNKARD API

**Versione:** 0.0.1-SNAPSHOT  
**Framework:** Spring Boot 3.5.6  
**Java:** 17  
**Database:** PostgreSQL  
**Data Aggiornamento:** 24 Novembre 2024

---

## 📋 INDICE

1. [Albero del Progetto](#albero-del-progetto)
2. [Struttura Package Java](#struttura-package-java)
3. [Endpoint API Completi](#endpoint-api-completi)
4. [Collegamenti e Dipendenze](#collegamenti-e-dipendenze)
5. [Database Schema](#database-schema)
6. [Funzionalità Implementate](#funzionalità-implementate)
7. [Configurazioni](#configurazioni)
8. [Servizi e Componenti](#servizi-e-componenti)

---

## 1. 🌳 ALBERO DEL PROGETTO

```
funkard-api/
├── backend/
│   └── sql/
│       └── migrations/
│           └── 2025-10-15_add_grading_columns.sql
├── docs/                          # Documentazione completa
│   ├── ADMIN_API_ENDPOINTS.md
│   ├── GDPR_*.md                  # Documentazione GDPR
│   ├── TRANSLATION_SYSTEM.md
│   ├── REPORT_COMPLETO_SISTEMA_VALUTE_BACKEND.md
│   └── ... (35+ documenti)
├── logs/                          # Log applicazione
│   └── funkard-api.log
├── src/
│   ├── main/
│   │   ├── java/com/funkard/
│   │   │   ├── admin/             # Sistema admin completo
│   │   │   ├── adminaccess/       # Gestione accessi admin
│   │   │   ├── adminauth/         # Autenticazione admin
│   │   │   ├── api/               # API utilities
│   │   │   ├── common/            # Componenti comuni
│   │   │   ├── config/            # Configurazioni Spring
│   │   │   ├── controller/       # REST Controllers pubblici
│   │   │   ├── currency/          # Sistema multi-valuta
│   │   │   ├── dto/               # Data Transfer Objects
│   │   │   ├── gradelens/         # Sistema grading carte
│   │   │   ├── grading/           # Grading service
│   │   │   ├── maintenance/       # Manutenzione sistema
│   │   │   ├── market/            # Marketplace
│   │   │   ├── model/             # Entity JPA
│   │   │   ├── payload/           # Request payloads
│   │   │   ├── realtime/          # Server-Sent Events
│   │   │   ├── repository/        # Repository JPA
│   │   │   ├── scheduler/         # Scheduled jobs
│   │   │   ├── security/          # Sicurezza JWT
│   │   │   ├── service/           # Business logic
│   │   │   ├── storage/           # Cloudflare R2 storage
│   │   │   ├── support/           # Support system
│   │   │   └── user/              # User management
│   │   └── resources/
│   │       ├── application*.properties/yml  # Configurazioni
│   │       ├── data/
│   │       │   └── franchises.json         # Catalogo franchise
│   │       ├── db/
│   │       │   ├── migration/             # Flyway migrations (22 file)
│   │       │   └── *.sql                   # SQL inizializzazione
│   │       ├── email-templates/            # Template email (25+ lingue)
│   │       └── static/                     # File statici
│   └── test/
│       └── java/com/funkard/
├── target/                        # Build output
├── Dockerfile
├── Makefile
├── mvnw, mvnw.cmd
├── pom.xml                        # Maven configuration
├── render.yaml                    # Render deployment config
└── spring-boot.log
```

---

## 2. 📦 STRUTTURA PACKAGE JAVA

### **2.1 Package Principali**

```
com.funkard
├── FunkardApiApplication.java          # Entry point Spring Boot
│
├── admin/                               # Sistema Admin Panel (62 file)
│   ├── AdminController.java
│   ├── AdminService.java
│   ├── AdminConfig.java
│   ├── controller/                      # 21 controller admin
│   │   ├── AdminDashboardController.java
│   │   ├── AdminFranchiseController.java
│   │   ├── AdminSupportController.java
│   │   ├── AdminNotificationController.java
│   │   ├── AdminStatsController.java
│   │   ├── AdminEmailLogController.java
│   │   ├── AdminPendingValueController.java
│   │   ├── FranchiseAdminController.java
│   │   └── ... (14 altri)
│   ├── dto/                             # 10 DTO admin
│   ├── log/                             # 4 classi logging
│   ├── model/                           # 6 entità admin
│   ├── repository/                      # 4 repository admin
│   ├── service/                         # 12 servizi admin
│   ├── system/                          # System maintenance
│   └── util/                            # Utilities admin
│
├── adminaccess/                         # Gestione accessi admin (6 file)
│   ├── controller/
│   │   └── AdminAccessController.java
│   ├── model/
│   │   ├── AdminAccessToken.java
│   │   └── AdminAccessRequest.java
│   ├── repository/
│   │   ├── AdminAccessTokenRepository.java
│   │   └── AdminAccessRequestRepository.java
│   └── service/
│       └── AdminAccessService.java
│
├── adminauth/                           # Autenticazione admin (14 file)
│   ├── AdminAuthController.java
│   ├── AdminTokenController.java
│   ├── AccessRequestController.java
│   ├── AdminUser.java
│   ├── AdminUserService.java
│   ├── AdminUserRepository.java
│   ├── AdminToken.java
│   ├── AdminTokenService.java
│   ├── AdminTokenRepository.java
│   ├── AccessRequest.java
│   ├── AccessRequestService.java
│   ├── AccessRequestRepository.java
│   ├── AdminBootstrap.java
│   └── AdminTableInitializer.java
│
├── api/                                 # API utilities (1 file)
│   └── i18n/
│       └── SupportedLanguages.java      # Whitelist 31 lingue
│
├── common/                               # Componenti comuni (1 file)
│   └── GlobalExceptionHandler.java      # Gestione errori centralizzata
│
├── config/                               # Configurazioni Spring (6 file)
│   ├── CacheConfig.java                 # Caffeine cache
│   ├── EmailConfig.java                 # Email configuration
│   ├── R2Config.java                    # Cloudflare R2
│   ├── SecurityConfig.java              # Spring Security + JWT
│   ├── SupportedCurrencies.java         # Whitelist 7 valute
│   └── WebSocketConfig.java             # WebSocket config
│
├── controller/                           # REST Controllers pubblici (21 file)
│   ├── AuthController.java              # Autenticazione
│   ├── UserController.java              # Gestione utenti
│   ├── ProductController.java           # Prodotti (market)
│   ├── ListingController.java           # Listings
│   ├── TransactionController.java       # Transazioni
│   ├── ChatController.java              # Chat utenti
│   ├── SupportChatController.java       # Chat support
│   ├── SupportController.java           # Support tickets
│   ├── FranchiseController.java          # Franchise pubblico
│   ├── PendingValueController.java      # Valori pending
│   ├── TranslateController.java         # Traduzione API
│   ├── UserCardController.java          # Collezione utente
│   ├── WishlistController.java          # Wishlist
│   ├── CardController.java              # Carte
│   ├── CollectionController.java        # Collezione
│   ├── AdsController.java               # Pubblicità
│   ├── TestController.java              # Test endpoints
│   ├── RootController.java              # Root endpoint
│   ├── AdminTicketAssignmentController.java
│   ├── GradeLensAdminController.java
│   └── LanguageWhitelist.java          # Validazione lingue
│
├── currency/                             # Sistema multi-valuta (2 file)
│   ├── CurrencyController.java          # Endpoint conversione
│   └── CurrencyConversionService.java   # Servizio conversione
│
├── dto/                                  # Data Transfer Objects (19 file)
│   ├── UserProfileDTO.java
│   ├── UserPreferencesDTO.java
│   ├── LoginResponse.java
│   ├── CreateListingRequest.java
│   ├── ListingDTO.java
│   ├── TransactionDTO.java
│   ├── ChatMessageDTO.java
│   ├── CookiePreferencesDTO.java
│   ├── FranchiseDTO.java
│   ├── FranchiseProposalDTO.java
│   ├── PendingValueDTO.java
│   ├── TranslateRequest.java
│   ├── TranslateResponse.java
│   └── ... (6 altri)
│
├── gradelens/                            # Sistema grading carte (8 file)
│   ├── controller/
│   │   └── GradeLensController.java
│   ├── GradeResult.java
│   ├── HeuristicAiProvider.java
│   ├── model/                           # 4 modelli grading
│   └── service/
│       └── GradeLensService.java
│
├── grading/                              # Grading service (4 file)
│   ├── controller/
│   │   └── GradingController.java
│   ├── model/
│   │   └── GradingRequest.java
│   ├── repository/
│   │   └── GradingRequestRepository.java
│   └── service/
│       └── GradingService.java
│
├── maintenance/                          # Manutenzione (1 file)
│   └── GradeReportCleanup.java
│
├── market/                               # Marketplace (16 file)
│   ├── controller/
│   │   ├── ProductController.java
│   │   ├── TrendController.java
│   │   └── MarketValuationController.java
│   ├── model/
│   │   ├── Product.java
│   │   ├── MarketListing.java
│   │   └── MarketValuation.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   ├── MarketListingRepository.java
│   │   └── MarketValuationRepository.java
│   ├── service/
│   │   ├── ProductService.java
│   │   ├── TrendService.java
│   │   ├── MarketValuationService.java
│   │   └── FranchiseJsonService.java
│   └── trend/
│       └── Trend.java, TrendDTO.java, TrendService.java
│
├── model/                                # Entity JPA (23 file)
│   ├── User.java
│   ├── UserCard.java
│   ├── UserAddress.java
│   ├── UserPreferences.java
│   ├── UserDeletion.java
│   ├── Card.java
│   ├── Listing.java
│   ├── Transaction.java
│   ├── Wishlist.java
│   ├── ChatMessage.java
│   ├── TranslationLog.java
│   ├── EmailLog.java
│   ├── CookieConsentLog.java
│   ├── GradeReport.java
│   ├── GradeLensResult.java
│   ├── VerificationToken.java
│   ├── Franchise.java
│   ├── FranchiseProposal.java
│   ├── FranchiseCatalog.java
│   ├── PendingValue.java
│   ├── CardType.java
│   ├── CardSource.java
│   └── dto/
│
├── payload/                              # Request payloads (1 file)
│   └── RegisterRequest.java
│
├── realtime/                             # Server-Sent Events (4 file)
│   ├── AdminStreamController.java       # SSE admin
│   ├── SupportStreamController.java     # SSE support
│   ├── RealtimeConfig.java
│   └── EventType.java
│
├── repository/                           # Repository JPA (20 file)
│   ├── UserRepository.java
│   ├── UserCardRepository.java
│   ├── UserAddressRepository.java
│   ├── UserPreferencesRepository.java
│   ├── CardRepository.java
│   ├── ListingRepository.java
│   ├── TransactionRepository.java
│   ├── WishlistRepository.java
│   ├── ChatMessageRepository.java
│   ├── TranslationLogRepository.java
│   ├── EmailLogRepository.java
│   ├── CookieConsentLogRepository.java
│   ├── UserDeletionRepository.java
│   ├── FranchiseRepository.java
│   ├── FranchiseProposalRepository.java
│   ├── PendingValueRepository.java
│   └── ... (4 altri)
│
├── scheduler/                            # Scheduled jobs (3 file)
│   ├── UserDeletionScheduler.java       # Cancellazione account (GDPR)
│   ├── EmailLogCleanupScheduler.java    # Pulizia log email
│   └── GradeCleanupScheduler.java       # Pulizia grading
│
├── security/                             # Sicurezza (2 file)
│   ├── JwtFilter.java                   # JWT authentication filter
│   └── JwtUtil.java                      # JWT utilities
│
├── service/                               # Business logic (32 file)
│   ├── UserService.java
│   ├── UserAddressService.java
│   ├── UserPreferencesService.java
│   ├── UserAccountDeletionService.java
│   ├── CookieConsentLogService.java
│   ├── CookieLogExportService.java
│   ├── ProductService.java              # (market)
│   ├── ListingService.java
│   ├── TransactionService.java
│   ├── ChatService.java
│   ├── TranslationService.java
│   ├── UnifiedTranslationService.java   # GPT + DeepL
│   ├── OpenAiTranslateService.java
│   ├── DeepLTranslateService.java
│   ├── EmailService.java
│   ├── EmailTemplateManager.java
│   ├── EmailLogService.java
│   ├── EmailLocaleHelper.java
│   ├── EmailTemplateTestService.java
│   ├── FranchiseAdminService.java
│   ├── FranchiseJsonService.java
│   ├── FranchiseCatalogService.java
│   ├── PendingValueService.java
│   ├── UserDeletionService.java
│   └── ... (11 altri)
│
├── storage/                              # Cloudflare R2 (1 file)
│   └── ImageStorageService.java
│
└── user/                                 # User management (6 file)
    └── payment/
        ├── PaymentMethod.java
        ├── PaymentMethodController.java
        └── ... (4 altri)
```

**Totale File Java:** ~254 file

---

## 3. 🔌 ENDPOINT API COMPLETI

### **3.1 Autenticazione** (`/api/auth`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `POST` | `/api/auth/register` | Registrazione utente (GDPR compliant) | ❌ Pubblico |
| `POST` | `/api/auth/login` | Login utente | ❌ Pubblico |
| `GET` | `/api/auth/validate` | Validazione token JWT | ❌ Pubblico |

**Dettagli:**
- `POST /api/auth/register`: Accetta `RegisterRequest` con `acceptTerms`, `acceptPrivacy`, `language`, `preferredCurrency`
- `POST /api/auth/login`: Restituisce `LoginResponse` con `token`, `language`, `preferredCurrency`
- `GET /api/auth/validate`: Valida token JWT

---

### **3.2 Utenti** (`/api/user`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/user/me` | Profilo utente corrente | ✅ JWT |
| `PUT` | `/api/user/me` | Aggiorna profilo utente | ✅ JWT |
| `GET` | `/api/user/users` | Lista tutti gli utenti | ✅ JWT |
| `POST` | `/api/user/users` | Crea nuovo utente | ✅ JWT |
| `DELETE` | `/api/user/users/{id}` | Elimina utente | ✅ JWT |
| `DELETE` | `/api/user/delete-account` | Cancellazione account (GDPR) | ✅ JWT |

**Preferenze:**
| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/user/preferences` | Preferenze cookie | ❌ Opzionale |
| `PUT` | `/api/user/preferences` | Salva preferenze cookie | ❌ Opzionale |
| `PATCH` | `/api/user/preferences` | Aggiorna preferenze (language, currency) | ✅ JWT |
| `GET` | `/api/user/preferences/cookies` | Cookie preferences utente | ✅ JWT |
| `POST` | `/api/user/preferences/cookies` | Salva cookie preferences | ✅ JWT |
| `GET` | `/api/user/preferences/cookies/export` | Export log cookie (PDF/JSON) | ✅ JWT |

**Indirizzi:**
| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/user/address` | Lista indirizzi utente | ✅ JWT |
| `POST` | `/api/user/address` | Aggiungi indirizzo | ✅ JWT |
| `PUT` | `/api/user/address/{id}` | Aggiorna indirizzo | ✅ JWT |
| `DELETE` | `/api/user/address/{id}` | Elimina indirizzo | ✅ JWT |
| `PATCH` | `/api/user/address/{id}/default` | Imposta indirizzo default | ✅ JWT |
| `GET` | `/api/user/address/default` | Indirizzo predefinito | ✅ JWT |

---

### **3.3 Prodotti** (`/api/products`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/products` | Lista tutti i prodotti | ❌ Pubblico (cached) |
| `POST` | `/api/products` | Crea nuovo prodotto | ✅ JWT |
| `GET` | `/api/products/{id}` | Dettagli prodotto | ❌ Pubblico |

**Caratteristiche:**
- Supporta `descriptionOriginal`, `descriptionLanguage`
- Generazione automatica `nameEn` (GPT-4o-mini)
- Validazione `currency` (default USD)

---

### **3.4 Listings** (`/api/listings`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/listings` | Lista tutti i listings | ❌ Pubblico (cached) |
| `POST` | `/api/listings` | Crea nuovo listing | ✅ JWT |
| `POST` | `/api/listings/legacy` | Crea listing (legacy) | ✅ JWT |

**Caratteristiche:**
- Supporta `currency` (default USD)
- Gestione valori personalizzati "Altro" (TCG, Language, Franchise)

---

### **3.5 Transazioni** (`/api/transactions`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/transactions` | Lista transazioni | ✅ JWT |
| `POST` | `/api/transactions` | Crea transazione | ✅ JWT |

**Caratteristiche:**
- Supporta `currency` (default USD)
- Collegata a `Listing` e `User` (buyer)

---

### **3.6 Chat** (`/api/chat`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `POST` | `/api/chat/message` | Invia messaggio | ✅ JWT |
| `GET` | `/api/chat/conversation/{userId}` | Conversazione con utente | ✅ JWT |
| `GET` | `/api/chat/unread` | Messaggi non letti | ✅ JWT |
| `PUT` | `/api/chat/message/{messageId}/read` | Segna come letto | ✅ JWT |

**Caratteristiche:**
- Traduzione automatica (GPT + DeepL)
- Supporta `originalText`, `translatedText`, `originalLanguage`, `targetLanguage`

---

### **3.7 Support** (`/api/support`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `POST` | `/api/support/tickets` | Crea ticket support | ❌ Pubblico |
| `GET` | `/api/support/tickets` | Lista ticket utente | ❌ Pubblico |
| `GET` | `/api/support/tickets/{id}` | Dettagli ticket | ❌ Pubblico |
| `POST` | `/api/support/tickets/{id}/reply` | Rispondi a ticket | ❌ Pubblico |
| `POST` | `/api/support/tickets/{id}/reopen` | Riapri ticket | ❌ Pubblico |
| `GET` | `/api/support/stats` | Statistiche utente | ❌ Pubblico |

**Chat Support:**
| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `POST` | `/api/support/chat/{ticketId}/message` | Invia messaggio support | ✅ JWT |
| `GET` | `/api/support/chat/{ticketId}/messages` | Messaggi ticket | ✅ JWT |
| `POST` | `/api/support/chat/{ticketId}/read` | Segna come letto (admin) | ✅ Admin |
| `GET` | `/api/support/chat/{ticketId}/stats` | Statistiche chat | ✅ JWT |

**Streaming:**
| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/support/stream` | SSE support stream | ✅ JWT |

---

### **3.8 Franchise** (`/api/franchises`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/franchises` | Lista franchise (JSON) | ❌ Pubblico (cached) |
| `GET` | `/api/franchises/database` | Lista franchise (DB) | ❌ Pubblico |
| `GET` | `/api/franchises/categories` | Lista categorie | ❌ Pubblico (cached) |
| `GET` | `/api/franchises/category/{category}` | Franchise per categoria | ❌ Pubblico (cached) |
| `POST` | `/api/franchises/propose` | Proponi nuovo franchise | ❌ Pubblico |
| `GET` | `/api/franchises/stats` | Statistiche franchise | ❌ Pubblico (cached) |

---

### **3.9 Pending Values** (`/api/pending-values`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `POST` | `/api/pending-values/submit` | Invia valore personalizzato | ✅ JWT |
| `GET` | `/api/pending-values` | Lista pending (admin) | ✅ Admin |
| `GET` | `/api/pending-values/my` | I miei pending | ✅ JWT |
| `POST` | `/api/pending-values/{id}/approve` | Approva pending (admin) | ✅ Admin |
| `DELETE` | `/api/pending-values/{id}` | Rifiuta pending (admin) | ✅ Admin |
| `GET` | `/api/pending-values/stats` | Statistiche pending | ✅ Admin |

---

### **3.10 Traduzione** (`/api/translate`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `POST` | `/api/translate` | Traduci testo (GPT + DeepL) | ❌ Pubblico |

**Request:**
```json
{
  "text": "Hello world",
  "targetLanguage": "it"
}
```

**Response:**
```json
{
  "translated": "Ciao mondo"
}
```

**Caratteristiche:**
- Primary: GPT-4o-mini
- Fallback: DeepL
- Whitelist: 31 lingue supportate

---

### **3.11 Currency** (`/api/currency`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/currency/convert` | Converte valuta | ❌ Pubblico |

**Query Parameters:**
- `from`: Valuta origine (es. "USD")
- `to`: Valuta destinazione (es. "EUR")
- `amount`: Importo da convertire

**Response:**
```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100.0,
  "converted": 85.0,
  "rate": 0.85
}
```

**Caratteristiche:**
- Cache interna (TTL 1 ora)
- API: ExchangeRate-API
- Valute supportate: EUR, USD, GBP, JPY, BRL, CAD, AUD

---

### **3.12 Collezione Utente** (`/api/user-cards`, `/api/wishlist`, `/api/cards`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/user-cards` | Collezione utente | ✅ JWT |
| `POST` | `/api/user-cards` | Aggiungi carta | ✅ JWT |
| `GET` | `/api/wishlist` | Wishlist utente | ✅ JWT |
| `POST` | `/api/wishlist` | Aggiungi a wishlist | ✅ JWT |
| `DELETE` | `/api/wishlist/{id}` | Rimuovi da wishlist | ✅ JWT |
| `GET` | `/api/cards` | Lista carte | ❌ Pubblico |

---

### **3.13 Grading** (`/api/grading`, `/api/gradelens`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `POST` | `/api/grading/analyze` | Analizza carta | ✅ JWT |
| `POST` | `/api/gradelens/analyze` | GradeLens analysis | ✅ JWT |
| `GET` | `/api/gradelens/admin/stats` | Statistiche grading | ✅ Admin |

---

### **3.14 Marketplace** (`/api/market`, `/api/trends`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/market/valuation` | Valutazione mercato | ❌ Pubblico |
| `GET` | `/api/trends` | Trend prodotti | ❌ Pubblico |

---

### **3.15 Admin Panel** (`/api/admin/**`)

**Autenticazione:** Bearer Token o JWT (ruoli: ADMIN, SUPER_ADMIN, SUPERVISOR)

#### **Dashboard:**
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/dashboard` | Dashboard admin |
| `GET` | `/api/admin/stats` | Statistiche generali |

#### **Support:**
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/support/tickets` | Lista ticket (paginated) |
| `GET` | `/api/admin/support/stats` | Statistiche support |
| `POST` | `/api/admin/support/reply/{id}` | Rispondi ticket |
| `POST` | `/api/admin/support/resolve/{id}` | Risolvi ticket |
| `POST` | `/api/admin/support/close/{id}` | Chiudi ticket |
| `POST` | `/api/admin/support/reopen/{id}` | Riapri ticket |
| `POST` | `/api/admin/support/{id}/mark-read` | Segna come letto |
| `GET` | `/api/admin/support/new-messages-count` | Contatore nuovi messaggi |
| `POST` | `/api/admin/support/{id}/assign` | Assegna ticket |
| `POST` | `/api/admin/support/{id}/release` | Rilascia ticket |
| `GET` | `/api/admin/support/assigned/{email}` | Ticket assegnati |
| `GET` | `/api/admin/support/assigned-count` | Contatore assegnati |

#### **Franchise:**
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/franchises` | Lista franchise e proposte |
| `POST` | `/api/admin/franchises/approve/{proposalId}` | Approva proposta |
| `POST` | `/api/admin/franchises/reject/{proposalId}` | Rifiuta proposta |
| `PATCH` | `/api/admin/franchises/{id}/disable` | Disabilita franchise |
| `PATCH` | `/api/admin/franchises/{id}/enable` | Abilita franchise |
| `POST` | `/api/admin/franchises/add` | Crea franchise manualmente |
| `GET` | `/api/admin/franchises/catalog` | Catalogo franchise |

#### **Pending Values:**
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/pending-values` | Lista pending values |
| `POST` | `/api/admin/pending-values/{id}/approve` | Approva pending |
| `DELETE` | `/api/admin/pending-values/{id}` | Rifiuta pending |
| `GET` | `/api/admin/pending-values/stats` | Statistiche pending |

#### **Email:**
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/email-logs` | Log email (paginated) |
| `GET` | `/api/admin/email-logs/{id}` | Dettagli log email |
| `GET` | `/api/admin/email-logs/stats` | Statistiche email |
| `POST` | `/api/admin/email-templates/test/all` | Test tutti template |
| `POST` | `/api/admin/email-templates/test/variables` | Test variabili |

#### **Cookie Logs:**
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/cookie-logs/logs` | Log cookie utente |
| `GET` | `/api/admin/cookie-logs/logs/export` | Export log cookie (admin) |

#### **Notifications:**
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/notifications` | Lista notifiche (paginated) |
| `GET` | `/api/admin/notifications/{id}` | Dettagli notifica |
| `POST` | `/api/admin/notifications/{id}/read` | Segna come letta |
| `POST` | `/api/admin/notifications/{id}/assign` | Assegna notifica |
| `POST` | `/api/admin/notifications/{id}/resolve` | Risolvi notifica |
| `POST` | `/api/admin/notifications/{id}/archive` | Archivia notifica |
| `DELETE` | `/api/admin/notifications/cleanup` | Pulizia notifiche |
| `GET` | `/api/admin/notifications/stream` | SSE notifiche |
| `GET` | `/api/admin/notifications/unread-count` | Contatore non lette |
| `GET` | `/api/admin/notifications/unread-latest` | Ultime non lette |

#### **System:**
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/system/cleanup/status` | Stato pulizia sistema |
| `POST` | `/api/admin/system/cleanup/status` | Aggiorna stato pulizia |
| `DELETE` | `/api/admin/dashboard/cleanup` | Pulizia dashboard |

#### **Streaming (SSE):**
| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/admin/support/stream` | SSE support stream |
| `GET` | `/api/admin/notifications/stream` | SSE notifications stream |

---

### **3.16 Admin Auth** (`/api/admin/auth`, `/api/admin/tokens`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `POST` | `/api/admin/auth/login` | Login admin | ❌ Token |
| `GET` | `/api/admin/auth/ping` | Ping admin | ✅ Token |
| `GET` | `/api/admin/tokens` | Lista token | ✅ Admin |
| `POST` | `/api/admin/tokens` | Crea token | ✅ Admin |
| `POST` | `/api/admin/tokens/{id}/regenerate` | Rigenera token | ✅ Admin |
| `POST` | `/api/admin/tokens/{id}/disable` | Disabilita token | ✅ Admin |

---

### **3.17 Admin Access** (`/api/admin/access`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/admin/access/requests` | Lista richieste accesso | ✅ Admin |
| `POST` | `/api/admin/access/requests` | Crea richiesta accesso | ❌ Pubblico |
| `POST` | `/api/admin/access/requests/{id}/approve` | Approva richiesta | ✅ Admin |
| `POST` | `/api/admin/access/requests/{id}/reject` | Rifiuta richiesta | ✅ Admin |

---

### **3.18 Test** (`/api/test`)

| Metodo | Endpoint | Descrizione | Autenticazione |
|--------|----------|-------------|----------------|
| `GET` | `/api/test/ping` | Ping test | ❌ Pubblico |
| `GET` | `/api/test/sse-test` | Test SSE | ❌ Pubblico |

---

## 4. 🔗 COLLEGAMENTI E DIPENDENZE

### **4.1 Architettura a Livelli**

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND (React)                      │
│              https://funkard.com                         │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ HTTPS REST API
                     │
┌────────────────────▼────────────────────────────────────┐
│              REST CONTROLLERS (21 file)                 │
│  AuthController, UserController, ProductController, ... │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ @Autowired
                     │
┌────────────────────▼────────────────────────────────────┐
│              SERVICES (32 file)                         │
│  UserService, ProductService, ChatService, ...          │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ @Autowired
                     │
┌────────────────────▼────────────────────────────────────┐
│              REPOSITORIES (20 file)                     │
│  UserRepository, ProductRepository, ...                 │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ JPA/Hibernate
                     │
┌────────────────────▼────────────────────────────────────┐
│              DATABASE (PostgreSQL)                       │
│  Neon / Render PostgreSQL                                │
└─────────────────────────────────────────────────────────┘
```

### **4.2 Dipendenze Principali**

#### **Spring Boot Dependencies:**
- `spring-boot-starter-web` → REST API
- `spring-boot-starter-data-jpa` → Database access
- `spring-boot-starter-security` → Security + JWT
- `spring-boot-starter-mail` → Email sending
- `spring-boot-starter-websocket` → WebSocket support
- `spring-boot-starter-actuator` → Monitoring

#### **Database:**
- `postgresql` (42.7.4) → PostgreSQL driver
- `flyway-core` → Database migrations

#### **Security:**
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.11.5) → JWT

#### **External APIs:**
- `RestTemplate` → OpenAI GPT, DeepL, ExchangeRate-API

#### **Storage:**
- `aws-sdk-s3` (2.25.60) → Cloudflare R2 (S3-compatible)

#### **Utilities:**
- `lombok` (1.18.30) → Boilerplate reduction
- `caffeine` → Caching
- `opencv` (4.9.0-0) → Image processing (grading)
- `itext7-core` (8.0.2) → PDF generation (GDPR export)

### **4.3 Collegamenti tra Componenti**

#### **User Flow:**
```
AuthController
  └─> UserService
      └─> UserRepository
          └─> User (Entity)
              └─> UserPreferences, UserAddress, UserCard
```

#### **Product Flow:**
```
ProductController
  └─> ProductService
      ├─> UnifiedTranslationService (nameEn generation)
      │   ├─> OpenAiTranslateService
      │   └─> DeepLTranslateService
      └─> ProductRepository
          └─> Product (Entity)
```

#### **Chat Flow:**
```
ChatController
  └─> ChatService
      ├─> UnifiedTranslationService (message translation)
      └─> ChatMessageRepository
          └─> ChatMessage (Entity)
              └─> TranslationLog (Entity)
```

#### **Support Flow:**
```
SupportController
  └─> SupportTicketService
      └─> SupportTicketRepository
          └─> SupportTicket (Entity)
              └─> SupportMessage (Entity)
                  └─> UnifiedTranslationService
```

#### **Currency Flow:**
```
CurrencyController
  └─> CurrencyConversionService
      ├─> RestTemplate (ExchangeRate-API)
      └─> Cache (ConcurrentHashMap)
```

#### **Email Flow:**
```
EmailService
  ├─> EmailTemplateManager (multi-language)
  ├─> EmailLogService
  └─> JavaMailSender
      └─> SMTP Server
```

#### **GDPR Flow:**
```
UserController.deleteAccount
  └─> UserAccountDeletionService
      └─> UserDeletionRepository
          └─> UserDeletion (Entity)
              └─> UserDeletionScheduler (cron)
                  └─> UserDeletionService
                      ├─> R2Service (file deletion)
                      └─> EmailService (confirmation email)
```

---

## 5. 🗄️ DATABASE SCHEMA

### **5.1 Tabelle Principali**

#### **Users & Authentication:**
- `users` → Utenti (language, preferredCurrency, GDPR fields)
- `admin_users` → Utenti admin
- `admin_access_tokens` → Token accesso admin
- `admin_access_requests` → Richieste accesso admin
- `verification_tokens` → Token verifica email

#### **Marketplace:**
- `products` → Prodotti (currency, descriptionOriginal, descriptionLanguage, nameEn)
- `listings` → Listings (currency)
- `transactions` → Transazioni (currency)
- `market_listings` → Market listings (priceEUR hardcoded)
- `market_valuations` → Valutazioni mercato

#### **Collection:**
- `cards` → Carte (category, franchise, language)
- `user_cards` → Collezione utente (grading fields)
- `wishlist` → Wishlist utente

#### **Chat & Support:**
- `chat_messages` → Messaggi chat (translation fields)
- `support_tickets` → Ticket support
- `support_messages` → Messaggi support (translation fields)

#### **Franchise:**
- `franchises` → Franchise approvati
- `franchise_proposals` → Proposte franchise
- `franchise_catalog` → Catalogo franchise
- `pending_values` → Valori personalizzati pending

#### **GDPR & Preferences:**
- `user_preferences` → Preferenze utente
- `cookie_consent_logs` → Log consenso cookie
- `user_deletions` → Richieste cancellazione account
- `user_addresses` → Indirizzi utente

#### **Translation:**
- `translation_logs` → Log traduzioni

#### **Email:**
- `email_logs` → Log email inviate

#### **Grading:**
- `grade_reports` → Report grading (currency field)
- `grade_lens_results` → Risultati GradeLens
- `grading_requests` → Richieste grading

#### **Admin:**
- `admin_notifications` → Notifiche admin
- `admin_action_logs` → Log azioni admin

### **5.2 Migrations Flyway (22 file)**

| Versione | Nome | Descrizione |
|----------|------|-------------|
| V1 | `add_grading_columns_to_usercard` | Colonne grading |
| V2 | `add_preferred_currency_to_users` | Currency utente |
| V3 | `create_user_addresses_table` | Indirizzi utente |
| V4 | `create_admin_tokens_and_access_requests` | Admin tokens |
| V5 | `add_gdpr_consent_timestamps_to_users` | GDPR timestamps |
| V6 | `create_user_preferences_table` | Preferenze utente |
| V7 | `create_cookie_consent_logs_table` | Log cookie |
| V8 | `update_cookie_consent_logs_add_action_useragent` | Cookie log fields |
| V9 | `create_user_deletions_table` | Cancellazione account |
| V10 | `add_deletion_fields_to_users` | Campi cancellazione |
| V11 | `create_email_logs_table` | Log email |
| V12 | `add_language_to_users` | Lingua utente |
| V13 | `add_translation_fields_to_support_messages` | Traduzione support |
| V14 | `create_chat_messages_table` | Chat messages |
| V15 | `create_translation_logs_table` | Log traduzioni |
| V16 | `create_pending_values_table` | Valori pending |
| V17 | `add_category_franchise_to_cards` | Categoria carte |
| V18 | `create_franchise_catalog_table` | Catalogo franchise |
| V19 | `create_franchises_table` | Franchise |
| V20 | `create_franchise_proposals_table` | Proposte franchise |
| V21 | `add_translation_fields_to_products_and_users` | Traduzione prodotti |
| V22 | `add_currency_to_products_listings_transactions` | Currency prodotti |

---

## 6. ⚙️ FUNZIONALITÀ IMPLEMENTATE

### **6.1 Autenticazione e Autorizzazione**

✅ **JWT Authentication**
- Login/Register con JWT
- Token validation
- Header: `X-User-Id` per identificazione utente

✅ **Admin Authentication**
- Bearer token per admin
- Ruoli: ADMIN, SUPER_ADMIN, SUPERVISOR
- `@PreAuthorize` per controllo accessi

✅ **GDPR Compliance**
- Accettazione Termini e Privacy obbligatoria
- Timestamp `termsAcceptedAt`, `privacyAcceptedAt`
- Cancellazione account con scheduler (7 giorni)

---

### **6.2 Sistema Multi-Lingua**

✅ **31 Lingue Supportate**
- Whitelist centralizzata: `SupportedLanguages`
- Validazione input utente: `LanguageWhitelist`
- Default: "en" (fallback)

✅ **Traduzione Automatica**
- Primary: GPT-4o-mini (OpenAI)
- Fallback: DeepL
- Servizio unificato: `UnifiedTranslationService`
- Logging traduzioni: `translation_logs`

✅ **Traduzione Contenuti**
- Chat messages (utente ↔ utente)
- Support messages (utente ↔ admin)
- Product descriptions (`descriptionOriginal`, `descriptionLanguage`)
- Seller bio (`descriptionOriginal`, `descriptionLanguage`)
- Product names (`nameEn` generato automaticamente)

✅ **Email Multi-Lingua**
- Template per 25+ lingue
- Fallback automatico a inglese
- Rilevamento lingua utente: `EmailLocaleHelper`
- Manager template: `EmailTemplateManager`

---

### **6.3 Sistema Multi-Valuta**

✅ **7 Valute Supportate**
- EUR, USD, GBP, JPY, BRL, CAD, AUD
- Whitelist centralizzata: `SupportedCurrencies`
- Validazione unificata

✅ **Currency Fields**
- `User.preferredCurrency` (default USD)
- `Product.currency` (default USD)
- `Listing.currency` (default USD)
- `Transaction.currency` (default USD)
- `GradeReport.currency`

✅ **Conversione Valute**
- Servizio: `CurrencyConversionService`
- API: ExchangeRate-API
- Cache interna (TTL 1 ora)
- Endpoint: `GET /api/currency/convert`

⚠️ **Limitazioni:**
- Nessuna conversione automatica nel marketplace
- MarketListing hardcoded EUR
- Nessuna formattazione prezzi

---

### **6.4 Marketplace**

✅ **Prodotti**
- CRUD completo
- Generazione automatica `nameEn` (GPT)
- Supporto traduzione descrizioni
- Validazione currency

✅ **Listings**
- CRUD completo
- Supporto valori personalizzati "Altro"
- Validazione currency

✅ **Transazioni**
- Creazione transazioni
- Collegamento buyer-listing
- Validazione currency

✅ **Franchise System**
- Catalogo franchise (JSON + DB)
- Proposte franchise da utenti
- Approvazione/rifiuto admin
- Enable/disable franchise

---

### **6.5 Chat e Support**

✅ **Chat Utenti**
- Messaggi tra utenti
- Traduzione automatica
- Messaggi non letti
- Segna come letto

✅ **Support System**
- Ticket support
- Chat support (utente ↔ admin)
- Assegnazione ticket
- Statistiche support
- Streaming SSE per admin

✅ **Traduzione Support**
- Traduzione automatica messaggi
- Logging traduzioni
- Campi: `originalText`, `translatedText`, `originalLanguage`, `targetLanguage`

---

### **6.6 Grading System**

✅ **GradeLens**
- Analisi automatica carte
- Heuristic AI provider
- Subgrades (centering, edges, corners, surface)
- Valutazione valore (low, mid, high)

✅ **Grading Service**
- Richieste grading
- Report grading
- Cleanup automatico (scheduler)

---

### **6.7 Email System**

✅ **Email Service**
- Invio email multi-lingua
- Template manager
- Fallback sender (primary → secondary)
- Logging email: `email_logs`

✅ **Template Email**
- Account confirmation
- Account deletion
- Password reset
- Order confirmation
- Order shipped
- Ticket opened

✅ **Email Logging**
- Audit completo email
- Status: SENT, FAILED, RETRIED
- Retry logic (3 tentativi)
- Cleanup automatico (90 giorni)

---

### **6.8 GDPR Compliance**

✅ **Registrazione**
- Accettazione Termini obbligatoria
- Accettazione Privacy obbligatoria
- Timestamp `termsAcceptedAt`, `privacyAcceptedAt`

✅ **Cookie Preferences**
- Gestione preferenze cookie
- Logging consenso: `cookie_consent_logs`
- Export log (PDF/JSON): `GET /api/user/preferences/cookies/export`

✅ **Right to be Forgotten**
- Cancellazione account: `DELETE /api/user/delete-account`
- Scheduler cancellazione (7 giorni)
- Eliminazione completa dati:
  - User record
  - User cards (R2 files)
  - Wishlist
  - User addresses
  - User preferences
  - Support tickets
  - Cookie consent logs
  - Email conferma cancellazione

✅ **Data Portability**
- Export cookie logs (PDF/JSON)
- Dati utente accessibili via API

---

### **6.9 Admin Panel**

✅ **Dashboard**
- Statistiche generali
- Notifiche admin
- Support tickets
- Franchise management

✅ **Support Management**
- Lista ticket (paginated)
- Assegnazione ticket
- Risposta ticket
- Statistiche support
- Streaming SSE

✅ **Franchise Management**
- Approvazione proposte
- Enable/disable franchise
- Creazione manuale franchise
- Sincronizzazione JSON ↔ DB

✅ **Pending Values**
- Approvazione valori personalizzati
- Statistiche pending

✅ **Email Management**
- Log email
- Test template
- Statistiche email

✅ **Notifications**
- Notifiche admin
- Assegnazione notifiche
- Archiviazione notifiche
- Streaming SSE

---

### **6.10 Caching**

✅ **Caffeine Cache**
- Configurazione: TTL 25s, Max 500 entries
- Cache endpoint pubblici:
  - `GET /api/franchises`
  - `GET /api/products`
  - `GET /api/listings`
  - `GET /api/franchises/categories`

✅ **Currency Cache**
- Cache interna `CurrencyConversionService`
- TTL: 1 ora
- Thread-safe (ConcurrentHashMap)

---

### **6.11 Real-Time Features**

✅ **Server-Sent Events (SSE)**
- Admin support stream: `GET /api/admin/support/stream`
- Support stream: `GET /api/support/stream`
- Admin notifications stream: `GET /api/admin/notifications/stream`

✅ **WebSocket**
- Configurazione: `WebSocketConfig`
- Support chat (opzionale)

---

### **6.12 Storage**

✅ **Cloudflare R2**
- Configurazione: `R2Config`
- Servizio: `ImageStorageService`
- Upload/delete immagini
- Integrazione cancellazione account

---

### **6.13 Scheduled Jobs**

✅ **UserDeletionScheduler**
- Cron: `0 0 * * * *` (ogni ora)
- Cancellazione account dopo 7 giorni
- Email conferma cancellazione

✅ **EmailLogCleanupScheduler**
- Pulizia log email > 90 giorni

✅ **GradeCleanupScheduler**
- Pulizia grading temporanei

---

## 7. ⚙️ CONFIGURAZIONI

### **7.1 Application Properties**

**File:**
- `application.properties` (dev)
- `application-prod.yml` (production)
- `application-test.properties` (test)

**Configurazioni Principali:**
- Database: PostgreSQL (Neon/Render)
- JWT: Secret, expiration
- Email: SMTP (primary + fallback)
- R2: Cloudflare R2 credentials
- CORS: funkard.com, localhost:3000, localhost:3002

### **7.2 Security Config**

**File:** `SecurityConfig.java`

**Configurazioni:**
- JWT filter: `JwtFilter`
- CORS: Configurato per frontend
- Public endpoints: `/api/auth/**`, `/api/test/**`
- Protected endpoints: `/api/user/**`, `/api/admin/**`

### **7.3 Cache Config**

**File:** `CacheConfig.java`

**Configurazioni:**
- Caffeine cache manager
- TTL: 25 secondi
- Max size: 500 entries
- Cache names: `homepage:latest`, `marketplace:search`, `reference:brands`

---

## 8. 📊 STATISTICHE PROGETTO

### **8.1 File e Linee di Codice**

- **File Java:** ~254 file
- **Package:** 20+ package principali
- **Controllers:** 56+ controller
- **Services:** 32+ servizi
- **Repositories:** 20+ repository
- **Entities:** 23+ entità
- **DTOs:** 19+ DTO
- **Migrations:** 22 migration SQL
- **Email Templates:** 25+ lingue, 6+ template per lingua

### **8.2 Endpoint API**

- **Endpoint Pubblici:** ~30 endpoint
- **Endpoint Autenticati:** ~40 endpoint
- **Endpoint Admin:** ~60 endpoint
- **Totale:** ~130 endpoint

### **8.3 Database**

- **Tabelle:** 25+ tabelle
- **Migrations:** 22 migration
- **Indici:** 10+ indici

---

## 9. 🔄 FLUSSI PRINCIPALI

### **9.1 Registrazione Utente**

```
1. POST /api/auth/register
   └─> AuthController.register()
       ├─> Validazione GDPR (acceptTerms, acceptPrivacy)
       ├─> Validazione currency (SupportedCurrencies)
       ├─> Validazione language (LanguageWhitelist)
       ├─> Password encoding
       └─> UserRepository.save()
           └─> User (Entity)
               ├─> termsAcceptedAt = now()
               ├─> privacyAcceptedAt = now()
               ├─> preferredCurrency = "USD"
               └─> language = "en" (default)
```

### **9.2 Creazione Prodotto**

```
1. POST /api/products
   └─> ProductController.createProduct()
       └─> ProductService.createProduct()
           ├─> Validazione currency (default "USD")
           ├─> Generazione nameEn (GPT-4o-mini)
           │   └─> UnifiedTranslationService
           │       ├─> OpenAiTranslateService (primary)
           │       └─> DeepLTranslateService (fallback)
           └─> ProductRepository.save()
               └─> Product (Entity)
```

### **9.3 Chat con Traduzione**

```
1. POST /api/chat/message
   └─> ChatController.sendMessage()
       └─> ChatService.sendMessage()
           ├─> Rilevamento lingua mittente
           ├─> Rilevamento lingua destinatario
           ├─> Traduzione automatica (se lingue diverse)
           │   └─> UnifiedTranslationService
           ├─> Salvataggio messaggio
           │   └─> ChatMessageRepository.save()
           │       └─> ChatMessage (Entity)
           │           ├─> originalText
           │           ├─> translatedText
           │           ├─> originalLanguage
           │           └─> targetLanguage
           └─> Logging traduzione
               └─> TranslationLogRepository.save()
```

### **9.4 Conversione Valuta**

```
1. GET /api/currency/convert?from=USD&to=EUR&amount=100
   └─> CurrencyController.convert()
       ├─> Validazione valute (SupportedCurrencies)
       └─> CurrencyConversionService.convert()
           ├─> Verifica cache (TTL 1 ora)
           ├─> Se cache scaduta:
           │   └─> fetchRates() → ExchangeRate-API
           │       └─> Aggiorna cache
           └─> Calcola conversione
               └─> Return converted amount
```

### **9.5 Cancellazione Account (GDPR)**

```
1. DELETE /api/user/delete-account
   └─> UserController.deleteAccount()
       └─> UserAccountDeletionService.requestDeletion()
           ├─> User.deletionPending = true
           ├─> User.deletionRequestedAt = now()
           └─> UserDeletionRepository.save()
               └─> UserDeletion (Entity)
                   └─> scheduledDeletionAt = now() + 7 giorni

2. UserDeletionScheduler (ogni ora)
   └─> UserDeletionService.deleteUser()
       ├─> Elimina UserCard (R2 files)
       ├─> Elimina Wishlist
       ├─> Elimina UserAddress
       ├─> Elimina UserPreferences
       ├─> Elimina SupportTickets
       ├─> Elimina CookieConsentLogs
       ├─> Elimina User record
       └─> EmailService.sendAccountDeletionCompletedEmail()
```

---

## 10. 📝 NOTE TECNICHE

### **10.1 Tecnologie Utilizzate**

- **Framework:** Spring Boot 3.5.6
- **Java:** 17
- **Database:** PostgreSQL (Neon/Render)
- **ORM:** JPA/Hibernate
- **Migrations:** Flyway
- **Security:** Spring Security + JWT
- **Caching:** Caffeine
- **Email:** JavaMailSender
- **PDF:** iText 7
- **Storage:** Cloudflare R2 (S3-compatible)
- **Image Processing:** OpenCV
- **Build:** Maven

### **10.2 Pattern Architetturali**

- **MVC:** Controller → Service → Repository
- **DTO Pattern:** Separazione entity/DTO
- **Repository Pattern:** JPA Repository
- **Service Layer:** Business logic isolata
- **Exception Handling:** GlobalExceptionHandler
- **Caching:** @Cacheable annotations
- **Scheduled Tasks:** @Scheduled annotations

### **10.3 Best Practices**

✅ **Validazione Input**
- `@Valid` su DTO
- Whitelist centralizzate (SupportedLanguages, SupportedCurrencies)
- Validazione servizi

✅ **Error Handling**
- GlobalExceptionHandler centralizzato
- Messaggi di errore consistenti
- Logging completo

✅ **Security**
- JWT authentication
- Role-based access control
- CORS configurato
- GDPR compliance

✅ **Performance**
- Caching (Caffeine + internal)
- Database indici
- Pagination per liste

✅ **Maintainability**
- Package structure chiara
- DTO separati da Entity
- Service layer isolato
- Documentazione completa

---

**Documento generato il:** 24 Novembre 2024  
**Versione Backend:** 0.0.1-SNAPSHOT  
**Spring Boot:** 3.5.6  
**Java:** 17

---

## 📚 RIFERIMENTI

- Documentazione completa in `/docs/`
- Report sistema valute: `REPORT_COMPLETO_SISTEMA_VALUTE_BACKEND.md`
- Report lingue: `AUDIT_GESTIONE_LINGUA_BACKEND.md`
- Documentazione GDPR: `GDPR_*.md`
- Documentazione traduzione: `TRANSLATION_SYSTEM.md`

