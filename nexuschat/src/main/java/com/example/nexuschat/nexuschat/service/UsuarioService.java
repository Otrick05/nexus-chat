/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.nexuschat.nexuschat.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.nexuschat.nexuschat.model.Usuario;
import com.example.nexuschat.nexuschat.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Value("${jwt.expiration-time}")
    private long jwtExpiration;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> getUsuarioByCorreo(String correo) {

        return usuarioRepository.findByCorreo(correo);
    }

    public void registrarSesion(String correo, String jti) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setJti(jti);
        usuario.setLastLogin(java.time.Instant.now());
        usuarioRepository.save(usuario);
    }

    public boolean validarSesion(String correo, String jtiActual) {
        return usuarioRepository.findByCorreo(correo)
                .map(usuario -> {
                    // Si no hay JTI guardado o no coincide, es inválido
                    if (usuario.getJti() == null || !usuario.getJti().equals(jtiActual)) {
                        return false;
                    }

                    // Validar TTL de 15 minutos (por ejemplo)
                    java.time.Instant now = java.time.Instant.now();
                    java.time.Instant expirationTime = usuario.getLastLogin().plusMillis(jwtExpiration);

                    if (now.isAfter(expirationTime)) {
                        return false; // Sesión expirada por tiempo
                        // (aunque el token JWT tenga su propia expiración, aqui validamos base de
                        // datos)
                    }

                    return true;
                })
                .orElse(false);
    }

    public void invalidarSesion(String correo) {
        usuarioRepository.findByCorreo(correo).ifPresent(usuario -> {
            usuario.setJti(null);
            usuarioRepository.save(usuario);
        });
    }

    public void actualizarNombreAppUsuario(String correo, String nuevoNombre) {
        System.out.println("Actualizando nombre en DB para correo: " + correo);
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setNombreAppUsuario(nuevoNombre);
        usuarioRepository.save(usuario);
        System.out.println("Nombre actualizado exitosamente.");
    }
}
