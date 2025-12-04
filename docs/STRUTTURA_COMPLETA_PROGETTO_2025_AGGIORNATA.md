# 📋 Struttura Completa Progetto Funkard API - 2025

**Ultimo aggiornamento:** 29 Novembre 2025  
**Versione:** Spring Boot 3.5.6, Java 17  
**Deploy:** Render.com + Cloudflare

---

## 🌳 Struttura Ad Albero del Progetto

```
funkard-api/
├── src/main/java/com/funkard/
│   ├── FunkardApiApplication.java          # Entry point Spring Boot
│   │
│   ├── 📁 admin/                           # Sistema Admin Panel (63 file)
│   │   ├── AdminConfig.java
│   │   ├── AdminController.java
│   │   ├── AdminService.java
│   │   ├── controller/                     # 21 controller admin
│   │   │   ├── AdminNotificationController.java      # Notifiche admin
│   │   │   ├── AdminSupportCleanupController.java    # Cleanup support (cron)
│   │   │   ├── AdminDashboardController.java
│   │   │   ├── AdminStatsController.java
│   │   │   ├── AdminSupportController.java
│   │   │   ├── AdminPendingValueController.java
│   │   │   ├── AdminValuationController.java
│   │   │   ├── AdminFranchiseController.java
│   │   │   ├── AdminEmailLogController.java
│   │   │   ├── CookieLogAdminController.java
│   │   │   └── ... (altri 11 controller)
│   │   ├── dto/                            # 10 DTO admin
│   │   ├── log/                            # Sistema logging azioni admin
│   │   │   ├── AdminActionLog.java
│   │   │   ├── AdminActionLogController.java    # Cleanup logs (cron)
│   │   │   ├── AdminActionLogger.java
│   │   │   └── AdminActionLogRepository.java
│   │   ├── model/                          # 6 entità admin
│   │   │   ├── AdminNotification.java
│   │   │   ├── SupportTicket.java
│   │   │   ├── SupportMessage.java
│   │   │   ├── SystemCleanupLog.java
│   │   │   └── ...
│   │   ├── repository/                     # 4 repository admin
│   │   │   ├── AdminNotificationRepository.java
│   │   │   ├── SupportTicketRepository.java
│   │   │   ├── SupportMessageRepository.java
│   │   │   └── SystemCleanupLogRepository.java
│   │   ├── service/                        # 12 service admin
│   │   │   ├── AdminNotificationService.java
│   │   │   ├── AdminNotificationCleanupService.java
│   │   │   ├── SupportCleanupService.java
│   │   │   ├── SystemCleanupService.java
│   │   │   └── ... (altri 8 service)
│   │   ├── system/                         # Sistema manutenzione
│   │   │   ├── SystemMaintenanceController.java      # Status cleanup (cron)
│   │   │   └── MaintenanceController.java             # Cleanup logs (cron)
│   │   └── util/
│   │       └── AdminAuthHelper.java
│   │
│   ├── 📁 adminaccess/                     # Sistema accesso admin (6 file)
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── 📁 adminauth/                       # Autenticazione admin (14 file)
│   │   ├── AdminUser.java
│   │   ├── AdminToken.java
│   │   ├── AccessRequest.java
│   │   ├── AdminAuthController.java
│   │   ├── AdminTokenController.java
│   │   ├── AccessRequestController.java
│   │   └── ... (service e repository)
│   │
│   ├── 📁 currency/                        # Sistema Multi-Valuta (6 file)
│   │   ├── CurrencyController.java                 # API valute pubbliche
│   │   ├── CurrencyConversionService.java          # Servizio conversione
│   │   ├── CurrencyRateStore.java                  # Store in-memory tassi
│   │   ├── CurrencyRateUpdateService.java          # Aggiornamento tassi
│   │   ├── CurrencyRateUpdateController.java       # Endpoint cron refresh
│   │   └── CurrencyRatePublicTestController.java    # Test endpoint
│   │
│   ├── 📁 config/                          # Configurazioni (6 file)
│   │   ├── SecurityConfig.java                     # ⚙️ Configurazione sicurezza
│   │   ├── SupportedCurrencies.java                # 📋 Whitelist 38 valute
│   │   ├── EmailConfig.java
│   │   ├── CacheConfig.java
│   │   ├── R2Config.java                           # Cloudflare R2 storage
│   │   └── WebSocketConfig.java
│   │
│   ├── 📁 security/                        # Sicurezza (2 file)
│   │   ├── JwtFilter.java                          # 🔐 Filtro JWT + bypass cron
│   │   └── JwtUtil.java                            # Utility JWT
│   │
│   ├── 📁 controller/                      # Controller pubblici (21 file)
│   │   ├── AuthController.java                     # Autenticazione
│   │   ├── UserController.java                     # Profilo utente
│   │   ├── ListingController.java                  # Marketplace listings
│   │   ├── CardController.java                     # Carte
│   │   ├── TransactionController.java              # Transazioni
│   │   ├── SupportController.java                  # Supporto clienti
│   │   ├── TranslateController.java                # Traduzione
│   │   ├── FranchiseController.java                # Franchise
│   │   ├── WishlistController.java
│   │   ├── CollectionController.java
│   │   └── ... (altri 11 controller)
│   │
│   ├── 📁 market/                          # Marketplace (16 file)
│   │   ├── controller/
│   │   │   ├── ProductController.java              # Prodotti (con conversione valuta)
│   │   │   ├── MarketValuationController.java      # Valutazioni (cron refreshIncremental)
│   │   │   └── TrendController.java
│   │   ├── model/
│   │   │   ├── Product.java
│   │   │   ├── MarketListing.java
│   │   │   └── MarketValuation.java
│   │   ├── repository/
│   │   ├── service/
│   │   │   ├── ProductService.java
│   │   │   ├── MarketValuationService.java
│   │   │   └── TrendService.java
│   │   └── trend/
│   │
│   ├── 📁 model/                           # Entità JPA (23 file)
│   │   ├── User.java
│   │   ├── UserPreferences.java
│   │   ├── UserCard.java
│   │   ├── Card.java
│   │   ├── Listing.java
│   │   ├── Transaction.java
│   │   ├── Product.java
│   │   ├── Franchise.java
│   │   ├── ChatMessage.java
│   │   ├── EmailLog.java
│   │   └── ... (altre 13 entità)
│   │
│   ├── 📁 repository/                       # Repository JPA (20 file)
│   │   ├── UserRepository.java
│   │   ├── ListingRepository.java
│   │   ├── TransactionRepository.java
│   │   └── ... (altri 17 repository)
│   │
│   ├── 📁 service/                         # Business Logic (32 file)
│   │   ├── UserService.java
│   │   ├── ListingService.java
│   │   ├── TransactionService.java
│   │   ├── EmailService.java
│   │   ├── TranslationService.java
│   │   ├── ChatService.java
│   │   └── ... (altri 26 service)
│   │
│   ├── 📁 dto/                             # Data Transfer Objects (20 file)
│   │   ├── UserDTO.java
│   │   ├── ListingDTO.java                 # Con convertedPrice/Currency
│   │   ├── ProductDTO.java                 # Con convertedPrice/Currency
│   │   ├── TransactionDTO.java
│   │   └── ... (altri 16 DTO)
│   │
│   ├── 📁 realtime/                        # Real-time (4 file)
│   │   ├── AdminStreamController.java      # SSE per admin
│   │   ├── SupportStreamController.java    # SSE per support
│   │   ├── RealtimeConfig.java
│   │   └── EventType.java
│   │
│   ├── 📁 scheduler/                        # Scheduled Tasks (3 file)
│   │   ├── UserDeletionScheduler.java
│   │   └── EmailLogCleanupScheduler.java
│   │
│   ├── 📁 gradelens/                       # GradeLens AI (8 file)
│   ├── 📁 grading/                         # Sistema grading (4 file)
│   ├── 📁 user/                            # Gestione utenti (6 file)
│   ├── 📁 support/                         # Supporto clienti
│   ├── 📁 common/                           # Componenti comuni
│   │   └── GlobalExceptionHandler.java
│   └── 📁 api/                              # API utilities
│
├── src/main/resources/
│   ├── application.properties              # Config principale
│   ├── application-prod.yml                # Config produzione
│   ├── application-dev.properties
│   ├── db/migration/                        # 22 migration SQL
│   │   ├── V1__add_grading_columns_to_usercard.sql
│   │   ├── V2__add_preferred_currency_to_users.sql
│   │   ├── V22__add_currency_to_products_listings_transactions.sql
│   │   └── ... (altre 19 migration)
│   ├── email-templates/                     # Template email multi-lingua
│   │   ├── en/
│   │   ├── it/
│   │   └── ... (altre 30+ lingue)
│   └── data/
│       └── franchises.json                  # Dati franchise statici
│
├── docs/                                    # Documentazione (30+ file MD)
├── pom.xml                                  # Maven dependencies
├── Dockerfile                               # Container Docker
└── render.yaml                              # Config Render.com

```

---

## 🔗 Collegamenti e Dipendenze Principali

### 🔐 Sistema di Sicurezza

```
SecurityConfig
    ├── JwtFilter (custom filter)
    │   ├── Bypass /public/**
    │   ├── Bypass FUNKARD_CRON_SECRET (per cron admin)
    │   └── Validazione JWT per utenti/admin
    │
    └── Regole di accesso:
        ├── Public: /api/auth/**, /api/translate/**, /api/listings/**, etc.
        ├── Cron endpoints: /api/admin/**/cleanup, /api/valuation/refreshIncremental
        ├── Authenticated: /api/user/**, /api/admin/**, /api/support/**
        └── Currency: /api/currency/** (authenticated), /api/currency/refresh-rates (cron)
```

### 💰 Sistema Multi-Valuta

```
SupportedCurrencies (38 valute)
    │
    ├── CurrencyRateStore (in-memory)
    │   └── Map<String, Double> rates (USD → altre valute)
    │
    ├── CurrencyRateUpdateService
    │   └── Chiama ExchangeRate-API (base USD)
    │       └── Filtra per SupportedCurrencies
    │           └── Aggiorna CurrencyRateStore
    │
    ├── CurrencyRateUpdateController
    │   └── POST /api/currency/refresh-rates
    │       └── Protetto: Bearer FUNKARD_CRON_SECRET_CURRENCY
    │
    └── CurrencyConversionService
        └── Converte usando CurrencyRateStore
            └── USD come pivot currency
                │
                ├── ProductController → ProductDTO (convertedPrice/Currency)
                ├── ListingController → ListingDTO (convertedPrice/Currency)
                └── TransactionService → Conversioni automatiche
```

### 🔔 Sistema Admin Notifiche

```
AdminNotificationController
    ├── GET /api/admin/notifications (lista)
    ├── POST /api/admin/notifications/{id}/read
    ├── POST /api/admin/notifications/{id}/resolve
    ├── DELETE|POST /api/admin/notifications/cleanup (cron)
    │   └── AdminNotificationRepository.deleteByArchivedTrueAndArchivedAtBefore()
    └── GET /api/admin/notifications/stream (SSE)
        │
        └── AdminNotificationService
            ├── AdminNotificationRepository
            └── AdminStreamController (SSE events)
```

### 🧹 Sistema Cleanup Cron

```
Cloudflare Cron Workers
    │
    ├── POST /api/admin/notifications/cleanup
    │   └── Bearer: FUNKARD_CRON_SECRET
    │       └── AdminNotificationController.cleanup()
    │
    ├── DELETE|POST /api/admin/support/cleanup
    │   └── Bearer: FUNKARD_CRON_SECRET
    │       └── AdminSupportCleanupController.cleanupOldMessages()
    │
    ├── POST /api/admin/maintenance/cleanup-logs
    │   └── Bearer: FUNKARD_CRON_SECRET
    │       └── MaintenanceController.cleanupLogs()
    │
    ├── DELETE|POST /api/admin/logs/cleanup
    │   └── Bearer: FUNKARD_CRON_SECRET
    │       └── AdminActionLogController.cleanupOldLogs()
    │
    ├── POST /api/admin/system/cleanup/status
    │   └── Bearer: FUNKARD_CRON_SECRET
    │       └── SystemMaintenanceController.updateCleanupStatus()
    │
    ├── POST /api/valuation/refreshIncremental
    │   └── Bearer: FUNKARD_CRON_SECRET
    │       └── MarketValuationController.refreshIncremental()
    │
    └── POST /api/currency/refresh-rates
        └── Bearer: FUNKARD_CRON_SECRET_CURRENCY
            └── CurrencyRateUpdateController.refreshRates()
```

**Pattern Autenticazione Cron:**
1. Verifica `Authorization: Bearer FUNKARD_CRON_SECRET`
2. Se match → bypass, esegui direttamente
3. Se non match → verifica ruolo ADMIN via SecurityContext
4. Se non admin → `RuntimeException("Access Denied")`

### 🛒 Sistema Marketplace

```
ProductController
    ├── GET /api/products
    │   └── ProductService.getAllProducts()
    │       └── ProductRepository
    │           └── ProductDTO (con convertedPrice/Currency)
    │               └── CurrencyConversionService.convert()
    │
ListingController
    ├── GET /api/listings
    │   └── ListingService.getAllListings()
    │       └── ListingRepository
    │           └── ListingDTO (con convertedPrice/Currency)
    │               └── CurrencyConversionService.convert()
    │
MarketValuationController
    └── POST /api/valuation/refreshIncremental (cron)
        └── MarketValuationService.refreshOnlyRecentSales()
```

### 👤 Sistema Utenti

```
AuthController
    ├── POST /api/auth/register
    ├── POST /api/auth/login
    └── POST /api/auth/refresh
        │
        └── UserService
            └── UserRepository

UserController
    ├── GET /api/user/me
    ├── PUT /api/user/me
    └── DELETE /api/user/delete-account (GDPR)
        │
        └── UserService
            ├── UserRepository
            ├── UserPreferencesRepository
            └── UserAddressService
```

### 🌍 Sistema Traduzione

```
TranslateController
    └── POST /api/translate
        │
        └── UnifiedTranslationService
            ├── DeepLTranslateService (fallback)
            └── OpenAiTranslateService (primary)
                │
                └── TranslationLogRepository (logging)
```

---

## 📡 Endpoint API Principali

### 🔓 Endpoint Pubblici (permitAll)

#### Autenticazione
- `POST /api/auth/register` - Registrazione
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token

#### Marketplace
- `GET /api/listings` - Lista listings (con conversione valuta)
- `GET /api/listings/{id}` - Dettaglio listing
- `POST /api/listings` - Crea listing (authenticated)
- `GET /api/products` - Lista prodotti (con conversione valuta)
- `GET /api/products/{id}` - Dettaglio prodotto
- `GET /api/cards` - Lista carte
- `GET /api/valuation/**` - Valutazioni mercato
- `GET /api/trends/**` - Trend mercato

#### Traduzione
- `POST /api/translate` - Traduci testo

#### Franchise
- `GET /api/franchises` - Lista franchise
- `GET /api/franchises/catalog` - Catalogo completo

### 🔐 Endpoint Autenticati

#### Utente
- `GET /api/user/me` - Profilo utente
- `PUT /api/user/me` - Aggiorna profilo
- `DELETE /api/user/delete-account` - Elimina account (GDPR)
- `GET /api/user/preferences` - Preferenze utente
- `PUT /api/user/preferences` - Aggiorna preferenze

#### Collezione
- `GET /api/usercards` - Carte utente
- `POST /api/usercards` - Aggiungi carta
- `GET /api/collection` - Collezione completa
- `GET /api/wishlist` - Wishlist

#### Supporto
- `POST /api/support` - Crea ticket
- `GET /api/support` - Lista ticket
- `GET /api/support/{id}` - Dettaglio ticket
- `POST /api/support/{id}/message` - Aggiungi messaggio

### 🔧 Endpoint Admin

#### Dashboard
- `GET /api/admin/dashboard` - Dashboard aggregata
- `GET /api/admin/stats` - Statistiche generali

#### Notifiche
- `GET /api/admin/notifications` - Lista notifiche
- `POST /api/admin/notifications/{id}/read` - Marca come letta
- `POST /api/admin/notifications/{id}/resolve` - Risolvi
- `GET /api/admin/notifications/stream` - SSE stream
- `DELETE|POST /api/admin/notifications/cleanup` - **Cron cleanup**

#### Supporto Admin
- `GET /api/admin/support` - Gestione supporto
- `DELETE|POST /api/admin/support/cleanup` - **Cron cleanup**

#### Manutenzione
- `POST /api/admin/maintenance/cleanup-logs` - **Cron cleanup logs**
- `DELETE|POST /api/admin/logs/cleanup` - **Cron cleanup action logs**
- `POST /api/admin/system/cleanup/status` - **Cron status update**

### 💰 Endpoint Valute

- `GET /api/currency/supported` - Lista 38 valute supportate
- `GET /api/currency/rates` - Tassi di cambio correnti
- `GET /api/currency/convert` - Conversione manuale
- `POST /api/currency/refresh-rates` - **Cron refresh tassi** (Bearer FUNKARD_CRON_SECRET_CURRENCY)

### ⚙️ Endpoint Cron (Cloudflare Workers)

Tutti protetti con `Bearer FUNKARD_CRON_SECRET` (o `FUNKARD_CRON_SECRET_CURRENCY`):

1. `POST /api/currency/refresh-rates` - Aggiorna tassi valuta (ogni ora)
2. `POST /api/valuation/refreshIncremental` - Refresh valutazioni mercato
3. `DELETE|POST /api/admin/notifications/cleanup` - Cleanup notifiche archiviate
4. `DELETE|POST /api/admin/support/cleanup` - Cleanup messaggi supporto
5. `POST /api/admin/maintenance/cleanup-logs` - Cleanup log manutenzione
6. `DELETE|POST /api/admin/logs/cleanup` - Cleanup action logs
7. `POST /api/admin/system/cleanup/status` - Aggiorna status cleanup

---

## 🔐 Configurazione Sicurezza

### SecurityConfig.java

```java
SecurityFilterChain:
    ├── CSRF: Disabilitato (REST API stateless)
    ├── CORS: Configurato per domini specifici
    ├── Session: STATELESS (JWT)
    │
    └── authorizeHttpRequests:
        ├── /public/** → permitAll()
        ├── /api/auth/**, /api/translate/** → permitAll()
        ├── Cron endpoints → permitAll() (protetti da Bearer token nei controller)
        ├── /api/listings/**, /api/products/** → permitAll()
        ├── /api/admin/** → authenticated() (con @PreAuthorize per ruoli)
        ├── /api/user/**, /api/support/** → authenticated()
        └── /api/currency/** → authenticated() (tranne refresh-rates)
```

### JwtFilter.java

```java
doFilterInternal():
    ├── Bypass /public/**
    ├── Bypass FUNKARD_CRON_SECRET (per cron admin)
    ├── Estrai JWT da Authorization header
    ├── Valida JWT
    ├── Estrai username e ruoli
    └── Imposta SecurityContext
```

---

## 💾 Database Schema

### Tabelle Principali

- `users` - Utenti (con `preferred_currency`, `language`)
- `user_preferences` - Preferenze utente
- `listings` - Annunci marketplace (con `currency`)
- `products` - Prodotti (con `currency`)
- `transactions` - Transazioni (con `currency`)
- `admin_notifications` - Notifiche admin
- `support_tickets` - Ticket supporto
- `support_messages` - Messaggi supporto
- `admin_action_logs` - Log azioni admin
- `system_cleanup_logs` - Log cleanup sistema
- `email_logs` - Log email inviate
- `franchises` - Franchise
- `franchise_catalog` - Catalogo franchise
- `pending_values` - Valori pending

### Migration SQL

22 migration Flyway in `src/main/resources/db/migration/`:
- V1-V22: Evoluzione schema database
- V2: Aggiunto `preferred_currency` a users
- V22: Aggiunto `currency` a products, listings, transactions

---

## 🌐 Integrazioni Esterne

### Cloudflare
- **R2 Storage**: Configurato in `R2Config.java`
- **Cron Workers**: Chiamano endpoint cron con Bearer token
- **CDN**: Frontend servito da Cloudflare

### ExchangeRate-API
- **Endpoint**: `https://open.er-api.com/v6/latest/USD`
- **Frequenza**: 1 chiamata/ora (via cron)
- **Uso**: Aggiornamento tassi valuta in `CurrencyRateStore`

### DeepL / OpenAI
- **Traduzione**: `UnifiedTranslationService` usa DeepL (fallback) e OpenAI (primary)
- **Logging**: Tutte le traduzioni loggate in `translation_logs`

---

## 📊 Statistiche Progetto

- **File Java**: ~260 file
- **Package principali**: 15+
- **Controller**: 40+ controller
- **Service**: 32+ service
- **Repository**: 20+ repository
- **Model/Entity**: 23+ entità JPA
- **DTO**: 20+ DTO
- **Migration SQL**: 22 migration
- **Template Email**: 30+ lingue supportate
- **Valute Supportate**: 38 valute ISO 4217

---

## 🔄 Flussi Principali

### 1. Conversione Valuta in Marketplace

```
GET /api/products
    ↓
ProductController.getAllProducts()
    ↓
ProductService.getAllProducts()
    ↓
UserRepository.findPreferredCurrency() o default "USD"
    ↓
CurrencyConversionService.convert(price, product.currency, userCurrency)
    ↓
ProductDTO { price, currency, convertedPrice, convertedCurrency }
```

### 2. Cleanup Cron Notifiche

```
Cloudflare Cron → POST /api/admin/notifications/cleanup
    ↓
JwtFilter: Bypass se Bearer FUNKARD_CRON_SECRET match
    ↓
AdminNotificationController.cleanup()
    ↓
AdminNotificationRepository.deleteByArchivedTrueAndArchivedAtBefore(thresholdDate)
    ↓
Return: { deleted: int, olderThanDays: int }
```

### 3. Aggiornamento Tassi Valuta

```
Cloudflare Cron → POST /api/currency/refresh-rates
    ↓
CurrencyRateUpdateController.refreshRates()
    ↓
CurrencyRateUpdateService.updateRates()
    ↓
ExchangeRate-API (base USD)
    ↓
Filtra per SupportedCurrencies (38 valute)
    ↓
CurrencyRateStore.updateRates(Map<String, Double>)
    ↓
Return: { success: true, currenciesCount: 38, lastUpdated: Instant }
```

---

## 🎯 Funzionalità Chiave

### ✅ Multi-Valuta
- 38 valute supportate
- Conversione automatica in marketplace
- Aggiornamento tassi orario (cron)
- USD come pivot currency

### ✅ Sistema Admin
- Notifiche real-time (SSE)
- Dashboard aggregata
- Gestione supporto
- Logging azioni admin
- Cleanup automatico (cron)

### ✅ Sicurezza
- JWT authentication
- Ruoli ADMIN/SUPER_ADMIN
- Bypass cron con Bearer token
- CORS configurato
- CSRF disabilitato (REST stateless)

### ✅ GDPR Compliance
- Eliminazione account
- Logging consensi cookie
- Tracciamento eliminazioni

### ✅ Multi-Lingua
- 30+ lingue supportate
- Template email localizzati
- Traduzione automatica (DeepL/OpenAI)

---

## ⏰ Scheduled Tasks Interni

### Scheduler Spring Boot

#### 1. UserDeletionScheduler
- **Frequenza**: Ogni ora (`0 0 * * * *`)
- **Zona**: Europe/Rome
- **Funzione**: Processa richieste di cancellazione account GDPR
- **Logica**:
  - Recupera richieste `PENDING` con `scheduledDeletionAt <= now`
  - Esegue cancellazione definitiva via `UserDeletionService`
  - Invia email di conferma cancellazione
  - Aggiorna stato a `COMPLETED` o `FAILED`

#### 2. EmailLogCleanupScheduler
- **Frequenza**: Ogni giorno alle 3:00 (`0 0 3 * * *`)
- **Zona**: Europe/Rome
- **Funzione**: Rimuove log email più vecchi di 90 giorni
- **Logica**:
  - Recupera `EmailLog` con `createdAt < now - 90 giorni`
  - Elimina record vecchi

#### 3. AdminNotificationCleanupService
- **Frequenza**: Ogni giorno alle 3:00 (`0 0 3 * * *`)
- **Zona**: Europe/Rome
- **Funzione**: Rimuove notifiche admin archiviate più vecchie di 30 giorni
- **Logica**:
  - Chiama `AdminNotificationRepository.deleteByArchivedTrueAndArchivedAtBefore()`
  - Elimina notifiche risolte e archiviate

#### 4. GradeCleanupScheduler
- **Frequenza**: Ogni giorno alle 3:00 (`0 0 3 * * *`)
- **Zona**: Europe/Rome
- **Funzione**: Rimuove carte con grading temporaneo scadute (30 giorni)
- **Logica**:
  - Recupera `UserCard` con `permanent = false` e `gradedAt < now - 30 giorni`
  - Elimina carte temporanee scadute

---

## 📡 Real-Time Features (SSE)

### Server-Sent Events (SSE)

#### 1. AdminStreamController
- **Endpoint**: `GET /api/admin/notifications/stream`
- **Funzione**: Stream real-time per notifiche admin
- **Eventi**:
  - `NEW_TICKET` - Nuovo ticket creato
  - `NEW_REPLY` - Nuova risposta
  - `TICKET_STATUS` - Cambio stato ticket
  - `TICKET_ASSIGNED` - Ticket assegnato
  - `TICKET_RESOLVED` - Ticket risolto
  - `TICKET_CLOSED` - Ticket chiuso
  - `NOTIFICATION` - Notifica generica
  - `PING` - Keep-alive
- **Implementazione**: `SseEmitter` con gestione connessioni multiple

#### 2. SupportStreamController
- **Endpoint**: `GET /api/support/stream`
- **Funzione**: Stream real-time per aggiornamenti supporto utente
- **Eventi**: Stessi eventi di `AdminStreamController` ma filtrati per utente
- **Autenticazione**: Richiede JWT con `userId` nel token

### EventType Enum
- Enum centralizzato per tipi di eventi SSE
- Valori: `NEW_TICKET`, `NEW_REPLY`, `TICKET_STATUS`, `TICKET_ASSIGNED`, `TICKET_RESOLVED`, `TICKET_CLOSED`, `TICKET_REOPENED`, `NOTIFICATION`, `PING`, `CONNECTED`, `ERROR`

### WebSocket (Configurato ma Non Utilizzato)
- `WebSocketConfig.java` presente ma non integrato
- Sistema attuale usa esclusivamente SSE

---

## 🗄️ Caching Strategy

### CacheConfig.java

#### Configurazione Caffeine
- **TTL**: 25 secondi (`expireAfterWrite`)
- **Max Size**: 500 entry
- **Statistiche**: Abilitate (`recordStats()`)

#### Cache Utilizzate
- `marketplace:search` - Ricerche marketplace
- `marketplace:filters` - Filtri marketplace
- `reference:brands` - Franchise e brand
- `translation:*` - Traduzioni (via `@Cacheable`)

#### Limitazioni
- **Nessuna invalidazione manuale**: Cache si invalida solo dopo TTL
- **Market Valuation**: Non invalidata dopo `refreshOnlyRecentSales()`
- **Franchise**: Cache in-memory in `FranchiseJsonService`, non Spring Cache

---

## ☁️ Storage (Cloudflare R2)

### R2Config.java
- **Client**: AWS S3 SDK compatibile con R2
- **Endpoint**: `R2_ENDPOINT` (env variable)
- **Credentials**: `R2_ACCESS_KEY`, `R2_SECRET_KEY`
- **Bucket**: `R2_BUCKET`
- **Public URL**: `R2_PUBLIC_BASE_URL`

### R2Service.java
- **Metodi**:
  - `uploadFile(MultipartFile, String path)` - Upload generico
  - `uploadUserCardFile(MultipartFile, String userCardId, String slot)` - Upload carte utente
  - `downloadFile(String key)` - Download file
  - `deleteFile(String key)` - Elimina file

### Utilizzo
- **UserCard Images**: `usercards/{userCardId}/{slot}-{filename}`
- **Collection Upload**: Multipart form-data via `CollectionController`
- **Public Access**: URL pubblici generati con `R2_PUBLIC_BASE_URL`

---

## 🤖 GradeLens (Stato Mock)

### GradeLensController
- **Endpoint**: `POST /api/gradelens/analyze`
- **Endpoint**: `POST /api/gradelens/confirm`

### GradeLensService

#### Stato Attuale: ⚠️ COMPLETAMENTE MOCK

**Metodo `analyze(String frontImageUrl, String backImageUrl)`**:
- ❌ **Ignora completamente gli URL delle immagini**
- ❌ **Non scarica immagini**
- ❌ **Non processa immagini**
- ✅ **Restituisce valori hardcoded**:
  - `Subgrades`: 9.12, 8.60, 8.95, 8.73
  - `Overall`: Calcolato con pesi PSA/BGS (0.4, 0.25, 0.2, 0.15)
  - `Diagnostics`: `["CORNER_DAMAGE_MINOR", "SLIGHT_GLARE_DETECTED"]`
  - `AnalysisMeta`: 0.92, 0.10, 0.03

**Metodo `saveGradedCard(...)`**:
- ✅ **Salva dati nel database** (`UserCard`)
- ✅ **Serializza subgrades in JSON**
- ⚠️ **Valore stimato**: `overallGrade * 10` (placeholder)

### OpenCV Integration
- ✅ **Dependency presente**: `org.openpnp:opencv:4.9.0-0`
- ✅ **Caricamento libreria**: `OpenCV.loadLocally()` in static block
- ❌ **Nessun utilizzo**: OpenCV caricato ma mai usato per analisi
- ❌ **Nessun preprocessing**: Resize, crop, normalizzazione assenti
- ❌ **Nessun algoritmo CV**: Canny, Hough, corner detection assenti

### HeuristicAiProvider
- **Classe presente ma non utilizzata**
- Genera valori random realistici (6.0-10.0)
- Non integrato in `GradeLensService`

### Conclusione
**GradeLens è completamente mock**: Nessuna analisi reale, nessun processing immagini, nessun modello ML. Solo placeholder funzionali per salvare dati nel database.

---

## 💬 Chat/Support System

### ChatController (User-to-User)
- **Endpoint**: `POST /api/chat/message`
- **Endpoint**: `GET /api/chat/conversation/{userId}`
- **Endpoint**: `GET /api/chat/unread`
- **Funzione**: Chat tra utenti con traduzione automatica

### SupportChatController (User-to-Admin)
- **Endpoint**: `POST /api/support/chat/{ticketId}/message`
- **Endpoint**: `GET /api/support/chat/{ticketId}/messages`
- **Endpoint**: `POST /api/support/chat/{ticketId}/read`
- **Funzione**: Chat supporto con traduzione automatica

### ChatService
- **Traduzione automatica**: Usa `UnifiedTranslationService`
- **Rilevamento lingua**: Da `user.language`
- **Salvataggio**: Testo originale + tradotto in `ChatMessage`

### SupportMessageService
- **Traduzione automatica**: Tra utente e admin
- **Campi traduzione**: `originalLanguage`, `translatedText`, `targetLanguage`, `isTranslated`
- **Logging**: Tutte le traduzioni loggate in `translation_logs`

### SupportTicketService
- **Pubblica eventi SSE**: A `AdminStreamController` e `SupportStreamController`
- **Gestione stati**: `OPEN`, `ASSIGNED`, `RESOLVED`, `CLOSED`, `REOPENED`
- **Assegnazione**: Ticket assegnati a admin specifici

---

## 🌍 Sistema Traduzione Dettagliato

### UnifiedTranslationService
- **Provider Primary**: `OpenAiTranslateService` (GPT-4o-mini)
- **Provider Fallback**: `DeepLTranslateService`
- **Fallback finale**: Restituisce testo originale se entrambi falliscono
- **Normalizzazione**: Codici lingua ISO 639-1 (es. "en-US" → "en")

### SupportedLanguages
- **31 lingue supportate**: `en`, `it`, `es`, `fr`, `de`, `pt`, `ja`, `zh`, `ru`, `ar`, `hi`, `ko`, `tr`, `id`, `vi`, `bn`, `tl`, `pl`, `nl`, `sv`, `no`, `da`, `el`, `cs`, `hu`, `ro`, `uk`, `th`, `ms`, `fa`, `sq`
- **Classe utility**: Non enum, contiene `Set` e `List` ordinata

### TranslationLog
- **Tabella audit**: `translation_logs`
- **Campi**: `provider`, `source_language`, `target_language`, `success`, `error_message`, `user_id`, `message_type`, `message_id`

### Utilizzo
- **Chat**: Traduzione automatica tra utenti
- **Support**: Traduzione automatica utente ↔ admin
- **Product**: `nameEn` generato automaticamente per prodotti
- **API**: `POST /api/translate` per traduzione on-demand

---

## 📚 Sistema Franchise

### FranchiseController (Pubblico)
- **Endpoint**: `GET /api/franchises`
- **Endpoint**: `GET /api/franchises/catalog`
- **Endpoint**: `GET /api/franchises/categories`
- **Funzione**: Lista franchise attivi, raggruppati per categoria

### FranchiseAdminController (Admin)
- **Endpoint**: `GET /api/admin/franchises`
- **Endpoint**: `POST /api/admin/franchises/approve/{proposalId}`
- **Endpoint**: `POST /api/admin/franchises/reject/{proposalId}`
- **Endpoint**: `PUT /api/admin/franchises/{id}` - Abilita/disabilita
- **Endpoint**: `POST /api/admin/franchises` - Crea manualmente

### FranchiseJsonService
- **File statico**: `src/main/resources/data/franchises.json`
- **Cache in-memory**: Aggiornata quando admin approva proposte
- **Metodi**: `getActiveFranchises()`, `updateJsonFile()`, `getFranchisesByCategory()`

### FranchiseAdminService
- **Approvazione proposte**: Converte `PendingValue` (type: `FRANCHISE`) in franchise attivo
- **Aggiornamento cache**: Chiama `FranchiseJsonService.updateJsonFile()` dopo approvazione
- **Gestione stato**: `active` flag per abilitare/disabilitare franchise

### Pending Values Integration
- **Proposte utente**: Salvate come `PendingValue` con `type = FRANCHISE`
- **Approvazione admin**: Converte proposta in franchise attivo
- **Notifiche**: Admin notificato quando nuova proposta creata

---

## 📊 Marketplace Valuation

### MarketValuationService

#### Metodo `getOrCreateValuation(...)`
- **Cerca esistente**: Per `itemName`, `setName`, `category`, `condition`
- **Crea nuovo**: Se non esiste, calcola valore provvisorio
- **Valore fallback**: Basato su categoria e condizione
  - CARD: 50.0, BOX: 200.0, ETB: 120.0, BOOSTER: 15.0, SLAB: 100.0, ACCESSORY: 25.0
  - Moltiplicatori condizione: SEALED 1.2, MINT 1.1, NM 1.0, LP 0.8, HP 0.6, DAMAGED 0.4
  - Fattore grade: `grade / 10.0` se presente

#### Metodo `refreshOnlyRecentSales()` (Cron)
- **Frequenza**: Chiamato da cron `POST /api/valuation/refreshIncremental`
- **Logica**:
  - Recupera vendite ultime 6 ore (`findSoldAfter(since)`)
  - Raggruppa per chiave: `itemName|setName|category|condition`
  - Ricalcola valutazione per ogni gruppo
  - Aggiorna `avgPrice` e `lastSoldPrice`
- **Limitazione**: ⚠️ **Non invalida cache marketplace** - Aggiornamenti visibili solo dopo TTL 25s

#### Metodo `recalcValuation(...)`
- **Vendite recenti**: Ultimi 30 giorni
- **Calcolo**: Media prezzi vendite recenti
- **Aggiornamento**: `estimatedValueProvvisorio = false`, `manualCheck = false`

### MarketValuationController
- **Endpoint**: `POST /api/valuation/get` - Ottieni valutazione
- **Endpoint**: `POST /api/valuation/refreshIncremental` - **Cron refresh**

---

## 💳 Payment Methods

### PaymentMethodController
- **Endpoint**: `GET /api/user/payments` - Lista metodi
- **Endpoint**: `POST /api/user/payments` - Aggiungi metodo
- **Endpoint**: `DELETE /api/user/payments/{id}` - Elimina metodo
- **Endpoint**: `PATCH /api/user/payments/{id}/default` - Imposta default
- **Endpoint**: `GET /api/user/payments/default` - Ottieni default
- **Endpoint**: `GET /api/user/payments/stats` - Statistiche

### PaymentMethodService
- **Validazione**: Algoritmo Luhn per numeri carta
- **Sicurezza**: Mai salva numeri completi, solo versioni mascherate
- **Limite**: Max 5 metodi per utente
- **Brand supportati**: VISA, MASTERCARD, AMEX, DISCOVER
- **Scadenza**: Controllo automatico date scadenza

### PaymentMethod Entity
- **Campi**: `cardHolder`, `cardNumberMasked`, `expiryDate`, `brand`, `isDefault`, `lastFourDigits`
- **Sicurezza**: `cardNumber` mai salvato, solo `cardNumberMasked`

---

## 🏠 User Addresses

### UserController (Address Endpoints)
- **Endpoint**: `GET /api/user/address` - Lista indirizzi
- **Endpoint**: `POST /api/user/address` - Aggiungi indirizzo
- **Endpoint**: `PUT /api/user/address/{id}` - Aggiorna indirizzo
- **Endpoint**: `DELETE /api/user/address/{id}` - Elimina indirizzo
- **Endpoint**: `PATCH /api/user/address/{id}/default` - Imposta default
- **Endpoint**: `GET /api/user/address/default` - Ottieni default

### UserAddressService
- **Limite**: Max 10 indirizzi per utente
- **Gestione default**: Solo un indirizzo può essere default
- **Validazione**: Controllo esistenza utente

### UserAddress Entity
- **Campi**: `fullName`, `street`, `city`, `state`, `postalCode`, `country`, `phone`, `addressLabel`, `isDefault`
- **Trigger**: `updated_at` automatico via trigger database

---

## ⭐ Wishlist

### WishlistController
- **Endpoint**: `GET /api/wishlist` - Lista wishlist
- **Endpoint**: `POST /api/wishlist` - Aggiungi a wishlist
- **Endpoint**: `DELETE /api/wishlist/{id}` - Rimuovi da wishlist

### WishlistService
- **Funzionalità base**: CRUD semplice
- **Limitazione**: ⚠️ Non filtrato per utente (tutti gli endpoint restituiscono tutte le wishlist)

### Wishlist Entity
- **Campi**: `userId`, `cardId`, `createdAt`
- **Relazioni**: Con `User` e `Card`

---

## 🎴 Collection

### CollectionController
- **Endpoint**: `POST /api/collection` - Crea carta (multipart/form-data)
- **Endpoint**: `GET /api/collection/{userId}` - Collezione utente

### UserCardController
- **Endpoint**: `GET /api/usercards/collection/{userId}` - Collezione utente
- **Endpoint**: `GET /api/usercards/{id}` - Dettaglio carta
- **Endpoint**: `POST /api/usercards` - Aggiungi carta
- **Endpoint**: `PUT /api/usercards/{id}` - Aggiorna carta
- **Endpoint**: `DELETE /api/usercards/{id}` - Elimina carta
- **Endpoint**: `PUT /api/usercards/{id}/raw-images` - Upload immagini (multipart)

### UserCard Entity
- **Campi grading**: `gradeService`, `gradeOverall`, `gradeLabel`, `gradedAt`, `subgrades` (JSONB)
- **Campi immagini**: `frontImage`, `backImage`
- **Source**: `CardSource` enum (GRADELENS, MANUAL, etc.)
- **Permanent**: Flag per carte temporanee (cleanup dopo 30 giorni)

### R2Service Integration
- **Upload immagini**: `uploadUserCardFile()` per front/back images
- **Path**: `usercards/{userCardId}/{slot}-{filename}`

---

## 📝 Grading System

### GradingController
- **Endpoint**: `POST /api/grading/submit` - Invia per grading
- **Endpoint**: `PATCH /api/grading/{cardId}/status` - Aggiorna stato
- **Endpoint**: `POST /api/grading/{cardId}/failed` - Marca fallito
- **Endpoint**: `POST /api/grading/{cardId}/completed` - Marca completato

### GradingService
- **Gestione richieste**: Crea `GradingRequest` e notifica admin
- **Stati**: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`
- **Notifiche**: Admin notificato quando nuova richiesta creata

### GradingRequest Entity
- **Campi**: `userId`, `cardId`, `status`, `submittedAt`, `completedAt`, `errorMessage`
- **Relazioni**: Con `User` e `UserCard`

---

## 📈 Trends

### TrendController
- **Endpoint**: `GET /api/trends/{rangeType}/{itemName}`
- **Query Params**: `category` (default: "card")
- **Range Types**: `daily`, `weekly`, `monthly`, `yearly`

### TrendService
- **Calcolo trend**: Basato su `MarketValuation` storici
- **Aggregazione**: Media, min, max per periodo
- **Categoria**: Filtra per categoria (card, box, etc.)

### TrendDTO
- **Campi**: `itemName`, `category`, `rangeType`, `dataPoints[]`, `average`, `min`, `max`, `trend` (UP/DOWN/STABLE)

---

## 🔍 Analisi Completa Stato Progetto

### ✅ Completamente Implementato
1. **Multi-Valuta**: Sistema completo con 38 valute, conversione automatica, cron refresh
2. **Sistema Admin**: Notifiche real-time, dashboard, supporto, logging
3. **Sicurezza**: JWT, ruoli, bypass cron, CORS
4. **GDPR**: Eliminazione account, logging consensi
5. **Traduzione**: 31 lingue, GPT+DeepL, traduzione automatica chat/support
6. **Chat/Support**: Sistema completo con SSE real-time
7. **Payment Methods**: CRUD completo con validazione Luhn
8. **User Addresses**: CRUD completo con gestione default
9. **Franchise**: Sistema completo con proposte e approvazioni
10. **Marketplace Valuation**: Calcolo automatico con refresh incrementale

### ⚠️ Parzialmente Implementato
1. **GradeLens**: Completamente mock, nessuna analisi reale
2. **Wishlist**: CRUD base ma non filtrato per utente
3. **Collection**: Upload funzionante ma ricerca/filtri limitati
4. **Trends**: Calcolo presente ma non integrato nel frontend
5. **Marketplace Cache**: TTL 25s ma nessuna invalidazione manuale

### ❌ Non Implementato / Placeholder
1. **GradeLens AI**: Nessun modello ML, nessun processing immagini
2. **Marketplace Search Avanzato**: Filtri e ricerca limitati
3. **Notifiche Utente**: Sistema presente ma non utilizzato
4. **WebSocket**: Configurato ma non utilizzato (usa solo SSE)

---

**Documento generato automaticamente**  
**Data**: 29 Novembre 2025  
**Versione**: 2.0 (Completo)

