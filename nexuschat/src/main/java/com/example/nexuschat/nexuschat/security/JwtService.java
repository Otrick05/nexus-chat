package com.example.nexuschat.nexuschat.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.nexuschat.nexuschat.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private SecretKey getSigningKey(){

        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);

        byte[] decodeKey = Decoders.BASE64.decode(base64Key);
        return Keys.hmacShaKeyFor(decodeKey);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(),userDetails);
    }

    public String generateToken(Map<String,Object>extraClaims, UserDetails userDetails){

        if (userDetails instanceof Usuario usuario) {
        extraClaims.put("nombreUsuario", usuario.getNombreUsuario());
        extraClaims.put("rol", usuario.getRol().getNombre());
        }

        extraClaims.put("permissions", userDetails.getAuthorities()
                                     .stream()
                                     .map(Object::toString)
                                     .toList());
        


        return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setId(java.util.UUID.randomUUID().toString())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // en esta linea se setea el tiempo de expiración del jwt
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractEmail(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token){
        return extractClaim(token, Claims::getId);
    }

    public <T> T extractClaim(String token, Function <Claims, T> claimsResolver){

        final Claims claims = extractAllClaims(token);
        if (claims == null) {
            throw new IllegalArgumentException("Invalid JWT token: claims are null");
        }
        return claimsResolver.apply(claims);
        
    }

    private Claims extractAllClaims(String token) {
        
        try {
            return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (JwtException | IllegalArgumentException ex) {
            
            throw new IllegalArgumentException("Invalid JWT token: " + ex.getMessage(), ex);
        }
    }

    public Boolean isTokenValid(String token, UserDetails userDetails){
        
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        
        return extractClaim(token, Claims::getExpiration);
    }

    public Duration timeToExpiry(String token){
        Instant exp = extractExpiration(token).toInstant();
        Instant now = Instant.now();
        Duration d = Duration.between(now, exp);
        return d.isNegative() ? Duration.ZERO : d;
    }

    public String extractRol(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    public List<String> extractPermissions(String token) {
        return extractClaim(token, claims -> claims.get("permissions", List.class));
    }

    public Authentication getAuthentication(String token, UserDetails userDetails) {
        if (isTokenValid(token, userDetails)) {
            String username = extractEmail(token);
            List<String> permissions = extractPermissions(token);
            List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        }else{
            return null;
        }

    }
}
