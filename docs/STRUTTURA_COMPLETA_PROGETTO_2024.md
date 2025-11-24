# 📚 STRUTTURA COMPLETA PROGETTO FUNKARD API

**Data aggiornamento:** 24 Novembre 2024  
**Versione:** 0.0.1-SNAPSHOT  
**Java:** 17  
**Spring Boot:** 3.5.6  
**Database:** PostgreSQL 17.5

---

## 📁 STRUTTURA AD ALBERO DEL PROGETTO

```
funkard-api/
├── src/
│   ├── main/
│   │   ├── java/com/funkard/
│   │   │   ├── FunkardApiApplication.java          # Entry point
│   │   │   │
│   │   │   ├── admin/                              # Modulo Admin Panel
│   │   │   │   ├── AdminConfig.java
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AdminService.java
│   │   │   │   ├── controller/                     # 21 controller admin
│   │   │   │   │   ├── AdminCleanupController.java
│   │   │   │   │   ├── AdminDashboardController.java
│   │   │   │   │   ├── AdminEmailLogController.java
│   │   │   │   │   ├── AdminFranchiseController.java
│   │   │   │   │   ├── AdminLegacyAuthController.java
│   │   │   │   │   ├── AdminNotificationActionController.java
│   │   │   │   │   ├── AdminNotificationArchiveController.java
│   │   │   │   │   ├── AdminNotificationBatchController.java
│   │   │   │   │   ├── AdminNotificationCleanupController.java
│   │   │   │   │   ├── AdminNotificationController.java
│   │   │   │   │   ├── AdminNotificationStreamController.java
│   │   │   │   │   ├── AdminPendingValueController.java
│   │   │   │   │   ├── AdminStatsController.java
│   │   │   │   │   ├── AdminSupportCleanupController.java
│   │   │   │   │   ├── AdminSupportController.java
│   │   │   │   │   ├── AdminValuationController.java
│   │   │   │   │   ├── CookieLogAdminController.java
│   │   │   │   │   ├── EmailTemplateTestController.java
│   │   │   │   │   ├── FranchiseAdminController.java
│   │   │   │   │   ├── RolePermissionController.java
│   │   │   │   │   └── SupportTicketController.java
│   │   │   │   ├── dto/                            # 10 DTO admin
│   │   │   │   ├── log/                            # Audit logging
│   │   │   │   │   ├── AdminActionLog.java
│   │   │   │   │   ├── AdminActionLogController.java
│   │   │   │   │   ├── AdminActionLogger.java
│   │   │   │   │   └── AdminActionLogRepository.java
│   │   │   │   ├── model/                          # 6 entità admin
│   │   │   │   ├── repository/                     # 4 repository admin
│   │   │   │   └── service/                        # 12 servizi admin
│   │   │   │
│   │   │   ├── adminaccess/                        # Sistema accesso admin
│   │   │   │   ├── controller/
│   │   │   │   │   └── AdminAccessController.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── AdminAccessRequest.java
│   │   │   │   │   └── AdminAccessToken.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── AdminAccessRequestRepository.java
│   │   │   │   │   └── AdminAccessTokenRepository.java
│   │   │   │   └── service/
│   │   │   │       └── AdminAccessService.java
│   │   │   │
│   │   │   ├── adminauth/                          # Autenticazione admin
│   │   │   │   ├── AccessRequest.java
│   │   │   │   ├── AccessRequestController.java
│   │   │   │   ├── AccessRequestRepository.java
│   │   │   │   ├── AccessRequestService.java
│   │   │   │   ├── AdminAuthController.java
│   │   │   │   ├── AdminBootstrap.java
│   │   │   │   ├── AdminTableInitializer.java
│   │   │   │   ├── AdminToken.java
│   │   │   │   ├── AdminTokenController.java
│   │   │   │   ├── AdminTokenRepository.java
│   │   │   │   ├── AdminTokenService.java
│   │   │   │   ├── AdminUser.java
│   │   │   │   ├── AdminUserRepository.java
│   │   │   │   └── AdminUserService.java
│   │   │   │
│   │   │   ├── api/
│   │   │   │   └── i18n/
│   │   │   │       └── SupportedLanguages.java     # 31 lingue supportate
│   │   │   │
│   │   │   ├── common/
│   │   │   │   └── GlobalExceptionHandler.java     # Gestione errori globale
│   │   │   │
│   │   │   ├── config/                             # Configurazioni
│   │   │   │   ├── CacheConfig.java                # Caffeine cache
│   │   │   │   ├── EmailConfig.java                # Config email (fallback)
│   │   │   │   ├── R2Config.java                  # Cloudflare R2
│   │   │   │   ├── SecurityConfig.java             # Spring Security + JWT
│   │   │   │   └── WebSocketConfig.java           # WebSocket/STOMP
│   │   │   │
│   │   │   ├── controller/                         # 21 controller pubblici
│   │   │   │   ├── AdminTicketAssignmentController.java
│   │   │   │   ├── AdsController.java
│   │   │   │   ├── AuthController.java            # Registrazione/Login
│   │   │   │   ├── CardController.java
│   │   │   │   ├── ChatController.java            # Chat utenti (traduzione)
│   │   │   │   ├── CollectionController.java
│   │   │   │   ├── FranchiseController.java       # Franchise pubblico
│   │   │   │   ├── GradeLensAdminController.java
│   │   │   │   ├── LanguageWhitelist.java          # Validazione lingue
│   │   │   │   ├── ListingController.java
│   │   │   │   ├── PendingValueController.java    # Proposte custom values
│   │   │   │   ├── RootController.java
│   │   │   │   ├── SupportChatController.java     # Chat supporto
│   │   │   │   ├── SupportController.java
│   │   │   │   ├── SupportWebSocketController.java
│   │   │   │   ├── TestController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── TranslateController.java      # API traduzione pubblica
│   │   │   │   ├── UserCardController.java
│   │   │   │   ├── UserController.java            # Profilo utente
│   │   │   │   └── WishlistController.java
│   │   │   │
│   │   │   ├── dto/                                # 19 DTO
│   │   │   │   ├── CardDTO.java
│   │   │   │   ├── ChatMessageDTO.java
│   │   │   │   ├── CookiePreferencesDTO.java
│   │   │   │   ├── CreateFranchiseRequest.java
│   │   │   │   ├── CreateListingRequest.java
│   │   │   │   ├── FranchiseDTO.java
│   │   │   │   ├── FranchiseProposalDTO.java
│   │   │   │   ├── ListingDTO.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── PendingValueDTO.java
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
│   │   │   ├── gradelens/                          # Sistema grading AI
│   │   │   │   ├── controller/
│   │   │   │   ├── GradeResult.java
│   │   │   │   ├── HeuristicAiProvider.java
│   │   │   │   ├── model/
│   │   │   │   └── service/
│   │   │   │
│   │   │   ├── grading/                            # Grading tradizionale
│   │   │   │   ├── controller/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   │
│   │   │   ├── market/                             # Marketplace
│   │   │   │   ├── controller/
│   │   │   │   │   ├── ProductController.java      # Prodotti (nameEn auto)
│   │   │   │   │   └── ...
│   │   │   │   ├── model/
│   │   │   │   │   ├── Product.java
│   │   │   │   │   └── ...
│   │   │   │   ├── repository/
│   │   │   │   ├── service/
│   │   │   │   │   ├── ProductService.java         # Genera nameEn con GPT
│   │   │   │   │   └── ...
│   │   │   │   └── trend/
│   │   │   │
│   │   │   ├── model/                              # 23 entità JPA
│   │   │   │   ├── Card.java
│   │   │   │   ├── CardSource.java
│   │   │   │   ├── CardType.java
│   │   │   │   ├── ChatMessage.java               # Con traduzione
│   │   │   │   ├── CookieConsentLog.java
│   │   │   │   ├── EmailLog.java
│   │   │   │   ├── Franchise.java
│   │   │   │   ├── FranchiseCatalog.java
│   │   │   │   ├── FranchiseProposal.java
│   │   │   │   ├── GradeLensResult.java
│   │   │   │   ├── GradeReport.java
│   │   │   │   ├── Listing.java
│   │   │   │   ├── PendingValue.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TranslationLog.java
│   │   │   │   ├── User.java                      # Con language, currency
│   │   │   │   ├── UserAddress.java
│   │   │   │   ├── UserCard.java
│   │   │   │   ├── UserDeletion.java
│   │   │   │   ├── UserPreferences.java
│   │   │   │   ├── VerificationToken.java
│   │   │   │   └── Wishlist.java
│   │   │   │
│   │   │   ├── payload/
│   │   │   │   └── RegisterRequest.java           # Con GDPR consent
│   │   │   │
│   │   │   ├── realtime/                           # Server-Sent Events
│   │   │   │   ├── AdminStreamController.java     # SSE admin support
│   │   │   │   ├── EventType.java
│   │   │   │   ├── RealtimeConfig.java
│   │   │   │   └── SupportStreamController.java   # SSE utenti
│   │   │   │
│   │   │   ├── repository/                         # 20 repository JPA
│   │   │   │   ├── CardRepository.java
│   │   │   │   ├── ChatMessageRepository.java
│   │   │   │   ├── CookieConsentLogRepository.java
│   │   │   │   ├── EmailLogRepository.java
│   │   │   │   ├── FranchiseCatalogRepository.java
│   │   │   │   ├── FranchiseProposalRepository.java
│   │   │   │   ├── FranchiseRepository.java
│   │   │   │   ├── ListingRepository.java
│   │   │   │   ├── PendingValueRepository.java
│   │   │   │   ├── TranslationLogRepository.java
│   │   │   │   ├── UserAddressRepository.java
│   │   │   │   ├── UserCardRepository.java
│   │   │   │   ├── UserDeletionRepository.java
│   │   │   │   ├── UserPreferencesRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── VerificationTokenRepository.java
│   │   │   │   └── WishlistRepository.java
│   │   │   │
│   │   │   ├── scheduler/                          # Job schedulati
│   │   │   │   ├── EmailLogCleanupScheduler.java  # Cleanup email logs
│   │   │   │   ├── GradeCleanupScheduler.java
│   │   │   │   └── UserDeletionScheduler.java     # GDPR deletion (7 giorni)
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── JwtFilter.java                 # Filtro JWT
│   │   │   │   └── JwtUtil.java                   # Utility JWT
│   │   │   │
│   │   │   ├── service/                            # 32 servizi
│   │   │   │   ├── AdminNotifier.java
│   │   │   │   ├── CardService.java
│   │   │   │   ├── ChatService.java               # Traduzione chat
│   │   │   │   ├── CookieConsentLogService.java
│   │   │   │   ├── CookieLogExportService.java    # Export GDPR (PDF/JSON)
│   │   │   │   ├── DeepLTranslateService.java     # Fallback traduzione
│   │   │   │   ├── EmailLocaleHelper.java
│   │   │   │   ├── EmailLogService.java
│   │   │   │   ├── EmailService.java              # Con fallback
│   │   │   │   ├── EmailTemplateManager.java      # Template multi-lingua
│   │   │   │   ├── EmailTemplateTestService.java
│   │   │   │   ├── FranchiseAdminService.java
│   │   │   │   ├── FranchiseCatalogService.java
│   │   │   │   ├── FranchiseJsonService.java      # Carica franchises.json
│   │   │   │   ├── GradeCalculator.java
│   │   │   │   ├── GradeLensCleanupService.java
│   │   │   │   ├── GradeReportLookupService.java
│   │   │   │   ├── ListingService.java
│   │   │   │   ├── OpenAiTranslateService.java    # GPT-4o-mini
│   │   │   │   ├── PendingValueService.java
│   │   │   │   ├── R2Service.java                 # Cloudflare R2 storage
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── TranslationException.java
│   │   │   │   ├── TranslationProvider.java       # Interface traduzione
│   │   │   │   ├── TranslationService.java
│   │   │   │   ├── UnifiedTranslationService.java # GPT + DeepL fallback
│   │   │   │   ├── UserAccountDeletionService.java
│   │   │   │   ├── UserAddressService.java
│   │   │   │   ├── UserDeletionService.java       # Cancellazione GDPR
│   │   │   │   ├── UserPreferencesService.java    # Cookie preferences
│   │   │   │   ├── UserService.java
│   │   │   │   └── WishlistService.java
│   │   │   │
│   │   │   ├── storage/
│   │   │   │   └── ImageStorageService.java
│   │   │   │
│   │   │   └── user/
│   │   │       └── payment/
│   │   │
│   │   └── resources/
│   │       ├── application.properties              # Config dev
│   │       ├── application-prod.yml                 # Config produzione
│   │       ├── data/
│   │       │   └── franchises.json                # Catalogo franchise
│   │       ├── db/
│   │       │   ├── migration/                     # 21 migrazioni Flyway
│   │       │   │   ├── V1__add_grading_columns_to_usercard.sql
│   │       │   │   ├── V2__add_preferred_currency_to_users.sql
│   │       │   │   ├── V3__create_user_addresses_table.sql
│   │       │   │   ├── V4__create_admin_tokens_and_access_requests.sql
│   │       │   │   ├── V5__add_gdpr_consent_timestamps_to_users.sql
│   │       │   │   ├── V6__create_user_preferences_table.sql
│   │       │   │   ├── V7__create_cookie_consent_logs_table.sql
│   │       │   │   ├── V8__update_cookie_consent_logs_add_action_useragent.sql
│   │       │   │   ├── V9__create_user_deletions_table.sql
│   │       │   │   ├── V10__add_deletion_fields_to_users.sql
│   │       │   │   ├── V11__create_email_logs_table.sql
│   │       │   │   ├── V12__add_language_to_users.sql
│   │       │   │   ├── V13__add_translation_fields_to_support_messages.sql
│   │       │   │   ├── V14__create_chat_messages_table.sql
│   │       │   │   ├── V15__create_translation_logs_table.sql
│   │       │   │   ├── V16__create_pending_values_table.sql
│   │       │   │   ├── V17__add_category_franchise_to_cards.sql
│   │       │   │   ├── V18__create_franchise_catalog_table.sql
│   │       │   │   ├── V19__create_franchises_table.sql
│   │       │   │   ├── V20__create_franchise_proposals_table.sql
│   │       │   │   └── V21__add_translation_fields_to_products_and_users.sql
│   │       │   └── admin_users_table.sql
│   │       └── email-templates/                   # Template multi-lingua
│   │           ├── en/                            # 6 template inglese
│   │           ├── it/                            # 6 template italiano
│   │           └── [25+ altre lingue]/           # Placeholder per altre lingue
│   │
│   └── test/
│
├── docs/                                          # Documentazione
├── Dockerfile
├── Makefile
├── pom.xml                                        # Maven dependencies
├── render.yaml                                    # Config Render.com
└── README.md
```

---

## 🔌 ENDPOINT API COMPLETI

### **AUTENTICAZIONE E UTENTI**

#### **AuthController** (`/api/auth`)
- `POST /api/auth/register` - Registrazione (GDPR: terms/privacy required)
  - Body: `RegisterRequest` (email, password, language, preferredCurrency, acceptTerms, acceptPrivacy)
  - Response: `LoginResponse` (token, language, preferredCurrency)
- `POST /api/auth/login` - Login
  - Response: `LoginResponse` (token, language, preferredCurrency)
- `GET /api/auth/validate?token=...` - Valida token JWT

#### **UserController** (`/api/user`)
- `GET /api/user/me` - Profilo utente corrente
  - Response: `UserProfileDTO` (include language, preferredCurrency, descriptionOriginal, descriptionLanguage)
- `PUT /api/user/me` - Aggiorna profilo
  - Validazione: `descriptionOriginal` max 500 caratteri
  - Validazione: `language` deve essere in whitelist (31 lingue)
- `PATCH /api/user/preferences` - Aggiorna preferenze (language, preferredCurrency)
  - Validazione: `language` deve essere in whitelist
- `GET /api/user/address` - Lista indirizzi
- `POST /api/user/address` - Crea indirizzo
- `PUT /api/user/address/{id}` - Aggiorna indirizzo
- `DELETE /api/user/address/{id}` - Elimina indirizzo
- `PATCH /api/user/address/{id}/default` - Imposta default
- `GET /api/user/address/default` - Ottieni indirizzo default
- `GET /api/user/preferences` - Ottieni preferenze
- `PUT /api/user/preferences` - Salva preferenze
- `GET /api/user/preferences/cookies` - Ottieni preferenze cookie
- `POST /api/user/preferences/cookies` - Salva preferenze cookie (GDPR logging)
  - Body: `CookiePreferencesDTO`
  - Logga in `cookie_consent_logs` (action, oldPreferences, newPreferences, ipAddress, userAgent)
- `GET /api/user/preferences/cookies/export?format=json|pdf` - Export log cookie (GDPR Art. 15, 20)
  - Solo utente stesso o admin
  - Formato: JSON (default) o PDF
- `DELETE /api/user/delete-account` - Richiedi cancellazione account (GDPR Art. 17)
  - Crea record in `user_deletions` con `scheduledDeletionAt = now() + 7 giorni`
  - Disabilita login immediatamente
  - `UserDeletionScheduler` esegue cancellazione dopo 7 giorni

---

### **MARKETPLACE E PRODOTTI**

#### **ProductController** (`/api/products`)
- `GET /api/products` - Lista prodotti (con paginazione)
- `GET /api/products/{id}` - Dettaglio prodotto
- `POST /api/products` - Crea prodotto
  - **Auto-genera `nameEn`**: Usa `UnifiedTranslationService` con prompt GPT avanzato
  - Salva `descriptionOriginal`, `descriptionLanguage`
  - Fallback: DeepL se GPT fallisce, originale se entrambi falliscono

#### **ListingController** (`/api/listings`)
- `GET /api/listings` - Lista annunci (con filtri)
- `GET /api/listings/{id}` - Dettaglio annuncio
- `POST /api/listings` - Crea annuncio
  - Gestisce valori custom: `customTcg`, `customLanguage`, `customFranchise`
  - Crea record in `pending_values` per approvazione admin
- `PUT /api/listings/{id}` - Aggiorna annuncio
- `DELETE /api/listings/{id}` - Elimina annuncio

#### **FranchiseController** (`/api/franchises`)
- `GET /api/franchises` - Lista franchise (pubblico, da `franchises.json`)
  - Cache: Caffeine (25s TTL)
  - Formato: `[{"category": "...", "franchises": [...]}]`
- `POST /api/franchises/propose` - Proponi nuovo franchise
  - Body: `ProposeFranchiseRequest` (category, franchise)
  - Crea record in `franchise_proposals` (status: PENDING)
  - Invia notifica admin

---

### **TRADUZIONE**

#### **TranslateController** (`/api/translate`)
- `POST /api/translate` - Traduzione pubblica
  - Body: `TranslateRequest` (text, targetLanguage)
  - Response: `TranslateResponse` (translated)
  - Validazione: `targetLanguage` deve essere in `SupportedLanguages.ALL` (31 lingue)
  - Usa: `UnifiedTranslationService` (GPT-4o-mini → DeepL fallback)

#### **Traduzione Automatica Integrata**
- **Chat utenti** (`ChatService`): Traduce messaggi se lingue diverse
- **Support chat** (`SupportMessageService`): Traduce se admin/utente hanno lingue diverse
- **Prodotti** (`ProductService`): Genera `nameEn` con GPT (normalizzazione, non traduzione letterale)
- **Email** (`EmailTemplateManager`): Carica template in lingua utente (fallback EN)

---

### **CHAT E COMUNICAZIONE**

#### **ChatController** (`/api/chat`)
- `POST /api/chat/message` - Invia messaggio
  - **Traduzione automatica**: Se `sender.language != recipient.language`
  - Salva: `originalText`, `translatedText`, `originalLanguage`, `targetLanguage`, `isTranslated`
  - Logga in `translation_logs`
- `GET /api/chat/conversation/{userId}` - Conversazione con utente
- `GET /api/chat/unread` - Conta messaggi non letti
- `PUT /api/chat/message/{messageId}/read` - Marca come letto

#### **SupportController** (`/api/support`)
- `POST /api/support/tickets` - Crea ticket supporto
- `GET /api/support/tickets` - Lista ticket utente
- `GET /api/support/tickets/{id}` - Dettaglio ticket

#### **SupportChatController** (`/api/support/chat`)
- `POST /api/support/chat/{ticketId}/message` - Invia messaggio supporto
  - **Traduzione automatica**: Se `admin.language != user.language`
- `GET /api/support/chat/{ticketId}/messages` - Messaggi ticket

---

### **REAL-TIME (SSE)**

#### **SupportStreamController** (`/api/support/stream`)
- `GET /api/support/stream?email={userEmail}` - SSE per notifiche utenti
  - Eventi: `CONNECTED`, `NEW_REPLY`, `TICKET_RESOLVED`, `TICKET_CLOSED`, `STATUS_UPDATE`, `PING`
  - Keep-alive: ogni 30 secondi
- `POST /api/support/stream/events` - Test invio eventi (admin)
- `GET /api/support/stream/stats` - Statistiche connessioni (admin)

#### **AdminStreamController** (`/api/admin/support/stream`)
- `GET /api/admin/support/stream?userId={adminId}&role={role}` - SSE per admin
  - Ruoli: `SUPER_ADMIN`, `ADMIN`, `SUPPORT`
  - Eventi: `CONNECTED`, `NEW_TICKET`, `NEW_MESSAGE`, `TICKET_ASSIGNED`, `TICKET_RESOLVED`, `TICKET_CLOSED`, `STATUS_UPDATE`, `PING`
- `POST /api/admin/support/stream/events` - Test invio eventi
- `GET /api/admin/support/stream/stats` - Statistiche connessioni

---

### **CARDS E COLLEZIONI**

#### **CardController** (`/api/cards`)
- `GET /api/cards` - Lista carte
- `GET /api/cards/{id}` - Dettaglio carta
- `POST /api/cards` - Crea carta (admin)
- `PUT /api/cards/{id}` - Aggiorna carta (admin)
- `DELETE /api/cards/{id}` - Elimina carta (admin)

#### **UserCardController** (`/api/user-cards`)
- `GET /api/user-cards` - Carte utente
- `POST /api/user-cards` - Aggiungi carta
- `PUT /api/user-cards/{id}` - Aggiorna carta
- `DELETE /api/user-cards/{id}` - Elimina carta

#### **CollectionController** (`/api/collections`)
- `GET /api/collections` - Collezioni utente

---

### **WISHLIST E TRANSAZIONI**

#### **WishlistController** (`/api/wishlist`)
- `GET /api/wishlist` - Lista wishlist
- `POST /api/wishlist` - Aggiungi a wishlist
- `DELETE /api/wishlist/{id}` - Rimuovi da wishlist

#### **TransactionController** (`/api/transactions`)
- `GET /api/transactions` - Lista transazioni utente
- `GET /api/transactions/{id}` - Dettaglio transazione
- `POST /api/transactions` - Crea transazione
- `PUT /api/transactions/{id}/complete` - Completa transazione

---

### **PENDING VALUES (PROPOSTE CUSTOM)**

#### **PendingValueController** (`/api/pending-values`)
- `POST /api/pending-values/submit` - Invia proposta valore custom
  - Body: `SubmitPendingValueRequest` (type: TCG/LANGUAGE/FRANCHISE, value)
  - Crea record in `pending_values` (status: PENDING)
- `GET /api/pending-values/my` - Le mie proposte

---

### **GRADING**

#### **GradeLensAdminController** (`/api/gradelens`)
- `POST /api/gradelens/grade` - Grading AI carta
- `GET /api/gradelens/result/{id}` - Risultato grading

---

### **ADMIN PANEL**

#### **Admin Dashboard**
- `GET /api/admin/dashboard` - Dashboard con statistiche complete
- `GET /api/admin/stats` - Statistiche aggregate
- `GET /api/admin/stats/users` - Statistiche utenti
- `GET /api/admin/stats/market` - Statistiche marketplace
- `GET /api/admin/stats/support` - Statistiche supporto

#### **Admin Support**
- `GET /api/admin/support/tickets` - Lista ticket (con filtri)
- `GET /api/admin/support/tickets/{id}` - Dettaglio ticket
- `PUT /api/admin/support/tickets/{id}/assign` - Assegna ticket
- `PUT /api/admin/support/tickets/{id}/resolve` - Risolvi ticket
- `PUT /api/admin/support/tickets/{id}/close` - Chiudi ticket

#### **Admin Franchise** (`/api/admin/franchises`)
- `GET /api/admin/franchises` - Lista franchise e proposte
  - Query: `?status=pending|active|disabled`
  - Response: `{proposals: [...], franchises: [...], stats: {...}}`
- `POST /api/admin/franchises/approve/{proposalId}` - Approva proposta
  - Crea `Franchise` record
  - Aggiorna `franchises.json`
  - Invia notifica admin
- `POST /api/admin/franchises/reject/{proposalId}` - Rifiuta proposta
  - Invia email utente (se presente)
- `PATCH /api/admin/franchises/{id}/disable` - Disabilita franchise
- `PATCH /api/admin/franchises/{id}/enable` - Riabilita franchise
- `POST /api/admin/franchises/add` - Crea franchise manualmente

#### **Admin Franchise Catalog** (`/api/admin/franchises/catalog`)
- `GET /api/admin/franchises/catalog` - Lista franchise catalogo
- `POST /api/admin/franchises/catalog` - Crea franchise catalogo
- `PUT /api/admin/franchises/catalog/{id}` - Aggiorna franchise catalogo
- `DELETE /api/admin/franchises/catalog/{id}` - Elimina franchise catalogo
- `GET /api/admin/franchises/catalog/stats` - Statistiche catalogo

#### **Admin Pending Values**
- `GET /api/admin/pending-values` - Lista proposte (con filtri)
- `POST /api/admin/pending-values/{id}/approve` - Approva proposta
- `DELETE /api/admin/pending-values/{id}` - Rifiuta proposta

#### **Admin Email Logs**
- `GET /api/admin/email-logs` - Lista log email (con paginazione)
- `GET /api/admin/email-logs/{id}` - Dettaglio log email

#### **Admin Cookie Logs**
- `GET /api/admin/cookie-logs` - Lista log cookie (con filtri)
- `GET /api/admin/cookie-logs/export?userId={userId}&format=json|pdf` - Export log utente

#### **Admin Notifications**
- `GET /api/admin/notifications` - Lista notifiche
- `POST /api/admin/notifications/{id}/mark-read` - Marca come letta
- `POST /api/admin/notifications/{id}/archive` - Archivia notifica
- `GET /api/admin/notifications/stream` - SSE notifiche real-time

---

### **ADMIN AUTHENTICATION**

#### **AdminAuthController** (`/api/admin/auth`)
- `POST /api/admin/auth/login` - Login admin (con access token)
- `GET /api/admin/auth/me` - Profilo admin corrente

#### **AdminTokenController** (`/api/admin/tokens`)
- `GET /api/admin/tokens` - Lista token attivi
- `POST /api/admin/tokens` - Crea nuovo token
- `POST /api/admin/tokens/{id}/regenerate` - Rigenera token
- `DELETE /api/admin/tokens/{id}` - Disabilita token

#### **AccessRequestController** (`/api/admin/access-requests`)
- `GET /api/admin/access-requests` - Lista richieste accesso
- `POST /api/admin/access-requests` - Crea richiesta accesso
- `POST /api/admin/access-requests/{id}/approve` - Approva richiesta
- `POST /api/admin/access-requests/{id}/reject` - Rifiuta richiesta

---

## 🗄️ SCHEMA DATABASE

### **Tabelle Principali**

#### **users**
- `id`, `email`, `password`, `name`, `role`
- `language` (varchar(5), default 'en') - Lingua utente
- `preferred_currency` (varchar(3), default 'EUR') - Valuta preferita
- `description_original` (TEXT) - Bio venditore originale
- `description_language` (varchar(5)) - Lingua bio
- `terms_accepted_at` (timestamp) - GDPR
- `privacy_accepted_at` (timestamp) - GDPR
- `deletion_pending` (boolean) - GDPR
- `deletion_requested_at` (timestamp) - GDPR

#### **products**
- `id`, `name`, `name_en` (generato automaticamente con GPT)
- `description_original` (TEXT)
- `description_language` (varchar(5))
- `price`, `currency`, `seller_id`, `category`, `franchise`, `language`

#### **chat_messages**
- `id`, `sender_id`, `recipient_id`
- `original_text`, `translated_text`
- `original_language`, `target_language`
- `is_translated` (boolean)
- `created_at`, `read_at`

#### **support_messages**
- `id`, `ticket_id`, `sender_id`, `message`
- `original_language`, `target_language`
- `translated_text`, `is_translated`

#### **franchises**
- `id`, `category`, `name`, `status` (ACTIVE/DISABLED)
- `created_at`, `updated_at`

#### **franchise_proposals**
- `id`, `category`, `franchise`, `user_id`, `user_email`
- `status` (PENDING/APPROVED/REJECTED)
- `processed_by`, `processed_at`, `created_at`

#### **pending_values**
- `id`, `type` (TCG/LANGUAGE/FRANCHISE), `value`
- `submitted_by`, `approved` (boolean)
- `approved_by`, `approved_at`, `created_at`

#### **user_preferences**
- `id`, `user_id`
- `cookies_accepted` (boolean)
- `cookies_preferences` (TEXT, JSON)
- `cookies_accepted_at` (timestamp)

#### **cookie_consent_logs**
- `id`, `user_id`, `action` (ACCEPT/REJECT/UPDATE)
- `old_preferences` (TEXT), `new_preferences` (TEXT)
- `ip_address`, `user_agent`, `created_at`

#### **user_deletions**
- `id`, `user_id`, `email`
- `requested_at`, `scheduled_deletion_at`
- `status` (PENDING/COMPLETED/FAILED)
- `completed_at`, `reason`

#### **email_logs**
- `id`, `recipient`, `sender`, `subject`
- `type`, `status` (SENT/FAILED/RETRIED)
- `locale`, `template_name`
- `retry_count`, `error_message`, `sent_at`

#### **translation_logs**
- `id`, `user_id`, `message_id`, `message_type`
- `source_language`, `target_language`
- `source_text`, `translated_text`
- `translation_provider` (GPT/DEEPL)
- `success` (boolean), `error_message`, `created_at`

---

## 🔗 COLLEGAMENTI E DIPENDENZE

### **Sistema di Traduzione**

```
TranslateController
    ↓
UnifiedTranslationService
    ├──→ OpenAiTranslateService (GPT-4o-mini) [PRIMARY]
    └──→ DeepLTranslateService [FALLBACK]
         ↓
    TranslationLog (logging)
```

**Utilizzato da:**
- `ChatService` → Traduzione messaggi chat
- `SupportMessageService` → Traduzione messaggi supporto
- `ProductService` → Generazione `nameEn` (normalizzazione)
- `EmailTemplateManager` → Selezione template lingua

---

### **Sistema Email**

```
EmailService
    ├──→ Primary SMTP (no-reply@funkard.com)
    └──→ Fallback SMTP (support@funkard.com)
         ↓
    EmailTemplateManager
         ├──→ Carica template da /email-templates/{locale}/
         └──→ Fallback a /email-templates/en/
         ↓
    EmailLog (audit logging)
```

**Template disponibili:**
- `account_confirmation.html`
- `account_deletion.html`
- `order_confirmation.html`
- `order_shipped.html`
- `password_reset.html`
- `ticket_opened.html`

**Lingue supportate:** 31 lingue (en, it, es, fr, de, pt, ja, zh, ru, ar, hi, ko, tr, id, vi, bn, tl, pl, nl, sv, no, da, el, cs, hu, ro, uk, th, ms, fa, sq)

---

### **Sistema GDPR**

```
UserController.deleteAccount()
    ↓
UserAccountDeletionService
    ↓
user_deletions (status: PENDING, scheduledDeletionAt: +7 giorni)
    ↓
UserDeletionScheduler (cron: ogni ora)
    ↓
UserDeletionService.deleteUserPermanently()
    ├──→ Elimina user record
    ├──→ Elimina user_cards + R2 files
    ├──→ Elimina wishlist
    ├──→ Elimina user_addresses
    ├──→ Elimina user_preferences
    ├──→ Elimina support_tickets
    ├──→ Elimina cookie_consent_logs
    └──→ Elimina translation_logs
    ↓
EmailService.sendAccountDeletionCompletedEmail()
    ↓
user_deletions (status: COMPLETED)
```

---

### **Sistema Franchise**

```
FranchiseController
    ├──→ GET /api/franchises
    │       ↓
    │   FranchiseJsonService
    │       ↓
    │   Carica da /data/franchises.json (cache: 25s)
    │
    └──→ POST /api/franchises/propose
            ↓
        franchise_proposals (status: PENDING)
            ↓
        AdminNotifier.notify(...)
            ↓
FranchiseAdminController
    ├──→ POST /api/admin/franchises/approve/{id}
    │       ├──→ Crea Franchise record
    │       ├──→ Aggiorna franchises.json
    │       └──→ AdminNotifier
    │
    └──→ POST /api/admin/franchises/reject/{id}
            └──→ EmailService (se userEmail presente)
```

---

### **Sistema Real-Time (SSE)**

```
SupportStreamController
    ├──→ GET /api/support/stream?email={email}
    │       ↓
    │   SseEmitter (connessione utente)
    │       ↓
    │   SupportStreamController.sendEventToUser()
    │       ↓
    │   Eventi: NEW_REPLY, TICKET_RESOLVED, etc.
    │
    └──→ Keep-alive (ogni 30s)

AdminStreamController
    ├──→ GET /api/admin/support/stream?userId={id}&role={role}
    │       ↓
    │   SseEmitter (connessione admin, separata per ruolo)
    │       ↓
    │   AdminStreamController.sendToRole() / sendToUser()
    │       ↓
    │   Eventi: NEW_TICKET, NEW_MESSAGE, TICKET_ASSIGNED, etc.
    │
    └──→ Keep-alive (ogni 30s)
```

**Chiamato da:**
- `SupportTicketService` → Notifica nuovi ticket/messaggi
- `SupportMessageService` → Notifica nuovi messaggi

---

### **Sistema Cache**

```
CacheConfig
    ↓
Caffeine CacheManager
    ├──→ expireAfterWrite: 25s
    └──→ maximumSize: 500
         ↓
@Cacheable("homepage:latest")
@Cacheable("homepage:trending")
@Cacheable("marketplace:search")
@Cacheable("marketplace:filters")
@Cacheable("reference:brands")
```

**Metodi cached:**
- `FranchiseJsonService.getAllFranchises()` → `"reference:brands"`
- Altri metodi pubblici read-only (da verificare)

---

### **Sistema Storage**

```
R2Service (Cloudflare R2, S3-compatible)
    ├──→ Upload file (immagini carte, prodotti)
    ├──→ Delete file (durante cancellazione utente)
    └──→ Get file URL
```

---

## 🔐 SICUREZZA E AUTENTICAZIONE

### **JWT Authentication**

```
JwtFilter
    ├──→ Intercetta richieste (esclusi /api/auth/**)
    ├──→ Valida token JWT
    ├──→ Estrae user da token
    └──→ Imposta Authentication in SecurityContext
         ↓
SecurityConfig
    ├──→ Configura Spring Security
    ├──→ CORS: funkard.com, admin.funkard.com
    └──→ @PreAuthorize("hasRole('USER')") / hasRole('ADMIN')
```

### **Admin Authentication**

```
AdminAuthController
    ├──→ Login con access token
    └──→ Genera JWT per admin
         ↓
AdminUserService
    ├──→ Verifica access token
    └──→ Crea/aggiorna AdminUser
         ↓
AdminBootstrap (on startup)
    └──→ Crea SUPER_ADMIN se non esiste
```

---

## 📊 SCHEDULER E JOB

### **UserDeletionScheduler**
- **Cron:** `0 0 * * * *` (ogni ora)
- **Funzione:** Cancella account dopo 7 giorni dalla richiesta
- **Chiama:** `UserDeletionService.deleteUserPermanently()`
- **Invia:** Email conferma cancellazione

### **EmailLogCleanupScheduler**
- **Funzione:** Rimuove log email vecchi (>90 giorni)

### **GradeCleanupScheduler**
- **Funzione:** Cleanup report grading vecchi

---

## 🌍 GESTIONE LINGUE

### **SupportedLanguages** (31 lingue)
```java
public static final Set<String> ALL = Set.of(
    "en", "it", "es", "fr", "de", "pt", "ja", "zh", "ru",
    "ar", "hi", "ko", "tr", "id", "vi", "bn", "tl", "pl", "nl", "sv", "no", "da",
    "el", "cs", "hu", "ro", "uk", "th", "ms", "fa", "sq"
);
```

**Utilizzato da:**
- `TranslateController` → Validazione `targetLanguage`
- `LanguageWhitelist` → Validazione `user.language` in `UserController`
- `EmailTemplateManager` → Selezione template

---

## 📦 DIPENDENZE PRINCIPALI

```xml
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Security
- Spring Mail
- PostgreSQL Driver 42.7.4
- Lombok 1.18.30
- AWS SDK v2 (Cloudflare R2)
- OpenCV 4.9.0 (Grading)
- iText 7 (PDF export)
- Caffeine (Cache)
- Jackson (JSON)
```

---

## 🔄 FLUSSI PRINCIPALI

### **1. Registrazione Utente**
```
POST /api/auth/register
    ↓
AuthController.register()
    ├──→ Valida: acceptTerms && acceptPrivacy (GDPR)
    ├──→ Crea User (termsAcceptedAt, privacyAcceptedAt = now())
    ├──→ Genera JWT token
    └──→ EmailService.sendAccountConfirmationEmail()
         ↓
    Response: {token, language, preferredCurrency}
```

### **2. Creazione Prodotto con Traduzione**
```
POST /api/products
    ↓
ProductController.create()
    ↓
ProductService.createProduct()
    ├──→ Salva descriptionOriginal, descriptionLanguage
    ├──→ ProductService.generateGlobalEnglishName()
    │       ↓
    │   UnifiedTranslationService.translate()
    │       ├──→ GPT-4o-mini (prompt avanzato normalizzazione)
    │       └──→ DeepL (fallback)
    │       ↓
    │   Salva nameEn
    └──→ Salva Product
```

### **3. Chat con Traduzione Automatica**
```
POST /api/chat/message
    ↓
ChatService.sendMessage()
    ├──→ Recupera sender.language, recipient.language
    ├──→ Se lingue diverse:
    │       ↓
    │   UnifiedTranslationService.translate()
    │       ├──→ GPT-4o-mini
    │       └──→ DeepL (fallback)
    │       ↓
    │   Salva: originalText, translatedText, originalLanguage, targetLanguage, isTranslated
    │   Logga in translation_logs
    └──→ Salva ChatMessage
         ↓
    SupportStreamController.sendEventToUser() (notifica real-time)
```

### **4. Cancellazione Account GDPR**
```
DELETE /api/user/delete-account
    ↓
UserController.deleteAccount()
    ↓
UserAccountDeletionService.requestDeletion()
    ├──→ Crea UserDeletion (status: PENDING, scheduledDeletionAt: +7 giorni)
    ├──→ user.deletionPending = true
    └──→ user.deletionRequestedAt = now()
         ↓
UserDeletionScheduler (ogni ora)
    ├──→ Trova UserDeletion con scheduledDeletionAt <= now()
    ├──→ UserDeletionService.deleteUserPermanently()
    │       ├──→ Elimina tutti i dati utente
    │       └──→ Elimina file R2
    ├──→ EmailService.sendAccountDeletionCompletedEmail()
    └──→ UserDeletion.status = COMPLETED
```

### **5. Proposta Franchise**
```
POST /api/franchises/propose
    ↓
FranchiseController.propose()
    ↓
FranchiseAdminService.createProposal()
    ├──→ Crea FranchiseProposal (status: PENDING)
    └──→ AdminNotifier.notify("Nuova proposta franchise")
         ↓
Admin Panel
    ├──→ GET /api/admin/franchises (vede proposta)
    ├──→ POST /api/admin/franchises/approve/{id}
    │       ├──→ Crea Franchise record
    │       ├──→ Aggiorna franchises.json
    │       └──→ AdminNotifier
    └──→ POST /api/admin/franchises/reject/{id}
            └──→ EmailService (se userEmail presente)
```

---

## 📈 STATISTICHE E MONITORAGGIO

### **Metriche Disponibili**
- Connessioni SSE attive (admin e utenti)
- Statistiche franchise (proposte, approvate, rifiutate)
- Statistiche email (inviate, fallite, retry)
- Statistiche traduzioni (successi, fallimenti, provider utilizzato)
- Statistiche supporto (ticket aperti, risolti, in attesa)

---

## 🎯 FUNZIONALITÀ CHIAVE

### **✅ Implementate**
1. ✅ Registrazione/Login con GDPR compliance
2. ✅ Gestione lingua e valuta utente (31 lingue)
3. ✅ Traduzione automatica (GPT-4o-mini + DeepL fallback)
4. ✅ Generazione automatica `nameEn` per prodotti
5. ✅ Chat con traduzione automatica
6. ✅ Support chat con traduzione
7. ✅ Email multi-lingua con fallback
8. ✅ Cookie preferences con logging GDPR
9. ✅ Export log cookie (PDF/JSON) - GDPR Art. 15, 20
10. ✅ Cancellazione account con scheduler (7 giorni) - GDPR Art. 17
11. ✅ Email conferma cancellazione
12. ✅ Sistema franchise con proposte e approvazioni
13. ✅ Pending values (TCG/Language custom)
14. ✅ Real-time notifications (SSE)
15. ✅ Admin panel completo
16. ✅ Audit logging (email, admin actions)
17. ✅ Cache Caffeine per performance
18. ✅ Storage Cloudflare R2
19. ✅ Grading AI (GradeLens)

### **🔧 Configurazioni**
- **Database:** PostgreSQL (Neon) con HikariCP (max 5 connections)
- **Cache:** Caffeine (25s TTL, max 500 entries)
- **Email:** SMTP con fallback automatico
- **Storage:** Cloudflare R2 (S3-compatible)
- **Traduzione:** OpenAI GPT-4o-mini + DeepL
- **Security:** JWT + Spring Security
- **Real-time:** Server-Sent Events (SSE)

---

## 📝 NOTE TECNICHE

### **Validazione Lingue**
- `LanguageWhitelist.isValid()` → Valida `user.language` in `PUT /api/user/me` e `PATCH /api/user/preferences`
- `SupportedLanguages.ALL` → Utilizzato da `TranslateController` per validazione `targetLanguage`

### **Traduzione Prodotti**
- `nameEn` viene generato con prompt GPT avanzato (normalizzazione, non traduzione letterale)
- Se GPT fallisce → DeepL
- Se entrambi falliscono → `nameEn = null` (mantiene nome originale)

### **Email Fallback**
- Primary: `no-reply@funkard.com`
- Fallback: `support@funkard.com`
- Se entrambi falliscono → Alert a `legal@funkard.com`

### **GDPR Compliance**
- ✅ Consenso Terms/Privacy obbligatorio alla registrazione
- ✅ Cookie preferences con logging completo
- ✅ Export dati utente (cookie logs)
- ✅ Cancellazione account con periodo di grazia (7 giorni)
- ✅ Email conferma cancellazione
- ✅ Audit logging completo

---

**Documento generato il:** 24 Novembre 2024  
**Versione backend:** 0.0.1-SNAPSHOT

