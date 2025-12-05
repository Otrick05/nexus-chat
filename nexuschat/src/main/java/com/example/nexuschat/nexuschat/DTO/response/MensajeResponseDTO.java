package com.example.nexuschat.nexuschat.DTO.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponseDTO {

    private Long id;
    private Long chatId;
    private String contenido;
    private LocalDateTime hora;
    private Boolean borradoParaTodos;
    private String tipoMensaje; // TEXTO o MULTIMEDIA

    private RemitenteDTO remitente;
    private List<MultimediaResponseDTO> multimedia;

    @Data
    @NoArgsConstructor
    public static class RemitenteDTO {
        private Long id;
        private String correo4;
        private String nombreUsuario;
        private String avatarUrl;
    }

    @Data
    @NoArgsConstructor
    public static class MultimediaResponseDTO {
        private Long id;
        private String urlStorage;
        private String uploadUrl; // URL firmada para subir el archivo (solo en respuesta a creación)
        private String tipo;
        private Long tamañoBytes;
        private String duracion;
        private int orden;
    }
}
