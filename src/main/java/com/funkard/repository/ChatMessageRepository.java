package com.funkard.repository;

import com.funkard.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 💬 Repository per messaggi chat tra utenti
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    
    /**
     * Trova tutti i messaggi tra due utenti (ordinati per data)
     */
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.sender.id = :userId1 AND m.recipient.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.recipient.id = :userId1) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversationBetweenUsers(
        @Param("userId1") Long userId1,
        @Param("userId2") Long userId2
    );
    
    /**
     * Trova messaggi non letti per un utente
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.recipient.id = :userId AND m.readAt IS NULL ORDER BY m.createdAt ASC")
    List<ChatMessage> findUnreadMessages(@Param("userId") Long userId);
    
    /**
     * Conta messaggi non letti per un utente
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.recipient.id = :userId AND m.readAt IS NULL")
    long countUnreadMessages(@Param("userId") Long userId);
}

