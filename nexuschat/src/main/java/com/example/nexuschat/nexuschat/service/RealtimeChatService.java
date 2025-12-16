package com.example.nexuschat.nexuschat.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.example.nexuschat.nexuschat.DTO.request.ChatMessageDTO;
import com.example.nexuschat.nexuschat.DTO.response.ChatEventDTO;
import com.example.nexuschat.nexuschat.DTO.response.ChatListResponseDTO;
import com.example.nexuschat.nexuschat.DTO.response.MensajeResponseDTO;
import com.example.nexuschat.nexuschat.DTO.response.MensajeResponseWBDTO;
import com.example.nexuschat.nexuschat.model.Chat;
import com.example.nexuschat.nexuschat.model.Mensaje;
import com.example.nexuschat.nexuschat.model.Multimedia;
import com.example.nexuschat.nexuschat.model.ParticipanteChat;
import com.example.nexuschat.nexuschat.model.Usuario;
import com.example.nexuschat.nexuschat.repository.ChatRepository;
import com.example.nexuschat.nexuschat.repository.MensajeEstadoUsuarioRepository;
import com.example.nexuschat.nexuschat.repository.MensajeRepository;
import com.example.nexuschat.nexuschat.repository.MultimediaRepository;
import com.example.nexuschat.nexuschat.repository.ParticipanteChatRepository;
import com.example.nexuschat.nexuschat.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeChatService {

    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Difunde un mensaje ya persistido a los suscriptores del chat vía WebSocket.
     * 
     * @param mensajeResponseDTO El DTO del mensaje ya guardado y listo para
     *                           mostrar.
     */
    public void difundirMensaje(MensajeResponseDTO mensajeResponseDTO) {
        String destination = "/topic/chat/" + mensajeResponseDTO.getChatId();
        log.info("Difundiendo mensaje a topic {}: {}", destination, mensajeResponseDTO);
        messagingTemplate.convertAndSend(destination, mensajeResponseDTO);
    }

    /**
     * Notifica a un usuario específico sobre un nuevo chat.
     * Envía un evento NEW_CHAT a la cola privada del usuario.
     * 
     * @param correoDestino Email del usuario destinatario (username).
     * @param chatDTO       El DTO del chat recién creado.
     */
    /**
     * Notifica un nuevo mensaje:
     * 1. A la suscripción del chat (/topic/chat/{id}) con el mensaje completo.
     * 2. A la cola privada de cada participante (/queue/mensajes) con el resumen
     * ligero (MensajeResponseWBDTO).
     * 
     * @param fullMessage   El DTO completo del mensaje.
     * @param lightMessage  El DTO ligero para notificación.
     * @param destinatarios Lista de emails de los destinatarios (incluyendo
     *                      remitente si se desea actualizar su lista).
     */
    public void notificarMensaje(MensajeResponseDTO fullMessage, MensajeResponseWBDTO lightMessage,
            List<String> destinatarios) {

        // 1. Difundir al topic del chat (chat abierto)
        difundirMensaje(fullMessage);

        // 2. Notificar a la cola privada de cada usuario (lista de chats)
        ChatEventDTO evento = new ChatEventDTO(
                ChatEventDTO.EventType.NEW_MESSAGE_NOTIFICATION,
                lightMessage);

        log.info("Notificando evento NEW_MESSAGE_NOTIFICATION a usuarios: {}. Payload: {}", destinatarios,
                lightMessage);

        for (String email : destinatarios) {
            messagingTemplate.convertAndSendToUser(email, "/queue/notificaciones", evento);
        }
    }

    /**
     * Notifica a un usuario específico sobre un nuevo chat.
     * Envía un evento NEW_CHAT a la cola privada del usuario.
     * Opcionalmente adjunta el mensaje inicial si existe (para chats 1 a 1).
     * 
     * @param correoDestino  Email del usuario destinatario (username).
     * @param chatDTO        El DTO del chat recién creado.
     * @param mensajeInicial El mensaje inicial si existe (puede ser null).
     */
    public void notificarNuevoChat(String correoDestino,
            ChatListResponseDTO chatDTO, MensajeResponseDTO mensajeInicial) {

        // Si hay mensaje inicial, lo inyectamos en el DTO del chat para que el front lo
        // ve
        if (mensajeInicial != null) {
            ChatListResponseDTO.MensajeResumenDTO resumen = new ChatListResponseDTO.MensajeResumenDTO();
            resumen.setContenido(mensajeInicial.getContenido());

            // Lógica para tipo de contenido (multimedia vs texto)
            if (!"TEXTO".equals(mensajeInicial.getTipoMensaje())) {
                resumen.setContenido("Archivo multimedia");
            }

            if (mensajeInicial.getRemitente() != null) {
                resumen.setNombreRemitente(mensajeInicial.getRemitente().getNombreUsuario());
                resumen.setRemitenteCorreo(mensajeInicial.getRemitente().getCorreo());
            }

            if (mensajeInicial.getHora() != null) {
                resumen.setHora(mensajeInicial.getHora().atZone(java.time.ZoneId.systemDefault()).toInstant());
            }

            resumen.setBorradoParaTodos(mensajeInicial.getBorradoParaTodos());

            chatDTO.setUltimoMensaje(resumen);
        }

        ChatEventDTO evento = new ChatEventDTO(
                ChatEventDTO.EventType.NEW_CHAT,
                chatDTO);

        // Destino final: /user/{correoDestino}/queue/notificaciones
        messagingTemplate.convertAndSendToUser(correoDestino, "/queue/notificaciones", evento);
    }

    /**
     * Difunde un evento de actualización de chat (CHAT_UPDATED) a todos los
     * suscriptores.
     * 
     * @param chatId          ID del chat actualizado.
     * @param chatResponseDTO El ChatListResponseDTO actualizado.
     */
    public void difundirActualizacionChat(Long chatId, ChatListResponseDTO chatResponseDTO) {
        String destination = "/topic/chat/" + chatId;
        ChatEventDTO evento = new ChatEventDTO(
                ChatEventDTO.EventType.CHAT_UPDATED,
                chatResponseDTO);

        messagingTemplate.convertAndSend(destination, evento);
    }

    // Método opcional para otros eventos (actualización de chat, etc.)
    /*
     * public void difundirActualizacionChat(Long chatId, ChatResumenResponseDTO
     * chatDTO) {
     * ChatEventDTO evento = new ChatEventDTO(ChatEventDTO.EventType.CHAT_UPDATED,
     * chatDTO);
     * messagingTemplate.convertAndSend("/topic/chat/" + chatId, evento);
     * }
     */
    // }

}
