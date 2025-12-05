package com.example.nexuschat.nexuschat.DTO;

import com.example.nexuschat.nexuschat.model.Multimedia;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MultimediaDTO {

    @NotEmpty(message = "La URL del Bucket no puede estar vacía")
    private String urlStorge;

    @NotEmpty
    private Multimedia.TipoMultimedia tipo;

    private Long tamañoBytes;
    
    private String duracion;

    private int orden;
}
