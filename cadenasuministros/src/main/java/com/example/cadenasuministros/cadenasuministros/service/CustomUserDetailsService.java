package com.example.cadenasuministros.cadenasuministros.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import com.example.cadenasuministros.cadenasuministros.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService{
    
    
    private final UsuarioRepository usuarioRepository;
 

    public CustomUserDetailsService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException{
        return usuarioRepository.findByCorreo(correo)
        .orElseThrow(() -> new UsernameNotFoundException("Correo o contraseña inválidos: "));
    }
}
