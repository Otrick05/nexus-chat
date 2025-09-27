package com.example.nexusChat.cadenasuministros.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.nexusChat.cadenasuministros.service.SessionStoreService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{

    private final JwtService jwtService;
    
    private final UserDetailsService userDetailsService;
    private final SessionStoreService sessionService;


    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService, SessionStoreService sessionService){
        this.jwtService = jwtService;
        
        this.userDetailsService = userDetailsService;
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    )throws ServletException, IOException{

        final String authHeader = request.getHeader("Authorization"); 
        System.out.println(authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        try{
            final String jwt = authHeader.substring(7);
            System.out.println(jwt);
            String jti = jwtService.extractJti(jwt);

            /*if (jti != null && tokenBlacklistRepository.existsByJti(jti)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Token en blacklist");
            
                return; 
            }*/

            final String username=jwtService.extractUsername(jwt);
            String ultimoJti = sessionService.obtener(username);
            if (ultimoJti == null || !ultimoJti.equals(jti)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Token inválido: hay un login más reciente");
                return;
            }
        

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {

                    if (ultimoJti != null && ultimoJti.equals(jti)) {

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
                            .toList()
                            );
                        }
           
                        UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }    
            }                       
                    
        }catch(ExpiredJwtException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expirado");
            return;
            
        }
        filterChain.doFilter(request, response);
        }

}