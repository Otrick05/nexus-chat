package com.example.nexuschat.nexuschat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexuschat.nexuschat.model.Rol;

public interface RolRepository extends JpaRepository<Rol,Long>{

    Optional<Rol> findByNombre(String nombre);

    
}
