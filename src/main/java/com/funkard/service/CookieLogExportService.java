package com.funkard.service;

import com.funkard.model.CookieConsentLog;
import com.funkard.model.User;
import com.funkard.service.CookieConsentLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 📥 Service per export log consenso cookie (GDPR Art. 15 e 20)
 * 
 * Supporta export in formato JSON e PDF
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CookieLogExportService {
    
    private final CookieConsentLogService logService;
    private final ObjectMapper objectMapper;
    
    /**
     * 📥 Esporta log in formato JSON
     * @param user Utente
     * @return JSON string
     */
    public String exportAsJson(User user) {
        List<CookieConsentLog> logs = logService.getLogsByUserId(user.getId());
        
        Map<String, Object> export = new HashMap<>();
        export.put("header", "Funkard — Storico Consenso Cookie (Audit Log GDPR)");
        export.put("metadata", Map.of(
            "userId", user.getId(),
            "email", user.getEmail(),
            "generatedAt", java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "totalRecords", logs.size()
        ));
        export.put("logs", logs.stream().map(this::logToMap).toList());
        export.put("footer", "Generato da Funkard API — " + 
            java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(export);
        } catch (Exception e) {
            log.error("Errore nella serializzazione JSON: {}", e.getMessage());
            throw new RuntimeException("Errore nell'export JSON", e);
        }
    }
    
    /**
     * 📥 Esporta log in formato PDF
     * @param user Utente
     * @return Byte array del PDF
     */
    public byte[] exportAsPdf(User user) {
        List<CookieConsentLog> logs = logService.getLogsByUserId(user.getId());
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Crea PDF document
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
            com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);
            
            // Header
            document.add(new com.itextpdf.layout.element.Paragraph("Funkard — Storico Consenso Cookie (Audit Log GDPR)")
                .setFontSize(18).setBold());
            document.add(new com.itextpdf.layout.element.Paragraph(" "));
            
            // Metadati
            document.add(new com.itextpdf.layout.element.Paragraph("Utente ID: " + user.getId()));
            document.add(new com.itextpdf.layout.element.Paragraph("Email: " + user.getEmail()));
            document.add(new com.itextpdf.layout.element.Paragraph("Generato il: " + 
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
            document.add(new com.itextpdf.layout.element.Paragraph("Totale record: " + logs.size()));
            document.add(new com.itextpdf.layout.element.Paragraph(" "));
            
            // Tabella log
            float[] columnWidths = {2, 1.5f, 3, 3, 2};
            com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(columnWidths);
            table.addHeaderCell("Data");
            table.addHeaderCell("Azione");
            table.addHeaderCell("Vecchie Preferenze");
            table.addHeaderCell("Nuove Preferenze");
            table.addHeaderCell("IP/User Agent");
            
            for (CookieConsentLog log : logs) {
                table.addCell(log.getCreatedAt() != null 
                    ? log.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "N/A");
                table.addCell(log.getAction());
                table.addCell(log.getOldPreferences() != null && !log.getOldPreferences().isEmpty() 
                    ? log.getOldPreferences() : "N/A");
                table.addCell(log.getNewPreferences());
                String ipUA = (log.getIpAddress() != null ? log.getIpAddress() : "N/A");
                if (log.getUserAgent() != null && !log.getUserAgent().isEmpty()) {
                    ipUA += " / " + (log.getUserAgent().length() > 50 
                        ? log.getUserAgent().substring(0, 50) + "..." 
                        : log.getUserAgent());
                }
                table.addCell(ipUA);
            }
            
            document.add(table);
            
            // Footer
            document.add(new com.itextpdf.layout.element.Paragraph(" "));
            document.add(new com.itextpdf.layout.element.Paragraph(
                "Generato da Funkard API — " + 
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .setFontSize(8).setItalic());
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Errore nella generazione PDF: {}", e.getMessage());
            throw new RuntimeException("Errore nell'export PDF", e);
        }
    }
    
    /**
     * 🔄 Converte log in Map per JSON
     */
    private Map<String, Object> logToMap(CookieConsentLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("action", log.getAction());
        map.put("oldPreferences", log.getOldPreferences());
        map.put("newPreferences", log.getNewPreferences());
        map.put("ipAddress", log.getIpAddress());
        map.put("userAgent", log.getUserAgent());
        map.put("createdAt", log.getCreatedAt() != null 
            ? log.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            : null);
        return map;
    }
}

