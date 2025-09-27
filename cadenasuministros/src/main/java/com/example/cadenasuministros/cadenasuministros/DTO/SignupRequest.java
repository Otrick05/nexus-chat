package com.example.cadenasuministros.cadenasuministros.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank
    private String nombreUsuario;
    @Email
    @NotBlank
    private String correo;
    @NotBlank
    private String password;

}
