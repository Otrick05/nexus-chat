package com.example.nexuschat.nexuschat.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.nexuschat.nexuschat.DTO.SignupRequest;
import com.example.nexuschat.nexuschat.DTO.request.ChangePasswordRequest;
import com.example.nexuschat.nexuschat.exception.CorreoYaExisteException;
import com.example.nexuschat.nexuschat.model.Usuario;
import com.example.nexuschat.nexuschat.repository.RolRepository;
import com.example.nexuschat.nexuschat.repository.UsuarioRepository;
import com.example.nexuschat.nexuschat.service.UsuarioService;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;
    private final UsuarioService usuarioService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
            UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, RolRepository rolRepository,
            UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolRepository = rolRepository;
        this.usuarioService = usuarioService;
    }

    public String authenticateAndGenerateToken(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        String subject = userDetails.getUsername();
        String jti = jwtService.extractJti(token);

        usuarioService.registrarSesion(subject, jti);

        return token;
    }

    @Transactional
    public String registerAndAuthenticateUser(SignupRequest signupRequest) {

        if (usuarioRepository.findByCorreo(signupRequest.getCorreo()).isPresent()) {
            throw new CorreoYaExisteException("El Correo ya está en uso");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreUsuario(signupRequest.getNombreUsuario());
        nuevoUsuario.setNombreAppUsuario(signupRequest.getNombreUsuario());
        nuevoUsuario.setCorreo(signupRequest.getCorreo());
        nuevoUsuario.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        rolRepository.findByNombre("USUARIO")
                .ifPresent(nuevoUsuario::setRol);

        usuarioRepository.save(nuevoUsuario);

        return authenticateAndGenerateToken(
                nuevoUsuario.getCorreo(),
                signupRequest.getPassword());
    }

    public String changePasswordAndAuthenticate(Usuario usuario,
            ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);

        return authenticateAndGenerateToken(usuario.getCorreo(), request.getNewPassword());
    }

    public void logout(String token) {
        String email = jwtService.extractEmail(token);
        usuarioService.invalidarSesion(email);
    }

}
