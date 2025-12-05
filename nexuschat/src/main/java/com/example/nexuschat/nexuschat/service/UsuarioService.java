/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.nexuschat.nexuschat.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.nexuschat.nexuschat.model.Usuario;
import com.example.nexuschat.nexuschat.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> getUsuarioByCorreo(String correo) {

        return usuarioRepository.findByCorreo(correo);
    }
}
