package com.example.nexusChat.cadenasuministros.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LoginRequest {

    @NotBlank
    private String correo;
    @NotBlank
    private String password;
}
