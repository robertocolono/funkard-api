# 📚 Struttura Progetto e Funzionalità - Funkard API

**Data:** 2025-01-15  
**Versione:** 0.0.1-SNAPSHOT  
**Framework:** Spring Boot 3.5.6  
**Java:** 17

---

## 📋 Indice

1. [Panoramica Generale](#panoramica-generale)
2. [Struttura Directory](#struttura-directory)
3. [Moduli e Componenti](#moduli-e-componenti)
4. [Funzionalità Implementate](#funzionalità-implementate)
5. [Architettura](#architettura)
6. [Configurazione](#configurazione)

---

## 🎯 Panoramica Generale

**Funkard API** è un backend Spring Boot per una piattaforma di marketplace di carte collezionabili con funzionalità avanzate di:
- Gestione collezioni utente
- Sistema di grading AI (GradeLens)
- Marketplace con valutazioni di mercato
- Sistema di supporto ticket con chat real-time
- Pannello admin completo
- Notifiche real-time (SSE)
- Gestione pagamenti e indirizzi

---

## 📁 Struttura Directory

```
funkard-api/
├── src/main/java/com/funkard/
│   ├── admin/                    # Modulo Admin Panel
│   │   ├── controller/           # 15 controller admin
│   │   ├── dto/                  # 10 DTO admin
│   │   ├── log/                  # Sistema logging azioni admin
│   │   ├── model/                # 6 modelli admin
│   │   ├── notification/        # Sistema notifiche admin
│   │   ├── repository/           # 4 repository admin
│   │   ├── service/              # 12 servizi admin
│   │   ├── system/               # Manutenzione sistema
│   │   └── util/                 # Utility admin
│   │
│   ├── adminaccess/              # Gestione accessi admin
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── adminauth/                # Autenticazione admin
│   │   ├── AccessRequest.java
│   │   ├── AdminAccessRequest.java
│   │   ├── AdminAccessToken.java
│   │   ├── AdminAuthController.java
│   │   ├── AdminBootstrap.java
│   │   ├── AdminToken.java
│   │   ├── AdminUser.java
│   │   └── ...
│   │
│   ├── common/                    # Componenti comuni
│   │   └── GlobalExceptionHandler.java
│   │
│   ├── config/                    # Configurazioni
│   │   ├── R2Config.java         # Cloudflare R2 storage
│   │   ├── SecurityConfig.java   # Sicurezza e CORS
│   │   └── WebSocketConfig.java  # WebSocket/SSE
│   │
│   ├── controller/                # Controller pubblici/utente
│   │   ├── AdminSupportSseController.java
│   │   ├── AdminTicketAssignmentController.java
│   │   ├── AdsController.java
│   │   ├── AuthController.java
│   │   ├── CardController.java
│   │   ├── CollectionController.java
│   │   ├── GradeLensAdminController.java
│   │   ├── ListingController.java
│   │   ├── RootController.java
│   │   ├── SupportChatController.java
│   │   ├── SupportController.java
│   │   ├── SupportSseController.java
│   │   ├── SupportWebSocketController.java
│   │   ├── TestController.java
│   │   ├── TransactionController.java
│   │   ├── UserCardController.java
│   │   ├── UserController.java
│   │   └── WishlistController.java
│   │
│   ├── dto/                       # Data Transfer Objects
│   │   ├── CardDTO.java
│   │   ├── ListingDTO.java
│   │   ├── TransactionDTO.java
│   │   ├── UserDTO.java
│   │   ├── UserProfileDTO.java
│   │   └── WishlistDTO.java
│   │
│   ├── gradelens/                 # Sistema AI Grading
│   │   ├── controller/
│   │   ├── model/
│   │   ├── service/
│   │   ├── GradeResult.java
│   │   └── HeuristicAiProvider.java
│   │
│   ├── grading/                   # Gestione grading
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── maintenance/               # Manutenzione
│   │   └── GradeReportCleanup.java
│   │
│   ├── market/                    # Marketplace
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── service/
│   │   └── trend/                # Analisi trend
│   │
│   ├── model/                     # Modelli entità
│   │   ├── Card.java
│   │   ├── CardSource.java
│   │   ├── CardType.java
│   │   ├── GradeLensResult.java
│   │   ├── GradeReport.java
│   │   ├── Listing.java
│   │   ├── Transaction.java
│   │   ├── User.java
│   │   ├── UserAddress.java
│   │   ├── UserCard.java
│   │   ├── VerificationToken.java
│   │   └── Wishlist.java
│   │
│   ├── payload/                   # Payload richieste
│   │   └── RegisterRequest.java
│   │
│   ├── realtime/                  # Sistema real-time
│   │   ├── AdminStreamController.java
│   │   ├── EventType.java
│   │   ├── RealtimeConfig.java
│   │   └── SupportStreamController.java
│   │
│   ├── repository/                # Repository JPA
│   │   ├── CardRepository.java
│   │   ├── GradeLensRepository.java
│   │   ├── GradeReportRepository.java
│   │   ├── ListingRepository.java
│   │   ├── TransactionRepository.java
│   │   ├── UserAddressRepository.java
│   │   ├── UserCardRepository.java
│   │   ├── UserRepository.java
│   │   ├── VerificationTokenRepository.java
│   │   └── WishlistRepository.java
│   │
│   ├── scheduler/                 # Job schedulati
│   │   └── GradeCleanupScheduler.java
│   │
│   ├── security/                  # Sicurezza
│   │   ├── JwtFilter.java
│   │   └── JwtUtil.java
│   │
│   ├── service/                   # Servizi business logic
│   │   ├── AdminNotifier.java
│   │   ├── CardService.java
│   │   ├── EmailService.java
│   │   ├── GradeCalculator.java
│   │   ├── GradeLensCleanupService.java
│   │   ├── GradeReportLookupService.java
│   │   ├── ListingService.java
│   │   ├── R2Service.java
│   │   ├── TransactionService.java
│   │   ├── UserAddressService.java
│   │   ├── UserService.java
│   │   └── WishlistService.java
│   │
│   ├── storage/                   # Storage
│   │   └── ImageStorageService.java
│   │
│   ├── user/                      # Modulo utente
│   │   └── payment/               # Gestione pagamenti
│   │       └── [6 file]
│   │
│   └── FunkardApiApplication.java # Entry point
│
├── src/main/resources/
│   ├── application.properties     # Config principale
│   ├── application-dev.properties # Config sviluppo
│   ├── application-prod.yml       # Config produzione
│   ├── application-test.properties # Config test
│   ├── db/                        # Script SQL
│   │   ├── migration/            # Migrazioni Flyway
│   │   └── *.sql                 # Script inizializzazione
│   └── static/                   # File statici
│
├── backend/sql/migrations/         # Migrazioni database
├── docs/                          # Documentazione
├── logs/                          # Log applicazione
├── target/                        # Build output
├── Dockerfile                     # Container Docker
├── Makefile                       # Comandi build
├── pom.xml                        # Dipendenze Maven
└── render.yaml                    # Configurazione Render.com

```

---

## 🧩 Moduli e Componenti

### 1. **Modulo Autenticazione** (`com.funkard.controller.AuthController`)

**Funzionalità:**
- Registrazione utenti con verifica email
- Login con JWT
- Validazione token
- Gestione sessioni stateless

**Endpoint:**
- `POST /api/auth/register` - Registrazione
- `POST /api/auth/login` - Login
- `GET /api/auth/validate?token={token}` - Validazione token

---

### 2. **Modulo Utente** (`com.funkard.controller.UserController`)

**Funzionalità:**
- Gestione profilo utente
- CRUD indirizzi utente
- Gestione metodi di pagamento
- Preferenze utente (valuta preferita)

**Endpoint:**
- `GET /api/user/me` - Profilo corrente
- `PUT /api/user/me` - Aggiorna profilo
- `GET /api/user/address` - Lista indirizzi
- `POST /api/user/address` - Aggiungi indirizzo
- `PUT /api/user/address/{id}` - Aggiorna indirizzo
- `DELETE /api/user/address/{id}` - Elimina indirizzo
- `PATCH /api/user/address/{id}/default` - Imposta default
- `GET /api/user/address/default` - Indirizzo predefinito

---

### 3. **Modulo Pagamenti** (`com.funkard.user.payment`)

**Funzionalità:**
- Gestione metodi di pagamento
- Validazione carte di credito
- Impostazione metodo predefinito
- Pulizia automatica metodi scaduti

**Endpoint:**
- `GET /api/user/payments` - Lista metodi
- `POST /api/user/payments` - Aggiungi metodo
- `DELETE /api/user/payments/{id}` - Rimuovi metodo
- `PATCH /api/user/payments/{id}/default` - Imposta default
- `GET /api/user/payments/default` - Metodo predefinito
- `GET /api/user/payments/stats` - Statistiche
- `POST /api/user/payments/cleanup` - Pulizia scaduti
- `POST /api/user/payments/validate` - Valida senza salvare

---

### 4. **Modulo Collezione** (`com.funkard.controller.CollectionController`)

**Funzionalità:**
- Upload carte con immagini
- Gestione collezione utente
- Integrazione con Cloudflare R2 storage
- Gestione UserCard con grading

**Endpoint:**
- `POST /api/collection` - Crea carta (multipart/form-data)
- `GET /api/collection/{userId}` - Collezione utente

**Componenti:**
- `UserCardController` - CRUD carte utente
- `ImageStorageService` - Upload immagini R2
- `R2Service` - Integrazione Cloudflare R2

---

### 5. **Modulo GradeLens** (`com.funkard.gradelens`)

**Funzionalità:**
- Analisi AI automatica delle carte
- Calcolo grading con OpenCV
- Stima valore basata su grading
- Storico analisi

**Endpoint:**
- `POST /api/gradelens/analyze` - Analisi AI carta
- `POST /api/gradelens/confirm` - Conferma grading

**Componenti:**
- `HeuristicAiProvider` - Provider AI grading
- `GradeResult` - Risultato analisi
- `GradeCalculator` - Calcolo grading
- `GradeLensCleanupService` - Pulizia risultati vecchi

---

### 6. **Modulo Grading** (`com.funkard.grading`)

**Funzionalità:**
- Invio carte per grading professionale
- Tracking stato grading
- Gestione report grading
- Integrazione con servizi esterni

**Endpoint:**
- `POST /api/grading/submit` - Invia per grading
- `PATCH /api/grading/{cardId}/status` - Aggiorna stato
- `POST /api/grading/{cardId}/failed` - Marca fallito
- `POST /api/grading/{cardId}/completed` - Marca completato

**Componenti:**
- `GradeReport` - Modello report
- `GradeReportRepository` - Persistenza
- `GradeReportLookupService` - Ricerca report

---

### 7. **Modulo Marketplace** (`com.funkard.market`)

**Funzionalità:**
- Listings (annunci vendita)
- Transazioni
- Valutazioni di mercato
- Analisi trend prezzi

**Endpoint:**
- `GET /api/listings` - Lista annunci
- `POST /api/listings` - Crea annuncio
- `GET /api/transactions` - Lista transazioni
- `POST /api/transactions` - Crea transazione
- `POST /api/valuation/get` - Valutazione mercato
- `GET /api/trends/{rangeType}/{itemName}` - Trend prezzi

**Componenti:**
- `ListingService` - Business logic annunci
- `TransactionService` - Gestione transazioni
- `MarketTrendService` - Analisi trend

---

### 8. **Modulo Supporto** (`com.funkard.controller.SupportController`)

**Funzionalità:**
- Sistema ticket completo
- Chat real-time (SSE)
- Assegnazione ticket
- Tracking stato ticket

**Endpoint:**
- `POST /api/support/tickets` - Crea ticket
- `GET /api/support/tickets?email={email}` - Ticket utente
- `GET /api/support/tickets/{id}` - Dettaglio ticket
- `POST /api/support/tickets/{id}/reply` - Rispondi
- `POST /api/support/tickets/{id}/reopen` - Riapri
- `GET /api/support/stats?email={email}` - Statistiche
- `GET /api/support/stream` - SSE real-time

**Componenti:**
- `SupportTicketService` - Business logic ticket
- `SupportMessageService` - Gestione messaggi
- `SupportStreamController` - SSE per utenti
- `SupportChatController` - API chat

---

### 9. **Modulo Admin Panel** (`com.funkard.admin`)

**Funzionalità:**
- Dashboard aggregata
- Gestione notifiche real-time
- Gestione ticket supporto
- Statistiche sistema
- Sistema di ruoli e permessi
- Logging azioni admin

**Controller Admin:**
1. `AdminAuthController` - Autenticazione admin
2. `AdminNotificationController` - Notifiche
3. `AdminNotificationStreamController` - SSE notifiche
4. `AdminNotificationActionController` - Azioni notifiche
5. `AdminNotificationArchiveController` - Archivio
6. `AdminNotificationBatchController` - Operazioni batch
7. `AdminNotificationCleanupController` - Pulizia
8. `AdminSupportController` - Supporto admin
9. `AdminStatsController` - Statistiche
10. `AdminDashboardController` - Dashboard
11. `AdminValuationController` - Valutazioni
12. `AdminCleanupController` - Cleanup sistema
13. `RolePermissionController` - Ruoli e permessi
14. `SupportTicketController` - Ticket admin
15. `AdminLegacyAuthController` - Auth legacy

**Endpoint Principali:**
- `/api/admin/auth/**` - Autenticazione
- `/api/admin/notifications/**` - Notifiche
- `/api/admin/support/**` - Supporto
- `/api/admin/stats` - Statistiche
- `/api/admin/dashboard` - Dashboard
- `/api/admin/valuation/**` - Valutazioni
- `/api/admin/logs/**` - Log sistema

---

### 10. **Modulo Autenticazione Admin** (`com.funkard.adminauth`)

**Funzionalità:**
- Sistema token-based per admin
- Gestione utenti admin
- Richieste di accesso
- Ruoli: SUPER_ADMIN, ADMIN, SUPPORT

**Componenti:**
- `AdminUser` - Modello utente admin
- `AdminToken` - Token admin
- `AdminAccessRequest` - Richieste accesso
- `AdminAccessToken` - Token accesso
- `AdminAuthController` - API autenticazione
- `AdminBootstrap` - Inizializzazione admin

---

### 11. **Modulo Real-Time** (`com.funkard.realtime`)

**Funzionalità:**
- Server-Sent Events (SSE) per notifiche real-time
- WebSocket fallback (SockJS)
- Eventi filtrati per ruolo/utente
- Keep-alive automatico

**Componenti:**
- `RealtimeConfig` - Configurazione SSE/WebSocket
- `EventType` - Enum tipi evento
- `SupportStreamController` - SSE utenti
- `AdminStreamController` - SSE admin

**Eventi Supportati:**
- `NEW_TICKET` - Nuovo ticket
- `NEW_REPLY` - Nuova risposta
- `TICKET_STATUS` - Cambio stato
- `TICKET_ASSIGNED` - Ticket assegnato
- `TICKET_RESOLVED` - Ticket risolto
- `TICKET_CLOSED` - Ticket chiuso
- `NOTIFICATION` - Notifica generica
- `PING` - Keep-alive

---

### 12. **Modulo Storage** (`com.funkard.storage`)

**Funzionalità:**
- Upload immagini su Cloudflare R2
- Gestione file utente
- CDN integration

**Componenti:**
- `R2Config` - Configurazione R2
- `R2Service` - Servizio R2
- `ImageStorageService` - Gestione immagini

---

### 13. **Modulo Sicurezza** (`com.funkard.security`)

**Funzionalità:**
- JWT authentication
- Password encryption (BCrypt)
- CORS configuration
- Role-based access control

**Componenti:**
- `JwtFilter` - Filtro JWT
- `JwtUtil` - Utility JWT
- `SecurityConfig` - Configurazione sicurezza

**CORS Origins:**
- `https://funkard.com`
- `https://www.funkard.com`
- `https://admin.funkard.com`
- `http://localhost:3000`
- `http://localhost:3002`

---

### 14. **Modulo Wishlist** (`com.funkard.controller.WishlistController`)

**Funzionalità:**
- Gestione wishlist utente
- Tracking carte desiderate

**Endpoint:**
- `GET /api/wishlist` - Lista wishlist
- `POST /api/wishlist` - Aggiungi
- `DELETE /api/wishlist/{id}` - Rimuovi

---

### 15. **Modulo Manutenzione** (`com.funkard.maintenance`)

**Funzionalità:**
- Cleanup automatico dati vecchi
- Schedulazione job
- Manutenzione database

**Componenti:**
- `GradeReportCleanup` - Pulizia report
- `GradeCleanupScheduler` - Scheduler cleanup

---

## 🚀 Funzionalità Implementate

### ✅ **Autenticazione e Autorizzazione**

1. **Registrazione Utenti**
   - Validazione email
   - Hash password BCrypt
   - Token verifica email
   - Gestione sessioni JWT

2. **Login e JWT**
   - Generazione token JWT
   - Validazione token
   - Refresh token
   - Expiration configurabile

3. **Autenticazione Admin**
   - Sistema token-based
   - Ruoli multipli (SUPER_ADMIN, ADMIN, SUPPORT)
   - Richieste accesso
   - Gestione team admin

---

### ✅ **Gestione Utenti**

1. **Profilo Utente**
   - CRUD profilo completo
   - Preferenze utente
   - Valuta preferita
   - Immagine profilo

2. **Indirizzi**
   - CRUD indirizzi
   - Indirizzo predefinito
   - Validazione indirizzi

3. **Metodi di Pagamento**
   - CRUD metodi pagamento
   - Validazione carte
   - Metodo predefinito
   - Pulizia automatica scaduti

---

### ✅ **Collezione Carte**

1. **Upload Carte**
   - Upload immagini multipart
   - Storage Cloudflare R2
   - Metadati carte
   - Integrazione con database carte

2. **Gestione Collezione**
   - Visualizzazione collezione
   - Filtri e ricerca
   - Statistiche collezione
   - Organizzazione carte

3. **UserCard**
   - CRUD completo
   - Grading informazioni
   - Valutazioni
   - Immagini multiple

---

### ✅ **Sistema Grading**

1. **GradeLens (AI Grading)**
   - Analisi automatica con OpenCV
   - Calcolo grading (PSA-style)
   - Stima valore
   - Storico analisi

2. **Grading Professionale**
   - Invio per grading
   - Tracking stato
   - Report grading
   - Integrazione servizi esterni

3. **Calcolo Grading**
   - Algoritmi euristici
   - Analisi immagini
   - Confronto standard
   - Validazione risultati

---

### ✅ **Marketplace**

1. **Listings**
   - Creazione annunci
   - Ricerca e filtri
   - Gestione prezzi
   - Immagini annunci

2. **Transazioni**
   - Creazione transazioni
   - Tracking stato
   - Integrazione pagamenti
   - Storico transazioni

3. **Valutazioni Mercato**
   - Calcolo valori
   - Analisi trend
   - Confronti prezzi
   - Aggiornamenti automatici

4. **Trend Analysis**
   - Analisi storico prezzi
   - Grafici trend
   - Previsioni
   - Alert prezzi

---

### ✅ **Sistema Supporto**

1. **Ticket System**
   - Creazione ticket
   - Categorizzazione
   - Priorità
   - Stati (open, in_progress, resolved, closed)

2. **Chat Real-Time**
   - Messaggi in tempo reale
   - SSE per notifiche
   - Fallback polling
   - Storico messaggi

3. **Assegnazione Ticket**
   - Assegnazione a support
   - Lock/unlock ticket
   - Tracking assegnazioni
   - Statistiche assegnazioni

4. **Notifiche Real-Time**
   - SSE per utenti
   - SSE per admin
   - Eventi filtrati
   - Keep-alive automatico

---

### ✅ **Admin Panel**

1. **Dashboard**
   - Statistiche aggregate
   - Metriche sistema
   - Grafici e visualizzazioni
   - Quick actions

2. **Gestione Notifiche**
   - Notifiche real-time (SSE)
   - Filtri e ricerca
   - Azioni batch
   - Archivio notifiche
   - Cleanup automatico

3. **Gestione Supporto**
   - Visualizzazione ticket
   - Assegnazione ticket
   - Risposte ticket
   - Statistiche supporto
   - Stream real-time

4. **Statistiche**
   - Utenti attivi
   - Transazioni
   - Carte e listings
   - Performance sistema

5. **Sistema Ruoli**
   - Gestione ruoli
   - Permessi granulari
   - Team management
   - Audit log

6. **Logging Azioni**
   - Log tutte le azioni admin
   - Storico modifiche
   - Audit trail
   - Ricerca log

---

### ✅ **Sistema Real-Time**

1. **Server-Sent Events (SSE)**
   - Connessioni persistenti
   - Eventi filtrati
   - Keep-alive automatico
   - Gestione disconnessioni

2. **WebSocket (Fallback)**
   - SockJS support
   - STOMP protocol
   - Fallback automatico
   - Gestione connessioni

3. **Eventi Real-Time**
   - Nuovi ticket
   - Nuovi messaggi
   - Cambi stato
   - Assegnazioni
   - Notifiche

---

### ✅ **Storage e File**

1. **Cloudflare R2**
   - Upload immagini
   - CDN integration
   - Gestione file
   - Cleanup automatico

2. **Image Processing**
   - Validazione immagini
   - Ottimizzazione
   - Thumbnails
   - Multiple formats

---

### ✅ **Email e Notifiche**

1. **Email Service**
   - Invio email
   - Template email
   - Verifica email
   - Notifiche sistema

2. **Notifiche Admin**
   - Sistema notifiche
   - Priorità
   - Categorizzazione
   - Archivio

---

### ✅ **Manutenzione e Cleanup**

1. **Schedulazione Job**
   - Cleanup automatico
   - Aggiornamenti periodici
   - Backup dati
   - Manutenzione database

2. **Cleanup Servizi**
   - Pulizia report vecchi
   - Pulizia notifiche archiviate
   - Pulizia token scaduti
   - Pulizia file temporanei

---

## 🏗️ Architettura

### **Pattern Architetturali**

1. **MVC (Model-View-Controller)**
   - Controller per API endpoints
   - Service per business logic
   - Repository per data access

2. **Layered Architecture**
   - Presentation Layer (Controllers)
   - Business Layer (Services)
   - Data Access Layer (Repositories)
   - Model Layer (Entities)

3. **Dependency Injection**
   - Spring IoC container
   - Constructor injection
   - Service autowiring

---

### **Tecnologie Utilizzate**

1. **Framework**
   - Spring Boot 3.5.6
   - Spring Security
   - Spring Data JPA
   - Spring WebSocket

2. **Database**
   - PostgreSQL
   - Flyway migrations
   - JPA/Hibernate

3. **Autenticazione**
   - JWT (jjwt)
   - BCrypt password encoding
   - Role-based access control

4. **Storage**
   - Cloudflare R2 (S3-compatible)
   - AWS SDK v2

5. **Real-Time**
   - Server-Sent Events (SSE)
   - WebSocket (SockJS)
   - STOMP protocol

6. **Image Processing**
   - OpenCV (OpenPNP binding)

7. **Email**
   - Spring Mail
   - SMTP support

8. **Build Tool**
   - Maven
   - Java 17

---

### **Sicurezza**

1. **Autenticazione**
   - JWT tokens
   - Password hashing (BCrypt)
   - Token expiration
   - Refresh tokens

2. **Autorizzazione**
   - Role-based access control
   - Method-level security
   - Endpoint protection
   - Admin token system

3. **CORS**
   - Configurazione specifica origini
   - Credentials support
   - Preflight handling

4. **Validazione**
   - Input validation
   - DTO validation
   - SQL injection prevention
   - XSS protection

---

## ⚙️ Configurazione

### **File di Configurazione**

1. **application.properties**
   - Configurazione principale
   - Database connection
   - JWT settings
   - Email settings

2. **application-dev.properties**
   - Configurazione sviluppo
   - Debug logging
   - Local database

3. **application-prod.yml**
   - Configurazione produzione
   - Production database
   - Security settings

4. **application-test.properties**
   - Configurazione test
   - Test database
   - Mock services

---

### **Variabili d'Ambiente**

- `MAIL_USERNAME` - Email SMTP username
- `MAIL_PASSWORD` - Email SMTP password
- `ADMIN_EMAIL` - Email admin principale
- `ADMIN_TOKEN` - Token admin legacy
- `FUNKARD_CRON_SECRET` - Secret per cron jobs
- `JWT_SECRET` - Secret JWT
- Database credentials

---

### **Database Migrations**

Flyway migrations in `src/main/resources/db/migration/`:
- `V1__add_grading_columns_to_usercard.sql`
- `V2__add_preferred_currency_to_users.sql`
- `V3__create_user_addresses_table.sql`
- `V4__create_admin_tokens_and_access_requests.sql`

---

## 📊 Statistiche Progetto

- **Totale File Java:** ~200+
- **Controller:** 30+
- **Service:** 20+
- **Repository:** 15+
- **Model/Entity:** 15+
- **DTO:** 15+
- **Endpoint API:** 150+
- **Endpoint Autenticati:** 80+
- **Endpoint Pubblici:** 20+
- **Endpoint Admin:** 60+
- **Endpoint SSE:** 4
- **Endpoint WebSocket:** 1

---

## 🔄 Flussi Principali

### **1. Registrazione Utente**
```
POST /api/auth/register
  → Validazione dati
  → Hash password
  → Creazione utente
  → Generazione token verifica
  → Invio email verifica
  → Ritorno JWT token
```

### **2. Upload Carta**
```
POST /api/collection
  → Validazione file
  → Upload immagini R2
  → Creazione UserCard
  → Salvataggio metadati
  → Ritorno carta creata
```

### **3. Analisi GradeLens**
```
POST /api/gradelens/analyze
  → Caricamento immagini
  → Analisi OpenCV
  → Calcolo grading
  → Stima valore
  → Salvataggio risultato
  → Ritorno GradeResult
```

### **4. Creazione Ticket Supporto**
```
POST /api/support/tickets
  → Creazione ticket
  → Notifica admin (SSE)
  → Notifica utente (SSE)
  → Salvataggio database
  → Ritorno ticket creato
```

### **5. Messaggio Chat Real-Time**
```
POST /api/support/chat/{ticketId}/message
  → Creazione messaggio
  → Aggiornamento ticket
  → Notifica real-time (SSE)
  → Salvataggio database
  → Ritorno messaggio
```

---

## 📝 Note Finali

### **Punti di Forza**
- ✅ Architettura modulare e scalabile
- ✅ Sistema real-time completo (SSE + WebSocket)
- ✅ Sicurezza robusta (JWT + RBAC)
- ✅ Admin panel completo
- ✅ Integrazione storage cloud (R2)
- ✅ Sistema grading AI avanzato
- ✅ Documentazione completa

### **Aree di Miglioramento**
- ⚠️ Paginazione standardizzata
- ⚠️ Validazione input completa
- ⚠️ Response format unificato
- ⚠️ OpenAPI/Swagger documentation
- ⚠️ Test coverage
- ⚠️ Rate limiting

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

