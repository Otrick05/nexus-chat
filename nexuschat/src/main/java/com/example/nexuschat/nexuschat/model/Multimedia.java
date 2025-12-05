package com.example.nexuschat.nexuschat.model;


import java.time.Duration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name="multimedia")
@Entity
@Data
public class Multimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat", nullable = false)
    private Mensaje mensaje;

    @Column(name="url_storage", nullable=false)
    private String url_storage;

    @Column(name="tipo", nullable=false)
    private TipoMultimedia tipo;
    public enum TipoMultimedia{
        AUDIO,
        VIDEO,
        FOTO
    };
    @Column(name="bytes_size", nullable=false)
    private Long bytes_size;
    @Column(name="duracion", nullable=true)
    private Duration duracion;

}
