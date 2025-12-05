package com.example.nexuschat.nexuschat.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.example.nexuschat.nexuschat.DTO.request.ChatMessageDTO;

import com.example.nexuschat.nexuschat.DTO.response.MensajeResponseDTO;
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

@Service
@RequiredArgsConstructor
public class RealtimeChatService {
    /*
     * private final MensajeRepository mensajeRepository;
     * private final MultimediaRepository multimediaRepository;
     * private final UsuarioRepository usuarioRepository;
     * private final ChatRepository chatRepository;
     * private final ParticipanteChatRepository participanteChatRepository;
     * private final MensajeEstadoUsuarioRepository mensajeEstadoUsuarioRepository;
     * 
     * private final SimpMessageSendingOperations messagingTemplate;
     * 
     * @Transactional
     * public void procesarYDifundirMensaje(ChatMessageDTO dto, String
     * emailRemitente) {
     * Usuario remitente = buscarUsuarioPorEmail(emailRemitente);
     * Chat chat = buscarChatPorId(dto.getChatId());
     * 
     * // 1. Validación de permisos
     * ParticipanteChat participante = buscarParticipacionActiva(chat, remitente);
     * 
     * // 2. Mapeo Manual (DTO -> Entidad Mensaje)
     * Mensaje nuevoMensaje = new Mensaje();
     * nuevoMensaje.setChat(chat);
     * nuevoMensaje.setRemitente(remitente);
     * nuevoMensaje.setHora(Instant.now());
     * nuevoMensaje.setBorradoTodos(false);
     * 
     * // Lógica de contenido y tipo
     * boolean esMultimedia = dto.getMultimedia() != null &&
     * !dto.getMultimedia().isEmpty();
     * if (esMultimedia) {
     * nuevoMensaje.setTipoMensaje(Mensaje.TipoMensaje.MULTIMEDIA);
     * // El 'contenido' puede ser el pie de foto/video
     * nuevoMensaje.setContenido(dto.getContenido());
     * } else {
     * nuevoMensaje.setTipo(Mensaje.TipoMensaje.TEXTO);
     * nuevoMensaje.setContenido(dto.getContenido());
     * }
     * 
     * Mensaje mensajeGuardado = mensajeRepository.save(nuevoMensaje);
     * 
     * // 3. Mapeo Manual (DTO -> Entidad Multimedia)
     * List<Multimedia> multimediaGuardada = new ArrayList<>();
     * if (esMultimedia) {
     * for (ChatMessageDTO.MultimediaDTO mediaDTO : dto.getMultimedia()) {
     * Multimedia media = new Multimedia();
     * media.setMensaje(mensajeGuardado);
     * media.setUrlStorage(mediaDTO.getUrlStorage());
     * media.setTipo(mediaDTO.getTipo());
     * media.setTamañoBytes(mediaDTO.getTamañoBytes());
     * media.setDuracion(mediaDTO.getDuracion());
     * media.setOrden(mediaDTO.getOrden());
     * multimediaGuardada.add(multimediaRepository.save(media));
     * }
     * }
     * 
     * // 4. Mapeo Manual (Entidad -> DTO de Respuesta)
     * MensajeResponseDTO responseDTO = mapToMensajeResponseDTO(mensajeGuardado,
     * multimediaGuardada);
     * 
     * // 5. Difundir a todos los miembros del chat
     * String destination = "/topic/chat/" + chat.getId();
     * messagingTemplate.convertAndSend(destination, responseDTO);
     * }
     * 
     * public void difundirActualizacionChat(Long chatId, ChatResumenResponseDTO
     * chatDTO) {
     * ChatEventDTO evento = new ChatEventDTO(ChatEventDTO.EventType.CHAT_UPDATED,
     * chatDTO);
     * messagingTemplate.convertAndSend("/topic/chat/" + chatId, evento);
     * }
     */

}
