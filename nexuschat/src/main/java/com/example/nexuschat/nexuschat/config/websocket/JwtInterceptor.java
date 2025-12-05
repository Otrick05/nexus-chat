package com.example.nexuschat.nexuschat.config.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.example.nexuschat.nexuschat.security.JwtService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

     @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            final String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                final String jwt = authHeader.substring(7);
                log.debug("JWT extraído de la cabecera para conexión STOMP.");

                try {
                    
                    final String userEmail = jwtService.extractEmail(jwt);

                   
                    final UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                    
                    
                    Authentication authentication = jwtService.getAuthentication(jwt, userDetails);

                    if (authentication != null) {
                        
                        accessor.setUser(authentication);
                        log.info("Usuario '{}' autenticado exitosamente para la sesión WebSocket.", userEmail);
                    }
                    
                } catch (UsernameNotFoundException e) {

                    log.warn("Validación de JWT fallida. Rechazando conexión WebSocket. Razón: {}", e.getMessage());
                }
            } else {
                 log.warn("Conexión WebSocket anónima. Cabecera 'Authorization' no encontrada o mal formada.");
            }
        }
        return message;
    }


}
