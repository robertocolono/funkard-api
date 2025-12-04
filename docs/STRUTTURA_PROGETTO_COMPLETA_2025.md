# 📋 STRUTTURA COMPLETA PROGETTO FUNKARD API

**Data aggiornamento:** 2025-01-26  
**Versione Spring Boot:** 3.5.6  
**Java Version:** 17

---

## 📁 ALBERO DEL PROGETTO

```
funkard-api/
├── src/
│   ├── main/
│   │   ├── java/com/funkard/
│   │   │   ├── FunkardApiApplication.java          # Entry point Spring Boot
│   │   │   │
│   │   │   ├── admin/                              # Sistema Admin (62 file)
│   │   │   │   ├── AdminConfig.java
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AdminService.java
│   │   │   │   ├── controller/                     # 21 controller admin
│   │   │   │   │   ├── AdminCleanupController.java
│   │   │   │   │   ├── AdminDashboardController.java
│   │   │   │   │   ├── AdminEmailLogController.java
│   │   │   │   │   ├── AdminFranchiseController.java
│   │   │   │   │   ├── AdminLegacyAuthController.java
│   │   │   │   │   ├── AdminNotification*.java     # 6 file notifiche
│   │   │   │   │   ├── AdminPendingValueController.java
│   │   │   │   │   ├── AdminStatsController.java
│   │   │   │   │   ├── AdminSupport*.java          # 2 file support
│   │   │   │   │   ├── AdminValuationController.java
│   │   │   │   │   ├── CookieLogAdminController.java
│   │   │   │   │   ├── EmailTemplateTestController.java
│   │   │   │   │   ├── FranchiseAdminController.java
│   │   │   │   │   ├── RolePermissionController.java
│   │   │   │   │   └── SupportTicketController.java
│   │   │   │   ├── dto/                            # 10 DTO admin
│   │   │   │   ├── log/                            # 4 file logging
│   │   │   │   ├── model/                          # 6 entità admin
│   │   │   │   ├── notification/                   # Sistema notifiche
│   │   │   │   ├── repository/                     # 4 repository
│   │   │   │   ├── service/                        # 12 servizi admin
│   │   │   │   ├── system/                         # 1 file system
│   │   │   │   └── util/                           # 1 utility
│   │   │   │
│   │   │   ├── adminaccess/                        # Accesso Admin (6 file)
│   │   │   │   ├── controller/AdminAccessController.java
│   │   │   │   ├── model/                          # 2 entità
│   │   │   │   ├── repository/                     # 2 repository
│   │   │   │   └── service/AdminAccessService.java
│   │   │   │
│   │   │   ├── adminauth/                          # Autenticazione Admin (14 file)
│   │   │   │   ├── AccessRequest.java
│   │   │   │   ├── AccessRequestController.java
│   │   │   │   ├── AccessRequestRepository.java
│   │   │   │   ├── AccessRequestService.java
│   │   │   │   ├── AdminAuthController.java
│   │   │   │   ├── AdminBootstrap.java
│   │   │   │   ├── AdminTableInitializer.java
│   │   │   │   │   ├── AdminToken.java
│   │   │   │   │   ├── AdminTokenController.java
│   │   │   │   │   ├── AdminTokenRepository.java
│   │   │   │   │   ├── AdminTokenService.java
│   │   │   │   │   ├── AdminUser.java
│   │   │   │   │   ├── AdminUserRepository.java
│   │   │   │   │   └── AdminUserService.java
│   │   │   │
│   │   │   ├── api/i18n/                           # Internazionalizzazione
│   │   │   │   └── SupportedLanguages.java
│   │   │   │
│   │   │   ├── common/                             # Componenti comuni
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   ├── config/                             # Configurazioni (6 file)
│   │   │   │   ├── CacheConfig.java                # Caffeine cache
│   │   │   │   ├── EmailConfig.java                 # Configurazione email
│   │   │   │   ├── R2Config.java                    # Cloudflare R2 storage
│   │   │   │   ├── SecurityConfig.java              # Spring Security + JWT
│   │   │   │   ├── SupportedCurrencies.java         # 38 valute supportate
│   │   │   │   └── WebSocketConfig.java             # WebSocket/SSE
│   │   │   │
│   │   │   ├── controller/                         # Controller pubblici (21 file)
│   │   │   │   ├── AdminTicketAssignmentController.java
│   │   │   │   ├── AdsController.java
│   │   │   │   ├── AuthController.java              # Login/Register/JWT
│   │   │   │   ├── CardController.java
│   │   │   │   ├── ChatController.java
│   │   │   │   ├── CollectionController.java
│   │   │   │   ├── FranchiseController.java
│   │   │   │   ├── GradeLensAdminController.java
│   │   │   │   ├── LanguageWhitelist.java
│   │   │   │   ├── ListingController.java          # Marketplace listings
│   │   │   │   ├── PendingValueController.java
│   │   │   │   ├── RootController.java
│   │   │   │   ├── SupportChatController.java
│   │   │   │   ├── SupportController.java
│   │   │   │   ├── SupportWebSocketController.java
│   │   │   │   ├── TestController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── TranslateController.java
│   │   │   │   ├── UserCardController.java
│   │   │   │   ├── UserController.java              # Profilo utente
│   │   │   │   └── WishlistController.java
│   │   │   │
│   │   │   ├── currency/                           # Sistema Multi-Valuta (6 file)
│   │   │   │   ├── CurrencyController.java          # API valute pubbliche
│   │   │   │   ├── CurrencyConversionService.java    # Conversione valute
│   │   │   │   ├── CurrencyRatePublicTestController.java  # Test pubblico
│   │   │   │   ├── CurrencyRateStore.java           # Store in-memory tassi
│   │   │   │   ├── CurrencyRateUpdateController.java # Endpoint cron refresh
│   │   │   │   └── CurrencyRateUpdateService.java    # Aggiornamento tassi
│   │   │   │
│   │   │   ├── dto/                                 # Data Transfer Objects (20 file)
│   │   │   │   ├── CardDTO.java
│   │   │   │   ├── ChatMessageDTO.java
│   │   │   │   ├── CookiePreferencesDTO.java
│   │   │   │   ├── CreateFranchiseRequest.java
│   │   │   │   ├── CreateListingRequest.java
│   │   │   │   ├── FranchiseDTO.java
│   │   │   │   ├── FranchiseProposalDTO.java
│   │   │   │   ├── ListingDTO.java                 # Con convertedPrice/Currency
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── PendingValueDTO.java
│   │   │   │   ├── ProductDTO.java                  # Con convertedPrice/Currency
│   │   │   │   ├── ProposeFranchiseRequest.java
│   │   │   │   ├── SubmitPendingValueRequest.java
│   │   │   │   ├── TransactionDTO.java
│   │   │   │   ├── TranslateRequest.java
│   │   │   │   ├── TranslateResponse.java
│   │   │   │   ├── UserDTO.java
│   │   │   │   ├── UserPreferencesDTO.java
│   │   │   │   ├── UserProfileDTO.java
│   │   │   │   └── WishlistDTO.java
│   │   │   │
│   │   │   ├── gradelens/                          # GradeLens AI (8 file)
│   │   │   │   ├── controller/GradeLensController.java
│   │   │   │   ├── GradeResult.java
│   │   │   │   ├── HeuristicAiProvider.java
│   │   │   │   ├── model/                           # 4 entità
│   │   │   │   └── service/GradeLensService.java
│   │   │   │
│   │   │   ├── grading/                             # Sistema Grading (4 file)
│   │   │   │   ├── controller/GradingController.java
│   │   │   │   ├── model/GradingRequest.java
│   │   │   │   ├── repository/GradingRepository.java
│   │   │   │   └── service/GradingService.java
│   │   │   │
│   │   │   ├── maintenance/                         # Manutenzione
│   │   │   │   └── GradeReportCleanup.java
│   │   │   │
│   │   │   ├── market/                              # Marketplace (16 file)
│   │   │   │   ├── controller/
│   │   │   │   │   ├── MarketValuationController.java
│   │   │   │   │   └── ProductController.java       # Con conversione valuta
│   │   │   │   ├── model/
│   │   │   │   │   ├── MarketListing.java
│   │   │   │   │   ├── MarketValuation.java
│   │   │   │   │   └── Product.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── MarketListingRepository.java
│   │   │   │   │   └── ProductRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── AdminNotifierService.java
│   │   │   │   │   ├── ProductService.java
│   │   │   │   │   └── ValuationService.java
│   │   │   │   └── trend/                           # Trend analysis (3 file)
│   │   │   │
│   │   │   ├── model/                               # Entità JPA (23 file)
│   │   │   │   ├── Card.java
│   │   │   │   ├── CardSource.java
│   │   │   │   ├── CardType.java
│   │   │   │   ├── ChatMessage.java
│   │   │   │   ├── CookieConsentLog.java
│   │   │   │   ├── dto/GradeRequest.java
│   │   │   │   ├── EmailLog.java
│   │   │   │   ├── Franchise.java
│   │   │   │   ├── FranchiseCatalog.java
│   │   │   │   ├── FranchiseProposal.java
│   │   │   │   ├── GradeLensResult.java
│   │   │   │   ├── GradeReport.java
│   │   │   │   ├── Listing.java                     # Con currency
│   │   │   │   ├── PendingValue.java
│   │   │   │   ├── Transaction.java                  # Con currency
│   │   │   │   ├── TranslationLog.java
│   │   │   │   ├── User.java                        # Con preferredCurrency
│   │   │   │   ├── UserAddress.java
│   │   │   │   ├── UserCard.java
│   │   │   │   ├── UserDeletion.java
│   │   │   │   ├── UserPreferences.java             # Con language/currency
│   │   │   │   ├── VerificationToken.java
│   │   │   │   └── Wishlist.java
│   │   │   │
│   │   │   ├── payload/                             # Payload request
│   │   │   │   └── RegisterRequest.java
│   │   │   │
│   │   │   ├── realtime/                            # Server-Sent Events (4 file)
│   │   │   │   ├── AdminStreamController.java
│   │   │   │   ├── EventType.java
│   │   │   │   ├── RealtimeConfig.java
│   │   │   │   └── SupportStreamController.java
│   │   │   │
│   │   │   ├── repository/                          # Repository JPA (20 file)
│   │   │   │   ├── CardRepository.java
│   │   │   │   ├── ChatMessageRepository.java
│   │   │   │   ├── CookieConsentLogRepository.java
│   │   │   │   ├── EmailLogRepository.java
│   │   │   │   ├── FranchiseCatalogRepository.java
│   │   │   │   ├── FranchiseProposalRepository.java
│   │   │   │   ├── FranchiseRepository.java
│   │   │   │   ├── GradeLensRepository.java
│   │   │   │   ├── GradeReportRepository.java
│   │   │   │   ├── ListingRepository.java
│   │   │   │   ├── PendingValueRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── TranslationLogRepository.java
│   │   │   │   ├── UserAddressRepository.java
│   │   │   │   ├── UserCardRepository.java
│   │   │   │   ├── UserDeletionRepository.java
│   │   │   │   ├── UserPreferencesRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── VerificationTokenRepository.java
│   │   │   │   └── WishlistRepository.java
│   │   │   │
│   │   │   ├── scheduler/                           # Scheduled tasks (3 file)
│   │   │   │   └── [scheduled services]
│   │   │   │
│   │   │   ├── security/                            # Sicurezza (2 file)
│   │   │   │   ├── JwtFilter.java                   # Filtro JWT (bypass /public/ e /api/currency/refresh-rates)
│   │   │   │   └── JwtUtil.java                      # Utility JWT
│   │   │   │
│   │   │   ├── service/                             # Servizi business (32 file)
│   │   │   │   ├── AdminNotifier.java
│   │   │   │   ├── CardService.java
│   │   │   │   ├── ChatService.java
│   │   │   │   ├── CookieConsentLogService.java
│   │   │   │   ├── CookieLogExportService.java
│   │   │   │   ├── DeepLTranslateService.java
│   │   │   │   ├── EmailLocaleHelper.java
│   │   │   │   ├── EmailLogService.java
│   │   │   │   ├── EmailService.java                # Multi-lingua
│   │   │   │   ├── EmailTemplateManager.java
│   │   │   │   ├── EmailTemplateTestService.java
│   │   │   │   ├── FranchiseAdminService.java
│   │   │   │   ├── FranchiseCatalogService.java
│   │   │   │   ├── FranchiseJsonService.java
│   │   │   │   ├── GradeCalculator.java
│   │   │   │   ├── GradeLensCleanupService.java
│   │   │   │   ├── GradeReportLookupService.java
│   │   │   │   ├── ListingService.java
│   │   │   │   ├── OpenAiTranslateService.java
│   │   │   │   ├── PendingValueService.java
│   │   │   │   ├── R2Service.java                   # Cloudflare R2 storage
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── TranslationException.java
│   │   │   │   ├── TranslationProvider.java
│   │   │   │   ├── TranslationService.java
│   │   │   │   ├── UnifiedTranslationService.java
│   │   │   │   ├── UserAccountDeletionService.java
│   │   │   │   ├── UserAddressService.java
│   │   │   │   ├── UserDeletionService.java
│   │   │   │   ├── UserPreferencesService.java
│   │   │   │   ├── UserService.java
│   │   │   │   └── WishlistService.java
│   │   │   │
│   │   │   ├── storage/                             # Storage
│   │   │   │   └── ImageStorageService.java
│   │   │   │
│   │   │   ├── support/                             # Support system
│   │   │   │
│   │   │   └── user/                                # User management (6 file)
│   │   │       └── payment/                         # Payment methods
│   │   │
│   │   └── resources/
│   │       ├── application.properties                # Config principale
│   │       ├── application-dev.properties
│   │       ├── application-prod.yml
│   │       ├── application-test.properties
│   │       ├── data/
│   │       │   └── franchises.json                  # Catalogo franchise
│   │       ├── db/
│   │       │   ├── migration/                      # Flyway migrations (22 file)
│   │       │   ├── admin_access_requests.sql
│   │       │   ├── admin_access_tokens.sql
│   │       │   └── admin_users_table.sql
│   │       └── email-templates/                    # Template email multi-lingua
│   │           ├── en/                              # 6 template inglese
│   │           ├── it/                              # 6 template italiano
│   │           └── [altre lingue]/                  # Placeholder per altre lingue
│   │
│   └── test/
│       └── java/com/funkard/
│
├── backend/sql/migrations/                          # SQL migrations aggiuntive
│
├── docs/                                            # Documentazione (30+ file)
│
├── Dockerfile                                       # Container Docker
├── Makefile                                         # Comandi build
├── mvnw / mvnw.cmd                                  # Maven wrapper
├── pom.xml                                          # Dipendenze Maven
└── render.yaml                                      # Configurazione Render.com
```

---

## 🔗 ARCHITETTURA E COLLEGAMENTI

### 📊 Pattern Architetturali

**1. Layered Architecture (Controller → Service → Repository)**
```
Controller (REST API)
    ↓
Service (Business Logic)
    ↓
Repository (Data Access)
    ↓
Database (PostgreSQL)
```

**2. Dependency Injection**
- **Lombok `@RequiredArgsConstructor`** per constructor injection
- **Spring `@Autowired`** (meno comune, preferito constructor injection)
- Tutti i servizi sono **singleton** (default Spring)

**3. Security Flow**
```
HTTP Request
    ↓
CORS Filter (SecurityConfig)
    ↓
JWT Filter (JwtFilter) → Bypass per /public/** e /api/currency/refresh-rates
    ↓
SecurityConfig (authorizeHttpRequests)
    ↓
Controller
```

---

## 🌐 ENDPOINT PRINCIPALI

### 🔐 Autenticazione (`/api/auth/**`)
- `POST /api/auth/register` - Registrazione utente
- `POST /api/auth/login` - Login (ritorna JWT)
- `POST /api/auth/verify` - Verifica email
- `POST /api/auth/reset-password` - Reset password
- `GET /api/auth/me` - Profilo utente corrente

### 👤 Utente (`/api/user/**`)
- `GET /api/user/me` - Profilo utente
- `PUT /api/user/me` - Aggiorna profilo
- `GET /api/user/preferences` - Preferenze utente
- `PUT /api/user/preferences` - Aggiorna preferenze (lingua/valuta)
- `GET /api/user/address` - Indirizzi utente
- `POST /api/user/address` - Aggiungi indirizzo
- `DELETE /api/user/address/{id}` - Rimuovi indirizzo
- `POST /api/user/delete-account` - Eliminazione account (GDPR)

### 💰 Valute (`/api/currency/**`)
- `GET /api/currency/supported` - Lista valute supportate
- `GET /api/currency/rates` - Tassi di cambio correnti
- `POST /api/currency/refresh-rates` - **Cron endpoint** (protetto Bearer token)
- `POST /api/currency/refresh-rates/test` - Test endpoint
- `POST /public/currency/test` - Test pubblico

### 🛒 Marketplace (`/api/listings/**`, `/api/products/**`)
- `GET /api/listings` - Lista tutti i listing (con `convertedPrice`/`convertedCurrency`)
- `POST /api/listings` - Crea nuovo listing
- `GET /api/listings/{id}` - Dettaglio listing
- `GET /api/products` - Lista prodotti (con `convertedPrice`/`convertedCurrency`)
- `GET /api/products/{id}` - Dettaglio prodotto
- `POST /api/products` - Crea prodotto

### 💳 Transazioni (`/api/transactions/**`)
- `GET /api/transactions` - Lista transazioni utente
- `POST /api/transactions` - Crea transazione
- `GET /api/transactions/{id}` - Dettaglio transazione

### 📚 Franchise (`/api/franchises/**`)
- `GET /api/franchises` - Lista franchise disponibili
- `POST /api/franchises/propose` - Proponi nuovo franchise
- `GET /api/franchises/catalog` - Catalogo completo

### 🎴 Collezione (`/api/collection/**`, `/api/usercards/**`)
- `GET /api/usercards` - Carte dell'utente
- `POST /api/usercards` - Aggiungi carta
- `GET /api/collection` - Collezione completa

### 📝 Support (`/api/support/**`)
- `POST /api/support` - Crea ticket supporto
- `GET /api/support` - Lista ticket
- `GET /api/support/{id}` - Dettaglio ticket
- `POST /api/support/{id}/message` - Aggiungi messaggio

### 🌍 Traduzione (`/api/translate/**`)
- `POST /api/translate` - Traduci testo (DeepL/OpenAI)

### 🔧 Admin (`/api/admin/**`)
- `GET /api/admin/dashboard` - Dashboard admin
- `GET /api/admin/stats` - Statistiche
- `GET /api/admin/notifications` - Notifiche admin
- `POST /api/admin/notifications/{id}/action` - Azione su notifica
- `GET /api/admin/support` - Gestione supporto
- `GET /api/admin/pending-values` - Valori pending
- `POST /api/admin/pending-values/{id}/approve` - Approva valore
- `POST /api/admin/support/cleanup` - **Cron cleanup** (protetto Bearer token)
- `POST /api/valuation/refreshIncremental` - **Cron valuation** (protetto Bearer token)

### 📊 Realtime/SSE (`/api/realtime/**`)
- `GET /api/realtime/admin/stream` - Stream eventi admin (SSE)
- `GET /api/realtime/support/stream` - Stream supporto (SSE)

---

## 🔧 CONFIGURAZIONI PRINCIPALI

### 1. **SecurityConfig** (`config/SecurityConfig.java`)
- **CSRF:** Disabilitato globalmente (REST API stateless)
- **CORS:** Configurato per `funkard.com`, `admin.funkard.com`, `localhost:3000/3002`
- **JWT Filter:** Aggiunto prima di `UsernamePasswordAuthenticationFilter`
- **Session:** Stateless (`SessionCreationPolicy.STATELESS`)
- **Public Endpoints:**
  - `/public/**`
  - `/api/auth/**`
  - `/api/translate/**`
  - `/api/listings/**`, `/api/products/**`, `/api/cards/**`
  - `/api/valuation/**`, `/api/trends/**`, `/api/ads/**`

### 2. **JwtFilter** (`security/JwtFilter.java`)
- **Bypass esplicito per:**
  - Path che iniziano con `/public/`
  - Path che contengono `/api/currency/refresh-rates`
- Estrae JWT da header `Authorization: Bearer {token}`
- Valida token e imposta `SecurityContext`

### 3. **SupportedCurrencies** (`config/SupportedCurrencies.java`)
- **38 valute supportate:** USD, EUR, GBP, CHF, SEK, DKK, NOK, PLN, CZK, HUF, RON, BGN, HRK, RSD, TRY, ILS, AED, SAR, CAD, AUD, NZD, JPY, SGD, HKD, MXN, BRL, CLP, COP, PEN, ARS, ZAR, INR, IDR, MYR, PHP, THB, KRW, CNY
- Metodo `isValid(String currency)` per validazione
- Metodo `getSupportedCurrenciesOrdered()` per lista ordinata

### 4. **Currency System** (`currency/`)
- **CurrencyRateStore:** Store in-memory per tassi di cambio (USD → altre valute)
- **CurrencyRateUpdateService:** Chiama API esterna (`https://open.er-api.com/v6/latest/USD`) e aggiorna store
- **CurrencyConversionService:** Converte tra valute usando USD come pivot
- **CurrencyRateUpdateController:** Endpoint `/api/currency/refresh-rates` protetto con Bearer token (`FUNKARD_CRON_SECRET_CURRENCY`)

### 5. **CacheConfig** (`config/CacheConfig.java`)
- **Caffeine Cache** per:
  - `marketplace:filters` - Filtri marketplace
  - `reference:brands` - Franchise/brands
  - TTL configurabile

### 6. **EmailConfig** (`config/EmailConfig.java`)
- **Primary:** `no-reply@funkard.com` (SMTP register.it)
- **Fallback:** `support@funkard.com`
- Template multi-lingua in `resources/email-templates/`

### 7. **R2Config** (`config/R2Config.java`)
- **Cloudflare R2** storage (compatibile S3)
- AWS SDK v2 per upload/download immagini

### 8. **WebSocketConfig** (`config/WebSocketConfig.java`)
- Configurazione WebSocket/SSE per realtime updates

---

## 🗄️ DATABASE E ENTITÀ

### Entità Principali (23 entità JPA)

**User & Authentication:**
- `User` - Utente principale (con `preferredCurrency`)
- `UserPreferences` - Preferenze (lingua, valuta, notifiche)
- `UserAddress` - Indirizzi utente
- `UserCard` - Carte collezionate
- `VerificationToken` - Token verifica email
- `UserDeletion` - Log eliminazione account (GDPR)

**Marketplace:**
- `Listing` - Listing vendita (con `currency`, `price`)
- `Product` - Prodotto marketplace
- `Transaction` - Transazione (con `currency`, `amount`)
- `MarketListing` - Listing di mercato
- `MarketValuation` - Valutazioni di mercato

**Franchise:**
- `Franchise` - Franchise principale
- `FranchiseCatalog` - Catalogo franchise
- `FranchiseProposal` - Proposte franchise
- `PendingValue` - Valori pending (TCG, Lingua custom)

**Grading:**
- `GradeReport` - Report grading
- `GradeLensResult` - Risultati GradeLens AI
- `GradingRequest` - Richieste grading

**Support & Admin:**
- `SupportTicket` - Ticket supporto
- `SupportMessage` - Messaggi supporto
- `AdminNotification` - Notifiche admin
- `AdminActionLog` - Log azioni admin

**Altro:**
- `Card` - Carta base
- `ChatMessage` - Messaggi chat
- `Wishlist` - Lista desideri
- `EmailLog` - Log email inviate
- `TranslationLog` - Log traduzioni
- `CookieConsentLog` - Log consenso cookie (GDPR)

### Repository (20 repository JPA)
Tutti estendono `JpaRepository<T, ID>` con query custom quando necessario.

---

## 🔄 FLUSSI PRINCIPALI

### 1. **Registrazione Utente**
```
POST /api/auth/register
    ↓
AuthController.register()
    ↓
UserService.createUser()
    ↓
EmailService.sendVerificationEmail() (multi-lingua)
    ↓
UserRepository.save()
```

### 2. **Login e JWT**
```
POST /api/auth/login
    ↓
AuthController.login()
    ↓
UserService.authenticate()
    ↓
JwtUtil.generateToken()
    ↓
Response: { token, user }
```

### 3. **Conversione Valuta (Marketplace)**
```
GET /api/listings (con Authentication)
    ↓
ListingController.getAllListings()
    ↓
getTargetCurrency() → User.preferredCurrency o "USD"
    ↓
toListingDTO() → CurrencyConversionService.convert()
    ↓
Response: ListingDTO { price, currency, convertedPrice, convertedCurrency }
```

### 4. **Aggiornamento Tassi Valuta (Cron)**
```
Cloudflare Cron → POST /api/currency/refresh-rates
    Header: Authorization: Bearer {FUNKARD_CRON_SECRET_CURRENCY}
    ↓
CurrencyRateUpdateController.refreshRates()
    ↓ (bypass JWT filter)
    ↓
CurrencyRateUpdateService.updateRates()
    ↓
API Call: https://open.er-api.com/v6/latest/USD
    ↓
Filter by SupportedCurrencies
    ↓
CurrencyRateStore.updateRates()
```

### 5. **Traduzione Messaggi**
```
POST /api/translate
    ↓
TranslateController.translate()
    ↓
UnifiedTranslationService.translate()
    ↓
DeepLTranslateService o OpenAiTranslateService
    ↓
Response: { translatedText, sourceLanguage, targetLanguage }
```

### 6. **Notifiche Admin (Realtime)**
```
Admin Action (es. approva pending value)
    ↓
AdminNotificationService.createNotification()
    ↓
AdminStreamController.broadcast() (SSE)
    ↓
Client riceve evento in realtime
```

---

## 📦 DIPENDENZE PRINCIPALI (pom.xml)

### Core Spring Boot
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - JPA/Hibernate
- `spring-boot-starter-security` - Spring Security
- `spring-boot-starter-websocket` - WebSocket/SSE
- `spring-boot-starter-mail` - Email
- `spring-boot-starter-actuator` - Monitoring

### Database
- `postgresql` (42.7.4) - Driver PostgreSQL
- `flyway-core` - Database migrations

### Security
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.11.5) - JWT

### Storage
- `aws-sdk-s3` (2.25.60) - Cloudflare R2 (compatibile S3)

### Utilities
- `lombok` (1.18.30) - Code generation
- `caffeine` - In-memory cache
- `opencv` (4.9.0-0) - OpenCV per image processing
- `itext7-core` (8.0.2) - PDF generation (GDPR export)

### Validation
- `jakarta.validation-api` - Bean validation
- `hibernate-validator` - Implementation

---

## 🔐 SICUREZZA E AUTENTICAZIONE

### JWT Token
- **Secret:** `jwt.secret` (env: `JWT_SECRET`)
- **Expiration:** 86400000ms (24 ore)
- **Header:** `Authorization: Bearer {token}`
- **Claims:** `userId`, `email`, `role`

### Admin Authentication
- **Token-based:** `FUNKARD_CRON_SECRET` per cron jobs
- **Role-based:** `ADMIN`, `SUPERVISOR`, `SUPER_ADMIN`
- **Admin Users:** Tabella `admin_users` separata

### CORS
- **Origini permesse:**
  - `https://funkard.com`
  - `https://www.funkard.com`
  - `https://admin.funkard.com`
  - `http://localhost:3000`
  - `http://localhost:3002`
- **Credentials:** Abilitati (`allowCredentials = true`)

### CSRF
- **Stato:** Disabilitato globalmente (REST API stateless)

---

## 🌍 INTERNAZIONALIZZAZIONE

### Lingue Supportate
- Definizione: `api/i18n/SupportedLanguages.java`
- Template email: `resources/email-templates/{locale}/`
- Lingue attive: `en`, `it` (altre in placeholder)

### Valute Supportate
- **38 valute** definite in `SupportedCurrencies.java`
- Conversione automatica in marketplace responses
- Default: `USD` se utente non autenticato

### Traduzione
- **Provider:** DeepL (primario), OpenAI (fallback)
- **Service:** `UnifiedTranslationService`
- **Logging:** `TranslationLog` entity

---

## 📧 SISTEMA EMAIL

### Template Multi-lingua
- **Percorso:** `resources/email-templates/{locale}/`
- **Template disponibili:**
  - `account_confirmation.html`
  - `account_deletion.html`
  - `order_confirmation.html`
  - `order_shipped.html`
  - `password_reset.html`
  - `ticket_opened.html`

### Configurazione
- **Primary SMTP:** `no-reply@funkard.com` (register.it)
- **Fallback SMTP:** `support@funkard.com`
- **Service:** `EmailService` con `EmailTemplateManager`

---

## 🔄 SCHEDULED TASKS

### Cron Jobs (Cloudflare Cron)
1. **Currency Rate Update**
   - Endpoint: `POST /api/currency/refresh-rates`
   - Frequenza: Ogni ora
   - Auth: Bearer `FUNKARD_CRON_SECRET_CURRENCY`

2. **Support Cleanup**
   - Endpoint: `POST /api/admin/support/cleanup`
   - Auth: Bearer `FUNKARD_CRON_SECRET`

3. **Valuation Refresh**
   - Endpoint: `POST /api/valuation/refreshIncremental`
   - Auth: Bearer `FUNKARD_CRON_SECRET`

---

## 📊 STATISTICHE PROGETTO

- **File Java totali:** ~260 file
- **Controller:** 40+ controller
- **Service:** 50+ servizi
- **Repository:** 20 repository
- **Entity:** 23 entità JPA
- **DTO:** 20+ DTO
- **Configurazioni:** 6 classi config
- **Package principali:** 15+ package

---

## 🔗 COLLEGAMENTI TRA COMPONENTI

### Currency System
```
CurrencyRateUpdateController
    ↓
CurrencyRateUpdateService
    ↓
CurrencyRateStore (in-memory)
    ↑
CurrencyConversionService
    ↑
ProductController / ListingController
    ↓
ProductDTO / ListingDTO (con convertedPrice/Currency)
```

### Admin System
```
AdminController
    ↓
AdminService
    ↓
AdminNotificationService
    ↓
AdminStreamController (SSE)
    ↓
Client (realtime updates)
```

### Marketplace
```
ProductController / ListingController
    ↓
ProductService / ListingService
    ↓
ProductRepository / ListingRepository
    ↓
CurrencyConversionService (per convertedPrice)
    ↓
ProductDTO / ListingDTO
```

### Translation System
```
TranslateController
    ↓
UnifiedTranslationService
    ↓
DeepLTranslateService / OpenAiTranslateService
    ↓
TranslationLog (logging)
```

---

## ✅ STATO ATTUALE

### ✅ Implementato
- ✅ Sistema multi-valuta (38 valute)
- ✅ Conversione automatica in marketplace
- ✅ Aggiornamento tassi via cron (Cloudflare)
- ✅ JWT authentication
- ✅ Admin panel completo
- ✅ Support system
- ✅ Email multi-lingua
- ✅ Realtime notifications (SSE)
- ✅ GDPR compliance (account deletion, cookie consent)
- ✅ Translation system (DeepL/OpenAI)
- ✅ Cloudflare R2 storage

### 🔄 In Sviluppo
- 🔄 GradeLens AI improvements
- 🔄 Marketplace trends analysis
- 🔄 Payment integration (Stripe)

---

**Documento generato automaticamente**  
**Ultimo aggiornamento:** 2025-01-26

