package com.example.nexuschat.nexuschat.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatEventDTO {

    private EventType type;
    private Object payload; // Map o DTO específico según el evento

    public enum EventType {
        
        MESSAGE_READ,     // Payload: { messageId, emailLector }
        USER_JOINED,      // Payload: { emailUsuario }
        USER_LEFT,        // Payload: { emailUsuario }
        OWNER_CHANGED,    // Payload: { emailNuevoPropietario, emailAntiguoPropietario }
        CHAT_UPDATED,     // Payload: ChatListResponseDTO
        TYPING            // Payload: { emailRemitente, isTyping }
    }
}
