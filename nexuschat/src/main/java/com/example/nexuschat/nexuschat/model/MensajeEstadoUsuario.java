package com.example.nexuschat.nexuschat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name="mensaje_estado_usuario")
@Entity
public class MensajeEstadoUsuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "id_mensaje")
    private Mensaje id_mensaje;

    @OneToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "id_participante")
    private Usuario id_participante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVisibilidad estado;
    public enum EstadoVisibilidad {
        LEIDO,      
        BORRADO     
    }

}
