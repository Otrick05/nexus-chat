package com.example.nexuschat.nexuschat.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

import com.example.nexuschat.nexuschat.DTO.response.PerfilUsuarioDTO;
import com.example.nexuschat.nexuschat.model.Usuario;
import com.example.nexuschat.nexuschat.service.UsuarioService;

@RestController()
@RequestMapping("/api/perfil-usuario")
@PreAuthorize("hasRole('ROLE_USUARIO')")
public class PerfilUsuarioController {

    private final UsuarioService usuarioService;

    public PerfilUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{correo}")
    public ResponseEntity<PerfilUsuarioDTO> getPerfilUsuario(@PathVariable String correo,
            Authentication authentication) {

        String emailAutenticado = authentication.getName();
        Optional<Usuario> usuarioExistente = usuarioService.getUsuarioByCorreo(correo);

        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            PerfilUsuarioDTO perfil;

            if (emailAutenticado.equals(correo)) {
                // Es el propio usuario, devolver todo
                perfil = new PerfilUsuarioDTO(
                        usuario.getNombreUsuario(),
                        usuario.getCorreo(),
                        usuario.getNombreAppUsuario(),
                        usuario.getAvatarUrl());
            } else {
                // Es otro usuario, devolver información limitada
                perfil = new PerfilUsuarioDTO(
                        null, // Ocultar nombreUsuario real
                        usuario.getCorreo(),
                        usuario.getNombreAppUsuario(),
                        usuario.getAvatarUrl());
            }

            return new ResponseEntity<>(perfil, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/actualizar-nombre")
    public ResponseEntity<?> actualizarNombreAppUsuario(@RequestBody Map<String, String> body,
            Authentication authentication) {
        String emailAutenticado = authentication.getName();
        String nuevoNombre = body.get("nombreAppUsuario");
        System.out.println("Solicitud de actualización de nombre recibida para: " + emailAutenticado);
        System.out.println("Nuevo nombre: " + nuevoNombre);

        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre no puede estar vacío");
        }

        try {
            usuarioService.actualizarNombreAppUsuario(emailAutenticado, nuevoNombre);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
