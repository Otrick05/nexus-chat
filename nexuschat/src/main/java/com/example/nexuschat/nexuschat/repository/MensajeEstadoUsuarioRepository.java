package com.example.nexuschat.nexuschat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexuschat.nexuschat.model.Mensaje;
import com.example.nexuschat.nexuschat.model.MensajeEstadoUsuario;
import com.example.nexuschat.nexuschat.model.ParticipanteChat;

public interface MensajeEstadoUsuarioRepository extends JpaRepository<MensajeEstadoUsuario, Long> {

    boolean existsByMensajeAndParticipanteAndEstado(
            Mensaje mensaje,
            ParticipanteChat participante,
            MensajeEstadoUsuario.EstadoVisibilidad estado
    );
}
