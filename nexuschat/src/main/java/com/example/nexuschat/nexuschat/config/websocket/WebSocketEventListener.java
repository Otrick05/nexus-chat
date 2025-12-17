package com.example.nexuschat.nexuschat.config.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketEventListener {

    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        log.info("Intento de conexión WebSocket recibido.");
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String user = (headerAccessor.getUser() != null) ? headerAccessor.getUser().getName() : "Desconocido";
        String sessionId = headerAccessor.getSessionId();

        log.info("Nueva Suscripción: Usuario '{}' | Destino '{}' | SessionId '{}'", user, destination, sessionId);
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String user = (headerAccessor.getUser() != null) ? headerAccessor.getUser().getName() : "Desconocido";
        String sessionId = headerAccessor.getSessionId();

        log.info("Desuscripción: Usuario '{}' | SessionId '{}'", user, sessionId);
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String user = (headerAccessor.getUser() != null) ? headerAccessor.getUser().getName() : "Desconocido";
        String sessionId = headerAccessor.getSessionId();

        log.info("Desconexión WebSocket: Usuario '{}' | SessionId '{}'", user, sessionId);
    }
}
