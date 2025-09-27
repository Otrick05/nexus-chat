package com.example.nexusChat.cadenasuministros.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexusChat.cadenasuministros.model.Rol;

public interface RolRepository extends JpaRepository<Rol,Long>{

    Optional<Rol> findByNombre(String nombre);

    
}
