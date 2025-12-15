# 🔍 Diagnosi 401 Unauthorized su `/api/admin/franchises`

**Data Analisi:** 2025-12-15  
**Endpoint Problematico:** `GET /api/admin/franchises?size=1&status=pending`  
**Problema:** Ritorna `401 Unauthorized` mentre altri endpoint admin funzionano correttamente

---

## 📋 1. ANALISI PUNTI CHE POSSONO CAUSARE 401

### 1.1 Controller: `FranchiseAdminController`

**File:** `src/main/java/com/funkard/admin/controller/FranchiseAdminController.java`

**Configurazione:**
```java
@RestController
@RequestMapping("/api/admin/franchises")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR', 'ADMIN')")  // ← Livello classe
public class FranchiseAdminController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFranchisesAndProposals(
            @RequestParam(required = false) String status) {
        // Nessun controllo aggiuntivo nel metodo
    }
}
```

**Analisi:**
- ✅ `@PreAuthorize` a livello di classe (si applica a tutti i metodi)
- ✅ Ruoli richiesti: `SUPER_ADMIN`, `SUPERVISOR`, `ADMIN`
- ✅ Nessun controllo manuale aggiuntivo nel metodo `getAllFranchisesAndProposals()`
- ✅ Nessun controllo nel service `FranchiseAdminService.getAllFranchisesAndProposals()`

---

### 1.2 SecurityConfig: Regole di Accesso

**File:** `src/main/java/com/funkard/config/SecurityConfig.java`

**Configurazione:**
```java
.authorizeHttpRequests(auth -> auth
    // ... endpoint pubblici ...
    .anyRequest().authenticated()  // ← Richiede autenticazione
)
```

**Analisi:**
- ✅ `/api/admin/franchises` NON è in lista `permitAll()`
- ✅ Richiede autenticazione tramite `.authenticated()`
- ✅ Nessuna regola specifica che blocca questo endpoint

---

### 1.3 AdminSessionFilterModern: Popolazione SecurityContext

**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionFilterModern.java`

**Logica:**
```java
private void setSecurityContext(AdminUser admin, HttpServletRequest request) {
    String role = "ROLE_" + admin.getRole();  // ← Crea "ROLE_SUPER_ADMIN"
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
    
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        admin.getEmail(),
        null,
        Collections.singletonList(authority)  // ← Solo ROLE_SUPER_ADMIN
    );
    
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

**Analisi:**
- ✅ Se admin ha ruolo `SUPER_ADMIN` → crea `ROLE_SUPER_ADMIN`
- ⚠️ **PROBLEMA IDENTIFICATO**: Il filtro moderno NON aggiunge `ROLE_ADMIN` per compatibilità
- ⚠️ Il filtro legacy aggiungeva `ROLE_ADMIN` se ruolo era `SUPER_ADMIN` o `SUPERVISOR`
- ⚠️ Spring Security con `hasAnyRole('SUPER_ADMIN', 'SUPERVISOR', 'ADMIN')` cerca:
  - `ROLE_SUPER_ADMIN` ✅ (presente)
  - `ROLE_SUPERVISOR` ❌ (non presente se admin è SUPER_ADMIN)
  - `ROLE_ADMIN` ❌ (non presente - manca per compatibilità)

---

### 1.4 Confronto con Altri Endpoint Funzionanti

**Endpoint che funzionano:**
- `GET /api/admin/auth/me` → Usa controller che legge direttamente cookie (non usa SecurityContext)
- `GET /api/admin/dashboard` → `@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")`
- `GET /api/admin/valuation/pending` → `@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")`

**Differenza chiave:**
- Gli endpoint funzionanti usano `hasRole()` o `hasAnyRole()` con `ADMIN` come primo ruolo
- `FranchiseAdminController` usa `hasAnyRole('SUPER_ADMIN', 'SUPERVISOR', 'ADMIN')` con `SUPER_ADMIN` come primo ruolo

**Ma questo non dovrebbe essere un problema** perché `hasAnyRole()` verifica tutti i ruoli.

---

## 🎯 2. CAUSA PRIMARIA IDENTIFICATA

### 2.1 Problema: SecurityContext Non Popolato o Ruolo Non Trovato

**Scenario più probabile:**
1. Il filtro `AdminSessionFilterModern` popola il SecurityContext con `ROLE_SUPER_ADMIN`
2. Spring Security valuta `@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SUPERVISOR', 'ADMIN')")`
3. Spring Security cerca `ROLE_SUPER_ADMIN`, `ROLE_SUPERVISOR`, `ROLE_ADMIN` nel SecurityContext
4. **PROBLEMA**: Se il SecurityContext non è popolato correttamente o viene pulito prima del controller, `@PreAuthorize` fallisce

**Verifica necessaria:**
- Controllare se il SecurityContext viene popolato correttamente per questo endpoint
- Verificare se c'è un problema con l'ordine dei filtri
- Verificare se il ruolo nel database corrisponde esattamente a `SUPER_ADMIN` (case-sensitive)

---

### 2.2 Possibile Causa: Mismatch Ruolo o SecurityContext Vuoto

**Ipotesi 1: Ruolo nel database non corrisponde**
- Database: `super_admin` (minuscolo) o `Super_Admin` (mixed case)
- Filtro crea: `ROLE_super_admin` o `ROLE_Super_Admin`
- Spring Security cerca: `ROLE_SUPER_ADMIN`
- **Risultato**: Mismatch → 401

**Ipotesi 2: SecurityContext non popolato**
- Il filtro non viene eseguito per questo endpoint specifico
- Il SecurityContext viene pulito da qualche altro filtro
- L'ordine dei filtri causa il problema

**Ipotesi 3: Admin non attivo o onboarding non completato**
- Il filtro verifica `admin.isActive() && admin.isOnboardingCompleted()`
- Se uno dei due è `false`, il SecurityContext NON viene popolato
- Ma altri endpoint funzionano, quindi questo non dovrebbe essere il problema

---

## 🔧 3. FIX PROPOSTO

### 3.1 Fix Minimale: Aggiungere ROLE_ADMIN per Compatibilità

**Problema:** Il filtro moderno non aggiunge `ROLE_ADMIN` per compatibilità come faceva il filtro legacy.

**Soluzione:** Modificare `AdminSessionFilterModern.setSecurityContext()` per aggiungere `ROLE_ADMIN` se il ruolo è `SUPER_ADMIN` o `SUPERVISOR`.

**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionFilterModern.java`

**Modifica:**
```java
private void setSecurityContext(AdminUser admin, HttpServletRequest request) {
    String role = admin.getRole();
    String roleAuthority = "ROLE_" + role;
    
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(roleAuthority));
    
    // Aggiungi ROLE_ADMIN per compatibilità (come filtro legacy)
    if ("SUPER_ADMIN".equals(role) || "SUPERVISOR".equals(role)) {
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        admin.getEmail(),
        null,
        authorities
    );
    
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

**Vantaggi:**
- ✅ Allinea comportamento filtro moderno con filtro legacy
- ✅ Garantisce compatibilità con `hasAnyRole('ADMIN', 'SUPER_ADMIN')`
- ✅ Fix minimale e sicuro
- ✅ Non modifica altri comportamenti

---

### 3.2 Fix Alternativo: Verificare Case-Sensitivity del Ruolo

**Se il problema è case-sensitivity:**
```java
private void setSecurityContext(AdminUser admin, HttpServletRequest request) {
    String role = admin.getRole();
    if (role == null) {
        logger.warn("⚠️ Admin senza ruolo: {}", admin.getEmail());
        return;
    }
    
    // Normalizza ruolo a uppercase per sicurezza
    String normalizedRole = role.toUpperCase();
    String roleAuthority = "ROLE_" + normalizedRole;
    
    // ... resto del codice ...
}
```

---

### 3.3 Fix Alternativo: Verificare Ordine Filtri

**Se il problema è ordine filtri:**
- Verificare che `AdminSessionFilterModern` sia eseguito PRIMA di Spring Security
- Verificare che nessun altro filtro pulisca il SecurityContext

**Configurazione attuale:**
```java
.addFilterBefore(adminSessionFilterModern, UsernamePasswordAuthenticationFilter.class)
```

**Dovrebbe essere corretto**, ma verificare se ci sono altri filtri che interferiscono.

---

## ✅ 4. RACCOMANDAZIONE FINALE

### Fix Consigliato: Aggiungere ROLE_ADMIN per Compatibilità

**Motivo:**
1. Allinea comportamento con filtro legacy
2. Garantisce compatibilità con tutti gli endpoint che usano `hasAnyRole('ADMIN', 'SUPER_ADMIN')`
3. Fix minimale e sicuro
4. Non introduce nuovi token o onboarding

**Implementazione:**
Modificare `AdminSessionFilterModern.setSecurityContext()` come mostrato in 3.1.

---

## 📝 5. CHECKLIST VERIFICA

- [ ] Verificare ruolo admin nel database (deve essere esattamente `SUPER_ADMIN`)
- [ ] Verificare che `admin.isActive() == true`
- [ ] Verificare che `admin.isOnboardingCompleted() == true`
- [ ] Verificare che il SecurityContext sia popolato correttamente (logging)
- [ ] Verificare che nessun altro filtro pulisca il SecurityContext
- [ ] Applicare fix aggiungendo `ROLE_ADMIN` per compatibilità
- [ ] Testare endpoint dopo fix

---

## 🔍 6. DEBUG SUGGERITO

**Aggiungere logging temporaneo in `AdminSessionFilterModern.setSecurityContext()`:**
```java
logger.info("🔐 Popolando SecurityContext per admin: {} con ruolo: {}", 
    admin.getEmail(), admin.getRole());
logger.info("🔐 Authorities create: {}", authorities);
```

**Aggiungere logging in `FranchiseAdminController.getAllFranchisesAndProposals()`:**
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth != null) {
    log.info("🔐 Authentication presente: {}", auth.getAuthorities());
} else {
    log.warn("⚠️ Authentication NULL nel controller!");
}
```

