package com.example.messaging_stomp_websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // two subscription channels are opened
        config.setApplicationDestinationPrefixes("/app"); // messages sent to the server by client should be prefixed with /app, for example /app/validate/ and /app/addUser/
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chess-websocket").setAllowedOrigins("*"); // to start STOMP over WebSocket connection using "ws://localhost:8080/chess-websocket"
    }

}
