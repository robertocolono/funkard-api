package com.funkard.admin.model;

/**
 * Enum per i ruoli degli utenti nel sistema di supporto
 */
public enum UserRole {
    SUPER_ADMIN("SUPER_ADMIN", "Super Admin", 3),
    ADMIN("ADMIN", "Admin", 2),
    SUPPORT("SUPPORT", "Support", 1),
    USER("USER", "User", 0);
    
    private final String code;
    private final String description;
    private final int level; // Livello di autorizzazione (più alto = più permessi)
    
    UserRole(String code, String description, int level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }
    
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public int getLevel() { return level; }
    
    /**
     * Verifica se questo ruolo ha permessi superiori o uguali a quello specificato
     */
    public boolean hasPermission(UserRole otherRole) {
        return this.level >= otherRole.level;
    }
    
    /**
     * Verifica se questo ruolo può gestire ticket
     */
    public boolean canManageTickets() {
        return this.level >= SUPPORT.level;
    }
    
    /**
     * Verifica se questo ruolo può assegnare ticket
     */
    public boolean canAssignTickets() {
        return this.level >= SUPPORT.level;
    }
    
    /**
     * Verifica se questo ruolo può sbloccare ticket assegnati ad altri
     */
    public boolean canUnlockAnyTicket() {
        return this.level >= ADMIN.level;
    }
}
