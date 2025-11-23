# 📚 Struttura Completa File e Funzionalità - Funkard API

**Data:** 2025-01-15  
**Versione:** 1.0  
**Framework:** Spring Boot 3.5.6  
**Java:** 21

---

## 📋 Indice

1. [Struttura Directory Completa](#struttura-directory-completa)
2. [Modelli (Entities)](#modelli-entities)
3. [Repository](#repository)
4. [Service](#service)
5. [Controller](#controller)
6. [DTO](#dto)
7. [Migration Database](#migration-database)
8. [Funzionalità Implementate](#funzionalità-implementate)
9. [Endpoint API Completi](#endpoint-api-completi)

---

## 📁 Struttura Directory Completa

```
funkard-api/
├── src/main/java/com/funkard/
│   ├── FunkardApiApplication.java          # Entry point applicazione
│   │
│   ├── admin/                              # 🎛️ MODULO ADMIN PANEL
│   │   ├── AdminConfig.java
│   │   ├── AdminController.java
│   │   ├── AdminService.java
│   │   │
│   │   ├── controller/                    # 21 Controller Admin
│   │   │   ├── AdminCleanupController.java
│   │   │   ├── AdminDashboardController.java
│   │   │   ├── AdminEmailLogController.java
│   │   │   ├── AdminFranchiseController.java
│   │   │   ├── AdminLegacyAuthController.java
│   │   │   ├── AdminNotificationActionController.java
│   │   │   ├── AdminNotificationArchiveController.java
│   │   │   ├── AdminNotificationBatchController.java
│   │   │   ├── AdminNotificationCleanupController.java
│   │   │   ├── AdminNotificationController.java
│   │   │   ├── AdminNotificationStreamController.java
│   │   │   ├── AdminPendingValueController.java
│   │   │   ├── AdminStatsController.java
│   │   │   ├── AdminSupportCleanupController.java
│   │   │   ├── AdminSupportController.java
│   │   │   ├── AdminValuationController.java
│   │   │   ├── CookieLogAdminController.java
│   │   │   ├── EmailTemplateTestController.java
│   │   │   ├── FranchiseAdminController.java
│   │   │   ├── RolePermissionController.java
│   │   │   └── SupportTicketController.java
│   │   │
│   │   ├── dto/                           # 10 DTO Admin
│   │   │   ├── AdminDashboardDTO.java
│   │   │   ├── AdminStatsDTO.java
│   │   │   ├── DashboardDTO.java
│   │   │   ├── MarketOverviewDTO.java
│   │   │   ├── NotificationDTO.java
│   │   │   ├── PendingItemDTO.java
│   │   │   ├── SupportMessageDTO.java
│   │   │   ├── SupportStatsDTO.java
│   │   │   ├── SupportTicketDTO.java
│   │   │   └── TicketDTO.java
│   │   │
│   │   ├── log/                           # Sistema Logging Admin
│   │   │   ├── AdminActionLog.java
│   │   │   ├── AdminActionLogController.java
│   │   │   ├── AdminActionLogger.java
│   │   │   └── AdminActionLogRepository.java
│   │   │
│   │   ├── model/                         # 6 Modelli Admin
│   │   │   ├── AdminNotification.java
│   │   │   ├── SupportMessage.java
│   │   │   ├── SupportTicket.java
│   │   │   ├── SystemCleanupLog.java
│   │   │   ├── TicketStatus.java
│   │   │   └── UserRole.java
│   │   │
│   │   ├── repository/                    # 4 Repository Admin
│   │   │   ├── AdminNotificationRepository.java
│   │   │   ├── SupportMessageRepository.java
│   │   │   ├── SupportTicketRepository.java
│   │   │   └── SystemCleanupLogRepository.java
│   │   │
│   │   ├── service/                        # 12 Servizi Admin
│   │   │   ├── AdminDashboardService.java
│   │   │   ├── AdminNotificationCleanupService.java
│   │   │   ├── AdminNotificationService.java
│   │   │   ├── AdminStatsService.java
│   │   │   ├── AdminSupportService.java
│   │   │   ├── AdminValuationService.java
│   │   │   ├── RolePermissionService.java
│   │   │   ├── SupportCleanupService.java
│   │   │   ├── SupportMessageService.java
│   │   │   ├── SupportService.java
│   │   │   ├── SupportTicketService.java
│   │   │   └── SystemCleanupService.java
│   │   │
│   │   ├── system/
│   │   │   └── SystemMaintenanceController.java
│   │   │
│   │   └── util/
│   │       └── AdminAuthHelper.java
│   │
│   ├── adminaccess/                        # 🔐 Gestione Accessi Admin
│   │   ├── controller/
│   │   │   └── AdminAccessController.java
│   │   ├── model/
│   │   │   ├── AdminAccessRequest.java
│   │   │   └── AdminAccessToken.java
│   │   ├── repository/
│   │   │   ├── AdminAccessRequestRepository.java
│   │   │   └── AdminAccessTokenRepository.java
│   │   └── service/
│   │       └── AdminAccessService.java
│   │
│   ├── adminauth/                          # 🔑 Autenticazione Admin
│   │   ├── AccessRequest.java
│   │   ├── AccessRequestController.java
│   │   ├── AccessRequestRepository.java
│   │   ├── AccessRequestService.java
│   │   ├── AdminAccessRequest.java
│   │   ├── AdminAccessRequestRepository.java
│   │   ├── AdminAccessToken.java
│   │   ├── AdminAccessTokenRepository.java
│   │   ├── AdminAuthController.java
│   │   ├── AdminBootstrap.java
│   │   ├── AdminTableInitializer.java
│   │   ├── AdminToken.java
│   │   ├── AdminTokenController.java
│   │   ├── AdminTokenRepository.java
│   │   ├── AdminTokenService.java
│   │   ├── AdminUser.java
│   │   ├── AdminUserRepository.java
│   │   └── AdminUserService.java
│   │
│   ├── common/                             # 🌐 Componenti Comuni
│   │   └── GlobalExceptionHandler.java
│   │
│   ├── config/                             # ⚙️ Configurazioni
│   │   ├── EmailConfig.java
│   │   ├── R2Config.java                   # Cloudflare R2 Storage
│   │   ├── SecurityConfig.java             # Sicurezza e CORS
│   │   └── WebSocketConfig.java            # WebSocket/SSE
│   │
│   ├── controller/                         # 🌍 CONTROLLER PUBBLICI/UTENTE (21 file)
│   │   ├── AdminSupportSseController.java
│   │   ├── AdminTicketAssignmentController.java
│   │   ├── AdsController.java
│   │   ├── AuthController.java             # Autenticazione
│   │   ├── CardController.java             # Gestione carte
│   │   ├── ChatController.java             # Chat tra utenti
│   │   ├── CollectionController.java       # Collezioni
│   │   ├── FranchiseController.java        # Franchise pubblici
│   │   ├── GradeLensAdminController.java   # GradeLens admin
│   │   ├── ListingController.java          # Annunci marketplace
│   │   ├── PendingValueController.java     # Proposte valori custom
│   │   ├── RootController.java             # Root endpoint
│   │   ├── SupportChatController.java      # Chat supporto
│   │   ├── SupportController.java          # Ticket supporto
│   │   ├── SupportSseController.java       # SSE supporto
│   │   ├── SupportWebSocketController.java  # WebSocket supporto
│   │   ├── TestController.java             # Test endpoint
│   │   ├── TransactionController.java      # Transazioni
│   │   ├── UserCardController.java          # Carte utente
│   │   ├── UserController.java             # Profilo utente
│   │   └── WishlistController.java         # Wishlist
│   │
│   ├── dto/                                # 📦 DATA TRANSFER OBJECTS (17 file)
│   │   ├── CardDTO.java
│   │   ├── ChatMessageDTO.java
│   │   ├── CookiePreferencesDTO.java
│   │   ├── CreateFranchiseRequest.java
│   │   ├── CreateListingRequest.java
│   │   ├── FranchiseDTO.java
│   │   ├── FranchiseProposalDTO.java
│   │   ├── ListingDTO.java
│   │   ├── LoginResponse.java
│   │   ├── PendingValueDTO.java
│   │   ├── ProposeFranchiseRequest.java
│   │   ├── SubmitPendingValueRequest.java
│   │   ├── TransactionDTO.java
│   │   ├── UserDTO.java
│   │   ├── UserPreferencesDTO.java
│   │   ├── UserProfileDTO.java
│   │   └── WishlistDTO.java
│   │
│   ├── gradelens/                          # 🔍 Sistema GradeLens (AI Grading)
│   │   ├── controller/
│   │   │   └── GradeLensController.java
│   │   ├── GradeResult.java
│   │   ├── HeuristicAiProvider.java
│   │   ├── model/
│   │   │   ├── GradeLensResult.java
│   │   │   ├── GradeReport.java
│   │   │   └── ...
│   │   └── service/
│   │       └── GradeLensService.java
│   │
│   ├── grading/                            # 📊 Sistema Grading
│   │   ├── controller/
│   │   │   └── GradingController.java
│   │   ├── model/
│   │   │   └── GradeReport.java
│   │   ├── repository/
│   │   │   └── GradeReportRepository.java
│   │   └── service/
│   │       └── GradingService.java
│   │
│   ├── maintenance/                        # 🔧 Manutenzione
│   │   └── GradeReportCleanup.java
│   │
│   ├── market/                             # 🛒 MODULO MARKETPLACE
│   │   ├── controller/
│   │   │   ├── MarketController.java
│   │   │   └── ProductController.java
│   │   ├── model/
│   │   │   ├── Listing.java
│   │   │   ├── Product.java
│   │   │   └── Transaction.java
│   │   ├── repository/
│   │   │   ├── ListingRepository.java
│   │   │   ├── ProductRepository.java
│   │   │   └── TransactionRepository.java
│   │   ├── service/
│   │   │   ├── ListingService.java
│   │   │   ├── MarketService.java
│   │   │   ├── ProductService.java
│   │   │   └── TransactionService.java
│   │   └── trend/
│   │       ├── MarketTrendAnalyzer.java
│   │       ├── PriceHistoryService.java
│   │       └── TrendData.java
│   │
│   ├── model/                              # 📊 MODELLI (ENTITIES) (22 file)
│   │   ├── Card.java
│   │   ├── CardSource.java
│   │   ├── CardType.java
│   │   ├── ChatMessage.java
│   │   ├── CookieConsentLog.java
│   │   ├── dto/
│   │   │   └── GradeRequest.java
│   │   ├── EmailLog.java
│   │   ├── Franchise.java
│   │   ├── FranchiseCatalog.java
│   │   ├── FranchiseProposal.java
│   │   ├── GradeLensResult.java
│   │   ├── GradeReport.java
│   │   ├── Listing.java
│   │   ├── PendingValue.java
│   │   ├── Transaction.java
│   │   ├── TranslationLog.java
│   │   ├── User.java
│   │   ├── UserAddress.java
│   │   ├── UserCard.java
│   │   ├── UserDeletion.java
│   │   ├── UserPreferences.java
│   │   ├── VerificationToken.java
│   │   └── Wishlist.java
│   │
│   ├── payload/                            # 📝 Payload Request
│   │   └── RegisterRequest.java
│   │
│   ├── realtime/                           # ⚡ SISTEMA REAL-TIME
│   │   ├── AdminStreamController.java
│   │   ├── EventType.java
│   │   ├── RealtimeConfig.java
│   │   └── SupportStreamController.java
│   │
│   ├── repository/                         # 💾 REPOSITORY (20 file)
│   │   ├── CardRepository.java
│   │   ├── ChatMessageRepository.java
│   │   ├── CookieConsentLogRepository.java
│   │   ├── EmailLogRepository.java
│   │   ├── FranchiseCatalogRepository.java
│   │   ├── FranchiseProposalRepository.java
│   │   ├── FranchiseRepository.java
│   │   ├── GradeReportRepository.java
│   │   ├── ListingRepository.java
│   │   ├── PendingValueRepository.java
│   │   ├── TranslationLogRepository.java
│   │   ├── TransactionRepository.java
│   │   ├── UserAddressRepository.java
│   │   ├── UserCardRepository.java
│   │   ├── UserDeletionRepository.java
│   │   ├── UserPreferencesRepository.java
│   │   ├── UserRepository.java
│   │   ├── VerificationTokenRepository.java
│   │   └── WishlistRepository.java
│   │
│   ├── scheduler/                          # ⏰ SCHEDULER (JOBS)
│   │   ├── EmailLogCleanupScheduler.java
│   │   ├── GradeCleanupScheduler.java
│   │   └── UserDeletionScheduler.java
│   │
│   ├── security/                           # 🔒 SICUREZZA
│   │   ├── JwtFilter.java
│   │   └── JwtUtil.java
│   │
│   ├── service/                             # 🔧 SERVIZI (27 file)
│   │   ├── AdminNotifier.java
│   │   ├── CardService.java
│   │   ├── ChatService.java
│   │   ├── CookieConsentLogService.java
│   │   ├── CookieLogExportService.java
│   │   ├── EmailLocaleHelper.java
│   │   ├── EmailLogService.java
│   │   ├── EmailService.java
│   │   ├── EmailTemplateManager.java
│   │   ├── EmailTemplateTestService.java
│   │   ├── FranchiseAdminService.java
│   │   ├── FranchiseCatalogService.java
│   │   ├── FranchiseJsonService.java
│   │   ├── GradeCalculator.java
│   │   ├── GradeLensCleanupService.java
│   │   ├── GradeReportLookupService.java
│   │   ├── ListingService.java
│   │   ├── PendingValueService.java
│   │   ├── R2Service.java                  # Cloudflare R2 Storage
│   │   ├── TransactionService.java
│   │   ├── TranslationService.java
│   │   ├── UserAccountDeletionService.java
│   │   ├── UserAddressService.java
│   │   ├── UserDeletionService.java
│   │   ├── UserPreferencesService.java
│   │   ├── UserService.java
│   │   └── WishlistService.java
│   │
│   ├── storage/                            # 💿 Storage
│   │   └── ImageStorageService.java
│   │
│   └── user/                               # 👤 Modulo Utente
│       └── payment/
│           ├── PaymentController.java
│           ├── PaymentService.java
│           └── ...
│
└── src/main/resources/
    ├── application.properties
    ├── application-dev.properties
    ├── application-prod.yml
    ├── application-test.properties
    ├── data/
    │   └── franchises.json                 # File JSON franchise
    ├── db/migration/                       # 🗄️ FLYWAY MIGRATIONS (20 file)
    │   ├── V1__add_grading_columns_to_usercard.sql
    │   ├── V2__add_preferred_currency_to_users.sql
    │   ├── V3__create_user_addresses_table.sql
    │   ├── V4__create_admin_tokens_and_access_requests.sql
    │   ├── V5__add_gdpr_consent_timestamps_to_users.sql
    │   ├── V6__create_user_preferences_table.sql
    │   ├── V7__create_cookie_consent_logs_table.sql
    │   ├── V8__update_cookie_consent_logs_add_action_useragent.sql
    │   ├── V9__create_user_deletions_table.sql
    │   ├── V10__add_deletion_fields_to_users.sql
    │   ├── V11__create_email_logs_table.sql
    │   ├── V12__add_language_to_users.sql
    │   ├── V13__add_translation_fields_to_support_messages.sql
    │   ├── V14__create_chat_messages_table.sql
    │   ├── V15__create_translation_logs_table.sql
    │   ├── V16__create_pending_values_table.sql
    │   ├── V17__add_category_franchise_to_cards.sql
    │   ├── V18__create_franchise_catalog_table.sql
    │   ├── V19__create_franchises_table.sql
    │   └── V20__create_franchise_proposals_table.sql
    └── email-templates/                    # 📧 Template Email Multi-lingua
        ├── it/
        ├── en/
        └── ...
```

---

## 📊 Modelli (Entities)

### **1. User** (`com.funkard.model.User`)
**Campi principali:**
- `id`, `email`, `password`, `username`, `handle`
- `nome`, `paese`, `tipoUtente` (PRIVATO/BUSINESS)
- `indirizzo`, `citta`, `cap`, `telefono`, `metodoPagamento`
- `language` (String, length=5, default="en")
- `preferredCurrency` (String, length=3, default="EUR")
- `termsAcceptedAt`, `privacyAcceptedAt` (GDPR)
- `deletionPending`, `deletionRequestedAt` (GDPR)
- `verified`, `flagged`, `role`, `avatarUrl`
- `createdAt`, `updatedAt`, `lastLoginAt`

### **2. Card** (`com.funkard.model.Card`)
**Campi:**
- `id`, `title`, `description`, `imageUrl`
- `category`, `franchise`, `language` (nuovi)
- `cardType`, `cardSource`
- `createdAt`, `updatedAt`

### **3. UserCard** (`com.funkard.model.UserCard`)
**Campi:**
- `id`, `user`, `card`
- `condition`, `grade`, `gradedBy`
- `imageUrl`, `notes`
- `createdAt`, `updatedAt`

### **4. Listing** (`com.funkard.model.Listing`)
**Campi:**
- `id`, `title`, `description`, `price`
- `condition`, `seller`, `card`
- `status` (ACTIVE, SOLD, CANCELLED)
- `createdAt`, `updatedAt`

### **5. Transaction** (`com.funkard.model.Transaction`)
**Campi:**
- `id`, `buyer`, `seller`, `listing`
- `amount`, `status`, `paymentMethod`
- `createdAt`, `completedAt`

### **6. Wishlist** (`com.funkard.model.Wishlist`)
**Campi:**
- `id`, `user`, `card`
- `priority`, `notes`
- `createdAt`

### **7. UserAddress** (`com.funkard.model.UserAddress`)
**Campi:**
- `id`, `user`
- `fullName`, `street`, `city`, `state`
- `postalCode`, `country`, `phone`
- `addressLabel`, `isDefault`
- `createdAt`, `updatedAt`

### **8. UserPreferences** (`com.funkard.model.UserPreferences`)
**Campi:**
- `id`, `user`
- `cookiesAccepted`, `cookiesPreferences` (JSON)
- `cookiesAcceptedAt`
- `createdAt`, `updatedAt`

### **9. CookieConsentLog** (`com.funkard.model.CookieConsentLog`)
**Campi:**
- `id`, `userId`
- `action`, `oldPreferences`, `newPreferences` (JSON)
- `ipAddress`, `userAgent`
- `createdAt`

### **10. UserDeletion** (`com.funkard.model.UserDeletion`)
**Campi:**
- `id`, `userId`, `email`
- `requestedAt`, `scheduledDeletionAt`
- `status` (PENDING, COMPLETED)
- `reason`, `completedAt`

### **11. EmailLog** (`com.funkard.model.EmailLog`)
**Campi:**
- `id`, `recipient`, `sender`, `subject`
- `type`, `status` (SENT, FAILED, RETRIED)
- `errorMessage`, `sentAt`, `retryCount`
- `locale`, `templateName`, `webhookId`

### **12. ChatMessage** (`com.funkard.model.ChatMessage`)
**Campi:**
- `id`, `sender`, `recipient`
- `originalText`, `translatedText`
- `originalLanguage`, `targetLanguage`
- `isTranslated`
- `createdAt`, `readAt`

### **13. TranslationLog** (`com.funkard.model.TranslationLog`)
**Campi:**
- `id`, `sourceText`, `translatedText`
- `sourceLanguage`, `targetLanguage`
- `translationProvider`, `success`
- `errorMessage`, `user`, `messageType`, `messageId`
- `createdAt`

### **14. PendingValue** (`com.funkard.model.PendingValue`)
**Campi:**
- `id`, `type` (TCG, LANGUAGE, FRANCHISE)
- `value`, `submittedBy`
- `approved`, `approvedBy`, `approvedAt`
- `createdAt`

### **15. Franchise** (`com.funkard.model.Franchise`)
**Campi:**
- `id`, `category`, `name` (unique)
- `status` (ACTIVE, DISABLED)
- `createdAt`, `updatedAt`

### **16. FranchiseProposal** (`com.funkard.model.FranchiseProposal`)
**Campi:**
- `id`, `category`, `franchise`
- `userEmail`, `user`
- `status` (PENDING, APPROVED, REJECTED)
- `processedBy`, `processedAt`
- `createdAt`

### **17. FranchiseCatalog** (`com.funkard.model.FranchiseCatalog`)
**Campi:**
- `id`, `category`, `name`
- `active`
- `createdAt`, `updatedAt`

### **18. SupportTicket** (`com.funkard.admin.model.SupportTicket`)
**Campi:**
- `id`, `userId`, `userEmail`
- `subject`, `message`, `status`
- `priority`, `category`
- `assignedTo`, `createdAt`, `updatedAt`

### **19. SupportMessage** (`com.funkard.admin.model.SupportMessage`)
**Campi:**
- `id`, `ticket`, `sender`
- `message`, `originalText`, `translatedText`
- `originalLanguage`, `targetLanguage`
- `isTranslated`
- `createdAt`

### **20. AdminNotification** (`com.funkard.admin.model.AdminNotification`)
**Campi:**
- `id`, `type`, `title`, `message`
- `priority`, `read`, `archived`
- `readBy`, `readAt`, `archivedAt`
- `assignedTo`, `assignedAt`
- `resolvedAt`, `resolvedBy`
- `history` (JSON)
- `createdAt`

### **21. GradeReport** (`com.funkard.model.GradeReport`)
**Campi:**
- `id`, `userCard`, `grade`
- `condition`, `gradedBy`
- `createdAt`

### **22. VerificationToken** (`com.funkard.model.VerificationToken`)
**Campi:**
- `id`, `user`, `token`
- `expiryDate`

---

## 💾 Repository

### **Repository Principali:**
1. **UserRepository** - CRUD utenti, findByEmail, findByHandle
2. **CardRepository** - CRUD carte, findByTitle, findByCategory
3. **UserCardRepository** - CRUD carte utente, findByUser
4. **ListingRepository** - CRUD annunci, findByStatus, findBySeller
5. **TransactionRepository** - CRUD transazioni, findByBuyer, findBySeller
6. **WishlistRepository** - CRUD wishlist, findByUser
7. **UserAddressRepository** - CRUD indirizzi, findByUser, findByIsDefault
8. **UserPreferencesRepository** - CRUD preferenze, findByUser
9. **CookieConsentLogRepository** - CRUD log consenso, findByUserId
10. **UserDeletionRepository** - CRUD cancellazioni, findByStatus
11. **EmailLogRepository** - CRUD log email, findByRecipient, findByStatus
12. **ChatMessageRepository** - CRUD messaggi, findBySender, findByRecipient
13. **TranslationLogRepository** - CRUD log traduzioni, findByUserId
14. **PendingValueRepository** - CRUD proposte, findByType, findByApproved
15. **FranchiseRepository** - CRUD franchise, findByStatus, findByCategory
16. **FranchiseProposalRepository** - CRUD proposte franchise, findByStatus
17. **FranchiseCatalogRepository** - CRUD catalogo franchise
18. **SupportTicketRepository** - CRUD ticket supporto
19. **SupportMessageRepository** - CRUD messaggi supporto
20. **AdminNotificationRepository** - CRUD notifiche admin

---

## 🔧 Service

### **1. UserService**
**Metodi:**
- `getAll()` - Lista utenti
- `create(User)` - Crea utente
- `findById(Long)` - Trova per ID
- `findByEmail(String)` - Trova per email
- `delete(Long)` - Elimina utente
- `getUserProfile(User)` - Ottieni profilo
- `updateUserProfile(User, UserProfileDTO)` - Aggiorna profilo
- `updateLastLogin(Long)` - Aggiorna ultimo accesso
- `getUserCount()` - Conta utenti
- `emailExists(String)` - Verifica email

### **2. CardService**
**Metodi:**
- `create(Card)` - Crea carta
- `findById(Long)` - Trova per ID
- `findAll()` - Lista carte
- `update(Card)` - Aggiorna carta
- `delete(Long)` - Elimina carta

### **3. ListingService**
**Metodi:**
- `create(Listing, CreateListingRequest, Long)` - Crea annuncio
- `findById(Long)` - Trova per ID
- `findAll()` - Lista annunci
- `findByStatus(String)` - Filtra per stato
- `update(Listing)` - Aggiorna annuncio
- `delete(Long)` - Elimina annuncio

### **4. TransactionService**
**Metodi:**
- `create(Transaction)` - Crea transazione
- `findById(Long)` - Trova per ID
- `findByBuyer(Long)` - Transazioni acquirente
- `findBySeller(Long)` - Transazioni venditore
- `complete(Long)` - Completa transazione

### **5. WishlistService**
**Metodi:**
- `addToWishlist(Long, Long)` - Aggiungi a wishlist
- `removeFromWishlist(Long, Long)` - Rimuovi da wishlist
- `getUserWishlist(Long)` - Ottieni wishlist utente
- `isInWishlist(Long, Long)` - Verifica presenza

### **6. UserAddressService**
**Metodi:**
- `create(UserAddress, Long)` - Crea indirizzo
- `findById(Long)` - Trova per ID
- `findByUser(Long)` - Indirizzi utente
- `update(UserAddress, Long)` - Aggiorna indirizzo
- `delete(Long, Long)` - Elimina indirizzo
- `setDefault(Long, Long)` - Imposta default

### **7. UserPreferencesService**
**Metodi:**
- `saveCookiePreferences(Long, CookiePreferencesDTO)` - Salva preferenze cookie
- `getCookiePreferences(Long)` - Ottieni preferenze cookie
- `updatePreferences(Long, UserPreferencesDTO)` - Aggiorna preferenze

### **8. CookieConsentLogService**
**Metodi:**
- `logConsent(Long, String, String, String, String, String)` - Log consenso
- `getUserLogs(Long)` - Log utente
- `getAllLogs()` - Tutti i log

### **9. CookieLogExportService**
**Metodi:**
- `exportUserLogs(Long, String)` - Export log (JSON/PDF)
- `generatePdfReport(List<CookieConsentLog>)` - Genera PDF

### **10. UserDeletionService**
**Metodi:**
- `requestDeletion(Long, String)` - Richiedi cancellazione
- `processDeletion(Long)` - Processa cancellazione
- `getPendingDeletions()` - Cancellazioni pending

### **11. UserAccountDeletionService**
**Metodi:**
- `deleteUserAccount(Long)` - Elimina account completo
- `deleteUserData(User)` - Elimina dati utente
- `deleteR2Files(User)` - Elimina file R2

### **12. EmailService**
**Metodi:**
- `sendEmail(String, String, String, boolean)` - Invia email generica
- `sendAccountConfirmationEmail(String, String, String)` - Conferma account
- `sendAccountDeletionCompletedEmail(String, String, String)` - Cancellazione completata
- `sendUsingPrimary(String, String, String, boolean)` - Invia con sender primario
- `sendUsingFallback(String, String, String, boolean)` - Invia con sender fallback

### **13. EmailTemplateManager**
**Metodi:**
- `loadTemplate(String, String)` - Carica template (lingua)
- `renderTemplate(String, Map<String, Object>)` - Renderizza template
- `getAvailableLanguages()` - Lingue disponibili
- `templateExists(String, String)` - Verifica esistenza template

### **14. EmailLogService**
**Metodi:**
- `logEmail(EmailLog)` - Log email
- `findByRecipient(String)` - Email per destinatario
- `findByStatus(String)` - Email per stato
- `getStats()` - Statistiche email

### **15. ChatService**
**Metodi:**
- `sendMessage(Long, Long, String)` - Invia messaggio (con traduzione)
- `getConversation(Long, Long)` - Ottieni conversazione
- `countUnreadMessages(Long)` - Conta non letti
- `markMessageAsRead(UUID, Long)` - Marca come letto

### **16. TranslationService**
**Metodi:**
- `translate(String, String, String, Long, String, UUID)` - Traduce testo
- `normalizeLanguage(String)` - Normalizza codice lingua
- `logTranslation(...)` - Log traduzione

### **17. PendingValueService**
**Metodi:**
- `submitPendingValue(ValueType, String, Long)` - Invia proposta
- `approvePendingValue(UUID, Long)` - Approva proposta
- `rejectPendingValue(UUID)` - Rifiuta proposta
- `getPendingValues(ValueType, Pageable)` - Lista proposte
- `getPendingValuesBySubmittedBy(Long)` - Proposte utente

### **18. FranchiseAdminService**
**Metodi:**
- `getAllFranchisesAndProposals(String)` - Lista tutto
- `approveProposal(Long, Long)` - Approva proposta
- `rejectProposal(Long, Long)` - Rifiuta proposta
- `disableFranchise(Long, Long)` - Disabilita franchise
- `enableFranchise(Long, Long)` - Riabilita franchise
- `createFranchise(String, String, Long)` - Crea franchise
- `createProposal(String, String, String, Long)` - Crea proposta

### **19. FranchiseJsonService**
**Metodi:**
- `loadFranchisesFromJson()` - Carica da JSON
- `getAllFranchises()` - Lista tutti
- `getFranchisesByCategory(String)` - Per categoria
- `getCategories()` - Lista categorie
- `updateJsonFile(String, String, boolean)` - Aggiorna JSON

### **20. FranchiseCatalogService**
**Metodi:**
- `getActiveFranchises()` - Franchise attivi
- `getFranchisesByCategory(String)` - Per categoria
- `getFranchisesGroupedByCategory()` - Raggruppati
- `createFranchise(String, String)` - Crea franchise
- `updateFranchise(Long, String, String, Boolean)` - Aggiorna
- `deleteFranchise(Long)` - Elimina
- `getStats()` - Statistiche

### **21. R2Service** (Cloudflare R2 Storage)
**Metodi:**
- `uploadFile(InputStream, String, String)` - Upload file
- `deleteFile(String, String)` - Elimina file
- `getFileUrl(String, String)` - URL file
- `listFiles(String)` - Lista file

### **22. SupportTicketService** (Admin)
**Metodi:**
- `create(String, String, String)` - Crea ticket
- `findById(UUID)` - Trova per ID
- `findAll()` - Lista tutti
- `updateStatus(UUID, String)` - Aggiorna stato
- `assignTicket(UUID, String)` - Assegna ticket
- `closeTicket(UUID)` - Chiudi ticket

### **23. SupportMessageService** (Admin)
**Metodi:**
- `addMessage(UUID, String, String)` - Aggiungi messaggio (con traduzione)
- `getMessages(UUID)` - Messaggi ticket
- `markAsRead(UUID)` - Marca come letto

### **24. AdminNotificationService**
**Metodi:**
- `createAdminNotification(String, String, String, String)` - Crea notifica
- `listActiveChrono()` - Lista attive
- `markRead(UUID, String)` - Marca come letta
- `archive(UUID, String, String)` - Archivia
- `assign(UUID, String)` - Assegna
- `getUnreadCount()` - Conta non lette
- `subscribe()` - SSE subscription

### **25. AdminStatsService**
**Metodi:**
- `getDashboardStats()` - Statistiche dashboard
- `getUserStats()` - Statistiche utenti
- `getMarketStats()` - Statistiche marketplace
- `getSupportStats()` - Statistiche supporto

### **26. GradeLensService**
**Metodi:**
- `gradeCard(InputStream, String)` - Grading AI
- `getGradeResult(UUID)` - Risultato grading
- `cleanupOldResults()` - Pulizia risultati vecchi

### **27. TransactionService**
**Metodi:**
- `create(Transaction)` - Crea transazione
- `findById(Long)` - Trova per ID
- `findByBuyer(Long)` - Transazioni acquirente
- `findBySeller(Long)` - Transazioni venditore
- `complete(Long)` - Completa transazione

---

## 🌍 Controller

### **CONTROLLER PUBBLICI/UTENTE (21 file)**

#### **1. AuthController** (`/api/auth`)
**Endpoint:**
- `POST /api/auth/register` - Registrazione utente
- `POST /api/auth/login` - Login (restituisce token, language, preferredCurrency)
- `GET /api/auth/validate` - Valida token

#### **2. UserController** (`/api/user`)
**Endpoint:**
- `GET /api/user/me` - Profilo utente (include language, preferredCurrency)
- `PUT /api/user/me` - Aggiorna profilo
- `PATCH /api/user/preferences` - Aggiorna preferenze (language, currency)
- `GET /api/user/address` - Lista indirizzi
- `POST /api/user/address` - Crea indirizzo
- `PUT /api/user/address/{id}` - Aggiorna indirizzo
- `DELETE /api/user/address/{id}` - Elimina indirizzo
- `POST /api/user/preferences/cookies` - Salva preferenze cookie
- `GET /api/user/preferences/cookies` - Ottieni preferenze cookie
- `GET /api/user/preferences/cookies/export` - Export log cookie (JSON/PDF)
- `DELETE /api/user/delete-account` - Richiedi cancellazione account

#### **3. CardController** (`/api/cards`)
**Endpoint:**
- `GET /api/cards` - Lista carte
- `GET /api/cards/{id}` - Dettaglio carta
- `POST /api/cards` - Crea carta (admin)
- `PUT /api/cards/{id}` - Aggiorna carta (admin)
- `DELETE /api/cards/{id}` - Elimina carta (admin)

#### **4. UserCardController** (`/api/user-cards`)
**Endpoint:**
- `GET /api/user-cards` - Carte utente
- `POST /api/user-cards` - Aggiungi carta
- `PUT /api/user-cards/{id}` - Aggiorna carta
- `DELETE /api/user-cards/{id}` - Elimina carta

#### **5. ListingController** (`/api/listings`)
**Endpoint:**
- `GET /api/listings` - Lista annunci
- `GET /api/listings/{id}` - Dettaglio annuncio
- `POST /api/listings` - Crea annuncio (gestisce custom TCG/Language/Franchise)
- `PUT /api/listings/{id}` - Aggiorna annuncio
- `DELETE /api/listings/{id}` - Elimina annuncio

#### **6. TransactionController** (`/api/transactions`)
**Endpoint:**
- `GET /api/transactions` - Lista transazioni utente
- `GET /api/transactions/{id}` - Dettaglio transazione
- `POST /api/transactions` - Crea transazione
- `PUT /api/transactions/{id}/complete` - Completa transazione

#### **7. WishlistController** (`/api/wishlist`)
**Endpoint:**
- `GET /api/wishlist` - Lista wishlist
- `POST /api/wishlist` - Aggiungi a wishlist
- `DELETE /api/wishlist/{id}` - Rimuovi da wishlist

#### **8. FranchiseController** (`/api/franchises`)
**Endpoint:**
- `GET /api/franchises` - Lista franchise (pubblico, da JSON)
- `POST /api/franchises/propose` - Proponi nuovo franchise

#### **9. PendingValueController** (`/api/pending-values`)
**Endpoint:**
- `POST /api/pending-values/submit` - Invia proposta valore custom
- `GET /api/pending-values/my` - Le mie proposte

#### **10. ChatController** (`/api/chat`)
**Endpoint:**
- `POST /api/chat/message` - Invia messaggio (con traduzione automatica)
- `GET /api/chat/conversation/{userId}` - Conversazione con utente
- `GET /api/chat/unread` - Conta messaggi non letti
- `PUT /api/chat/message/{messageId}/read` - Marca come letto

#### **11. SupportController** (`/api/support`)
**Endpoint:**
- `POST /api/support/tickets` - Crea ticket supporto
- `GET /api/support/tickets` - Lista ticket utente
- `GET /api/support/tickets/{id}` - Dettaglio ticket

#### **12. SupportChatController** (`/api/support/chat`)
**Endpoint:**
- `POST /api/support/chat/{ticketId}/message` - Invia messaggio (con traduzione)
- `GET /api/support/chat/{ticketId}/messages` - Messaggi ticket

#### **13. SupportSseController** (`/api/support/stream`)
**Endpoint:**
- `GET /api/support/stream` - SSE per notifiche real-time

#### **14. CollectionController** (`/api/collections`)
**Endpoint:**
- `GET /api/collections` - Collezioni utente

#### **15. GradeLensAdminController** (`/api/gradelens`)
**Endpoint:**
- `POST /api/gradelens/grade` - Grading AI carta
- `GET /api/gradelens/result/{id}` - Risultato grading

#### **16. RootController** (`/`)
**Endpoint:**
- `GET /` - Health check
- `GET /api` - Info API

#### **17. TestController** (`/api/test`)
**Endpoint:**
- `GET /api/test` - Test endpoint

---

### **CONTROLLER ADMIN (21 file)**

#### **1. AdminDashboardController** (`/api/admin/dashboard`)
**Endpoint:**
- `GET /api/admin/dashboard` - Dashboard admin con statistiche

#### **2. AdminStatsController** (`/api/admin/stats`)
**Endpoint:**
- `GET /api/admin/stats` - Statistiche complete
- `GET /api/admin/stats/users` - Statistiche utenti
- `GET /api/admin/stats/market` - Statistiche marketplace
- `GET /api/admin/stats/support` - Statistiche supporto

#### **3. AdminSupportController** (`/api/admin/support`)
**Endpoint:**
- `GET /api/admin/support/tickets` - Lista ticket
- `GET /api/admin/support/tickets/{id}` - Dettaglio ticket
- `PUT /api/admin/support/tickets/{id}/status` - Aggiorna stato
- `PUT /api/admin/support/tickets/{id}/assign` - Assegna ticket

#### **4. AdminNotificationController** (`/api/admin/notifications`)
**Endpoint:**
- `GET /api/admin/notifications` - Lista notifiche
- `GET /api/admin/notifications/{id}` - Dettaglio notifica
- `PUT /api/admin/notifications/{id}/read` - Marca come letta
- `PUT /api/admin/notifications/{id}/archive` - Archivia
- `PUT /api/admin/notifications/{id}/assign` - Assegna

#### **5. AdminNotificationStreamController** (`/api/admin/notifications/stream`)
**Endpoint:**
- `GET /api/admin/notifications/stream` - SSE notifiche real-time

#### **6. FranchiseAdminController** (`/api/admin/franchises`)
**Endpoint:**
- `GET /api/admin/franchises` - Lista franchise e proposte
- `POST /api/admin/franchises/approve/{proposalId}` - Approva proposta
- `POST /api/admin/franchises/reject/{proposalId}` - Rifiuta proposta
- `PATCH /api/admin/franchises/{id}/disable` - Disabilita franchise
- `PATCH /api/admin/franchises/{id}/enable` - Riabilita franchise
- `POST /api/admin/franchises/add` - Crea franchise manualmente

#### **7. AdminPendingValueController** (`/api/admin/pending-values`)
**Endpoint:**
- `GET /api/admin/pending-values` - Lista proposte
- `POST /api/admin/pending-values/{id}/approve` - Approva proposta
- `DELETE /api/admin/pending-values/{id}` - Rifiuta proposta
- `GET /api/admin/pending-values/stats` - Statistiche proposte

#### **8. AdminEmailLogController** (`/api/admin/email-logs`)
**Endpoint:**
- `GET /api/admin/email-logs` - Lista log email
- `GET /api/admin/email-logs/{id}` - Dettaglio log
- `GET /api/admin/email-logs/stats` - Statistiche email

#### **9. CookieLogAdminController** (`/api/admin/cookies/logs`)
**Endpoint:**
- `GET /api/admin/cookies/logs` - Lista log cookie
- `GET /api/admin/cookies/logs/export` - Export log (admin)

#### **10. EmailTemplateTestController** (`/api/admin/email-templates/test`)
**Endpoint:**
- `POST /api/admin/email-templates/test` - Test template email

#### **11. AdminCleanupController** (`/api/admin/cleanup`)
**Endpoint:**
- `POST /api/admin/cleanup/email-logs` - Pulizia log email
- `POST /api/admin/cleanup/grade-reports` - Pulizia report grading

#### **12. AdminValuationController** (`/api/admin/valuations`)
**Endpoint:**
- `GET /api/admin/valuations` - Valutazioni carte

#### **13. RolePermissionController** (`/api/admin/roles`)
**Endpoint:**
- `GET /api/admin/roles` - Lista ruoli
- `POST /api/admin/roles` - Crea ruolo
- `PUT /api/admin/roles/{id}` - Aggiorna ruolo

---

## 📦 DTO

### **DTO Principali (17 file):**

1. **UserProfileDTO** - Profilo utente (language, preferredCurrency, theme, avatarUrl)
2. **UserDTO** - Dati utente base
3. **UserPreferencesDTO** - Preferenze utente (language, preferredCurrency)
4. **CookiePreferencesDTO** - Preferenze cookie
5. **LoginResponse** - Response login (token, language, preferredCurrency)
6. **CardDTO** - Dati carta
7. **ListingDTO** - Dati annuncio
8. **CreateListingRequest** - Request creazione annuncio (con custom TCG/Language/Franchise)
9. **TransactionDTO** - Dati transazione
10. **WishlistDTO** - Dati wishlist
11. **ChatMessageDTO** - Messaggio chat (con traduzione)
12. **FranchiseDTO** - Dati franchise
13. **FranchiseProposalDTO** - Proposta franchise
14. **CreateFranchiseRequest** - Request creazione franchise
15. **ProposeFranchiseRequest** - Request proposta franchise
16. **PendingValueDTO** - Proposta valore custom
17. **SubmitPendingValueRequest** - Request proposta valore

---

## 🗄️ Migration Database

### **Flyway Migrations (20 file):**

1. **V1** - `add_grading_columns_to_usercard.sql` - Colonne grading
2. **V2** - `add_preferred_currency_to_users.sql` - Campo preferredCurrency
3. **V3** - `create_user_addresses_table.sql` - Tabella indirizzi
4. **V4** - `create_admin_tokens_and_access_requests.sql` - Admin tokens
5. **V5** - `add_gdpr_consent_timestamps_to_users.sql` - GDPR timestamps
6. **V6** - `create_user_preferences_table.sql` - Preferenze utente
7. **V7** - `create_cookie_consent_logs_table.sql` - Log consenso cookie
8. **V8** - `update_cookie_consent_logs_add_action_useragent.sql` - Aggiorna log cookie
9. **V9** - `create_user_deletions_table.sql` - Tabella cancellazioni
10. **V10** - `add_deletion_fields_to_users.sql` - Campi cancellazione
11. **V11** - `create_email_logs_table.sql` - Log email
12. **V12** - `add_language_to_users.sql` - Campo language
13. **V13** - `add_translation_fields_to_support_messages.sql` - Campi traduzione supporto
14. **V14** - `create_chat_messages_table.sql` - Tabella chat
15. **V15** - `create_translation_logs_table.sql` - Log traduzioni
16. **V16** - `create_pending_values_table.sql` - Proposte valori custom
17. **V17** - `add_category_franchise_to_cards.sql` - Campi categoria/franchise carte
18. **V18** - `create_franchise_catalog_table.sql` - Catalogo franchise
19. **V19** - `create_franchises_table.sql` - Tabella franchise
20. **V20** - `create_franchise_proposals_table.sql` - Proposte franchise

---

## ✅ Funzionalità Implementate

### **1. 🔐 Autenticazione e Autorizzazione**
- ✅ Registrazione utente con GDPR compliance
- ✅ Login con JWT token
- ✅ Validazione token
- ✅ Ruoli utente (USER, ADMIN, SUPER_ADMIN, SUPERVISOR)
- ✅ Autenticazione admin con token

### **2. 👤 Gestione Utenti**
- ✅ CRUD utenti completo
- ✅ Profilo utente (GET/PUT /api/user/me)
- ✅ Preferenze utente (language, preferredCurrency)
- ✅ Indirizzi utente (CRUD completo)
- ✅ Preferenze cookie (GDPR compliant)
- ✅ Export log cookie (JSON/PDF)
- ✅ Cancellazione account (GDPR right to be forgotten)
- ✅ Scheduler cancellazione automatica (7 giorni)

### **3. 📧 Sistema Email**
- ✅ Invio email multi-lingua (25+ lingue)
- ✅ Template email modulari
- ✅ Fallback sender automatico
- ✅ Logging email completo
- ✅ Retry automatico (3 tentativi)
- ✅ Email conferma account
- ✅ Email cancellazione completata

### **4. 💬 Chat e Messaggistica**
- ✅ Chat tra utenti
- ✅ Traduzione automatica messaggi
- ✅ Supporto multi-lingua
- ✅ Messaggi non letti
- ✅ Log traduzioni

### **5. 🎫 Sistema Supporto**
- ✅ Creazione ticket supporto
- ✅ Chat supporto real-time
- ✅ Traduzione automatica messaggi supporto
- ✅ Assegnazione ticket admin
- ✅ Notifiche real-time (SSE)
- ✅ Statistiche supporto

### **6. 📚 Gestione Franchise**
- ✅ Lista franchise pubblica (da JSON)
- ✅ Proposte franchise utenti
- ✅ Approvazione/rifiuto proposte (admin)
- ✅ Abilitazione/disabilitazione franchise
- ✅ Creazione manuale franchise (admin)
- ✅ Sincronizzazione automatica DB ↔ JSON

### **7. ⏳ Valori Custom (Pending Values)**
- ✅ Proposte TCG custom
- ✅ Proposte Language custom
- ✅ Proposte Franchise custom
- ✅ Approvazione/rifiuto (admin)
- ✅ Integrazione con listing creation

### **8. 🛒 Marketplace**
- ✅ Creazione annunci
- ✅ Lista annunci
- ✅ Transazioni
- ✅ Wishlist
- ✅ Valutazioni carte

### **9. 🔍 GradeLens (AI Grading)**
- ✅ Grading automatico carte
- ✅ Risultati grading
- ✅ Pulizia risultati vecchi

### **10. 🎛️ Pannello Admin**
- ✅ Dashboard con statistiche
- ✅ Gestione utenti
- ✅ Gestione supporto
- ✅ Gestione notifiche
- ✅ Gestione franchise
- ✅ Gestione proposte
- ✅ Log email
- ✅ Log cookie
- ✅ Statistiche complete
- ✅ Notifiche real-time (SSE)

### **11. ⚡ Real-Time**
- ✅ Server-Sent Events (SSE)
- ✅ Notifiche admin real-time
- ✅ Notifiche supporto real-time
- ✅ WebSocket supporto

### **12. 🔒 GDPR Compliance**
- ✅ Consenso Termini e Privacy (timestamps)
- ✅ Gestione preferenze cookie
- ✅ Log consenso cookie
- ✅ Export dati utente
- ✅ Cancellazione account (right to be forgotten)
- ✅ Scheduler cancellazione automatica

### **13. 🌍 Multi-Lingua**
- ✅ Supporto 25+ lingue
- ✅ Traduzione automatica chat
- ✅ Traduzione automatica supporto
- ✅ Email multi-lingua
- ✅ Template email per lingua

### **14. 💾 Storage**
- ✅ Cloudflare R2 Storage
- ✅ Upload file
- ✅ Eliminazione file
- ✅ URL file

### **15. ⏰ Scheduler**
- ✅ Pulizia log email (90 giorni)
- ✅ Pulizia report grading
- ✅ Cancellazione account (7 giorni)

---

## 📊 Statistiche Progetto

- **File Java:** ~245 file
- **Controller:** 42 file (21 pubblici + 21 admin)
- **Service:** 27 file
- **Repository:** 20 file
- **Modelli:** 22 file
- **DTO:** 17 file
- **Migration:** 20 file
- **Endpoint API:** ~150+ endpoint

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 1.0

