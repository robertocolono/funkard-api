# 🔐 Standardizzazione Autenticazione e Paginazione - Funkard Backend

**Data:** 2025-01-15  
**Obiettivo:** Standardizzare autenticazione JWT, rimuovere X-Admin-Token, aggiungere paginazione a tutti gli endpoint lista

---

## ✅ Modifiche Completate

### 1. **SecurityConfig.java** ✅
- ✅ Aggiunto `@EnableMethodSecurity(prePostEnabled = true)`
- ✅ Rimosso `permitAll()` da `/api/admin/**`
- ✅ Aggiornato CORS: rimossi domini Vercel vecchi, aggiunto `admin.funkard.com`
- ✅ Rimosso header `X-Admin-Token` da CORS allowed headers

### 2. **JwtFilter.java** ✅
- ✅ Aggiunta gestione ruoli con `SimpleGrantedAuthority`
- ✅ Ruoli supportati: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- ✅ Aggiunto logging per autenticazioni: `✅ Authenticated request by {email} ({role}) to {endpoint}`

### 3. **AdminStatsController.java** ✅
- ✅ Rimosso controllo `X-Admin-Token`
- ✅ Aggiunto `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")`
- ✅ Aggiunto logging
- ✅ Aggiornato CORS

### 4. **AdminSupportController.java** ✅
- ✅ Rimosso `AdminAuthHelper` e controlli `X-Admin-Token`
- ✅ Aggiunto `@PreAuthorize` a tutti gli endpoint
- ✅ Aggiunta paginazione a `GET /tickets` e `GET /assigned/{supportEmail}`
- ✅ Aggiunto logging completo

### 5. **AdminNotificationController.java** ✅
- ✅ Aggiunto `@PreAuthorize` a tutti gli endpoint
- ✅ Aggiunta paginazione a `GET /` (lista notifiche)
- ✅ Aggiunto logging completo
- ✅ Aggiornato CORS

### 6. **SupportTicketService.java** ✅
- ✅ Aggiunto metodo `findAll(Pageable pageable)`
- ✅ Aggiunto metodo `findByAssignedTo(String supportEmail, Pageable pageable)`

---

## ⚠️ Modifiche Necessarie (Da Completare)

### **Controller da Aggiornare**

#### **Admin Controllers**

1. **AdminDashboardController.java**
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
   ```
   - Rimuovere controlli manuali se presenti
   - Aggiungere logging

2. **AdminSupportCleanupController.java**
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
   ```

3. **AdminNotificationBatchController.java**
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
   ```

4. **AdminNotificationArchiveController.java**
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
   // Aggiungere paginazione a GET /archive
   ```

5. **AdminNotificationActionController.java**
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
   ```

6. **AdminNotificationCleanupController.java**
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
   ```

7. **AdminCleanupController.java**
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
   ```

8. **AdminValuationController.java**
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
   ```

9. **AdminController.java**
   ```java
   @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
   ```

10. **SupportTicketController.java**
    ```java
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    // Aggiungere paginazione a GET /
    ```

11. **AdminActionLogController.java**
    ```java
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    // Aggiungere paginazione a GET /{type}/{id}
    ```

12. **RolePermissionController.java**
    ```java
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    ```

13. **AdminTicketAssignmentController.java**
    ```java
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    ```

14. **SystemMaintenanceController.java**
    ```java
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    ```

15. **AdminLegacyAuthController.java**
    - ⚠️ **DEPRECATO** - Considerare rimozione o migrazione completa

#### **AdminAuth Controllers**

16. **AdminAuthController.java**
    - Rimuovere controlli `X-Admin-Token`
    - Usare `Principal` o `@AuthenticationPrincipal` per ottenere utente corrente
    - Aggiungere `@PreAuthorize` dove necessario

17. **AdminTokenController.java**
    - Rimuovere controlli manuali `SUPER_ADMIN_TOKEN`
    - Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")`

18. **AccessRequestController.java**
    - Rimuovere controlli `X-Admin-Token`
    - Aggiungere `@PreAuthorize` appropriato

19. **AdminAccessController.java**
    - Rimuovere controlli manuali `SUPER_ADMIN_TOKEN`
    - Aggiungere `@PreAuthorize("hasRole('SUPER_ADMIN')")` per endpoint Root

#### **User Controllers**

20. **SupportChatController.java**
    - Rimuovere `AdminAuthHelper` e controlli `X-Admin-Token`
    - Per endpoint admin: `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")`
    - Per endpoint utente: `@PreAuthorize("hasRole('USER')")` o autenticazione base

21. **SupportController.java**
    - Verificare autenticazione appropriata
    - Aggiungere paginazione se necessario

#### **Public Controllers (Aggiungere Paginazione)**

22. **CardController.java**
    ```java
    @GetMapping
    public Page<Card> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return service.findAll(pageable);
    }
    ```

23. **ListingController.java**
    ```java
    @GetMapping
    public Page<Listing> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return service.findAll(pageable);
    }
    ```

24. **TransactionController.java**
    ```java
    @GetMapping
    public Page<Transaction> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return service.findAll(pageable);
    }
    ```

25. **WishlistController.java**
    ```java
    @GetMapping
    public Page<Wishlist> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return service.findAll(pageable);
    }
    ```

26. **UserCardController.java**
    ```java
    @GetMapping("/collection/{userId}")
    public Page<UserCard> getUserCollection(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        return service.findByUserId(userId, pageable);
    }
    ```

27. **CollectionController.java**
    ```java
    @GetMapping("/{userId}")
    public Page<UserCard> getUserCollection(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findByUserId(userId, pageable);
    }
    ```

28. **UserController.java**
    ```java
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Page<UserDTO> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return service.findAll(pageable);
    }
    ```

---

### **Service da Aggiornare**

#### **Aggiungere Metodi Paginati**

1. **CardService.java**
   ```java
   public Page<Card> findAll(Pageable pageable) {
       return cardRepository.findAll(pageable);
   }
   ```

2. **ListingService.java**
   ```java
   public Page<Listing> findAll(Pageable pageable) {
       return listingRepository.findAll(pageable);
   }
   ```

3. **TransactionService.java**
   ```java
   public Page<Transaction> findAll(Pageable pageable) {
       return transactionRepository.findAll(pageable);
   }
   ```

4. **WishlistService.java**
   ```java
   public Page<Wishlist> findAll(Pageable pageable) {
       return wishlistRepository.findAll(pageable);
   }
   ```

5. **UserCardRepository.java / Service**
   ```java
   // Nel repository
   Page<UserCard> findByUserId(String userId, Pageable pageable);
   
   // Nel service
   public Page<UserCard> findByUserId(String userId, Pageable pageable) {
       return userCardRepository.findByUserId(userId, pageable);
   }
   ```

6. **UserService.java**
   ```java
   public Page<UserDTO> findAll(Pageable pageable) {
       return userRepository.findAll(pageable)
           .map(this::toDTO);
   }
   ```

7. **AdminNotificationService.java**
   ```java
   public Page<AdminNotification> filterPaginated(String type, String priority, String status, Pageable pageable) {
       // Implementare filtro paginato
       // Usare Specification o Query dinamica
   }
   
   public Page<AdminNotification> listActiveChronoPaginated(Pageable pageable) {
       return adminNotificationRepository.findByArchivedFalseOrderByCreatedAtDesc(pageable);
   }
   ```

---

### **Repository da Aggiornare**

#### **Aggiungere Metodi Paginati**

1. **UserCardRepository.java**
   ```java
   Page<UserCard> findByUserId(String userId, Pageable pageable);
   ```

2. **AdminNotificationRepository.java**
   ```java
   Page<AdminNotification> findByArchivedFalseOrderByCreatedAtDesc(Pageable pageable);
   Page<AdminNotification> findByTypeAndPriorityAndStatus(String type, String priority, String status, Pageable pageable);
   ```

3. **SupportTicketRepository.java**
   - ✅ Già supporta `findAll(Pageable)` (ereditato da JpaRepository)

---

### **Helper/Utility da Rimuovere o Aggiornare**

1. **AdminAuthHelper.java**
   - ⚠️ **DEPRECATO** - Rimuovere o aggiornare per usare solo JWT
   - Se mantenuto, aggiornare per usare solo `Authorization: Bearer`

---

## 📋 Pattern Standard per Paginazione

### **Controller Pattern**
```java
@GetMapping
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')") // Se necessario
public Page<Entity> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
    log.info("📋 Richiesta lista (page={}, size={}, sort={})", page, size, sortBy);
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
    Page<Entity> result = service.findAll(pageable);
    
    log.info("✅ Restituiti {} elementi (totale: {})", 
        result.getNumberOfElements(), result.getTotalElements());
    return result;
}
```

### **Service Pattern**
```java
public Page<Entity> findAll(Pageable pageable) {
    return repository.findAll(pageable);
}
```

### **Response Format**
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "numberOfElements": 20,
  "first": true,
  "last": false
}
```

---

## 🔍 Esempi di Query con Paginazione

### **Esempio 1: Lista Carte**
```http
GET /api/cards?page=0&size=20&sortBy=name&sortDir=ASC
Authorization: Bearer {token}
```

**Response:**
```json
{
  "content": [
    {"id": 1, "name": "Card 1", ...},
    ...
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

### **Esempio 2: Notifiche Admin Filtrate**
```http
GET /api/admin/notifications?type=SYSTEM&priority=HIGH&page=0&size=10&sortBy=createdAt&sortDir=DESC
Authorization: Bearer {token}
```

### **Esempio 3: Ticket Supporto**
```http
GET /api/admin/support/tickets?page=0&size=20&sortBy=createdAt&sortDir=DESC
Authorization: Bearer {token}
```

---

## 🔐 Nuove Regole di Sicurezza

### **Autenticazione Standard**
- ✅ Tutti gli endpoint richiedono `Authorization: Bearer {token}`
- ✅ Token JWT valido con email verificata
- ✅ Ruoli gestiti tramite `ROLE_USER`, `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`

### **Autorizzazione**
- ✅ `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")` per endpoint admin
- ✅ `@PreAuthorize("hasRole('SUPER_ADMIN')")` per operazioni Root
- ✅ `@PreAuthorize("hasRole('USER')")` per endpoint utente (se necessario)

### **Error Handling**
- ✅ `401 Unauthorized` se token mancante o non valido
- ✅ `403 Forbidden` se ruolo insufficiente (gestito da Spring Security)

### **CORS**
- ✅ Origini permesse:
  - `https://funkard.com`
  - `https://www.funkard.com`
  - `https://admin.funkard.com`
  - `http://localhost:3000`
  - `http://localhost:3002`
- ✅ Header permessi: `Authorization`, `Content-Type`, `Accept`, `X-User-Id`
- ✅ Rimosso: `X-Admin-Token` da allowed headers

---

## 📝 Logging Standard

### **Autenticazione**
```java
log.info("✅ Authenticated request by {} ({}) to {}", email, role, endpoint);
```

### **Query Paginate**
```java
log.info("📋 Richiesta lista (page={}, size={}, sort={})", page, size, sortBy);
log.info("✅ Restituiti {} elementi (totale: {})", numberOfElements, totalElements);
```

### **Operazioni Admin**
```java
log.info("👨‍💻 Assegnazione ticket {} a {}", ticketId, adminEmail);
log.info("🎯 Risoluzione ticket {} da {}", ticketId, adminEmail);
log.info("📦 Archiviazione notifica {} da {}", notificationId, adminEmail);
```

---

## ✅ Checklist Finale

### **Autenticazione**
- [x] SecurityConfig aggiornato
- [x] JwtFilter aggiornato con ruoli
- [ ] Tutti i controller admin aggiornati con @PreAuthorize
- [ ] Rimossi tutti i controlli X-Admin-Token
- [ ] Rimossi AdminAuthHelper dove non necessario

### **Paginazione**
- [x] AdminSupportController - GET /tickets
- [x] AdminNotificationController - GET /
- [ ] CardController - GET /
- [ ] ListingController - GET /
- [ ] TransactionController - GET /
- [ ] WishlistController - GET /
- [ ] UserCardController - GET /collection/{userId}
- [ ] CollectionController - GET /{userId}
- [ ] UserController - GET /users

### **CORS**
- [x] Aggiornato SecurityConfig
- [ ] Verificati tutti i controller con @CrossOrigin

### **Logging**
- [x] JwtFilter
- [x] AdminStatsController
- [x] AdminSupportController
- [x] AdminNotificationController
- [ ] Tutti gli altri controller

---

## 🚀 Prossimi Passi

1. **Completare aggiornamento controller admin** (rimuovere X-Admin-Token, aggiungere @PreAuthorize)
2. **Aggiungere paginazione a tutti gli endpoint GET lista**
3. **Aggiornare service e repository** con metodi paginati
4. **Testare autenticazione** con token JWT
5. **Verificare CORS** da frontend
6. **Aggiornare documentazione API** con nuovi endpoint paginati

---

**Documento creato:** 2025-01-15  
**Ultimo aggiornamento:** 2025-01-15


