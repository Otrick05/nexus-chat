package com.example.nexuschat.nexuschat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nexuschat.nexuschat.model.Chat;
import com.example.nexuschat.nexuschat.model.ParticipanteChat;
import com.example.nexuschat.nexuschat.model.Usuario;

@Repository
public interface ParticipanteChatRepository extends JpaRepository<ParticipanteChat, Long>{

    List<ParticipanteChat> findByUsuarioAndSalidaIsNull(Usuario usuario);
    

    Optional<ParticipanteChat> findByChatAndUsuarioAndSalidaIsNull(Chat chat, Usuario usuario);

    Optional<ParticipanteChat> findFirstByChatAndTipoUsuarioAndSalidaIsNullOrderByIngresoAsc(
            Chat chat, ParticipanteChat.TipoUsuario tipoUsuario);

    Optional<ParticipanteChat> findByChatAndUsuarioNotAndSalidaIsNull(Chat chat, Usuario usuario);
}
