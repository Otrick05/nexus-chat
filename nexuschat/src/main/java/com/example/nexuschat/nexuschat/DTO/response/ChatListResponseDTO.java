package com.example.nexuschat.nexuschat.DTO.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // DTO anidado para el resumen del mensaje (solo usado aquí)
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MensajeResumenDTO {
        private String contenido;
        private String remitenteCorreo;
        private Instant hora;
        private Boolean borradoParaTodos;
    }

}
