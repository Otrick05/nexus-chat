package com.example.nexuschat.nexuschat.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;



@Data
public class ParticipanteChat {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat", nullable = false)
    private Chat chat;

    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipo;
    public enum TipoUsuario {
        MIEMBRO,      
        ADMIN_GRUPO,
        PROPIETARIO
    }

    @Column(name = "ingreso", nullable = false)
    private Instant ingreso;

    @Column(name = "salida", nullable=true)
    private Instant salida;

}
