package com.funkard.admin.model;

/**
 * Enum per gli stati dei ticket di supporto
 */
public enum TicketStatus {
    OPEN("open", "Aperto", "Ticket in attesa di assegnazione"),
    IN_PROGRESS("in_progress", "In Corso", "Ticket assegnato e in lavorazione"),
    RESOLVED("resolved", "Risolto", "Ticket risolto ma non ancora chiuso"),
    CLOSED("closed", "Chiuso", "Ticket chiuso definitivamente"),
    REOPENED("reopened", "Riaperto", "Ticket riaperto dopo essere stato chiuso");
    
    private final String code;
    private final String description;
    private final String details;
    
    TicketStatus(String code, String description, String details) {
        this.code = code;
        this.description = description;
        this.details = details;
    }
    
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getDetails() { return details; }
    
    /**
     * Verifica se lo stato permette l'assegnazione
     */
    public boolean canBeAssigned() {
        return this == OPEN || this == REOPENED;
    }
    
    /**
     * Verifica se lo stato permette la modifica
     */
    public boolean canBeModified() {
        return this != CLOSED;
    }
    
    /**
     * Verifica se lo stato è finale
     */
    public boolean isFinal() {
        return this == CLOSED;
    }
}
