package com.funkard.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * ⚙️ Configurazione per real-time communication (SSE + WebSocket)
 * 
 * SSE (Server-Sent Events): unidirezionale, server → client
 * WebSocket: bidirezionale, server ↔ client (fallback)
 */
@Configuration
@EnableWebSocketMessageBroker
public class RealtimeConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 🔌 Registra endpoint WebSocket
     * Endpoint: /ws
     * Supporta SockJS per fallback automatico
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                    "https://funkard.com",
                    "https://www.funkard.com",
                    "https://admin.funkard.com",
                    "http://localhost:3000",
                    "http://localhost:3002"
                )
                .withSockJS(); // Fallback automatico per browser che non supportano WebSocket
    }

    /**
     * 📡 Configura message broker
     * - /topic: destinazioni per broadcast (server → client)
     * - /app: destinazioni per messaggi client → server
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple broker in-memory per topic (broadcast)
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefisso per destinazioni client → server
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefisso per destinazioni user-specific
        registry.setUserDestinationPrefix("/user");
    }
}

