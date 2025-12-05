package com.example.nexuschat.nexuschat.DTO.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeerMensajeDTO {

    @NotNull
    private Long mensajeId;
    
    @NotNull
    private Long chatId;
}
