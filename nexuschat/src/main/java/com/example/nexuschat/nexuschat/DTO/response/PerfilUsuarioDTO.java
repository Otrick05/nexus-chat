package com.example.nexuschat.nexuschat.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilUsuarioDTO {
    String nombreUsuario;
    String correo;
    String nombreAppUsuario;
    String avatarUrl;
}
