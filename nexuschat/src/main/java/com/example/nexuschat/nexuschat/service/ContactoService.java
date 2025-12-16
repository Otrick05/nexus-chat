package com.example.nexuschat.nexuschat.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.nexuschat.nexuschat.DTO.response.PerfilUsuarioDTO;
import com.example.nexuschat.nexuschat.model.Usuario;
import com.example.nexuschat.nexuschat.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class ContactoService {

        private final UsuarioRepository usuarioRepository;

        public ContactoService(UsuarioRepository usuarioRepository) {
                this.usuarioRepository = usuarioRepository;
        }

        @Transactional
        public List<PerfilUsuarioDTO> obtenerContactosDeUsuario(String correo) {
                Usuario usuario = usuarioRepository.findByCorreo(correo)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                Set<Usuario> contactos = usuario.getContactos();

                return contactos.stream()
                                .map(contacto -> PerfilUsuarioDTO.builder()
                                                .nombreUsuario(null) // Ocultamos el nombre de usuario real por
                                                // privacidad si es necesario, o lo
                                                // mostramos
                                                .correo(contacto.getCorreo())
                                                .nombreAppUsuario(contacto.getNombreAppUsuario())
                                                .avatarUrl(contacto.getAvatarUrl())
                                                .build())
                                .collect(Collectors.toList());
        }

        @Transactional
        public PerfilUsuarioDTO agregarContacto(String correoContacto, String correoUsuario) {
                Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                                .orElseThrow(() -> new RuntimeException("Usuario principal no encontrado"));

                Usuario contacto = usuarioRepository.findByCorreo(correoContacto)
                                .orElseThrow(() -> new RuntimeException("Usuario a agregar no encontrado"));

                if (usuario.getContactos().contains(contacto)) {
                        throw new RuntimeException("El usuario ya está en tu lista de contactos");
                }

                usuario.getContactos().add(contacto);

                usuarioRepository.save(usuario);

                return PerfilUsuarioDTO.builder()
                                .nombreUsuario(null)
                                .correo(contacto.getCorreo())
                                .nombreAppUsuario(contacto.getNombreAppUsuario())
                                .avatarUrl(contacto.getAvatarUrl())
                                .build();
        }
}
