package com.example.nexuschat.nexuschat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nexuschat.nexuschat.model.Mensaje;
import com.example.nexuschat.nexuschat.model.Multimedia;

@Repository
public interface MultimediaRepository extends JpaRepository<Multimedia, Long>{


    List<Multimedia> findByMensaje(Mensaje mensaje);
}
