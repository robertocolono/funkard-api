package com.funkard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 🔧 Filtro per garantire che TUTTE le risposte per endpoint admin siano sempre in formato JSON
 * Intercetta le risposte prima che vengano inviate al client e converte errori in formato JSON
 * 
 * Questo filtro funziona come fallback per garantire che anche se Spring Security
 * o altri componenti generano risposte in formato testo plain (anche con status 200),
 * vengano convertite in JSON con status appropriato (401/403).
 * 
 * NOTA: Questo filtro viene registrato manualmente in SecurityConfig DOPO Spring Security
 * per garantire l'ordine corretto di esecuzione.
 */
public class AdminJsonErrorResponseFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AdminJsonErrorResponseFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            
            // Se non è JSON, convertilo in JSON con status appropriato
            if (!isJson && content != null && content.length > 0) {
                logger.warn("⚠️ Risposta non JSON intercettata per {}: status={}, contentType={}, content={}", 
                    path, status, contentType, 
                    content.length > 100 ? new String(content, 0, 100, StandardCharsets.UTF_8) + "..." 
                                         : new String(content, StandardCharsets.UTF_8));
                
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
                
                logger.debug("✅ Risposta convertita in JSON: status={}, formato={}", 
                    errorStatus.value(), isV2 ? "v2" : "v1");
            } else if (isJson) {
                // Assicura Content-Type JSON anche se già JSON
                if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                    responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
                }
            }
            
            // Copia la risposta al response originale
            responseWrapper.copyBodyToResponse();
        }
    }
    
    /**
     * Verifica se il contenuto è JSON valido
     */
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
                logger.debug("Content-Type è JSON ma contenuto non valido: {}", e.getMessage());
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
    
    /**
     * Determina lo status HTTP appropriato basato sullo status corrente e sul contenuto
     */
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
            String contentStr = new String(content, StandardCharsets.UTF_8).toLowerCase();
            if (contentStr.contains("session") || contentStr.contains("authentication") || 
                contentStr.contains("unauthorized") || contentStr.contains("this session")) {
                return HttpStatus.UNAUTHORIZED;
            }
            if (contentStr.contains("forbidden") || contentStr.contains("access denied") ||
                contentStr.contains("permission")) {
                return HttpStatus.FORBIDDEN;
            }
            // Default: 401 per sicurezza (sessione non valida)
            return HttpStatus.UNAUTHORIZED;
        }
        
        // Default: 401
        return HttpStatus.UNAUTHORIZED;
    }
    
    /**
     * Estrae il messaggio di errore dal contenuto
     */
    private String extractErrorMessage(byte[] content, HttpStatus status) {
        if (content == null || content.length == 0) {
            return status == HttpStatus.UNAUTHORIZED 
                ? "Sessione non valida o scaduta" 
                : "FORBIDDEN";
        }
        
        String contentStr = new String(content, StandardCharsets.UTF_8);
        
        // Se contiene "This session", estrai il messaggio
        if (contentStr.contains("This session") || contentStr.contains("this session")) {
            // Estrai il messaggio completo
            int start = contentStr.toLowerCase().indexOf("this session");
            String message = contentStr.substring(start).trim();
            // Rimuovi eventuali virgolette all'inizio/fine
            message = message.replaceAll("^[\"']|[\"']$", "");
            // Limita la lunghezza
            if (message.length() > 200) {
                message = message.substring(0, 200) + "...";
            }
            return message;
        }
        
        // Se contiene altri messaggi di errore, estraili
        if (contentStr.length() < 500) {
            // Rimuovi virgolette e spazi extra
            String cleanMessage = contentStr.trim()
                .replaceAll("^[\"']|[\"']$", "")
                .replaceAll("\\s+", " ");
            if (cleanMessage.length() > 200) {
                cleanMessage = cleanMessage.substring(0, 200) + "...";
            }
            return cleanMessage;
        }
        
        // Default
        return status == HttpStatus.UNAUTHORIZED 
            ? "Sessione non valida o scaduta" 
            : "FORBIDDEN";
    }
    
    /**
     * Crea il body JSON dell'errore nel formato appropriato (v1 o v2)
     */
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
}

