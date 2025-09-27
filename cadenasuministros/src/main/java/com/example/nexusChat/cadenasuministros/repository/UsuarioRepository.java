package com.example.nexusChat.cadenasuministros.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexusChat.cadenasuministros.model.Usuario;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario,Long>{
    
    Optional<Usuario> findByCorreo(String correo);
}
