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
public class MensajeResumenDTO {

    private String contenido;
    private String remitenteEmail;
    private Instant hora;
    private Boolean borradoTodos;
}
