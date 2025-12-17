package com.example.nexuschat.nexuschat.DTO.request;

import java.util.List;

import com.example.nexuschat.nexuschat.model.Mensaje;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnviarMensajeRequestDTO {

    @NotNull(message = "El ID del chat no puede ser nulo.")
    private Long chatId;

    private String contenido;

    private String correoRemitente;

    @NotNull(message = "El tipo de mensaje debe estar definido")
    private Mensaje.TipoMensaje tipoMensaje;

    private List<ArchivoSolicitudDTO> archivos;

    @Data
    @NoArgsConstructor
    public static class ArchivoSolicitudDTO {
        private String nombreArchivo;
        private String contentType;
        private Long tamanoBytes;
        private String duracion;
        private String fileName; // Optional: Used when file is already uploaded via FileController
    }
}
