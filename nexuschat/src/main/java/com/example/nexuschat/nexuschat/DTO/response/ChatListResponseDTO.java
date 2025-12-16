package com.example.nexuschat.nexuschat.DTO.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatListResponseDTO {

    private Long idChat;
    private String tipo;
    private String nombreChat;
    private String urlAvatar;
    private MensajeResumenDTO ultimoMensaje;
    private int conteoNoLeidos;
    private List<ParticipanteDTO> participantes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipanteDTO {
        private Long idUsuario;
        private String correo; // o email
        private String nombreUsuario; // nickname
        private String avatarUrl;
        private String rol; // PROPIETARIO, ADMIN, MIEMBRO
    }

    // DTO anidado para el resumen del mensaje (solo usado aquí)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MensajeResumenDTO {
        private String contenido;
        private String nombreRemitente;
        private String remitenteCorreo;
        private Instant hora;
        private Boolean borradoParaTodos;
    }

}
