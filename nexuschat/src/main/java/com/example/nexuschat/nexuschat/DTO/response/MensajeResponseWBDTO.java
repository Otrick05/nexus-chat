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
public class MensajeResponseWBDTO {
    private Long id;
    private Long chatId;
    private String contenido; // Texto o "Archivo multimedia"
    private String tipoMensaje; // TEXTO, AUDIO, VIDEO, FOTO
    private Instant hora;
    private String remitenteEmail;
    private String remitenteNombre;
}
