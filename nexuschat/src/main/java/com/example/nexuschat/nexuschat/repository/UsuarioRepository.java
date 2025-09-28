package com.example.nexuschat.nexuschat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.nexuschat.nexuschat.model.Usuario;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario,Long>{
    
    Optional<Usuario> findByCorreo(String correo);
}
