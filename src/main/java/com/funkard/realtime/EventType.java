package com.funkard.realtime;

/**
 * 📡 Enum per tipi di eventi real-time
 * Utilizzato per identificare il tipo di evento inviato via SSE/WebSocket
 */
public enum EventType {
    /**
     * 🎫 Nuovo ticket creato
     */
    NEW_TICKET("new-ticket"),
    
    /**
     * 💬 Nuova risposta/messaggio
     */
    NEW_REPLY("new-reply"),
    
    /**
     * 🔄 Cambio stato ticket
     */
    TICKET_STATUS("ticket-status"),
    
    /**
     * 👨‍💻 Ticket assegnato a un admin/support
     */
    TICKET_ASSIGNED("ticket-assigned"),
    
    /**
     * ✅ Ticket risolto
     */
    TICKET_RESOLVED("ticket-resolved"),
    
    /**
     * 🔒 Ticket chiuso
     */
    TICKET_CLOSED("ticket-closed"),
    
    /**
     * 🔄 Ticket riaperto
     */
    TICKET_REOPENED("ticket-reopened"),
    
    /**
     * 🔔 Notifica generica
     */
    NOTIFICATION("notification"),
    
    /**
     * 🏓 Keep-alive ping
     */
    PING("ping"),
    
    /**
     * ✅ Connessione stabilita
     */
    CONNECTED("connected"),
    
    /**
     * ❌ Errore
     */
    ERROR("error");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}

