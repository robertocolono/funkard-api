# 📊 Analisi Entità Duplicate: AdminAccessToken

## 🎯 Obiettivo
Identificare quale delle due entità `AdminAccessToken` duplicate mantenere e quale eliminare.

---

## 📋 Entità Identificate

### 1. `com.funkard.adminaccess.model.AdminAccessToken`
- **Percorso**: `src/main/java/com/funkard/adminaccess/model/AdminAccessToken.java`
- **Tabella DB**: `admin_access_tokens`
- **Stato**: ✅ **ATTIVAMENTE UTILIZZATA**

### 2. `com.funkard.adminauth.AdminAccessToken`
- **Percorso**: `src/main/java/com/funkard/adminauth/AdminAccessToken.java`
- **Tabella DB**: `admin_access_tokens`
- **Stato**: ❌ **NON REFERENZIATA**

---

## 🔍 Analisi Dettagliata dei Riferimenti

### ✅ `com.funkard.adminaccess.model.AdminAccessToken` - UTILIZZATA

#### File che la utilizzano:

1. **`AdminAccessService.java`**
   - **Percorso**: `src/main/java/com/funkard/adminaccess/service/AdminAccessService.java`
   - **Import**: `import com.funkard.adminaccess.model.AdminAccessToken;`
   - **Utilizzo**:
     - Linea 4: Import
     - Linea 25: Campo `tokenRepository` di tipo `AdminAccessTokenRepository`
     - Linea 46: Creazione token con `AdminAccessToken.builder()`
     - Linea 66-67: Metodo `validateToken()` che ritorna `Optional<AdminAccessToken>`
     - Linea 73: Variabile `adminToken` di tipo `AdminAccessToken`
     - Linea 91-96: Validazione e utilizzo di `AdminAccessToken`
     - Linea 185: Metodo `listTokens()` che ritorna `List<AdminAccessToken>`

2. **`AdminAccessController.java`**
   - **Percorso**: `src/main/java/com/funkard/adminaccess/controller/AdminAccessController.java`
   - **Import**: `import com.funkard.adminaccess.model.AdminAccessToken;`
   - **Utilizzo**:
     - Linea 4: Import
     - Linea 216: Variabile `tokens` di tipo `List<AdminAccessToken>`

3. **`AdminAccessTokenRepository.java`**
   - **Percorso**: `src/main/java/com/funkard/adminaccess/repository/AdminAccessTokenRepository.java`
   - **Import**: `import com.funkard.adminaccess.model.AdminAccessToken;`
   - **Utilizzo**:
     - Linea 3: Import
     - Linea 15: Repository estende `JpaRepository<AdminAccessToken, UUID>`
     - Linea 20: Metodo `findByToken()` che ritorna `Optional<AdminAccessToken>`
     - Linea 25: Metodo `findByActiveTrue()` che ritorna `List<AdminAccessToken>`
     - Linea 30: Metodo `findByRole()` che ritorna `List<AdminAccessToken>`

4. **`AdminTokenController.java`** (in `adminauth`)
   - **Percorso**: `src/main/java/com/funkard/adminauth/AdminTokenController.java`
   - **Import**: `import com.funkard.adminaccess.model.AdminAccessToken;`
   - **Utilizzo**:
     - Linea 3: Import
     - Linea 21: Campo `tokenRepository` di tipo `AdminAccessTokenRepository`
     - Linea 52: Creazione token con `AdminAccessToken.builder()`
   - **Nota**: Questo controller è stato aggiornato recentemente per usare l'entità da `adminaccess.model`

---

### ❌ `com.funkard.adminauth.AdminAccessToken` - NON REFERENZIATA

#### File che la utilizzano:
**NESSUNO**

- ✅ **Verifica grep**: `grep -r "com.funkard.adminauth.AdminAccessToken"` → **0 risultati**
- ✅ **Verifica import**: Nessun file importa esplicitamente questa entità
- ✅ **Verifica utilizzo**: Nessun file utilizza questa entità

---

## 🔗 Dipendenze per Modulo

### Modulo `adminaccess` (✅ MANTENERE)
- **Service**: `AdminAccessService` → usa `AdminAccessToken` da `adminaccess.model`
- **Controller**: `AdminAccessController` → usa `AdminAccessToken` da `adminaccess.model`
- **Repository**: `AdminAccessTokenRepository` → usa `AdminAccessToken` da `adminaccess.model`
- **Repository**: `AdminAccessRequestRepository` → NON usa `AdminAccessToken` (usa solo `AdminAccessRequest`)

### Modulo `adminauth` (⚠️ DA PULIRE)
- **Controller**: `AdminTokenController` → usa `AdminAccessToken` da `adminaccess.model` (già aggiornato)
- **Entità**: `AdminAccessToken` in `adminauth` → **NON REFERENZIATA**

---

## 🔐 JwtFilter - Verifica

**`JwtFilter.java`**:
- **Percorso**: `src/main/java/com/funkard/security/JwtFilter.java`
- **Utilizzo di AdminAccessToken**: ❌ **NON UTILIZZA**
- **Dipendenze**:
  - `JwtUtil` (per validazione JWT)
  - `UserRepository` (per recupero utenti)
- **Conclusione**: `JwtFilter` non ha bisogno di `AdminAccessToken`

---

## 📊 Confronto Strutturale

### Entità `com.funkard.adminaccess.model.AdminAccessToken`
```java
@Entity
@Table(name = "admin_access_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 50)
    private String role;
    
    @Column(nullable = false, unique = true, length = 255)
    private String token;
    
    @Column(name = "created_by", length = 255)
    private String createdBy;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### Entità `com.funkard.adminauth.AdminAccessToken`
```java
@Entity
@Table(name = "admin_access_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 50)
    private String role;
    
    @Column(nullable = false, unique = true, length = 255)
    private String token;
    
    @Column(name = "created_by", length = 255)
    private String createdBy;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

**Conclusione**: Le due entità sono **IDENTICHE** in struttura e mappano la **STESSA TABELLA** (`admin_access_tokens`).

---

## ✅ Raccomandazione Finale

### 🎯 **MANTENERE**: `com.funkard.adminaccess.model.AdminAccessToken`

**Motivi**:
1. ✅ È utilizzata da tutti i servizi e controller attivi
2. ✅ È referenziata dal repository ufficiale
3. ✅ È parte del modulo `adminaccess` che gestisce l'intero flusso di accesso admin
4. ✅ `AdminTokenController` è già stato aggiornato per usarla

### 🗑️ **ELIMINARE**: `com.funkard.adminauth.AdminAccessToken`

**Motivi**:
1. ❌ Non è referenziata da nessun file
2. ❌ È una duplicazione non necessaria
3. ❌ Causa conflitti Hibernate (due entità con stesso nome mappano stessa tabella)
4. ❌ È probabilmente un residuo di refactoring precedente

---

## 📝 File da Eliminare

```
src/main/java/com/funkard/adminauth/AdminAccessToken.java
```

---

## ⚠️ Note Importanti

1. **Nessun file dipende** da `com.funkard.adminauth.AdminAccessToken`
2. **Tutti i file attivi** usano `com.funkard.adminaccess.model.AdminAccessToken`
3. **JwtFilter** non utilizza `AdminAccessToken` (usa solo JWT e `UserRepository`)
4. **AdminTokenController** è già stato aggiornato per usare l'entità corretta

---

## 🔄 Impatto della Rimozione

- ✅ **Nessun impatto funzionale**: Nessun file utilizza l'entità da eliminare
- ✅ **Risoluzione conflitti**: Elimina il `DuplicateMappingException` di Hibernate
- ✅ **Pulizia codice**: Rimuove codice morto/duplicato
- ✅ **Coerenza architetturale**: Mantiene un'unica fonte di verità per `AdminAccessToken`

---

**Data Analisi**: 2025-11-24  
**Analista**: AI Assistant  
**Stato**: ✅ Pronto per eliminazione sicura

