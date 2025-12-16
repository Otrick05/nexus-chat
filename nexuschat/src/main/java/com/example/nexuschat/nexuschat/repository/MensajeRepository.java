package com.example.nexuschat.nexuschat.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nexuschat.nexuschat.model.Chat;
import com.example.nexuschat.nexuschat.model.Mensaje;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    Optional<Mensaje> findTopByChatOrderByHoraDesc(Chat chat);

    List<Mensaje> findByChatOrderByHoraAsc(Chat chat);

    List<Mensaje> findByChatAndHoraAfterOrderByHoraAsc(Chat chat, Instant hora);

    Page<Mensaje> findByChatAndHoraAfter(Chat chat, Instant hora,
            Pageable pageable);

    Page<Mensaje> findByChatAndHoraBetween(Chat chat, Instant inicio, Instant fin, Pageable pageable);
}
