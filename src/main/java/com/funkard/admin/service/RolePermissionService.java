package com.funkard.admin.service;

import com.funkard.admin.model.UserRole;
import com.funkard.admin.model.TicketStatus;
import com.funkard.admin.model.SupportTicket;
import com.funkard.model.User;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * 🔐 Servizio per gestione ruoli e permessi nel sistema di supporto
 */
@Service
public class RolePermissionService {

    /**
     * 🔍 Verifica se l'utente può visualizzare il ticket
     */
    public boolean canViewTicket(User user, SupportTicket ticket) {
        UserRole userRole = getUserRole(user);
        
        switch (userRole) {
            case SUPER_ADMIN:
            case ADMIN:
                return true; // Possono vedere tutti i ticket
            case SUPPORT:
                return canSupportViewTicket(user, ticket);
            default:
                return false;
        }
    }

    /**
     * 🔍 Verifica se l'utente può modificare il ticket
     */
    public boolean canModifyTicket(User user, SupportTicket ticket) {
        UserRole userRole = getUserRole(user);
        
        switch (userRole) {
            case SUPER_ADMIN:
                return true; // Può modificare qualsiasi ticket
            case ADMIN:
                return !TicketStatus.CLOSED.getCode().equals(ticket.getStatus());
            case SUPPORT:
                return canSupportModifyTicket(user, ticket);
            default:
                return false;
        }
    }

    /**
     * 🔍 Verifica se l'utente può assegnare il ticket
     */
    public boolean canAssignTicket(User user, SupportTicket ticket) {
        UserRole userRole = getUserRole(user);
        
        if (!userRole.canAssignTickets()) {
            return false;
        }
        
        // Verifica stato del ticket
        TicketStatus status = getTicketStatus(ticket);
        return status.canBeAssigned();
    }

    /**
     * 🔍 Verifica se l'utente può sbloccare il ticket
     */
    public boolean canUnassignTicket(User user, SupportTicket ticket) {
        UserRole userRole = getUserRole(user);
        
        if (userRole.canUnlockAnyTicket()) {
            return true;
        }
        
        // SUPPORT può sbloccare solo i propri ticket
        if (userRole == UserRole.SUPPORT) {
            return isUserAssignedToTicket(user, ticket);
        }
        
        return false;
    }

    /**
     * 🔍 Verifica se l'utente può chiudere il ticket
     */
    public boolean canCloseTicket(User user, SupportTicket ticket) {
        UserRole userRole = getUserRole(user);
        
        switch (userRole) {
            case SUPER_ADMIN:
            case ADMIN:
                return !TicketStatus.CLOSED.getCode().equals(ticket.getStatus());
            case SUPPORT:
                return isUserAssignedToTicket(user, ticket) && 
                       !TicketStatus.CLOSED.getCode().equals(ticket.getStatus());
            default:
                return false;
        }
    }

    /**
     * 🔍 Verifica se l'utente può rispondere al ticket
     */
    public boolean canReplyToTicket(User user, SupportTicket ticket) {
        UserRole userRole = getUserRole(user);
        
        switch (userRole) {
            case SUPER_ADMIN:
            case ADMIN:
                return !TicketStatus.CLOSED.getCode().equals(ticket.getStatus());
            case SUPPORT:
                return isUserAssignedToTicket(user, ticket) && 
                       !TicketStatus.CLOSED.getCode().equals(ticket.getStatus());
            default:
                return false;
        }
    }

    /**
     * 🔍 Verifica se l'utente può vedere tutti i ticket
     */
    public boolean canViewAllTickets(User user) {
        UserRole userRole = getUserRole(user);
        return userRole == UserRole.SUPER_ADMIN || userRole == UserRole.ADMIN;
    }

    /**
     * 🔍 Verifica se l'utente può vedere solo i propri ticket
     */
    public boolean canViewOnlyOwnTickets(User user) {
        UserRole userRole = getUserRole(user);
        return userRole == UserRole.SUPPORT;
    }

    /**
     * 🔧 Helper: Ottieni ruolo utente
     */
    private UserRole getUserRole(User user) {
        String roleCode = user.getRole();
        
        for (UserRole role : UserRole.values()) {
            if (role.getCode().equals(roleCode)) {
                return role;
            }
        }
        
        return UserRole.USER; // Default
    }

    /**
     * 🔧 Helper: Ottieni stato ticket
     */
    private TicketStatus getTicketStatus(SupportTicket ticket) {
        String statusCode = ticket.getStatus();
        
        for (TicketStatus status : TicketStatus.values()) {
            if (status.getCode().equals(statusCode)) {
                return status;
            }
        }
        
        return TicketStatus.OPEN; // Default
    }

    /**
     * 🔧 Helper: Verifica se SUPPORT può vedere il ticket
     */
    private boolean canSupportViewTicket(User user, SupportTicket ticket) {
        // SUPPORT può vedere solo i propri ticket o ticket aperti
        return isUserAssignedToTicket(user, ticket) || 
               TicketStatus.OPEN.getCode().equals(ticket.getStatus());
    }

    /**
     * 🔧 Helper: Verifica se SUPPORT può modificare il ticket
     */
    private boolean canSupportModifyTicket(User user, SupportTicket ticket) {
        // SUPPORT può modificare solo i propri ticket
        return isUserAssignedToTicket(user, ticket) && 
               !TicketStatus.CLOSED.getCode().equals(ticket.getStatus());
    }

    /**
     * 🔧 Helper: Verifica se l'utente è assegnato al ticket
     */
    private boolean isUserAssignedToTicket(User user, SupportTicket ticket) {
        return ticket.getAssignedToUser() != null && 
               ticket.getAssignedToUser().getId().equals(user.getId());
    }

    /**
     * 📊 Ottieni statistiche permessi per un utente
     */
    public Map<String, Object> getUserPermissions(User user) {
        UserRole userRole = getUserRole(user);
        
        return Map.of(
            "role", userRole.getCode(),
            "roleDescription", userRole.getDescription(),
            "level", userRole.getLevel(),
            "permissions", Map.of(
                "canViewAllTickets", canViewAllTickets(user),
                "canViewOnlyOwnTickets", canViewOnlyOwnTickets(user),
                "canManageTickets", userRole.canManageTickets(),
                "canAssignTickets", userRole.canAssignTickets(),
                "canUnlockAnyTicket", userRole.canUnlockAnyTicket()
            )
        );
    }
}
