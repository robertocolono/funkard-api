# 📚 Struttura Completa e Funzioni - Funkard API

**Data:** 2025-01-15  
**Versione:** 3.0  
**Framework:** Spring Boot 3.5.6  
**Java:** 21

---

## 📋 Indice

1. [Panoramica Generale](#panoramica-generale)
2. [Struttura Directory Completa](#struttura-directory-completa)
3. [Schema Database](#schema-database)
4. [Modelli (Entities)](#modelli-entities)
5. [Repository](#repository)
6. [Service](#service)
7. [Controller](#controller)
8. [DTO](#dto)
9. [Funzionalità Implementate](#funzionalità-implementate)
10. [Endpoint API Completi](#endpoint-api-completi)
11. [Sistema Traduzione](#sistema-traduzione)
12. [Configurazione](#configurazione)

---

## 🎯 Panoramica Generale

**Funkard API** è un backend Spring Boot per una piattaforma di marketplace di carte collezionabili con:

- ✅ Gestione collezioni utente
- ✅ Sistema di grading AI (GradeLens)
- ✅ Marketplace con valutazioni di mercato
- ✅ Sistema di supporto ticket con chat real-time
- ✅ Pannello admin completo
- ✅ Notifiche real-time (SSE + WebSocket)
- ✅ Gestione pagamenti e indirizzi
- ✅ Sistema traduzione automatica (GPT-4o-mini + DeepL)
- ✅ Generazione automatica nomi prodotti in inglese
- ✅ GDPR compliance completo
- ✅ Email multi-lingua (25+ lingue)

---

## 📁 Struttura Directory Completa

```
funkard-api/
├── src/main/java/com/funkard/
│   ├── FunkardApiApplication.java          # Entry point
│   │
│   ├── admin/                              # 🎛️ MODULO ADMIN (56 file)
│   │   ├── AdminConfig.java
│   │   ├── AdminController.java
│   │   ├── AdminService.java
│   │   ├── controller/                     # 21 Controller Admin
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
│   │   ├── dto/                            # 10 DTO Admin
│   │   ├── log/                            # Sistema logging admin
│   │   ├── model/                          # 6 Modelli Admin
│   │   ├── repository/                     # 4 Repository Admin
│   │   ├── service/                        # 12 Servizi Admin
│   │   ├── system/                         # Manutenzione sistema
│   │   └── util/                           # Utility admin
│   │
│   ├── adminaccess/                        # 🔐 Gestione Accessi Admin
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── adminauth/                          # 🔑 Autenticazione Admin
│   │   ├── AccessRequest.java
│   │   ├── AdminAuthController.java
│   │   ├── AdminTokenController.java
│   │   ├── AdminUserService.java
│   │   └── ...
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
│   ├── controller/                         # 🌍 CONTROLLER PUBBLICI (21 file)
│   │   ├── AdsController.java
│   │   ├── AuthController.java             # Autenticazione
│   │   ├── CardController.java             # Gestione carte
│   │   ├── ChatController.java             # Chat tra utenti (con traduzione GPT+DeepL)
│   │   ├── CollectionController.java       # Collezioni
│   │   ├── FranchiseController.java        # Franchise pubblici
│   │   ├── GradeLensAdminController.java    # GradeLens admin
│   │   ├── ListingController.java          # Annunci marketplace
│   │   ├── PendingValueController.java    # Proposte valori custom
│   │   ├── RootController.java             # Root endpoint
│   │   ├── SupportChatController.java      # Chat supporto
│   │   ├── SupportController.java          # Ticket supporto
│   │   ├── SupportSseController.java       # SSE supporto
│   │   ├── SupportWebSocketController.java # WebSocket supporto
│   │   ├── TestController.java             # Test endpoint
│   │   ├── TransactionController.java      # Transazioni
│   │   ├── TranslateController.java        # Traduzione generica
│   │   ├── UserCardController.java         # Carte utente
│   │   ├── UserController.java             # Profilo utente
│   │   └── WishlistController.java         # Wishlist
│   │
│   ├── dto/                                # 📦 DATA TRANSFER OBJECTS (18 file)
│   │   ├── CardDTO.java
│   │   ├── ChatMessageDTO.java            # Con campi traduzione
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
│   │   ├── TranslateRequest.java
│   │   ├── TranslateResponse.java
│   │   ├── UserDTO.java
│   │   ├── UserPreferencesDTO.java
│   │   ├── UserProfileDTO.java            # Con descriptionOriginal, descriptionLanguage
│   │   └── WishlistDTO.java
│   │
│   ├── gradelens/                          # 🔍 Sistema GradeLens (AI Grading)
│   │   ├── controller/
│   │   ├── GradeResult.java
│   │   ├── HeuristicAiProvider.java
│   │   ├── model/
│   │   └── service/
│   │
│   ├── grading/                             # 📊 Sistema Grading
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   │
│   ├── market/                              # 🛒 MODULO MARKETPLACE
│   │   ├── controller/
│   │   │   ├── MarketController.java
│   │   │   ├── MarketValuationController.java
│   │   │   ├── ProductController.java      # Con generazione automatica nameEn
│   │   │   └── TrendController.java
│   │   ├── model/
│   │   │   ├── Listing.java
│   │   │   ├── Product.java                # Con descriptionOriginal, descriptionLanguage, nameEn
│   │   │   └── Transaction.java
│   │   ├── repository/
│   │   ├── service/
│   │   │   ├── ProductService.java         # Con generateGlobalEnglishName()
│   │   │   ├── ListingService.java
│   │   │   ├── MarketValuationService.java
│   │   │   └── TrendService.java
│   │   └── trend/
│   │
│   ├── model/                               # 📊 MODELLI (ENTITIES) (22 file)
│   │   ├── Card.java
│   │   ├── CardSource.java
│   │   ├── CardType.java
│   │   ├── ChatMessage.java                # Con campi traduzione
│   │   ├── CookieConsentLog.java
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
│   │   ├── User.java                       # Con descriptionOriginal, descriptionLanguage (max 500)
│   │   ├── UserAddress.java
│   │   ├── UserCard.java
│   │   ├── UserDeletion.java
│   │   ├── UserPreferences.java
│   │   ├── VerificationToken.java
│   │   └── Wishlist.java
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
│   ├── service/                             # 🔧 SERVIZI (32 file)
│   │   ├── ChatService.java                # ✅ Usa UnifiedTranslationService
│   │   ├── CardService.java
│   │   ├── CookieConsentLogService.java
│   │   ├── CookieLogExportService.java
│   │   ├── DeepLTranslateService.java      # Provider DeepL
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
│   │   ├── OpenAiTranslateService.java     # Provider GPT-4o-mini
│   │   ├── PendingValueService.java
│   │   ├── R2Service.java                  # Cloudflare R2 Storage
│   │   ├── TranslationException.java       # Eccezione traduzione
│   │   ├── TranslationProvider.java        # Interfaccia provider
│   │   ├── TranslationService.java         # Legacy (usato da supporto)
│   │   ├── UnifiedTranslationService.java  # ✅ Servizio unificato GPT+DeepL
│   │   ├── UserAccountDeletionService.java
│   │   ├── UserAddressService.java
│   │   ├── UserDeletionService.java
│   │   ├── UserPreferencesService.java
│   │   ├── UserService.java                # Con validazione bio max 500 caratteri
│   │   └── WishlistService.java
│   │
│   ├── storage/                            # 💿 Storage
│   │   └── ImageStorageService.java
│   │
│   └── user/                               # 👤 Modulo Utente
│       └── payment/
│           ├── PaymentController.java
│           └── PaymentService.java
│
└── src/main/resources/
    ├── application.properties
    ├── application-dev.properties
    ├── application-prod.yml
    ├── application-test.properties
    ├── data/
    │   └── franchises.json                 # File JSON franchise
    ├── db/migration/                       # 🗄️ FLYWAY MIGRATIONS (21 file)
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
    │   ├── V20__create_franchise_proposals_table.sql
    │   └── V21__add_translation_fields_to_products_and_users.sql
    └── email-templates/                    # 📧 Template Email Multi-lingua
        ├── it/
        ├── en/
        └── ... (25+ lingue)
```

---

## 🗄️ Schema Database

### **Tabelle Principali**

#### **users**
- `id`, `email`, `password`, `handle`, `nome`, `paese`, `tipo_utente`
- `language` (VARCHAR(5), default='en')
- `preferred_currency` (VARCHAR(3), default='EUR')
- `description_original` (TEXT, max 500 caratteri) - Bio venditore
- `description_language` (VARCHAR(5)) - Lingua bio
- `terms_accepted_at`, `privacy_accepted_at` (GDPR)
- `deletion_pending`, `deletion_requested_at` (GDPR)
- `created_at`, `updated_at`, `last_login_at`

#### **products**
- `id`, `name`, `price`, `estimated_value`, `user_id`
- `description_original` (TEXT) - Descrizione originale
- `description_language` (VARCHAR(5)) - Lingua descrizione
- `name_en` (VARCHAR(255)) - Nome inglese globale (generato automaticamente)
- `created_at`, `updated_at`
- Indici: `idx_products_description_language`, `idx_products_name_en`

#### **chat_messages**
- `id` (UUID), `sender_id`, `recipient_id`
- `original_text` (TEXT) - Testo originale
- `original_language` (VARCHAR(5)) - Lingua originale
- `translated_text` (TEXT) - Testo tradotto
- `target_language` (VARCHAR(5)) - Lingua destinazione
- `is_translated` (BOOLEAN) - Flag traduzione
- `created_at`, `read_at`
- Indici: `idx_chat_sender`, `idx_chat_recipient`, `idx_chat_original_lang`, `idx_chat_target_lang`

#### **support_tickets**
- `id` (UUID), `user_id`, `user_email`
- `subject`, `message`, `status`, `priority`, `category`
- `assigned_to`, `created_at`, `updated_at`

#### **support_messages**
- `id` (UUID), `ticket_id`, `sender`
- `message` - Testo originale
- `original_language`, `translated_text`, `target_language`, `is_translated`
- `created_at`

#### **franchises**
- `id`, `category`, `name` (UNIQUE)
- `status` (ACTIVE, DISABLED)
- `created_at`, `updated_at`

#### **franchise_proposals**
- `id`, `category`, `franchise`
- `user_email`, `user_id`
- `status` (PENDING, APPROVED, REJECTED)
- `processed_by`, `processed_at`, `created_at`

#### **pending_values**
- `id` (UUID), `type` (TCG, LANGUAGE, FRANCHISE)
- `value`, `submitted_by`
- `approved`, `approved_by`, `approved_at`
- `created_at`

#### **cookie_consent_logs**
- `id` (UUID), `user_id`
- `action`, `old_preferences`, `new_preferences` (JSONB)
- `ip_address`, `user_agent`, `created_at`

#### **user_deletions**
- `id` (UUID), `user_id`, `email`
- `requested_at`, `scheduled_deletion_at`
- `status` (PENDING, COMPLETED)
- `reason`, `completed_at`

#### **email_logs**
- `id` (UUID), `recipient`, `sender`, `subject`
- `type`, `status` (SENT, FAILED, RETRIED)
- `error_message`, `sent_at`, `retry_count`
- `locale`, `template_name`, `webhook_id`
- `created_at`

#### **translation_logs**
- `id` (UUID), `source_text`, `translated_text`
- `source_language`, `target_language`
- `translation_provider`, `success`
- `error_message`, `user_id`, `message_type`, `message_id`
- `created_at`

---

## 📊 Modelli (Entities)

### **1. User** (`com.funkard.model.User`)
**Campi principali:**
- `id`, `email`, `password`, `username`, `handle`
- `nome`, `paese`, `tipoUtente` (PRIVATO/BUSINESS)
- `language` (String, length=5, default="en")
- `preferredCurrency` (String, length=3, default="EUR")
- `descriptionOriginal` (TEXT, max 500 caratteri) - Bio venditore
- `descriptionLanguage` (VARCHAR(5)) - Lingua bio
- `termsAcceptedAt`, `privacyAcceptedAt` (GDPR)
- `deletionPending`, `deletionRequestedAt` (GDPR)
- `verified`, `flagged`, `role`, `avatarUrl`
- `createdAt`, `updatedAt`, `lastLoginAt`

### **2. Product** (`com.funkard.market.model.Product`)
**Campi:**
- `id`, `name`, `price`, `estimatedValue`, `userId`
- `descriptionOriginal` (TEXT) - Descrizione originale
- `descriptionLanguage` (VARCHAR(5)) - Lingua descrizione
- `nameEn` (VARCHAR(255)) - Nome inglese globale (generato automaticamente)
- `createdAt`, `updatedAt`

### **3. ChatMessage** (`com.funkard.model.ChatMessage`)
**Campi:**
- `id` (UUID), `sender`, `recipient`
- `originalText` (TEXT) - Testo originale
- `originalLanguage` (VARCHAR(5)) - Lingua originale
- `translatedText` (TEXT) - Testo tradotto
- `targetLanguage` (VARCHAR(5)) - Lingua destinazione
- `isTranslated` (BOOLEAN) - Flag traduzione
- `createdAt`, `readAt`

### **4. Card** (`com.funkard.model.Card`)
**Campi:**
- `id`, `title`, `description`, `imageUrl`
- `category`, `franchise`, `language`
- `cardType`, `cardSource`
- `createdAt`, `updatedAt`

### **5. Listing** (`com.funkard.model.Listing`)
**Campi:**
- `id`, `title`, `description`, `price`
- `condition`, `seller`, `card`
- `status` (ACTIVE, SOLD, CANCELLED)
- `createdAt`, `updatedAt`

### **6. Transaction** (`com.funkard.model.Transaction`)
**Campi:**
- `id`, `buyer`, `seller`, `listing`
- `amount`, `status`, `paymentMethod`
- `createdAt`, `completedAt`

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
- `id` (UUID), `userId`
- `action`, `oldPreferences`, `newPreferences` (JSON)
- `ipAddress`, `userAgent`
- `createdAt`

### **10. UserDeletion** (`com.funkard.model.UserDeletion`)
**Campi:**
- `id` (UUID), `userId`, `email`
- `requestedAt`, `scheduledDeletionAt`
- `status` (PENDING, COMPLETED)
- `reason`, `completedAt`

### **11. EmailLog** (`com.funkard.model.EmailLog`)
**Campi:**
- `id` (UUID), `recipient`, `sender`, `subject`
- `type`, `status` (SENT, FAILED, RETRIED)
- `errorMessage`, `sentAt`, `retryCount`
- `locale`, `templateName`, `webhookId`

### **12. Franchise** (`com.funkard.model.Franchise`)
**Campi:**
- `id`, `category`, `name` (unique)
- `status` (ACTIVE, DISABLED)
- `createdAt`, `updatedAt`

### **13. FranchiseProposal** (`com.funkard.model.FranchiseProposal`)
**Campi:**
- `id`, `category`, `franchise`
- `userEmail`, `user`
- `status` (PENDING, APPROVED, REJECTED)
- `processedBy`, `processedAt`
- `createdAt`

### **14. PendingValue** (`com.funkard.model.PendingValue`)
**Campi:**
- `id` (UUID), `type` (TCG, LANGUAGE, FRANCHISE)
- `value`, `submittedBy`
- `approved`, `approvedBy`, `approvedAt`
- `createdAt`

### **15. SupportTicket** (`com.funkard.admin.model.SupportTicket`)
**Campi:**
- `id` (UUID), `userId`, `userEmail`
- `subject`, `message`, `status`
- `priority`, `category`
- `assignedTo`, `createdAt`, `updatedAt`

### **16. SupportMessage** (`com.funkard.admin.model.SupportMessage`)
**Campi:**
- `id` (UUID), `ticket`, `sender`
- `message` - Testo originale
- `originalLanguage`, `translatedText`, `targetLanguage`
- `isTranslated`
- `createdAt`

### **17. AdminNotification** (`com.funkard.admin.model.AdminNotification`)
**Campi:**
- `id` (UUID), `type`, `title`, `message`
- `priority`, `read`, `archived`
- `readBy`, `readAt`, `archivedAt`
- `assignedTo`, `assignedAt`
- `resolvedAt`, `resolvedBy`
- `history` (JSON)
- `createdAt`

---

## 💾 Repository

### **Repository Principali (20 file):**

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
12. **ChatMessageRepository** - CRUD messaggi, findConversationBetweenUsers, findUnreadMessages
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

### **1. ChatService** ✅ **AGGIORNATO**
**File:** `src/main/java/com/funkard/service/ChatService.java`

**Dipendenze:**
- ✅ `UnifiedTranslationService` (GPT-4o-mini + DeepL fallback)

**Metodi:**
- `sendMessage(senderId, recipientId, text)` - Invia messaggio con traduzione automatica
  - Rileva lingue da `user.language`
  - Usa `UnifiedTranslationService.translate(text, recipientLanguage)`
  - Salva `originalText`, `translatedText`, `originalLanguage`, `targetLanguage`, `isTranslated`
- `getConversation(userId1, userId2)` - Recupera conversazione
- `getUnreadMessages(userId)` - Messaggi non letti
- `markAsRead(messageId, userId)` - Marca come letto
- `countUnreadMessages(userId)` - Conta non letti

### **2. UnifiedTranslationService** ✅ **NUOVO**
**File:** `src/main/java/com/funkard/service/UnifiedTranslationService.java`

**Funzionalità:**
- Servizio centrale traduzione con fallback automatico
- Provider primario: GPT-4o-mini (`OpenAiTranslateService`)
- Provider fallback: DeepL (`DeepLTranslateService`)
- Fallback finale: testo originale

**Metodi:**
- `translate(text, targetLanguage)` - Traduce testo
  - Flusso: GPT → DeepL → testo originale
  - Normalizzazione automatica codici lingua (ISO 639-1)

### **3. OpenAiTranslateService**
**File:** `src/main/java/com/funkard/service/OpenAiTranslateService.java`

**Funzionalità:**
- Provider GPT-4o-mini per traduzioni
- Implementa `TranslationProvider`
- Metodo `executeWithCustomPrompt()` per prompt personalizzati

**Metodi:**
- `translate(text, targetLanguage)` - Traduzione standard
- `executeWithCustomPrompt(customPrompt)` - Prompt personalizzato (usato per nameEn)
- `isAvailable()` - Verifica disponibilità API key

### **4. DeepLTranslateService**
**File:** `src/main/java/com/funkard/service/DeepLTranslateService.java`

**Funzionalità:**
- Provider DeepL per traduzioni (fallback)
- Implementa `TranslationProvider`
- Normalizzazione codici lingua per DeepL API

**Metodi:**
- `translate(text, targetLanguage)` - Traduzione DeepL
- `isAvailable()` - Verifica disponibilità API key

### **5. ProductService** ✅ **AGGIORNATO**
**File:** `src/main/java/com/funkard/market/service/ProductService.java`

**Funzionalità:**
- Generazione automatica `nameEn` alla creazione prodotto
- Usa GPT-4o-mini con prompt specializzato per nomi carte collezionistiche

**Metodi:**
- `createProduct(product)` - Crea prodotto con generazione automatica nameEn
- `generateGlobalEnglishName(product)` - Genera nome inglese globale
  - Usa `OpenAiTranslateService.executeWithCustomPrompt()`
  - Fallback DeepL se GPT fallisce
  - Fallback nome originale se entrambi falliscono
- `buildCardNameNormalizationPrompt(originalName, originalLanguage)` - Costruisce prompt avanzato GPT

### **6. UserService** ✅ **AGGIORNATO**
**File:** `src/main/java/com/funkard/service/UserService.java`

**Funzionalità:**
- Validazione bio venditore (max 500 caratteri)
- Gestione campi traduzione dinamica

**Metodi:**
- `getUserProfile(user)` - Ottieni profilo (include descriptionOriginal, descriptionLanguage)
- `updateUserProfile(user, dto)` - Aggiorna profilo
  - Validazione: `descriptionOriginal` max 500 caratteri
  - Errore 400 se supera limite: "La bio del venditore non può superare 500 caratteri."

### **7. TranslationService** (Legacy)
**File:** `src/main/java/com/funkard/service/TranslationService.java`

**Status:** Legacy, usato ancora da supporto
**Nota:** Implementazioni DeepL/Google incomplete (return null)

### **8. EmailService**
**Metodi:**
- `sendEmail(to, subject, bodyHtml, isHtml)` - Invio generico
- `sendAccountConfirmationEmail()` - Conferma account
- `sendAccountDeletionCompletedEmail()` - Cancellazione completata
- Fallback automatico sender (primary → fallback)

### **9. EmailTemplateManager**
**Funzionalità:**
- Gestione template email multi-lingua (25+ lingue)
- Fallback automatico a inglese se lingua non disponibile
- Sostituzione variabili dinamiche

### **10. UserDeletionService**
**Metodi:**
- `requestDeletion(userId, reason)` - Richiedi cancellazione
- `processDeletion(deletionId)` - Processa cancellazione
- `getPendingDeletions()` - Cancellazioni pending

### **11. UserAccountDeletionService**
**Metodi:**
- `deleteUserAccount(userId)` - Elimina account completo
- `deleteUserData(user)` - Elimina dati utente
- `deleteR2Files(user)` - Elimina file R2

### **12. FranchiseAdminService**
**Metodi:**
- `getAllFranchisesAndProposals(status)` - Lista tutto
- `approveProposal(proposalId, adminId)` - Approva proposta
- `rejectProposal(proposalId, adminId)` - Rifiuta proposta
- `disableFranchise(franchiseId, adminId)` - Disabilita franchise
- `enableFranchise(franchiseId, adminId)` - Riabilita franchise
- `createFranchise(category, name, adminId)` - Crea franchise
- Sincronizzazione automatica DB ↔ JSON

### **13. SupportTicketService**
**Metodi:**
- `create(subject, message, userEmail)` - Crea ticket
- `findById(ticketId)` - Trova per ID
- `updateStatus(ticketId, status)` - Aggiorna stato
- `assignTicket(ticketId, adminUsername)` - Assegna ticket

### **14. SupportMessageService**
**Metodi:**
- `addMessage(ticketId, message, sender)` - Aggiungi messaggio (con traduzione)
- `getMessages(ticketId)` - Messaggi ticket
- Usa `TranslationService` (legacy) per traduzione

### **15. AdminNotificationService**
**Metodi:**
- `createAdminNotification(type, title, message, priority)` - Crea notifica
- `listActiveChrono()` - Lista attive
- `markRead(notificationId, adminUsername)` - Marca come letta
- `archive(notificationId, adminUsername, reason)` - Archivia
- `assign(notificationId, adminUsername)` - Assegna
- `subscribe()` - SSE subscription

### **16. R2Service** (Cloudflare R2 Storage)
**Metodi:**
- `uploadFile(inputStream, bucket, key)` - Upload file
- `deleteFile(bucket, key)` - Elimina file
- `getFileUrl(bucket, key)` - URL file
- `listFiles(bucket)` - Lista file

### **17. GradeLensService**
**Metodi:**
- `gradeCard(imageInputStream, cardType)` - Grading AI
- `getGradeResult(resultId)` - Risultato grading
- `cleanupOldResults()` - Pulizia risultati vecchi

---

## 🌍 Controller

### **CONTROLLER PUBBLICI/UTENTE (21 file)**

#### **1. AuthController** (`/api/auth`)
**Endpoint:**
- `POST /api/auth/register` - Registrazione (GDPR compliant)
- `POST /api/auth/login` - Login (restituisce token, language, preferredCurrency)
- `GET /api/auth/validate` - Valida token

#### **2. UserController** (`/api/user`)
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

#### **3. ChatController** (`/api/chat`) ✅ **AGGIORNATO**
**Endpoint:**
- `POST /api/chat/message` - Invia messaggio (con traduzione GPT+DeepL)
  - Request: `{"recipientId": 123, "text": "Ciao, come stai?"}`
  - Response: `ChatMessageDTO` con `originalText`, `translatedText`, `originalLanguage`, `targetLanguage`, `isTranslated`
- `GET /api/chat/conversation/{userId}` - Conversazione con utente
- `GET /api/chat/unread` - Messaggi non letti
- `PUT /api/chat/message/{messageId}/read` - Marca come letto

**Traduzione:**
- ✅ Usa `UnifiedTranslationService` (GPT-4o-mini + DeepL fallback)
- ✅ Traduzione automatica se lingue diverse
- ✅ Salva testo originale e tradotto

#### **4. ProductController** (`/api/products`) ✅ **AGGIORNATO**
**Endpoint:**
- `GET /api/products` - Lista prodotti
- `GET /api/products/{id}` - Dettaglio prodotto
- `POST /api/products` - Crea prodotto
  - Genera automaticamente `nameEn` se non fornito
  - Usa GPT-4o-mini con prompt specializzato per nomi carte collezionistiche
  - Fallback DeepL se GPT fallisce
  - Fallback nome originale se entrambi falliscono

#### **5. TranslateController** (`/api/translate`)
**Endpoint:**
- `POST /api/translate` - Traduzione generica
  - Request: `{"text": "...", "targetLanguage": "it"}`
  - Response: `{"translated": "..."}`
  - Lingue supportate: `["en","it","es","fr","de","pt","ru","ja","zh"]`
  - Usa `UnifiedTranslationService` (GPT + DeepL fallback)

#### **6. CardController** (`/api/cards`)
**Endpoint:**
- `GET /api/cards` - Lista carte
- `GET /api/cards/{id}` - Dettaglio carta
- `POST /api/cards` - Crea carta (admin)
- `PUT /api/cards/{id}` - Aggiorna carta (admin)
- `DELETE /api/cards/{id}` - Elimina carta (admin)

#### **7. ListingController** (`/api/listings`)
**Endpoint:**
- `GET /api/listings` - Lista annunci
- `GET /api/listings/{id}` - Dettaglio annuncio
- `POST /api/listings` - Crea annuncio
- `PUT /api/listings/{id}` - Aggiorna annuncio
- `DELETE /api/listings/{id}` - Elimina annuncio

#### **8. TransactionController** (`/api/transactions`)
**Endpoint:**
- `GET /api/transactions` - Lista transazioni utente
- `GET /api/transactions/{id}` - Dettaglio transazione
- `POST /api/transactions` - Crea transazione
- `PUT /api/transactions/{id}/complete` - Completa transazione

#### **9. WishlistController** (`/api/wishlist`)
**Endpoint:**
- `GET /api/wishlist` - Lista wishlist
- `POST /api/wishlist` - Aggiungi a wishlist
- `DELETE /api/wishlist/{id}` - Rimuovi da wishlist

#### **10. FranchiseController** (`/api/franchises`)
**Endpoint:**
- `GET /api/franchises` - Lista franchise (pubblico, da JSON)
- `POST /api/franchises/propose` - Proponi nuovo franchise

#### **11. PendingValueController** (`/api/pending-values`)
**Endpoint:**
- `POST /api/pending-values/submit` - Invia proposta valore custom
- `GET /api/pending-values/my` - Le mie proposte

#### **12. SupportController** (`/api/support`)
**Endpoint:**
- `POST /api/support/tickets` - Crea ticket supporto
- `GET /api/support/tickets` - Lista ticket utente
- `GET /api/support/tickets/{id}` - Dettaglio ticket

#### **13. SupportChatController** (`/api/support/chat`)
**Endpoint:**
- `POST /api/support/chat/{ticketId}/message` - Invia messaggio (con traduzione)
- `GET /api/support/chat/{ticketId}/messages` - Messaggi ticket

#### **14. SupportSseController** (`/api/support/stream`)
**Endpoint:**
- `GET /api/support/stream` - SSE per notifiche real-time

#### **15. CollectionController** (`/api/collections`)
**Endpoint:**
- `GET /api/collections` - Collezioni utente

#### **16. GradeLensAdminController** (`/api/gradelens`)
**Endpoint:**
- `POST /api/gradelens/grade` - Grading AI carta
- `GET /api/gradelens/result/{id}` - Risultato grading

---

### **CONTROLLER ADMIN (21 file)**

#### **1. AdminDashboardController** (`/api/admin/dashboard`)
- `GET /api/admin/dashboard` - Dashboard con statistiche

#### **2. AdminStatsController** (`/api/admin/stats`)
- `GET /api/admin/stats` - Statistiche complete
- `GET /api/admin/stats/users` - Statistiche utenti
- `GET /api/admin/stats/market` - Statistiche marketplace
- `GET /api/admin/stats/support` - Statistiche supporto

#### **3. AdminSupportController** (`/api/admin/support`)
- `GET /api/admin/support/tickets` - Lista ticket
- `GET /api/admin/support/tickets/{id}` - Dettaglio ticket
- `PUT /api/admin/support/tickets/{id}/status` - Aggiorna stato
- `PUT /api/admin/support/tickets/{id}/assign` - Assegna ticket

#### **4. AdminNotificationController** (`/api/admin/notifications`)
- `GET /api/admin/notifications` - Lista notifiche
- `GET /api/admin/notifications/{id}` - Dettaglio notifica
- `PUT /api/admin/notifications/{id}/read` - Marca come letta
- `PUT /api/admin/notifications/{id}/archive` - Archivia
- `PUT /api/admin/notifications/{id}/assign` - Assegna

#### **5. AdminNotificationStreamController** (`/api/admin/notifications/stream`)
- `GET /api/admin/notifications/stream` - SSE notifiche real-time

#### **6. FranchiseAdminController** (`/api/admin/franchises`)
- `GET /api/admin/franchises` - Lista franchise e proposte
- `POST /api/admin/franchises/approve/{proposalId}` - Approva proposta
- `POST /api/admin/franchises/reject/{proposalId}` - Rifiuta proposta
- `PATCH /api/admin/franchises/{id}/disable` - Disabilita franchise
- `PATCH /api/admin/franchises/{id}/enable` - Riabilita franchise
- `POST /api/admin/franchises/add` - Crea franchise manualmente

#### **7. AdminPendingValueController** (`/api/admin/pending-values`)
- `GET /api/admin/pending-values` - Lista proposte
- `POST /api/admin/pending-values/{id}/approve` - Approva proposta
- `DELETE /api/admin/pending-values/{id}` - Rifiuta proposta
- `GET /api/admin/pending-values/stats` - Statistiche proposte

#### **8. AdminEmailLogController** (`/api/admin/email-logs`)
- `GET /api/admin/email-logs` - Lista log email
- `GET /api/admin/email-logs/{id}` - Dettaglio log
- `GET /api/admin/email-logs/stats` - Statistiche email

#### **9. CookieLogAdminController** (`/api/admin/cookies/logs`)
- `GET /api/admin/cookies/logs` - Lista log cookie
- `GET /api/admin/cookies/logs/export` - Export log (admin)

---

## ✅ Funzionalità Implementate

### **1. 🌍 Sistema Traduzione Unificato** ✅

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

**Utilizzato in:**
- ✅ Chat tra utenti (`ChatService`)
- ✅ Traduzione generica (`TranslateController`)
- ✅ Generazione nameEn prodotti (`ProductService`)

---

### **2. 💬 Chat con Traduzione Automatica** ✅ **AGGIORNATO**

**File:** `ChatService.java`

**Funzionalità:**
- ✅ Traduzione automatica messaggi tra utenti
- ✅ Usa `UnifiedTranslationService` (GPT-4o-mini + DeepL fallback)
- ✅ Salva testo originale e tradotto
- ✅ Rileva lingue da `user.language`
- ✅ Fallback graceful se traduzione fallisce

**Endpoint:**
- `POST /api/chat/message` - Invia messaggio (con traduzione)
- `GET /api/chat/conversation/{userId}` - Conversazione
- `GET /api/chat/unread` - Messaggi non letti
- `PUT /api/chat/message/{messageId}/read` - Marca come letto

**Response:**
```json
{
  "id": "...",
  "senderId": 123,
  "recipientId": 456,
  "originalText": "Ciao, come stai?",
  "translatedText": "Hi, how are you?",
  "originalLanguage": "it",
  "targetLanguage": "en",
  "isTranslated": true,
  "createdAt": "2025-01-15T10:00:00"
}
```

---

### **3. 🎴 Generazione Automatica nameEn Prodotti** ✅

**File:** `ProductService.java`

**Funzionalità:**
- ✅ Generazione automatica `nameEn` alla creazione prodotto
- ✅ Usa GPT-4o-mini con prompt specializzato per nomi carte collezionistiche
- ✅ Non traduzione letterale, riconoscimento nome ufficiale
- ✅ Fallback DeepL se GPT fallisce
- ✅ Fallback nome originale se entrambi falliscono

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
- Nome ≤3 caratteri o generico → usa nome originale
- Se `nameEn` già fornito → non viene sovrascritto

---

### **4. 👤 Validazione Bio Venditore** ✅

**File:** `UserService.java`, `User.java`, `UserProfileDTO.java`

**Funzionalità:**
- ✅ Validazione `descriptionOriginal` max 500 caratteri
- ✅ `@Size(max = 500)` su entity e DTO
- ✅ Validazione esplicita in service
- ✅ Errore 400 se supera limite: "La bio del venditore non può superare 500 caratteri."

**Endpoint:**
- `PUT /api/user/me` - Aggiorna profilo (include bio venditore)

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
- `GET /api/user/me` - Profilo utente
- `PUT /api/user/me` - Aggiorna profilo
- `PATCH /api/user/preferences` - Aggiorna preferenze
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

---

### **8. 🎫 Sistema Supporto**

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

### **9. 📚 Gestione Franchise**

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

### **10. ⏰ Scheduler**

**Jobs:**
- `UserDeletionScheduler` - Cancellazione account dopo 7 giorni
- `EmailLogCleanupScheduler` - Pulizia log email (90 giorni)
- `GradeCleanupScheduler` - Pulizia report grading

---

### **11. 📧 Sistema Email**

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

### **12. 🔒 GDPR Compliance**

**Funzionalità:**
- ✅ Consenso Termini e Privacy (timestamps)
- ✅ Gestione preferenze cookie
- ✅ Log consenso cookie
- ✅ Export dati utente (JSON/PDF)
- ✅ Cancellazione account (right to be forgotten)
- ✅ Scheduler cancellazione automatica (7 giorni)
- ✅ Email notifica cancellazione completata

---

## 🔄 Flussi Principali

### **1. Invio Messaggio Chat con Traduzione**
```
POST /api/chat/message
  → ChatController.sendMessage()
  → ChatService.sendMessage()
    → Rileva lingue (sender.language, recipient.language)
    → Se lingue diverse:
      → UnifiedTranslationService.translate(text, recipientLanguage)
        → Prova GPT-4o-mini
        → Se fallisce → Prova DeepL
        → Se fallisce → Testo originale
    → Salva ChatMessage (originalText, translatedText, originalLanguage, targetLanguage, isTranslated)
  → Restituisce ChatMessageDTO
```

### **2. Creazione Prodotto con nameEn Automatico**
```
POST /api/products
  → ProductController.createProduct()
  → ProductService.createProduct()
    → Se nameEn null/vuoto:
      → generateGlobalEnglishName()
        → buildCardNameNormalizationPrompt()
        → OpenAiTranslateService.executeWithCustomPrompt(prompt)
        → Se fallisce → DeepLTranslateService.translate()
        → Se fallisce → nome originale
    → Salva Product con nameEn
  → Restituisce Product
```

### **3. Aggiornamento Profilo con Bio Venditore**
```
PUT /api/user/me
  → UserController.updateProfile()
  → UserService.updateUserProfile()
    → Validazione: descriptionOriginal max 500 caratteri
    → Se supera → Errore 400
    → Aggiorna campi (inclusi descriptionOriginal, descriptionLanguage)
    → Salva User
  → Restituisce UserProfileDTO
```

### **4. Traduzione Testo Generica**
```
POST /api/translate
  → TranslateController.translate()
  → UnifiedTranslationService.translate(text, targetLanguage)
    → Prova GPT-4o-mini
    → Se fallisce → Prova DeepL
    → Se fallisce → Testo originale
  → Restituisce {"translated": "..."}
```

---

## ⚙️ Configurazione

### **Environment Variables Richieste:**

**Traduzione:**
- `OPENAI_API_KEY` - Chiave API OpenAI (per GPT-4o-mini)
- `DEEPL_API_KEY` - Chiave API DeepL (per fallback)
- `DEEPL_API_URL` - URL API DeepL (default: https://api-free.deepl.com/v2/translate)

**Email:**
- `MAIL_HOST` - SMTP host
- `MAIL_PORT` - SMTP port
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `MAIL_FROM` - Email sender (default: no-reply@funkard.com)
- `MAIL_FALLBACK` - Email fallback (default: support@funkard.com)
- `MAIL_FROM_NAME` - Nome sender

**Storage:**
- `R2_ACCESS_KEY_ID` - Cloudflare R2 access key
- `R2_SECRET_ACCESS_KEY` - Cloudflare R2 secret key
- `R2_ENDPOINT` - R2 endpoint URL
- `R2_BUCKET_NAME` - R2 bucket name

**Database:**
- `DATABASE_URL` - PostgreSQL connection string
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password

**JWT:**
- `JWT_SECRET` - Secret per JWT token
- `JWT_EXPIRATION` - Expiration time (default: 86400000)

---

## 📊 Statistiche Progetto

- **File Java:** ~250 file
- **Controller:** 42 file (21 pubblici + 21 admin)
- **Service:** 32 file
- **Repository:** 20 file
- **Modelli:** 22 file
- **DTO:** 18 file
- **Migration:** 21 file
- **Endpoint API:** ~160+ endpoint
- **Lingue Supportate:** 25+ lingue (email)
- **Lingue Traduzione Chat:** 9 lingue (en, it, es, fr, de, pt, ru, ja, zh)

---

## 🔄 Modifiche Recenti (Ultima Sessione)

### **1. Aggiornamento Traduzione Chat** ✅
- Sostituito `TranslationService` con `UnifiedTranslationService` in `ChatService`
- Ora usa GPT-4o-mini + DeepL fallback
- Nessuna modifica a endpoint, DTO, entity

### **2. Generazione Automatica nameEn** ✅
- Implementato in `ProductService.generateGlobalEnglishName()`
- Usa GPT-4o-mini con prompt specializzato
- Fallback DeepL e nome originale

### **3. Validazione Bio Venditore** ✅
- Aggiunto `@Size(max = 500)` su `User` e `UserProfileDTO`
- Validazione esplicita in `UserService`
- Errore 400 se supera limite

### **4. Campi Traduzione Dinamica** ✅
- Aggiunti campi a `Product` e `User`
- Migration `V21__add_translation_fields_to_products_and_users.sql`
- Indici per performance

---

## 📝 Note Finali

### **Punti di Forza**
- ✅ Sistema traduzione unificato con fallback automatico
- ✅ Generazione automatica nameEn per prodotti
- ✅ Validazione bio venditore (GDPR compliant)
- ✅ Traduzione dinamica contenuti utente
- ✅ Chat con traduzione automatica (GPT+DeepL)
- ✅ Architettura modulare e scalabile
- ✅ Logging completo e tracciabilità
- ✅ GDPR compliance completo

### **Aree di Miglioramento Future**
- ⚠️ Cache traduzione (per ottimizzare costi API)
- ⚠️ Rilevamento automatico lingua testo
- ⚠️ Supporto più lingue per traduzione chat
- ⚠️ Logging traduzioni in database (per UnifiedTranslationService)

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15  
**Versione:** 3.0

