package com.example.nexuschat.nexuschat.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nexuschat.nexuschat.DTO.response.PerfilUsuarioDTO;
import com.example.nexuschat.nexuschat.service.ContactoService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/api/contactos")
@PreAuthorize("hasRole('ROLE_USUARIO')")
public class ContactoController {

    private final ContactoService contactoService;

    public ContactoController(ContactoService contactoService) {
        this.contactoService = contactoService;
    }

    @GetMapping
    public ResponseEntity<List<PerfilUsuarioDTO>> getAllContactos(Principal principal) {
        System.out.println("Principal: " + principal.getName());
        List<PerfilUsuarioDTO> contactos = contactoService.obtenerContactosDeUsuario(principal.getName());
        return new ResponseEntity<>(contactos, HttpStatus.OK);
    }

    @PostMapping("/agregar/{correo}")
    public ResponseEntity<PerfilUsuarioDTO> agregarContacto(@PathVariable @NotEmpty @Email String correo,
            Principal principal) {
        PerfilUsuarioDTO contactoCreado = contactoService.agregarContacto(correo, principal.getName());
        return new ResponseEntity<>(contactoCreado, HttpStatus.ACCEPTED);
    }

}
