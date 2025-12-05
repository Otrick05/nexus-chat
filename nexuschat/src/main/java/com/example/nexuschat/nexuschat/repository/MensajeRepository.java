package com.example.nexuschat.nexuschat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nexuschat.nexuschat.model.Chat;
import com.example.nexuschat.nexuschat.model.Mensaje;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    Optional<Mensaje> findTopByChatOrderByHoraDesc(Chat chat);

    List<Mensaje> findByChatOrderByHoraAsc(Chat chat);
}
