package com.funkard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 🔧 Filtro per garantire che tutte le risposte 401/403 per endpoint admin siano sempre in formato JSON
 * Intercetta le risposte prima che vengano inviate al client e converte errori in formato JSON
 * 
 * Questo filtro funziona come fallback per garantire che anche se Spring Security
 * genera risposte in formato testo plain, vengano convertite in JSON.
 */
@Component
public class AdminJsonErrorResponseFilter extends OncePerRequestFilter {

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
            // Verifica se la risposta è un errore 401/403
            int status = responseWrapper.getStatus();
            
            if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
                // Verifica se il contenuto è già JSON
                String contentType = responseWrapper.getContentType();
                
                if (contentType == null || !contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                    // Il contenuto NON è JSON, convertilo
                    String errorMessage = status == HttpStatus.UNAUTHORIZED.value() 
                        ? "Sessione non valida o scaduta" 
                        : "FORBIDDEN";
                    
                    // Determina formato v1 o v2
                    boolean isV2 = path != null && path.contains("/v2/");
                    
                    Map<String, Object> errorBody;
                    if (isV2) {
                        // Formato v2
                        errorBody = new HashMap<>();
                        errorBody.put("success", false);
                        errorBody.put("data", null);
                        errorBody.put("error", errorMessage);
                    } else {
                        // Formato v1
                        errorBody = Map.of("error", errorMessage);
                    }
                    
                    // Svuota il contenuto esistente e scrivi JSON
                    responseWrapper.resetBuffer();
                    responseWrapper.setStatus(status);
                    responseWrapper.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    responseWrapper.setCharacterEncoding("UTF-8");
                    
                    objectMapper.writeValue(responseWrapper.getWriter(), errorBody);
                }
            }
            
            // Copia la risposta al response originale
            responseWrapper.copyBodyToResponse();
        }
    }
}

