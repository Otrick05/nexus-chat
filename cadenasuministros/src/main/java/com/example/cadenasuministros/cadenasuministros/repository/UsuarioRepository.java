package com.example.cadenasuministros.cadenasuministros.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cadenasuministros.cadenasuministros.model.Usuario;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario,Long>{
    
    Optional<Usuario> findByCorreo(String correo);
}
