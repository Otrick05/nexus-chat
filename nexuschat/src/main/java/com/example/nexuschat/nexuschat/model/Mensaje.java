package com.example.nexuschat.nexuschat.model;

import java.time.Instant;

import com.example.nexuschat.nexuschat.model.Chat.TipoChat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "mensaje")
@Data
public class Mensaje {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_remitente", nullable = false)
    private Usuario remitente;

    @Column(name = "hora")
    private Instant hora = Instant.now();

    @Column(name = "contenido", nullable = true)
    private String contenido;

    @Column(name = "borrado_todos")
    private Boolean borradoTodos = false;

    @Column(name = "tipo", nullable = false)
    private TipoMensaje tipo;

    public enum TipoMensaje {
        TEXTO,
        AUDIO,
        VIDEO,
        FOTO
    }

}
