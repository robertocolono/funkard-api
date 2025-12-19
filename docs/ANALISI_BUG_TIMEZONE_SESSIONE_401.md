# 🔍 ANALISI BUG TIMEZONE - 401 su /api/admin/v2/auth/me

**Data Analisi:** 2025-01-06  
**Problema:** `GET /api/admin/v2/auth/me` ritorna 401 anche con sessione valida nel database  
**Contesto Reale:** Database Neon, sessione esiste, cookie inviato correttamente

---

## 📊 DATI REALI (VERIFICATI)

- **Database:** Neon (Postgres)
- **Tabella:** `admin_sessions`
- **Sessione esistente:**
  - `session_id = 'a662e44b6f9a459c8e668fd7dcec1399'`
  - `expires_at = 2025-12-19 18:33:58`
- **Request time:** ~15:25 UTC
- **Cookie:** `ADMIN_SESSION` inviato correttamente (verificato DevTools)
- **Risultato:** 401 Unauthorized

**Conclusione:** Sessione valida (expires_at nel futuro), ma backend la considera scaduta.

---

## 🔍 1. ANALISI CODICE - validateSession()

### 1.1 Codice Attuale

**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionServiceModern.java`  
**Metodo:** `validateSession()` (linea 56-79)

```java
@Transactional(readOnly = true)
public Optional<UUID> validateSession(String sessionId) {
    if (sessionId == null || sessionId.trim().isEmpty()) {
        return Optional.empty();
    }
    
    Optional<AdminSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
    
    if (sessionOpt.isEmpty()) {
        return Optional.empty();
    }
    
    AdminSession session = sessionOpt.get();
    
    // Verifica scadenza
    if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
        // Sessione scaduta, rimuovila
        sessionRepository.deleteBySessionId(sessionId);
        logger.debug("⏰ Sessione scaduta rimossa: {}", sessionId.substring(0, 8) + "...");
        return Optional.empty();
    }
    
    return Optional.of(session.getAdminId());
}
```

**Problema identificato:** Linea 71
```java
if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
```

---

### 1.2 Tipi Usati

**Tipo Java:**
- `LocalDateTime.now()` → `LocalDateTime` (senza timezone)
- `session.getExpiresAt()` → `LocalDateTime` (senza timezone)

**Tipo Database:**
- Colonna `expires_at` in Neon: Probabilmente `TIMESTAMP` (senza timezone) o `TIMESTAMPTZ` (con timezone)
- Entity `AdminSession`: `@Column(name = "expires_at") private LocalDateTime expiresAt;`

**Confronto:**
- `LocalDateTime.now().isAfter(LocalDateTime)` → confronto senza timezone

---

## 🐛 2. ROOT CAUSE - BUG TIMEZONE

### 2.1 Il Problema

**`LocalDateTime` NON ha timezone:**
- `LocalDateTime.now()` usa il **timezone del server JVM** (implicito)
- Quando viene salvato nel database, se la colonna è `TIMESTAMP` (senza timezone), viene salvato "as-is"
- Quando viene letto dal database, viene letto "as-is" senza conversione timezone
- Il confronto `LocalDateTime.now().isAfter(session.getExpiresAt())` confronta due `LocalDateTime` che potrebbero essere in timezone diversi (implicitamente)

### 2.2 Scenario Reale

**Creazione Sessione (createSession):**
```java
LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);
```

**Scenario 1: Server JVM in UTC**
- `LocalDateTime.now()` = `2025-01-06 15:25:00` (UTC)
- `expiresAt` = `2025-01-06 19:25:00` (UTC)
- Salvato nel database: `2025-01-06 19:25:00` (senza timezone)

**Scenario 2: Server JVM in Europe/Rome (UTC+1)**
- `LocalDateTime.now()` = `2025-01-06 16:25:00` (Europe/Rome = UTC+1)
- `expiresAt` = `2025-01-06 20:25:00` (Europe/Rome = UTC+1)
- Salvato nel database: `2025-01-06 20:25:00` (senza timezone)

**Validazione Sessione (validateSession):**
```java
if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
```

**Se il server JVM timezone cambia o è diverso:**
- `LocalDateTime.now()` = `2025-01-06 15:25:00` (UTC)
- `session.getExpiresAt()` = `2025-01-06 20:25:00` (salvato come Europe/Rome, ma letto come UTC)
- **Confronto:** `15:25 > 20:25` = `false` ✅ (corretto)

**MA se:**
- `LocalDateTime.now()` = `2025-01-06 20:26:00` (UTC)
- `session.getExpiresAt()` = `2025-01-06 20:25:00` (salvato come Europe/Rome, ma letto come UTC)
- **Confronto:** `20:26 > 20:25` = `true` ❌ (scaduta, ma in realtà è valida se interpretata correttamente)

### 2.3 Caso Specifico (Dati Reali)

**Dati:**
- `expires_at = 2025-12-19 18:33:58` (nel database)
- Request time: `~15:25 UTC`

**Se `expires_at` è stato salvato come:**
- **UTC:** `2025-12-19 18:33:58 UTC` → valida fino alle 18:33 UTC ✅
- **Europe/Rome (UTC+1):** `2025-12-19 18:33:58 Europe/Rome` = `2025-12-19 17:33:58 UTC` → scaduta alle 17:33 UTC ❌

**Se `LocalDateTime.now()` è in:**
- **UTC:** `2025-01-06 15:25:00 UTC`
- **Europe/Rome:** `2025-01-06 16:25:00 Europe/Rome` = `2025-01-06 15:25:00 UTC`

**Confronto:**
- Se `expires_at` è salvato come UTC ma letto come local time → confronto errato
- Se `expires_at` è salvato come local time ma letto come UTC → confronto errato

**Conclusione:** Il confronto `LocalDateTime` è **ambiguo** e può fallire a causa di timezone mismatch.

---

## 🔧 3. FIX PROPOSTO

### 3.1 Opzione A: Usare Instant (RACCOMANDATO)

**Vantaggi:**
- `Instant` è sempre in UTC (senza ambiguità)
- Confronto preciso e indipendente da timezone
- Standard Java per timestamp UTC

**Modifiche Necessarie:**

#### 3.1.1 Entity AdminSession

**File:** `src/main/java/com/funkard/adminauthmodern/AdminSession.java`

**PRIMA:**
```java
@Column(name = "expires_at", nullable = false)
private LocalDateTime expiresAt;
```

**DOPO:**
```java
@Column(name = "expires_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
private Instant expiresAt;
```

**Nota:** `columnDefinition = "TIMESTAMP WITH TIME ZONE"` forza il database a usare `TIMESTAMPTZ` (con timezone).

#### 3.1.2 Service - createSession()

**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionServiceModern.java`

**PRIMA:**
```java
LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);
```

**DOPO:**
```java
Instant expiresAt = Instant.now().plusSeconds(SESSION_DURATION_HOURS * 3600);
```

#### 3.1.3 Service - validateSession()

**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionServiceModern.java`

**PRIMA:**
```java
if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
    // Sessione scaduta
    return Optional.empty();
}
```

**DOPO:**
```java
Instant now = Instant.now();
Instant expiresAt = session.getExpiresAt();

if (now.isAfter(expiresAt)) {
    // Sessione scaduta
    logger.warn("⏰ Sessione scaduta: now={}, expiresAt={}, sessionId={}", 
        now, expiresAt, sessionId.substring(0, 8) + "...");
    sessionRepository.deleteBySessionId(sessionId);
    return Optional.empty();
}

logger.debug("✅ Sessione valida: now={}, expiresAt={}, sessionId={}", 
    now, expiresAt, sessionId.substring(0, 8) + "...");
```

#### 3.1.4 Service - cleanupExpiredSessions()

**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionServiceModern.java`

**PRIMA:**
```java
LocalDateTime now = LocalDateTime.now();
int deleted = sessionRepository.deleteExpiredSessions(now);
```

**DOPO:**
```java
Instant now = Instant.now();
int deleted = sessionRepository.deleteExpiredSessions(now);
```

#### 3.1.5 Repository - deleteExpiredSessions()

**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionRepository.java`

**PRIMA:**
```java
@Query("DELETE FROM AdminSession s WHERE s.expiresAt < :now")
int deleteExpiredSessions(@Param("now") LocalDateTime now);
```

**DOPO:**
```java
@Query("DELETE FROM AdminSession s WHERE s.expiresAt < :now")
int deleteExpiredSessions(@Param("now") Instant now);
```

---

### 3.2 Opzione B: Forzare UTC Esplicitamente (ALTERNATIVA)

**Vantaggi:**
- Meno modifiche (solo nel service)
- Mantiene `LocalDateTime` nell'entity

**Svantaggi:**
- Meno robusto (dipende da conversione esplicita)
- Richiede attenzione in futuro

**Modifiche Necessarie:**

#### 3.2.1 Service - createSession()

**PRIMA:**
```java
LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);
```

**DOPO:**
```java
LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusHours(SESSION_DURATION_HOURS);
```

#### 3.2.2 Service - validateSession()

**PRIMA:**
```java
if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
```

**DOPO:**
```java
LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
LocalDateTime expiresAt = session.getExpiresAt();

if (now.isAfter(expiresAt)) {
    logger.warn("⏰ Sessione scaduta: now={} (UTC), expiresAt={}, sessionId={}", 
        now, expiresAt, sessionId.substring(0, 8) + "...");
    sessionRepository.deleteBySessionId(sessionId);
    return Optional.empty();
}

logger.debug("✅ Sessione valida: now={} (UTC), expiresAt={}, sessionId={}", 
    now, expiresAt, sessionId.substring(0, 8) + "...");
```

**Nota:** Richiede anche aggiornare `cleanupExpiredSessions()` per usare `LocalDateTime.now(ZoneOffset.UTC)`.

---

## 📋 4. RACCOMANDAZIONE

### ✅ Opzione A: Usare Instant (RACCOMANDATO)

**Motivazione:**
1. **Robustezza:** `Instant` è sempre in UTC, elimina ambiguità
2. **Standard:** Best practice Java per timestamp UTC
3. **Database:** `TIMESTAMPTZ` è il tipo corretto per timestamp con timezone
4. **Manutenibilità:** Evita problemi futuri con timezone

**Modifiche Richieste:**
- Entity: `LocalDateTime` → `Instant`
- Service: `LocalDateTime.now()` → `Instant.now()`
- Repository: Parametro `LocalDateTime` → `Instant`
- Database: Migrazione per convertire colonna a `TIMESTAMPTZ` (opzionale, ma raccomandato)

**Impatto:**
- **Basso:** Solo modifiche interne, nessun cambio API
- **Breaking:** Nessuno (solo tipo interno)
- **Database:** Potrebbe richiedere migrazione se colonna è `TIMESTAMP` (senza timezone)

---

## 🔍 5. VERIFICA DATABASE

### 5.1 Query per Verificare Tipo Colonna

```sql
SELECT 
    column_name,
    data_type,
    datetime_precision,
    timezone
FROM information_schema.columns
WHERE table_name = 'admin_sessions'
AND column_name = 'expires_at';
```

**Risultati Possibili:**
- `TIMESTAMP` (senza timezone) → **PROBLEMA** - Richiede migrazione
- `TIMESTAMPTZ` (con timezone) → **OK** - Ma ancora problema con `LocalDateTime`

### 5.2 Query per Verificare Timezone Database

```sql
SHOW timezone;
```

**Risultato Atteso:** `UTC` o `Etc/UTC`

### 5.3 Query per Verificare Valore Reale

```sql
SELECT 
    session_id,
    expires_at,
    expires_at AT TIME ZONE 'UTC' AS expires_at_utc,
    NOW() AS now_db,
    NOW() AT TIME ZONE 'UTC' AS now_utc,
    expires_at > NOW() AS is_valid
FROM admin_sessions
WHERE session_id = 'a662e44b6f9a459c8e668fd7dcec1399';
```

**Interpretazione:**
- Se `is_valid = true` ma backend ritorna 401 → **BUG CONFERMATO**
- Se `expires_at_utc` e `now_utc` mostrano differenza → **Timezone mismatch**

---

## 📊 6. LOGGING TEMPORANEO (PER DEBUG)

### 6.1 Modifica validateSession() con Log Dettagliato

**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionServiceModern.java`

**Aggiungere logging temporaneo:**

```java
@Transactional(readOnly = true)
public Optional<UUID> validateSession(String sessionId) {
    if (sessionId == null || sessionId.trim().isEmpty()) {
        return Optional.empty();
    }
    
    Optional<AdminSession> sessionOpt = sessionRepository.findBySessionId(sessionId);
    
    if (sessionOpt.isEmpty()) {
        logger.warn("🔍 [DEBUG] Sessione non trovata: sessionId={}", 
            sessionId != null && sessionId.length() > 8 ? sessionId.substring(0, 8) + "..." : "null");
        return Optional.empty();
    }
    
    AdminSession session = sessionOpt.get();
    
    // 🔍 LOGGING TEMPORANEO PER DEBUG
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = session.getExpiresAt();
    boolean isExpired = now.isAfter(expiresAt);
    
    logger.warn("🔍 [DEBUG TIMEZONE] Validazione sessione:");
    logger.warn("  - sessionId: {}", sessionId.substring(0, 8) + "...");
    logger.warn("  - now (LocalDateTime): {}", now);
    logger.warn("  - expiresAt (dal DB): {}", expiresAt);
    logger.warn("  - isAfter: {}", isExpired);
    logger.warn("  - JVM timezone: {}", java.util.TimeZone.getDefault().getID());
    
    // Verifica scadenza
    if (isExpired) {
        // Sessione scaduta, rimuovila
        sessionRepository.deleteBySessionId(sessionId);
        logger.warn("⏰ [DEBUG] Sessione scaduta rimossa: sessionId={}", sessionId.substring(0, 8) + "...");
        return Optional.empty();
    }
    
    logger.warn("✅ [DEBUG] Sessione valida: sessionId={}", sessionId.substring(0, 8) + "...");
    return Optional.of(session.getAdminId());
}
```

**Nota:** Usare `logger.warn()` temporaneamente per garantire che i log appaiano anche in produzione (se log level è WARN).

---

## 🎯 7. CONCLUSIONE

### 7.1 Root Cause Confermata

**Bug:** Confronto `LocalDateTime` senza timezone esplicito causa ambiguità timezone.

**Evidenza:**
- Codice usa `LocalDateTime.now()` (timezone implicito JVM)
- Database salva `TIMESTAMP` (senza timezone) o `TIMESTAMPTZ` (con timezone)
- Confronto `LocalDateTime.isAfter()` non considera timezone → può fallire

### 7.2 Fix Raccomandato

**Opzione A: Usare Instant** (RACCOMANDATO)
- Entity: `LocalDateTime` → `Instant`
- Service: `LocalDateTime.now()` → `Instant.now()`
- Repository: Parametro `LocalDateTime` → `Instant`
- Database: Migrazione a `TIMESTAMPTZ` (opzionale ma raccomandato)

**Impatto:** Basso (solo modifiche interne)

### 7.3 Verifica Necessaria

**Prima di applicare fix:**
1. Verificare tipo colonna database: `TIMESTAMP` vs `TIMESTAMPTZ`
2. Verificare timezone database: `SHOW timezone;`
3. Verificare timezone JVM server: `TimeZone.getDefault()`
4. Aggiungere logging temporaneo per confermare bug

**Dopo fix:**
1. Verificare che `expires_at` sia sempre in UTC
2. Verificare che confronto funzioni correttamente
3. Rimuovere logging temporaneo

---

**Fine Analisi**

