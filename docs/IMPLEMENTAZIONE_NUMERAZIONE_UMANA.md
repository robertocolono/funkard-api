# 📋 IMPLEMENTAZIONE: Numerazione Umana per System Errors

**Data Implementazione:** 2025-01-06  
**Formato:** `{PREFIX}-{YYYY}-{NNNN}` (es. `SYS-2025-0001`)

---

## ✅ MODIFICHE IMPLEMENTATE

### 1. Database

**File:** `src/main/resources/db/migration/V25__add_human_readable_number.sql`

**Modifiche:**
1. ✅ Tabella `human_readable_counters` (nuova)
2. ✅ Campo `human_readable_number` in `admin_notifications` (nullable)
3. ✅ Indice parziale su `human_readable_number` (solo NOT NULL)
4. ⚠️ UNIQUE constraint opzionale (commentato, da valutare)

**SQL da Applicare Manualmente su Neon:**
```sql
-- 1. Creazione tabella contatori
CREATE TABLE IF NOT EXISTS human_readable_counters (
    id BIGSERIAL PRIMARY KEY,
    prefix VARCHAR(10) NOT NULL,
    year INTEGER NOT NULL,
    current_value INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_counter_prefix_year UNIQUE (prefix, year)
);

-- 2. Aggiunta campo human_readable_number
ALTER TABLE admin_notifications 
ADD COLUMN IF NOT EXISTS human_readable_number VARCHAR(15) NULL;

-- 3. Indice parziale (solo per valori NOT NULL)
CREATE INDEX IF NOT EXISTS idx_notifications_human_number 
ON admin_notifications(human_readable_number) 
WHERE human_readable_number IS NOT NULL;

-- 4. UNIQUE constraint (opzionale - da valutare)
-- ALTER TABLE admin_notifications 
-- ADD CONSTRAINT uk_notifications_human_number UNIQUE (human_readable_number);
```

---

### 2. Backend

#### A) Entity HumanReadableCounter

**File:** `src/main/java/com/funkard/admin/model/HumanReadableCounter.java`

**Caratteristiche:**
- ✅ JPA Entity standard
- ✅ Unique constraint su (prefix, year)
- ✅ Campi: prefix, year, currentValue, updatedAt

---

#### B) Repository HumanReadableCounterRepository

**File:** `src/main/java/com/funkard/admin/repository/HumanReadableCounterRepository.java`

**Metodi:**
- ✅ `findByPrefixAndYearForUpdate()` - Lock esclusivo (SELECT FOR UPDATE)
- ✅ `findByPrefixAndYear()` - Lettura standard (senza lock)

---

#### C) Service HumanReadableNumberService

**File:** `src/main/java/com/funkard/admin/service/HumanReadableNumberService.java`

**Metodi:**
- ✅ `generateHumanReadableNumber(prefix)` - Genera numero umano thread-safe
- ✅ `determinePrefixForNotification(type, priority)` - Determina prefix per notifica

**Caratteristiche:**
- ✅ Thread-safe tramite SELECT FOR UPDATE
- ✅ Lazy creation contatore (on-demand)
- ✅ Gestione race condition (DataIntegrityViolationException)
- ✅ Fallback se generazione fallisce (ritorna null)
- ✅ Timeout lock 5 secondi
- ✅ Isolation REPEATABLE_READ

---

#### D) Modifica AdminNotification Entity

**File:** `src/main/java/com/funkard/admin/model/AdminNotification.java`

**Aggiunta:**
- ✅ Campo `humanReadableNumber` (String, nullable, length=15)
- ✅ Getter/Setter

---

#### E) Modifica AdminNotificationService

**File:** `src/main/java/com/funkard/admin/service/AdminNotificationService.java`

**Modifiche:**
- ✅ Iniezione `HumanReadableNumberService`
- ✅ Chiamata `generateHumanReadableNumber()` in `createAdminNotification()`
- ✅ Solo per `type="system"` e `priority="error"|"warn"`
- ✅ Fallback: se generazione fallisce, notifica creata comunque (humanReadableNumber = null)

---

## 🔄 FLUSSO DI ESECUZIONE

### Creazione Notifica System Error

1. **AdminNotificationService.createAdminNotification()**
   - Crea `AdminNotification`
   - Determina prefix: `numberService.determinePrefixForNotification(type, priority)`
   - Se prefix = "SYS" → chiama `generateHumanReadableNumber("SYS")`

2. **HumanReadableNumberService.generateHumanReadableNumber("SYS")**
   - Inizio transazione (REPEATABLE_READ, timeout 5s)
   - `SELECT ... FOR UPDATE` su contatore (prefix="SYS", year=2025)
   - Se non esiste → crea nuovo contatore (lazy)
   - Incrementa `currentValue`
   - Genera numero: `SYS-2025-0001`
   - Commit transazione

3. **AdminNotificationService**
   - Setta `humanReadableNumber = "SYS-2025-0001"`
   - Salva notifica
   - Broadcast SSE

---

## 🛡️ THREAD-SAFETY

**Meccanismo:**
- `SELECT ... FOR UPDATE` su riga contatore
- Lock esclusivo durante generazione numero
- Gestione race condition in creazione contatore (DataIntegrityViolationException)

**Garantito:**
- ✅ Unicità numeri (nessuna duplicazione)
- ✅ Funziona con multiple istanze backend
- ✅ Gestione rollback automatica

---

## 🔙 BACKWARD-COMPATIBILITY

**Garantita:**
- ✅ Campo `humanReadableNumber` nullable
- ✅ Errori esistenti hanno `humanReadableNumber = null`
- ✅ Se generazione fallisce, notifica creata comunque (null)
- ✅ UUID resta primary key
- ✅ Nessuna breaking change

---

## 📊 ESEMPI

### Notifica System Error

**Input:**
- type = "system"
- priority = "error"
- title = "Errore generico sistema"

**Output:**
- `humanReadableNumber = "SYS-2025-0001"`
- UUID resta primary key

### Notifica System Warn

**Input:**
- type = "system"
- priority = "warn"
- title = "Warning sistema"

**Output:**
- `humanReadableNumber = "SYS-2025-0002"`
- UUID resta primary key

### Notifica Non-System

**Input:**
- type = "new_card"
- priority = "normal"

**Output:**
- `humanReadableNumber = null`
- UUID resta primary key

---

## ⚠️ NOTE IMPORTANTI

### SQL da Applicare Manualmente

**⚠️ IMPORTANTE:** Il file SQL `V25__add_human_readable_number.sql` è solo documentazione.

**Applicare manualmente su Neon:**
1. Eseguire SQL per creare tabella `human_readable_counters`
2. Eseguire SQL per aggiungere colonna `human_readable_number`
3. Eseguire SQL per creare indice parziale
4. (Opzionale) Valutare UNIQUE constraint

### Hibernate ddl-auto=update

**Nota:** Hibernate creerà automaticamente:
- Tabella `human_readable_counters` (se non esiste)
- Colonna `human_readable_number` (se non esiste)

**Raccomandazione:** Applicare SQL manualmente per controllo esplicito.

---

## 🧪 TESTING

### Test Manuali

1. **Creare notifica system/error:**
   - Verificare che `humanReadableNumber` sia generato (es. "SYS-2025-0001")
   - Verificare che UUID sia primary key

2. **Creare notifica system/warn:**
   - Verificare che `humanReadableNumber` sia generato (es. "SYS-2025-0002")

3. **Creare notifica non-system:**
   - Verificare che `humanReadableNumber` sia null

4. **Test concorrenza:**
   - Creare multiple notifiche simultaneamente
   - Verificare che numeri siano univoci e sequenziali

---

## 📝 TODO FUTURO

### UI Admin (Fase 2)

- [ ] Mostrare `humanReadableNumber` invece di UUID in lista notifiche
- [ ] Mostrare `humanReadableNumber` in dettaglio notifica
- [ ] Fallback a UUID se `humanReadableNumber` è null
- [ ] Filtro ricerca per numero umano

### Estendibilità

- [ ] Aggiungere prefix "TKT" per support tickets
- [ ] Aggiungere prefix "DSP" per disputes
- [ ] Aggiungere prefix "USR" per user notifications

---

**Fine Implementazione**

