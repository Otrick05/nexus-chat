package com.example.nexuschat.nexuschat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nexuschat.nexuschat.model.Permiso;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso,Long>{

    Optional<Permiso> findByNombre(String nombre);


}
