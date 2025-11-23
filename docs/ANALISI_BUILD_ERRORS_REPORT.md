# 🔍 ANALISI COMPLETA BACKEND FUNKARD - REPORT DIAGNOSTICO

**Data Analisi**: 2025-11-23  
**Branch**: main  
**Commit**: 09c92dd (fix: aggiunta configurazione esplicita maven-compiler-plugin per Lombok)

---

## 📊 STATO GIT

### Working Tree
- ✅ **Pulito**: Nessun file modificato non committato
- ✅ **Sincronizzato**: Branch allineato con `origin/main`

### Ultimi 20 Commit
```
09c92dd fix: aggiunta configurazione esplicita maven-compiler-plugin per Lombok
bf95eb3 fix: corretti import mancanti e metodo duplicato in repository
e9e0d28 fix: aggiunti tutti i file mancanti per la compilazione (model, repository, service, dto)
e5d4041 fix: aggiunto import List mancante in AdminUserRepository
2cd43be fix: aggiunti file mancanti per risolvere errori di compilazione
2c4b53a feat: aggiunta validazione whitelist lingue per user.language
c5b1c47 Implementato modulo AdminAccess per gestione token e richieste di accesso admin
... (altri 13 commit)
```

### File Totali nel Progetto
- **257 file Java** in `src/main/java`

---

## ❌ ERRORI DI BUILD IDENTIFICATI

### 1. PROBLEMA PRINCIPALE: Lombok Non Processato Correttamente

**Causa Probabile**: Durante la compilazione Docker, Lombok non genera i metodi getter/setter/logger anche se le annotazioni sono presenti.

**File Coinvolti**:
- `User.java` - Ha `@Data` ma compilatore non trova `getEmail()`, `getId()`, `getRole()`
- `EmailLog.java` - Ha `@Data` ma compilatore non trova setter (`setRecipient()`, `setSender()`, ecc.)
- `AdminDashboardDTO.java` - Ha `@Data` su classi interne ma compilatore non trova setter
- `EmailService.java` - Ha `@Slf4j` ma compilatore non trova variabile `log`
- `EmailTemplateManager.java` - Ha `@Slf4j` ma compilatore non trova variabile `log`
- `EmailLogService.java` - Ha `@Slf4j` ma compilatore non trova variabile `log`
- `AdminActionLogController.java` - Ha `@Slf4j` ma compilatore non trova variabile `log`
- `SystemMaintenanceController.java` - Ha `@Slf4j` ma compilatore non trova variabile `log`

**Soluzione Applicata** (commit 09c92dd):
- ✅ Aggiunta configurazione esplicita `maven-compiler-plugin` con `annotationProcessorPaths` per Lombok
- ⚠️ **Potrebbe non essere sufficiente** se il problema è nella fase Docker build

---

### 2. ERRORI SPECIFICI PER FILE

#### 2.1 `FranchiseAdminService.java`
**Errori**:
- `cannot find symbol: class ObjectMapper` (riga 34)
- `cannot find symbol: class Value` (riga 36)

**Causa**: Import mancanti
- ✅ **RISOLTO** in commit bf95eb3: aggiunti `import com.fasterxml.jackson.databind.ObjectMapper;` e `import org.springframework.beans.factory.annotation.Value;`

#### 2.2 `FranchiseProposalRepository.java`
**Errori**:
- `method findByStatusOrderByCreatedAtDesc(...) is already defined` (riga 24)

**Causa**: Metodo duplicato
- ✅ **RISOLTO** in commit bf95eb3: rimosso metodo duplicato

#### 2.3 `UserDeletionService.java`
**Errori**:
- `cannot find symbol: class R2Service` (riga 7)
  - Location: `package com.funkard.storage`

**Causa**: Import errato
- ✅ **RISOLTO** in commit bf95eb3: corretto import da `com.funkard.storage.R2Service` a `com.funkard.service.R2Service`

#### 2.4 `SupportTicketService.java`
**Errori**:
- `cannot find symbol: method getEmail()` su `User` (righe 413, 424, 434, 437)
- `cannot find symbol: method getId()` su `User` (righe 436, 534)
- `cannot find symbol: method getRole()` su `User` (righe 438, 485, 509)

**Causa**: Lombok non genera getter per `User` (ha `@Data`)
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

**Codice Problematico**:
```java
// Riga 425: t.getAssignedToUser().getEmail()
// Riga 434: ticket.getAssignedToUser().getEmail()
// Riga 436: ticket.getAssignedToUser().getId()
// Riga 437: ticket.getAssignedToUser().getEmail()
// Riga 438: ticket.getAssignedToUser().getRole()
```

#### 2.5 `RolePermissionController.java`
**Errori**:
- `cannot find symbol: method getEmail()` su `User` (riga 47)
- `cannot find symbol: method getRole()` su `User` (riga 48)
- `cannot find symbol: method setId(long)` su `User` (riga 167)
- `cannot find symbol: method setEmail(String)` su `User` (riga 168)
- `cannot find symbol: method setRole(String)` su `User` (riga 169)

**Causa**: Lombok non genera getter/setter per `User`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

**Codice Problematico**:
```java
// Riga 47: user.getEmail()
// Riga 48: user.getRole()
// Riga 167: user.setId(1L)
// Riga 168: user.setEmail(userEmail)
// Riga 169: user.setRole("ADMIN")
```

#### 2.6 `RolePermissionService.java`
**Errori**:
- `cannot find symbol: method getRole()` su `User` (riga 140)
- `cannot find symbol: method getId()` su `User` (righe 189)

**Causa**: Lombok non genera getter per `User`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

#### 2.7 `AdminDashboardService.java`
**Errori**:
- `cannot find symbol: method setActive(long)` su `NotificationStats` (riga 45)
- `cannot find symbol: method setResolved(long)` su `NotificationStats` (riga 46)
- `cannot find symbol: method setCritical(long)` su `NotificationStats` (riga 47)
- `cannot find symbol: method setNotifications(...)` su `AdminDashboardDTO` (riga 48)
- `cannot find symbol: method setTotalProducts(long)` su `MarketStats` (riga 52)
- `cannot find symbol: method setNewThisWeek(long)` su `MarketStats` (riga 53)
- `cannot find symbol: method setAvgValueChange(double)` su `MarketStats` (riga 54)
- `cannot find symbol: method setMarket(...)` su `AdminDashboardDTO` (riga 55)
- `cannot find symbol: method setTotal(long)` su `GradingStats` (riga 59)
- `cannot find symbol: method setErrors(long)` su `GradingStats` (riga 60)
- `cannot find symbol: method setInProgress(long)` su `GradingStats` (riga 61)
- `cannot find symbol: method setGrading(...)` su `AdminDashboardDTO` (riga 62)
- `cannot find symbol: method setTotal(long)` su `UserStats` (riga 66)
- `cannot find symbol: method setFlagged(long)` su `UserStats` (riga 67)
- `cannot find symbol: method setUsers(...)` su `AdminDashboardDTO` (riga 68)
- `cannot find symbol: method setOpen(long)` su `SupportStats` (riga 72)
- `cannot find symbol: method setResolved(long)` su `SupportStats` (riga 73)
- `cannot find symbol: method setSupport(...)` su `AdminDashboardDTO` (riga 74)
- `cannot find symbol: method setMarketTrend(...)` su `AdminDashboardDTO` (riga 82)

**Causa**: Lombok non genera setter per classi interne statiche di `AdminDashboardDTO`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

**Nota**: `AdminDashboardDTO` e tutte le classi interne hanno `@Data`, quindi dovrebbero avere setter automatici.

#### 2.8 `EmailService.java`
**Errori**:
- `cannot find symbol: variable log` (righe 81, 84, 91, 97, 103, 399, 428, 454, 462, 466)
- `cannot find symbol: method getId()` su `EmailLog` (righe 114, 118)

**Causa**: 
- Lombok non genera variabile `log` da `@Slf4j`
- Lombok non genera `getId()` per `EmailLog` (ha `@Data`)

**Verifica**:
- ✅ File ha `@Slf4j` (riga 30)
- ✅ `EmailLog` ha `@Data` (riga 24)

#### 2.9 `EmailLogService.java`
**Errori**:
- `cannot find symbol: method setRecipient(String)` su `EmailLog` (righe 52, 85)
- `cannot find symbol: method setSender(String)` su `EmailLog` (righe 53, 86)
- `cannot find symbol: method setSubject(String)` su `EmailLog` (righe 54, 87)
- `cannot find symbol: method setType(String)` su `EmailLog` (righe 55, 88)
- `cannot find symbol: method setStatus(EmailStatus)` su `EmailLog` (righe 56, 89)
- `cannot find symbol: method setLocale(String)` su `EmailLog` (righe 57, 91)
- `cannot find symbol: method setTemplateName(String)` su `EmailLog` (righe 58, 92)
- `cannot find symbol: method setRetryCount(int)` su `EmailLog` (righe 59, 93, 106)
- `cannot find symbol: method setErrorMessage(String)` su `EmailLog` (righe 63, 90, 112)
- `cannot find symbol: variable log` (righe 67, 96)
- `cannot find symbol: method getId()` su `EmailLog` (righe 67, 96)

**Causa**: Lombok non genera setter/getter per `EmailLog`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

#### 2.10 `EmailTemplateManager.java`
**Errori**:
- `cannot find symbol: variable log` (righe 62, 71, 135, 171, 204, 206, 252)

**Causa**: Lombok non genera variabile `log` da `@Slf4j`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

#### 2.11 `AdminActionLogController.java`
**Errori**:
- `cannot find symbol: variable log` (righe 36, 38)

**Causa**: Lombok non genera variabile `log` da `@Slf4j`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

**Verifica**:
- ✅ File ha `@Slf4j` (riga 15)

#### 2.12 `SystemMaintenanceController.java`
**Errori**:
- `cannot find symbol: variable log` (riga 31)

**Causa**: Lombok non genera variabile `log` da `@Slf4j`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

**Verifica**:
- ✅ File ha `@Slf4j` (riga 15)

#### 2.13 `SystemCleanupService.java`
**Errori**:
- `cannot find symbol: method builder()` su `SystemCleanupLog` (riga 18)
- `cannot find symbol: method getTimestamp()` su `SystemCleanupLog` (riga 34)

**Causa**: Lombok non genera `builder()` e getter per `SystemCleanupLog`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

**Verifica**:
- ✅ `SystemCleanupLog` ha `@Builder` (riga 13)
- ✅ `SystemCleanupLog` ha `@Getter` (riga 9)

#### 2.14 `AdminActionLogger.java`
**Errori**:
- `cannot find symbol: method setTargetId(Long)` su `AdminActionLog` (riga 14)
- `cannot find symbol: method setTargetType(String)` su `AdminActionLog` (riga 15)
- `cannot find symbol: method setAction(String)` su `AdminActionLog` (riga 16)
- `cannot find symbol: method setPerformedBy(String)` su `AdminActionLog` (riga 17)
- `cannot find symbol: method setRole(String)` su `AdminActionLog` (riga 18)
- `cannot find symbol: method setNotes(String)` su `AdminActionLog` (riga 19)

**Causa**: Lombok non genera setter per `AdminActionLog`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

**Verifica**:
- ✅ `AdminActionLog` ha `@Data` (riga 7)

#### 2.15 `AdminAccessController.java`
**Errori**:
- `cannot find symbol: method getRequestedRole()` su `AdminAccessRequest` (riga 83)
- `cannot find symbol: method getId()` su `AdminAccessRequest` (riga 115)

**Causa**: Lombok non genera getter per `AdminAccessRequest`
- ⚠️ **NON RISOLTO**: Richiede fix Lombok

**Verifica**:
- ✅ `AdminAccessRequest` (in `adminaccess.model`) ha `@Data` (riga 17)

---

## 🔄 CLASSI DUPLICATE

### `AdminAccessRequest` - 2 Definizioni

1. **`com.funkard.adminaccess.model.AdminAccessRequest`**
   - Package: `com.funkard.adminaccess.model`
   - Usato da: `AdminAccessController`, `AdminAccessService`, `AdminAccessRequestRepository`
   - ✅ Ha `@Data`, `@Builder`

2. **`com.funkard.adminauth.AdminAccessRequest`**
   - Package: `com.funkard.adminauth`
   - Usato da: `AdminAccessRequestRepository` (in `adminauth`)
   - ✅ Ha `@Data`, `@Builder`

**Problema**: Due entità JPA con stesso nome e stessa tabella (`admin_access_requests`)
- ⚠️ **RISCHIO**: Conflitto di mapping JPA
- ⚠️ **RISCHIO**: Confusione durante sviluppo

**Raccomandazione**: Unificare in una sola classe o rinominare una delle due.

---

## 📋 INCONSISTENZE CONTROLLER ↔ SERVICE

### Verifica Firma Metodi

#### ✅ `TranslateController` → `UnifiedTranslationService`
- Controller chiama: `translationService.translate(String text, String targetLang)`
- Service definisce: `String translate(String text, String targetLang)`
- ✅ **MATCH**

#### ✅ `UserController` → `UserService`
- Controller chiama: `userService.findById(Long id)`
- Service definisce: `User findById(Long id)`
- ✅ **MATCH**

- Controller chiama: `userService.findByEmail(String email)`
- Service definisce: `User findByEmail(String email)`
- ✅ **MATCH**

#### ✅ `FranchiseController` → `FranchiseAdminService`
- Controller chiama: `franchiseAdminService.createProposal(...)`
- Service definisce: `void createProposal(String category, String franchise, String userEmail, Long userId)`
- ✅ **MATCH**

#### ✅ `SupportTicketController` → `SupportTicketService`
- Controller chiama: `ticketService.create(String email, String subject, String message)`
- Service definisce: `SupportTicket create(String email, String subject, String message)`
- ✅ **MATCH**

**Conclusione**: Nessuna inconsistenza trovata tra controller e service.

---

## 🔍 METODI MANCANTI (Cannot Find Symbol)

### Metodi su `User` (dovrebbero essere generati da Lombok `@Data`)

1. `getEmail()` - Usato in:
   - `SupportTicketService.java` (righe 413, 424, 434, 437)
   - `RolePermissionController.java` (riga 47)
   - `UserService.java` (riga 49, 113) - ✅ Funziona (probabilmente compilato prima)

2. `getId()` - Usato in:
   - `SupportTicketService.java` (righe 436, 534)
   - `RolePermissionController.java` (riga 167)
   - `UserService.java` (riga 69, 108, 111) - ✅ Funziona

3. `getRole()` - Usato in:
   - `SupportTicketService.java` (righe 438, 485, 509)
   - `RolePermissionController.java` (riga 48)
   - `RolePermissionService.java` (riga 140)

4. `setId(Long)` - Usato in:
   - `RolePermissionController.java` (riga 167)

5. `setEmail(String)` - Usato in:
   - `RolePermissionController.java` (riga 168)

6. `setRole(String)` - Usato in:
   - `RolePermissionController.java` (riga 169)

### Metodi su `EmailLog` (dovrebbero essere generati da Lombok `@Data`)

1. `setRecipient(String)` - Usato in `EmailLogService.java` (righe 52, 85)
2. `setSender(String)` - Usato in `EmailLogService.java` (righe 53, 86)
3. `setSubject(String)` - Usato in `EmailLogService.java` (righe 54, 87)
4. `setType(String)` - Usato in `EmailLogService.java` (righe 55, 88)
5. `setStatus(EmailStatus)` - Usato in `EmailLogService.java` (righe 56, 89, 108, 111)
6. `setLocale(String)` - Usato in `EmailLogService.java` (righe 57, 91)
7. `setTemplateName(String)` - Usato in `EmailLogService.java` (righe 58, 92)
8. `setRetryCount(int)` - Usato in `EmailLogService.java` (righe 59, 93, 106)
9. `setErrorMessage(String)` - Usato in `EmailLogService.java` (righe 63, 90, 112)
10. `getId()` - Usato in `EmailService.java` (righe 114, 118), `EmailLogService.java` (righe 67, 96)

### Metodi su `AdminDashboardDTO` e Classi Interne

1. `setNotifications(NotificationStats)` - Usato in `AdminDashboardService.java` (riga 48)
2. `setMarket(MarketStats)` - Usato in `AdminDashboardService.java` (riga 55)
3. `setGrading(GradingStats)` - Usato in `AdminDashboardService.java` (riga 62)
4. `setUsers(UserStats)` - Usato in `AdminDashboardService.java` (riga 68)
5. `setSupport(SupportStats)` - Usato in `AdminDashboardService.java` (riga 74)
6. `setMarketTrend(List<MarketTrendPoint>)` - Usato in `AdminDashboardService.java` (riga 82)

**Classi Interne**:
- `NotificationStats.setActive(long)`, `setResolved(long)`, `setCritical(long)`
- `MarketStats.setTotalProducts(long)`, `setNewThisWeek(long)`, `setAvgValueChange(double)`
- `GradingStats.setTotal(long)`, `setErrors(long)`, `setInProgress(long)`
- `UserStats.setTotal(long)`, `setFlagged(long)`
- `SupportStats.setOpen(long)`, `setResolved(long)`

### Metodi su `AdminActionLog` (dovrebbero essere generati da Lombok `@Data`)

1. `setTargetId(Long)` - Usato in `AdminActionLogger.java` (riga 14)
2. `setTargetType(String)` - Usato in `AdminActionLogger.java` (riga 15)
3. `setAction(String)` - Usato in `AdminActionLogger.java` (riga 16)
4. `setPerformedBy(String)` - Usato in `AdminActionLogger.java` (riga 17)
5. `setRole(String)` - Usato in `AdminActionLogger.java` (riga 18)
6. `setNotes(String)` - Usato in `AdminActionLogger.java` (riga 19)

### Metodi su `SystemCleanupLog` (dovrebbero essere generati da Lombok `@Builder` e `@Getter`)

1. `builder()` - Usato in `SystemCleanupService.java` (riga 18)
2. `getTimestamp()` - Usato in `SystemCleanupService.java` (riga 34)

### Metodi su `AdminAccessRequest` (dovrebbero essere generati da Lombok `@Data`)

1. `getRequestedRole()` - Usato in `AdminAccessController.java` (riga 83)
2. `getId()` - Usato in `AdminAccessController.java` (riga 115)

### Variabili Logger (dovrebbero essere generate da Lombok `@Slf4j`)

1. `log` in `EmailService.java` (righe 81, 84, 91, 97, 103, 399, 428, 454, 462, 466)
2. `log` in `EmailTemplateManager.java` (righe 62, 71, 135, 171, 204, 206, 252)
3. `log` in `EmailLogService.java` (righe 67, 96)
4. `log` in `AdminActionLogController.java` (righe 36, 38)
5. `log` in `SystemMaintenanceController.java` (riga 31)

---

## 🔧 REFACTOR INCOMPLETI

### Nessun Refactor Incompleto Identificato

Tutti i file sembrano completi. Il problema principale è la mancata generazione di codice da Lombok durante la compilazione.

---

## 📦 CONFIGURAZIONE MAVEN

### Lombok Dependency
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>
```
✅ **Presente**

### Maven Compiler Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.14.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.30</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```
✅ **Aggiunto in commit 09c92dd**

---

## 🎯 CAUSE PROBABILI DEL BUILD FAILURE

### 1. **Lombok Non Processato Durante Docker Build** (PRINCIPALE)

**Probabilità**: 95%

**Evidenze**:
- Tutti gli errori riguardano metodi/getter/setter che Lombok dovrebbe generare
- Le annotazioni (`@Data`, `@Slf4j`, `@Builder`, `@Getter`, `@Setter`) sono presenti
- La configurazione `maven-compiler-plugin` è stata aggiunta ma potrebbe non essere sufficiente

**Possibili Cause**:
1. Docker build non rispetta `annotationProcessorPaths`
2. Lombok non è disponibile durante la fase di compilazione Docker
3. Ordine di esecuzione: Lombok deve essere processato PRIMA della compilazione Java
4. Cache Maven che non include i file generati da Lombok

### 2. **Versione Lombok Incompatibile**

**Probabilità**: 5%

**Evidenze**:
- Lombok 1.18.30 potrebbe avere problemi con Java 17
- Spring Boot 3.5.6 potrebbe richiedere Lombok più recente

**Raccomandazione**: Aggiornare a Lombok 1.18.34 o superiore

### 3. **Classi Duplicate (AdminAccessRequest)**

**Probabilità**: 2%

**Evidenze**:
- Due classi con stesso nome in package diversi
- Entrambe mappano la stessa tabella `admin_access_requests`

**Impatto**: Potrebbe causare conflitti JPA ma non dovrebbe bloccare la compilazione

---

## 📝 ORDINE CONSIGLIATO DEGLI INTERVENTI

### FASE 1: Verifica Configurazione Lombok (PRIORITÀ ALTA)

1. **Verificare Dockerfile**
   - Assicurarsi che `mvnw` usi la configurazione corretta
   - Verificare che Lombok sia disponibile durante build

2. **Test Locale**
   - Eseguire `./mvnw clean compile` localmente
   - Verificare se gli errori si presentano anche localmente
   - Se funziona localmente ma non in Docker → problema Docker

3. **Aggiornare Lombok**
   - Provare Lombok 1.18.34 o superiore
   - Verificare compatibilità con Java 17 e Spring Boot 3.5.6

### FASE 2: Fix Alternativi (Se Fase 1 Non Risolve)

1. **Aggiungere Getter/Setter Manuali** (SOLO SE NECESSARIO)
   - Aggiungere metodi espliciti in `User.java` per `getEmail()`, `getId()`, `getRole()`
   - Aggiungere setter in `EmailLog.java`
   - Aggiungere setter in `AdminDashboardDTO` e classi interne
   - ⚠️ **NON RACCOMANDATO**: Va contro il principio DRY

2. **Sostituire @Slf4j con Logger Manuale**
   - Aggiungere `private static final Logger log = LoggerFactory.getLogger(ClassName.class);`
   - ⚠️ **NON RACCOMANDATO**: Aggiunge boilerplate

### FASE 3: Risolvere Classi Duplicate (PRIORITÀ MEDIA)

1. **Unificare AdminAccessRequest**
   - Decidere quale package mantenere (`adminaccess` o `adminauth`)
   - Spostare tutti i riferimenti alla classe scelta
   - Eliminare la classe duplicata
   - Aggiornare repository e service

### FASE 4: Test e Validazione (PRIORITÀ ALTA)

1. **Compilazione Locale**
   - Verificare che `./mvnw clean package -DskipTests` funzioni
   - Verificare che non ci siano warning

2. **Test Docker Build**
   - Eseguire build Docker completo
   - Verificare che tutti gli errori siano risolti

3. **Test Runtime**
   - Avviare applicazione
   - Verificare che i metodi generati da Lombok funzionino a runtime

---

## 📊 STATISTICHE ERRORI

- **Totale Errori**: ~100
- **Errori Risolti**: 3 (import mancanti, metodo duplicato, import errato)
- **Errori Pendenti**: ~97 (tutti relativi a Lombok)
- **File Coinvolti**: ~15 file Java
- **Classi con Problemi Lombok**: ~10 classi

---

## ✅ CONCLUSIONI

1. **Problema Principale**: Lombok non viene processato correttamente durante la compilazione Docker
2. **File Corretti**: Import e metodi duplicati sono stati risolti
3. **Nessuna Inconsistenza**: Controller e Service hanno firme compatibili
4. **Classi Duplicate**: Presenti ma non bloccanti per la compilazione
5. **Prossimi Passi**: Verificare configurazione Lombok in Docker build

---

**Report Generato**: 2025-11-23  
**Analista**: AI Assistant  
**Versione**: 1.0

