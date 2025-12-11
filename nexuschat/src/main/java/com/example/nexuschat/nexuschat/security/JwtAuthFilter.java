package com.example.nexuschat.nexuschat.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.nexuschat.nexuschat.service.UsuarioService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UsuarioService usuarioService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService, UsuarioService usuarioService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        System.out.println(authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            System.out.println(jwt);
            String jti = jwtService.extractJti(jwt);
            final String username = jwtService.extractEmail(jwt);

            if (!usuarioService.validarSesion(username, jti)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Token inválido: sesión expirada o hay un login más reciente");
                return;
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {

                    String rol = jwtService.extractRol(jwt);

                    List<String> permisos = jwtService.extractPermissions(jwt);

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                    if (rol != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));
                    }

                    if (permisos != null) {
                        authorities.addAll(
                                permisos.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .toList());
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (ExpiredJwtException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expirado");
            return;

        } catch (JwtException | IllegalArgumentException ex) {
            // catch other parsing / validation problems
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token inválido: " + ex.getMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }

}