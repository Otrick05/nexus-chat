package com.example.nexuschat.nexuschat.DTO.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarChatRequestDTO {

    @Size(max = 100, message = "El nombre del chat no puede exceder los 100 caracteres.")
    private String nombreChat;

    private String urlImagen;
}

