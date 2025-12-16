package com.example.nexuschat.nexuschat.DTO.request;

import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import com.example.nexuschat.nexuschat.model.Chat;
import com.example.nexuschat.nexuschat.model.Mensaje.TipoMensaje;
import com.example.nexuschat.nexuschat.DTO.request.EnviarMensajeRequestDTO.ArchivoSolicitudDTO;

@Data
public class CreateChatRequestDTO {

    @NotEmpty(message = "Debe haber al menos un miembro.")
    private List<@Email String> emailsMiembros;

    @Size(max = 100, message = "El nombre del chat no puede exceder los 100 caracteres.")
    private String nombreChat;

    @NotNull(message = "El tipo de chat (INDIVIDUAL o GRUPAL) no puede ser nulo.")
    private Chat.TipoChat tipo;

    // Campos opcionales para inicializar el chat con un mensaje
    private String mensajeInicial;

    private ArchivoSolicitudDTO[] archivos;

    private TipoMensaje tipoMensaje;
}
