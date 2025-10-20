package com.funkard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                    "https://funkard-admin.vercel.app",
                    "https://funkard.vercel.app", 
                    "https://funkardnew.vercel.app",
                    "http://localhost:3000",
                    "http://localhost:3001"
                )
                .withSockJS(); // fallback per browser vecchi
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // dove i client si iscrivono
        registry.setApplicationDestinationPrefixes("/app"); // dove i client inviano messaggi
    }
}
