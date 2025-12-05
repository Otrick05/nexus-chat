package com.example.nexuschat.nexuschat.DTO.request;

import java.util.List;

import com.example.nexuschat.nexuschat.DTO.MultimediaDTO;
import com.example.nexuschat.nexuschat.model.Mensaje.TipoMensaje;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessageDTO {

    private String contenido;
    @NotNull(message = "El ID del chat no puede ser nulo.")
    private Long chatId;
    @NotNull(message = "El tipo de mensaje debe estar definido")
    private TipoMensaje tipoMensaje;

    private List<MultimediaDTO> multimedia;

}
