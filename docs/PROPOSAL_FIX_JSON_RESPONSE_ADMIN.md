# 🔧 Proposta Fix: Garantire Risposte JSON Sempre per Endpoint Admin

**Data:** 2025-01-06  
**Problema:** Frontend riceve 200 OK con body plain text "This session..." invece di JSON  
**Impatto:** Crash frontend con `Unexpected token 'T'`, loading infinito

---

## 🔍 Analisi Problema

### Situazione Attuale

1. **Frontend riceve:**
   - Status: `200 OK`
   - Content-Type: `text/plain` (o mancante)
   - Body: `"This session..."` (plain text)

2. **Filtro attuale (`AdminJsonErrorResponseFilter`):**
   - Intercetta solo status `401` e `403`
   - Non intercetta status `200` con contenuto non JSON
   - Non verifica se il contenuto è JSON valido

3. **Handler Spring Security:**
   - `AuthenticationEntryPoint` e `AccessDeniedHandler` configurati
   - Potrebbero non essere chiamati se qualcosa scrive direttamente nella risposta con status 200

---

## 💡 Proposta Fix Strutturale

### 1. Modificare `AdminJsonErrorResponseFilter`

**Obiettivo:** Intercettare TUTTE le risposte admin e garantire che siano JSON valido

**Modifiche:**
1. Intercettare anche status `200` se il contenuto non è JSON
2. Verificare se il contenuto è JSON valido (parsing)
3. Se non è JSON, convertirlo in JSON con status appropriato
4. Garantire `Content-Type: application/json` sempre

**Logica:**
- Se status è `200` e contenuto non è JSON → Converti in JSON con status `401` o `403` (basato sul contenuto)
- Se status è `401/403` e contenuto non è JSON → Converti in JSON
- Se contenuto è già JSON → Lascia invariato

### 2. Assicurare Ordine Filtri

**Problema:** Il filtro `@Component` viene registrato automaticamente, potrebbe essere eseguito prima di Spring Security

**Soluzione:** Registrare il filtro esplicitamente in `SecurityConfig` DOPO Spring Security

### 3. Verificare Nessun Altro Punto Scrive Risposte

**Cercare:**
- Filtri che scrivono direttamente nella risposta
- Interceptor che modificano risposte
- Exception handler che ritornano plain text

---

## 📋 Implementazione Proposta

### File 1: `AdminJsonErrorResponseFilter.java` (Modificato)

```java
@Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
    
    String path = request.getRequestURI();
    
    // Applica solo a endpoint admin
    if (path == null || !path.startsWith("/api/admin/")) {
        filterChain.doFilter(request, response);
        return;
    }
    
    // Wrappa la risposta per poter leggere il contenuto
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
    
    try {
        filterChain.doFilter(request, responseWrapper);
    } finally {
        int status = responseWrapper.getStatus();
        String contentType = responseWrapper.getContentType();
        byte[] content = responseWrapper.getContentAsByteArray();
        
        // Verifica se il contenuto è JSON valido
        boolean isJson = isJsonContent(contentType, content);
        
        // Se non è JSON, convertilo
        if (!isJson) {
            // Determina status e messaggio appropriato
            HttpStatus errorStatus = determineErrorStatus(status, content);
            String errorMessage = extractErrorMessage(content, errorStatus);
            
            // Determina formato v1 o v2
            boolean isV2 = path != null && path.contains("/v2/");
            
            Map<String, Object> errorBody = createErrorBody(isV2, errorMessage);
            
            // Svuota e riscrivi come JSON
            responseWrapper.resetBuffer();
            responseWrapper.setStatus(errorStatus.value());
            responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
            responseWrapper.setCharacterEncoding("UTF-8");
            
            objectMapper.writeValue(responseWrapper.getWriter(), errorBody);
        } else {
            // Assicura Content-Type JSON anche se già JSON
            if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
            }
        }
        
        // Copia la risposta al response originale
        responseWrapper.copyBodyToResponse();
    }
}

private boolean isJsonContent(String contentType, byte[] content) {
    if (content == null || content.length == 0) {
        return false;
    }
    
    // Verifica Content-Type
    if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
        // Verifica anche che il contenuto sia JSON valido
        try {
            objectMapper.readTree(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Verifica se inizia con { o [
    try {
        String contentStr = new String(content, StandardCharsets.UTF_8).trim();
        if (contentStr.startsWith("{") || contentStr.startsWith("[")) {
            objectMapper.readTree(content);
            return true;
        }
    } catch (Exception e) {
        // Non è JSON
    }
    
    return false;
}

private HttpStatus determineErrorStatus(int status, byte[] content) {
    // Se status è già 401/403, mantienilo
    if (status == HttpStatus.UNAUTHORIZED.value()) {
        return HttpStatus.UNAUTHORIZED;
    }
    if (status == HttpStatus.FORBIDDEN.value()) {
        return HttpStatus.FORBIDDEN;
    }
    
    // Se status è 200 ma contenuto è errore, determina status appropriato
    if (status == HttpStatus.OK.value()) {
        String contentStr = new String(content, StandardCharsets.UTF_8);
        if (contentStr.contains("session") || contentStr.contains("authentication") || 
            contentStr.contains("unauthorized")) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (contentStr.contains("forbidden") || contentStr.contains("access denied")) {
            return HttpStatus.FORBIDDEN;
        }
        // Default: 401 per sicurezza
        return HttpStatus.UNAUTHORIZED;
    }
    
    // Default: 401
    return HttpStatus.UNAUTHORIZED;
}

private String extractErrorMessage(byte[] content, HttpStatus status) {
    if (content == null || content.length == 0) {
        return status == HttpStatus.UNAUTHORIZED 
            ? "Sessione non valida o scaduta" 
            : "FORBIDDEN";
    }
    
    String contentStr = new String(content, StandardCharsets.UTF_8);
    
    // Se contiene "This session", estrai il messaggio
    if (contentStr.contains("This session")) {
        // Estrai il messaggio completo
        int start = contentStr.indexOf("This session");
        String message = contentStr.substring(start).trim();
        // Limita la lunghezza
        if (message.length() > 200) {
            message = message.substring(0, 200) + "...";
        }
        return message;
    }
    
    // Default
    return status == HttpStatus.UNAUTHORIZED 
        ? "Sessione non valida o scaduta" 
        : "FORBIDDEN";
}

private Map<String, Object> createErrorBody(boolean isV2, String errorMessage) {
    if (isV2) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("success", false);
        errorBody.put("data", null);
        errorBody.put("error", errorMessage);
        return errorBody;
    } else {
        return Map.of("error", errorMessage);
    }
}
```

### File 2: `SecurityConfig.java` (Modificato)

**Aggiungere registrazione esplicita del filtro:**

```java
@Bean
@Order(0) // Prima di tutto
public FilterRegistrationBean<AdminJsonErrorResponseFilter> adminJsonErrorResponseFilterRegistration(
        AdminJsonErrorResponseFilter filter) {
    FilterRegistrationBean<AdminJsonErrorResponseFilter> registration = 
        new FilterRegistrationBean<>(filter);
    registration.setOrder(Ordered.LOWEST_PRECEDENCE); // Dopo Spring Security
    registration.addUrlPatterns("/api/admin/*");
    return registration;
}
```

**OPPURE** rimuovere `@Component` e registrarlo manualmente nella SecurityFilterChain:

```java
.addFilterAfter(adminJsonErrorResponseFilter, UsernamePasswordAuthenticationFilter.class)
```

---

## ✅ Checklist Verifica

- [ ] Filtro intercetta status 200 con contenuto non JSON
- [ ] Filtro verifica se contenuto è JSON valido
- [ ] Filtro converte plain text in JSON con status appropriato
- [ ] Filtro garantisce `Content-Type: application/json` sempre
- [ ] Filtro è eseguito DOPO Spring Security
- [ ] Nessun altro punto scrive risposte plain text
- [ ] Test con status 200 + plain text → JSON 401/403
- [ ] Test con status 401/403 + plain text → JSON 401/403
- [ ] Test con status 200 + JSON → JSON invariato
- [ ] Test endpoint v1 e v2

---

## 🎯 Risultato Atteso

**Prima:**
- Status: `200 OK`
- Content-Type: `text/plain`
- Body: `"This session..."`

**Dopo:**
- Status: `401 Unauthorized` (o `403 Forbidden`)
- Content-Type: `application/json`
- Body: `{"success": false, "data": null, "error": "Sessione non valida o scaduta"}` (v2) o `{"error": "..."}` (v1)

---

## ⚠️ Note Importanti

1. **Ordine Filtri:** Il filtro DEVE essere eseguito DOPO Spring Security per intercettare le risposte generate
2. **Performance:** Il parsing JSON aggiunge overhead, ma necessario per verificare validità
3. **Retrocompatibilità:** Mantiene formato v1 per endpoint non-v2
4. **Logging:** Aggiungere logging per debug quando viene convertita una risposta

