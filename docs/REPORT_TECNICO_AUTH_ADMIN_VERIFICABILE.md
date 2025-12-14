# 📋 Report Tecnico Autenticazione Admin - Verificabile

**Data Analisi:** 2025-01-06  
**Obiettivo:** Documentazione tecnica verificabile del sistema di autenticazione admin  
**Tipo:** Report descrittivo (NO refactor, NO fix)

---

## 📍 1. ENDPOINT AUTH ADMIN ESISTENTI

### 1.1 Controller e Service

**Controller:** `AdminAuthControllerModern`  
**Path Base:** `/api/admin/auth`  
**Package:** `com.funkard.adminauthmodern`  
**File:** `src/main/java/com/funkard/adminauthmodern/AdminAuthControllerModern.java`

**Service:** `AdminAuthServiceModern`  
**Package:** `com.funkard.adminauthmodern`  
**File:** `src/main/java/com/funkard/adminauthmodern/AdminAuthServiceModern.java`

---

### 1.2 Endpoint Login

**Metodo:** `POST`  
**Path:** `/api/admin/auth/login`  
**Controller Method:** `login(Map<String, String> request, HttpServletRequest httpRequest, HttpServletResponse response)`  
**Service Method:** `AdminAuthServiceModern.login(String email, String password)`  
**Autenticazione:** Pubblico (endpoint pubblico)

**Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

**Response 200 OK:**
```json
{
  "success": true,
  "admin": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "admin@example.com",
    "role": "ADMIN",
    "displayName": "Admin User"
  }
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Email richiesta"
}
```

**Response 500 Internal Server Error:**
```json
{
  "error": "Errore durante il login: ..."
}
```

**Flusso:**
1. Valida email e password
2. Cerca `AdminUser` per email
3. Verifica `active = true` e `onboardingCompleted = true`
4. Verifica password con `BCryptPasswordEncoder`
5. Aggiorna `lastLoginAt`
6. Crea sessione tramite `AdminSessionServiceModern.createSession(adminId)`
7. Imposta cookie `ADMIN_SESSION` con `sessionId`
8. Restituisce dati admin

---

### 1.3 Endpoint Logout

**Metodo:** `POST`  
**Path:** `/api/admin/auth/logout`  
**Controller Method:** `logout(HttpServletRequest request, HttpServletResponse response)`  
**Service Method:** `AdminAuthServiceModern.logout(String sessionId)`  
**Autenticazione:** Cookie `ADMIN_SESSION` (richiesto)

**Request:** Nessun body richiesto (cookie inviato automaticamente)

**Response 200 OK:**
```json
{
  "success": true
}
```

**Response 500 Internal Server Error:**
```json
{
  "error": "Errore durante il logout: ..."
}
```

**Flusso:**
1. Estrae `sessionId` da cookie `ADMIN_SESSION`
2. Invalida sessione tramite `AdminSessionServiceModern.invalidateSession(sessionId)`
3. Rimuove cookie `ADMIN_SESSION` (maxAge=0)
4. Restituisce success

---

### 1.4 Endpoint Me

**Metodo:** `GET`  
**Path:** `/api/admin/auth/me`  
**Controller Method:** `me(HttpServletRequest request)`  
**Service Method:** `AdminAuthServiceModern.getCurrentAdmin(String sessionId)`  
**Autenticazione:** Cookie `ADMIN_SESSION` (richiesto)

**Request:** Nessun body richiesto (cookie inviato automaticamente)

**Response 200 OK:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "admin@example.com",
  "role": "ADMIN",
  "displayName": "Admin User"
}
```

**Response 401 Unauthorized:**
```json
{
  "error": "Sessione non valida"
}
```

**Flusso:**
1. Estrae `sessionId` da cookie `ADMIN_SESSION`
2. Valida sessione tramite `AdminSessionServiceModern.validateSession(sessionId)`
3. Recupera `AdminUser` per `adminId`
4. Verifica `active = true`
5. Restituisce dati admin

---

### 1.5 Endpoint Onboarding Complete

**Metodo:** `POST`  
**Path:** `/api/admin/auth/onboarding-complete`  
**Controller Method:** `onboardingComplete(Map<String, String> request)`  
**Service Method:** `AdminAuthServiceModern.completeOnboarding(String token, String email, String password, String displayName)`  
**Autenticazione:** Pubblico (endpoint pubblico)

**Request Body:**
```json
{
  "token": "abc123def456...",
  "email": "newadmin@example.com",
  "password": "password123",
  "displayName": "New Admin"
}
```

**Response 200 OK:**
```json
{
  "success": true,
  "admin": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "newadmin@example.com",
    "role": "ADMIN",
    "displayName": "New Admin"
  }
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Token richiesto"
}
```

**Response 404 Not Found:**
```json
{
  "error": "Token non trovato"
}
```

**Response 409 Conflict:**
```json
{
  "error": "Email già registrata"
}
```

**Response 410 Gone:**
```json
{
  "error": "Token già utilizzato"
}
```

**Flusso:**
1. Valida input (token, email, password, displayName)
2. Valida formato email e password (min 8 caratteri)
3. Cerca token in `AdminTokenRepository.findByToken(token)`
4. Se non trovato, fallback a token legacy (`AdminUser.accessToken`)
5. Verifica token attivo e non scaduto
6. Verifica email non già registrata
7. Hash password con `BCryptPasswordEncoder`
8. Crea nuovo `AdminUser` con ruolo dal token
9. Invalida token (`active = false`)
10. Restituisce dati admin creato

---

## 🍪 2. GESTIONE SESSIONE

### 2.1 Meccanismo di Autenticazione

**Tipo:** Cookie-based session (stateful)  
**Storage:** Database (`admin_sessions` table)  
**Nome Cookie:** `ADMIN_SESSION`  
**Valore Cookie:** `sessionId` (UUID v4 senza trattini, 32 caratteri)

---

### 2.2 Configurazione Cookie

**File:** `src/main/java/com/funkard/adminauthmodern/AdminAuthControllerModern.java`  
**Metodo:** `createSessionCookie(String sessionId, HttpServletRequest request)`  
**Righe:** 205-216

**Parametri Cookie:**

| Parametro | Valore | Note |
|-----------|--------|------|
| **Nome** | `ADMIN_SESSION` | Maiuscolo |
| **HttpOnly** | `true` | Protezione XSS |
| **Secure** | `true` | Obbligatorio con SameSite=None |
| **SameSite** | `None` | Permette cross-site requests |
| **Domain** | `.funkard.com` | Condiviso tra sottodomini |
| **Path** | `/` | Disponibile su tutto il dominio |
| **Max-Age** | `14400` secondi | 4 ore |

**Codice Creazione Cookie:**
```java
ResponseCookie.from("ADMIN_SESSION", sessionId)
    .domain(".funkard.com")
    .httpOnly(true)
    .secure(true)
    .path("/")
    .maxAge(4 * 60 * 60) // 14400 secondi (4 ore)
    .sameSite("None")
    .build();
```

**Header HTTP Generato:**
```
Set-Cookie: ADMIN_SESSION=abc123def456...; Domain=.funkard.com; Path=/; Max-Age=14400; HttpOnly; Secure; SameSite=None
```

---

### 2.3 Durata Sessione

**Durata:** 4 ore (14400 secondi)  
**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionServiceModern.java`  
**Costante:** `SESSION_DURATION_HOURS = 4` (riga 24)

**Creazione Sessione:**
```java
LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);
AdminSession session = new AdminSession(sessionId, adminId, expiresAt);
```

**Validazione Sessione:**
- Verifica `expiresAt > now()` durante `validateSession()`
- Sessioni scadute vengono rimosse automaticamente
- Cleanup automatico ogni 2 ore tramite `@Scheduled`

---

### 2.4 Entity e Repository Sessione

**Entity:** `AdminSession`  
**Package:** `com.funkard.adminauthmodern`  
**File:** `src/main/java/com/funkard/adminauthmodern/AdminSession.java`

**Tabella Database:** `admin_sessions`

**Campi:**
- `id` (UUID, PK)
- `session_id` (String, unique, indexed)
- `admin_id` (UUID, indexed)
- `created_at` (LocalDateTime)
- `expires_at` (LocalDateTime, indexed)

**Repository:** `AdminSessionRepository`  
**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionRepository.java`

**Metodi:**
- `findBySessionId(String sessionId)` - Trova sessione per ID
- `deleteExpiredSessions(LocalDateTime now)` - Rimuove sessioni scadute
- `deleteByAdminId(UUID adminId)` - Rimuove tutte le sessioni di un admin
- `deleteBySessionId(String sessionId)` - Rimuove sessione specifica

---

### 2.5 Filtro Autenticazione

**Filtro:** `AdminSessionFilterModern`  
**Package:** `com.funkard.adminauthmodern`  
**File:** `src/main/java/com/funkard/adminauthmodern/AdminSessionFilterModern.java`

**Ordine:** Prima di `UsernamePasswordAuthenticationFilter`  
**Configurazione:** `SecurityConfig.java` riga 134

**Comportamento:**
1. Applica solo a path `/api/admin/**`
2. Salta endpoint pubblici (`/login`, `/onboarding-complete`)
3. Estrae cookie `ADMIN_SESSION`
4. Valida sessione tramite `AdminSessionServiceModern.validateSession()`
5. Carica `AdminUser` e popola `SecurityContext`
6. Verifica `active = true` e `onboardingCompleted = true`

---

## 🌐 3. CONFIGURAZIONE CORS E COOKIE CROSS-SITE

### 3.1 Configurazione CORS

**File:** `src/main/java/com/funkard/config/SecurityConfig.java`  
**Metodo:** `corsConfigurationSource()`  
**Righe:** 57-83

**Origini Permesse:**
```java
config.setAllowedOrigins(List.of(
    "https://www.funkard.com",
    "https://funkard.com",
    "https://admin.funkard.com",
    "https://funkard-adminreal.vercel.app",
    "http://localhost:3000",
    "http://localhost:3002"
));
```

**Metodi Permessi:**
```java
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
```

**Header Permessi:**
```java
config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-User-Id"));
```

**Header Esposti:**
```java
config.setExposedHeaders(List.of("Authorization", "X-User-Id"));
```

**Credentials:**
```java
config.setAllowCredentials(true); // ✅ Abilitato per cookie cross-site
```

**Max Age Preflight:**
```java
config.setMaxAge(3600L); // 1 ora
```

**Applicazione:**
```java
UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
source.registerCorsConfiguration("/**", config); // Applicato a tutte le rotte
```

---

### 3.2 Configurazione Cookie Cross-Site

**Requisiti per Cookie Cross-Site:**
- `SameSite=None` (obbligatorio per cross-site)
- `Secure=true` (obbligatorio con SameSite=None)
- `allowCredentials=true` in CORS (configurato)

**Configurazione Attuale:**
- ✅ `SameSite=None` (riga 214 `AdminAuthControllerModern.java`)
- ✅ `Secure=true` (riga 211 `AdminAuthControllerModern.java`)
- ✅ `Domain=.funkard.com` (riga 209 `AdminAuthControllerModern.java`)
- ✅ `allowCredentials=true` in CORS (riga 75 `SecurityConfig.java`)

**Header Set-Cookie Cross-Site:**
```
Set-Cookie: ADMIN_SESSION=abc123...; Domain=.funkard.com; Path=/; Max-Age=14400; HttpOnly; Secure; SameSite=None
```

**Header CORS Response:**
```
Access-Control-Allow-Origin: https://admin.funkard.com
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type, Accept, X-User-Id
```

---

### 3.3 @CrossOrigin sui Controller

**Controller:** `AdminAuthControllerModern`  
**Annotazione:** `@CrossOrigin` (riga 22)

```java
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "https://admin.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
```

**Nota:** Alcuni controller admin hanno origini diverse (es. `funkard-admin.vercel.app`), ma la configurazione globale in `SecurityConfig` ha priorità.

---

## 🔑 4. LOGICA ONBOARDING TOKEN MONOUSO

### 4.1 Endpoint Onboarding

**Endpoint:** `POST /api/admin/auth/onboarding-complete`  
**Controller:** `AdminAuthControllerModern.onboardingComplete()`  
**Service:** `AdminAuthServiceModern.completeOnboarding()`

**Vedi sezione 1.5 per dettagli endpoint.**

---

### 4.2 Entity AdminToken

**Entity:** `AdminToken`  
**Package:** `com.funkard.adminauth`  
**File:** `src/main/java/com/funkard/adminauth/AdminToken.java`

**Tabella Database:** `admin_tokens`

**Campi:**
- `id` (UUID, PK)
- `role` (String, length 50) - Ruolo: ADMIN, SUPERVISOR, SUPER_ADMIN
- `token` (String, unique, length 256) - Token SHA256
- `created_by` (UUID) - ID admin che ha creato il token
- `active` (boolean, default true) - Flag per invalidazione
- `created_at` (LocalDateTime)
- `expires_at` (LocalDateTime, nullable) - Scadenza opzionale

**Indici:**
- `idx_admin_tokens_token` su `token`
- `idx_admin_tokens_role` su `role`
- `idx_admin_tokens_active` su `active`

---

### 4.3 Repository AdminToken

**Repository:** `AdminTokenRepository`  
**Package:** `com.funkard.adminauth`  
**File:** `src/main/java/com/funkard/adminauth/AdminTokenRepository.java`

**Metodi:**
```java
Optional<AdminToken> findByToken(String token);
List<AdminToken> findByActiveTrue();
List<AdminToken> findByRole(String role);
List<AdminToken> findByRoleAndActiveTrue(String role);
```

---

### 4.4 Logica Invalidazione Token

**File:** `src/main/java/com/funkard/adminauthmodern/AdminAuthServiceModern.java`  
**Metodo:** `completeOnboarding()`  
**Righe:** 151-287

**Flusso Invalidazione:**

1. **Validazione Token:**
   ```java
   Optional<AdminToken> tokenOpt = adminTokenRepository.findByToken(token);
   ```

2. **Verifica Token Attivo:**
   ```java
   if (!adminToken.isActive()) {
       throw new ResponseStatusException(HttpStatus.GONE, "Token già utilizzato");
   }
   ```

3. **Verifica Scadenza:**
   ```java
   if (adminToken.getExpiresAt() != null && 
       adminToken.getExpiresAt().isBefore(LocalDateTime.now())) {
       throw new ResponseStatusException(HttpStatus.GONE, "Token scaduto");
   }
   ```

4. **Creazione AdminUser:**
   ```java
   AdminUser newAdmin = new AdminUser();
   // ... set campi ...
   AdminUser savedAdmin = adminUserRepository.save(newAdmin);
   ```

5. **Invalidazione Token (Monouso):**
   ```java
   adminToken.setActive(false);
   adminTokenRepository.save(adminToken);
   ```

**Transazione:**
- Metodo annotato con `@Transactional` (riga 151)
- Garantisce atomicità: creazione admin + invalidazione token

---

### 4.5 Fallback Token Legacy

**File:** `src/main/java/com/funkard/adminauthmodern/AdminAuthServiceModern.java`  
**Righe:** 184-223

**Logica:**
1. Se token non trovato in `AdminTokenRepository`
2. Fallback a token legacy (`AdminUser.accessToken`)
3. Usa `AdminUserService.completeOnboarding()` legacy
4. Token legacy viene azzerato (`accessToken = null`) dopo uso

**Nota:** Token legacy supportato per retrocompatibilità, ma sistema moderno usa `AdminToken`.

---

### 4.6 Service Generazione Token

**Service:** `AdminTokenService`  
**Package:** `com.funkard.adminauth`  
**File:** `src/main/java/com/funkard/adminauth/AdminTokenService.java`

**Metodo Creazione Token:**
```java
@Transactional
public String createToken(String role, UUID creatorId) {
    String uuid = UUID.randomUUID().toString();
    String timestamp = String.valueOf(System.currentTimeMillis());
    String combined = uuid + timestamp;
    String tokenHash = generateSHA256(combined);
    
    AdminToken adminToken = new AdminToken(role, tokenHash, creatorId);
    AdminToken saved = tokenRepository.save(adminToken);
    
    return tokenHash;
}
```

**Endpoint Creazione Token:**
- `POST /api/admin/auth/tokens/create` (in `AdminAuthController`)

---

## 🚀 5. AGGIUNGERE NAMESPACE /api/admin/v2/*

### 5.1 Requisiti per Aggiungere v2

Per aggiungere il namespace `/api/admin/v2/*` senza rompere i client esistenti, sono necessari i seguenti componenti:

---

### 5.2 1. Nuovo Controller v2

**Struttura Consigliata:**
```java
@RestController
@RequestMapping("/api/admin/v2/auth")
@CrossOrigin(origins = {...}, allowCredentials = "true")
public class AdminAuthControllerV2 {
    // Nuovi endpoint v2
}
```

**File:** `src/main/java/com/funkard/adminauthv2/AdminAuthControllerV2.java`

**Note:**
- Mantenere controller v1 (`AdminAuthControllerModern`) attivo
- Controller v2 può coesistere con v1
- Path base diverso: `/api/admin/v2/auth` vs `/api/admin/auth`

---

### 5.3 2. Configurazione SecurityConfig

**File:** `src/main/java/com/funkard/config/SecurityConfig.java`

**Modifiche Necessarie:**

1. **Aggiungere v2 agli endpoint pubblici:**
   ```java
   .requestMatchers("/api/admin/v2/auth/login").permitAll()
   .requestMatchers("/api/admin/v2/auth/onboarding-complete").permitAll()
   ```

2. **Aggiungere v2 al securityMatcher:**
   ```java
   .securityMatcher("/api/admin/**", "/api/admin/v2/**")
   ```

3. **Applicare stesso filtro:**
   - `AdminSessionFilterModern` si applica già a `/api/admin/**`
   - Se v2 usa stesso meccanismo cookie, funziona automaticamente
   - Se v2 usa meccanismo diverso (es. JWT), aggiungere filtro specifico

---

### 5.4 3. Configurazione CORS

**File:** `src/main/java/com/funkard/config/SecurityConfig.java`

**Modifiche:**
- Nessuna modifica necessaria se v2 usa stesse origini
- CORS configurato globalmente su `/**` (riga 80)
- Se v2 richiede origini diverse, aggiungere configurazione specifica:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    // ... configurazione esistente ...
    
    // Configurazione specifica per v2 (opzionale)
    CorsConfiguration v2Config = new CorsConfiguration();
    v2Config.setAllowedOrigins(List.of("https://v2.funkard.com"));
    v2Config.setAllowCredentials(true);
    // ... altre configurazioni ...
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    source.registerCorsConfiguration("/api/admin/v2/**", v2Config); // Override per v2
    
    return source;
}
```

---

### 5.5 4. Compatibilità Cookie

**Opzioni:**

**Opzione A: Stesso Cookie (Consigliato)**
- Usa stesso cookie `ADMIN_SESSION`
- Stesso meccanismo di sessione (`AdminSessionServiceModern`)
- Client v1 e v2 condividono sessione
- **Vantaggio:** Login una volta, accesso a v1 e v2
- **Svantaggio:** Accoppiamento tra versioni

**Opzione B: Cookie Separato**
- Nuovo cookie `ADMIN_SESSION_V2`
- Nuovo service `AdminSessionServiceV2` (opzionale)
- Sessioni separate per v1 e v2
- **Vantaggio:** Isolamento completo
- **Svantaggio:** Login separato per v1 e v2

**Raccomandazione:** Opzione A per semplicità e UX migliore.

---

### 5.6 5. Service Layer

**Opzioni:**

**Opzione A: Condividere Service (Consigliato)**
- Usa stesso `AdminAuthServiceModern`
- Controller v2 chiama stesso service
- **Vantaggio:** Logica business unificata
- **Svantaggio:** Nessuno

**Opzione B: Service Separato**
- Nuovo `AdminAuthServiceV2`
- Logica business separata
- **Vantaggio:** Isolamento completo
- **Svantaggio:** Duplicazione codice

**Raccomandazione:** Opzione A, a meno che v2 non richieda logica business completamente diversa.

---

### 5.7 6. Database Schema

**Modifiche Necessarie:**
- **Nessuna modifica** se v2 usa stesso schema
- Se v2 richiede nuove tabelle/colonne:
  - Creare migration Flyway/Liquibase
  - Non modificare tabelle esistenti (backward compatibility)

**Esempio:**
```sql
-- Se v2 richiede nuova tabella
CREATE TABLE admin_sessions_v2 (
    id UUID PRIMARY KEY,
    session_id VARCHAR(64) UNIQUE,
    admin_id UUID,
    created_at TIMESTAMP,
    expires_at TIMESTAMP
);
```

---

### 5.8 7. Versioning API Response

**Strategia Consigliata:**

1. **Mantenere v1 invariato:**
   - Non modificare response di v1
   - Client v1 continuano a funzionare

2. **v2 può avere response diverse:**
   ```json
   // v1 response
   {
     "success": true,
     "admin": {...}
   }
   
   // v2 response (esempio)
   {
     "data": {
       "admin": {...}
     },
     "meta": {
       "version": "2.0",
       "timestamp": "2025-01-06T10:00:00Z"
     }
   }
   ```

---

### 5.9 8. Testing e Verifica

**Checklist:**

- [ ] Controller v2 compila senza errori
- [ ] Endpoint v2 accessibili pubblicamente (login, onboarding)
- [ ] Cookie `ADMIN_SESSION` funziona con v2 (se Opzione A)
- [ ] CORS configurato correttamente per v2
- [ ] SecurityConfig permette accesso a v2
- [ ] Client v1 continuano a funzionare (nessuna regressione)
- [ ] Test integrazione: login v1 → accesso v2 (se cookie condiviso)
- [ ] Test integrazione: login v2 → accesso v1 (se cookie condiviso)

---

### 5.10 9. Documentazione

**Modifiche Necessarie:**

1. **Aggiornare documentazione API:**
   - Documentare endpoint v2
   - Specificare differenze con v1
   - Esempi request/response v2

2. **Versioning in URL:**
   - `/api/admin/auth/*` → v1 (esistente)
   - `/api/admin/v2/auth/*` → v2 (nuovo)

3. **Deprecation Policy:**
   - Documentare quando v1 sarà deprecato (se applicabile)
   - Timeline migrazione client da v1 a v2

---

### 5.11 10. Esempio Implementazione Minima

**File:** `src/main/java/com/funkard/adminauthv2/AdminAuthControllerV2.java`

```java
package com.funkard.adminauthv2;

import com.funkard.adminauthmodern.AdminAuthServiceModern;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/v2/auth")
@CrossOrigin(origins = {
    "https://funkard.com",
    "https://www.funkard.com",
    "https://admin.funkard.com",
    "http://localhost:3000",
    "http://localhost:3002"
}, allowCredentials = "true")
public class AdminAuthControllerV2 {
    
    private final AdminAuthServiceModern authService;
    
    public AdminAuthControllerV2(AdminAuthServiceModern authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {
        // Usa stesso service di v1
        // Stesso meccanismo cookie
        // Response può essere diversa (es. formato v2)
        return authService.login(...);
    }
    
    // Altri endpoint v2...
}
```

**Modifica SecurityConfig:**
```java
.requestMatchers("/api/admin/v2/auth/login").permitAll()
.requestMatchers("/api/admin/v2/auth/onboarding-complete").permitAll()
```

---

## ✅ CONCLUSIONI

### Stato Attuale Sistema Auth Admin

- ✅ **Sistema Moderno Attivo:** Cookie `ADMIN_SESSION`, sessioni database-backed
- ✅ **Endpoint Funzionanti:** login, logout, me, onboarding-complete
- ✅ **CORS Configurato:** Supporto cross-site con `SameSite=None; Secure=true`
- ✅ **Token Monouso:** `AdminToken` con invalidazione automatica
- ✅ **Backward Compatibility:** Fallback token legacy supportato

### Requisiti per Aggiungere v2

1. ✅ Nuovo controller con path `/api/admin/v2/auth`
2. ✅ Configurazione SecurityConfig per endpoint pubblici v2
3. ✅ CORS già configurato globalmente (nessuna modifica se stesse origini)
4. ✅ Cookie condiviso o separato (raccomandato condiviso)
5. ✅ Service condiviso o separato (raccomandato condiviso)
6. ✅ Database schema invariato (se v2 usa stesso schema)
7. ✅ Testing regressione client v1
8. ✅ Documentazione API v2

**Rischio Regressione:** 🟢 **BASSO** se v2 non modifica v1

---

_Report generato automaticamente. Nessuna modifica applicata al codice._

