package com.example.nexuschat.nexuschat.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.nexuschat.nexuschat.model.TokenBlacklist;
import com.example.nexuschat.nexuschat.repository.TokenBlacklistRepository;
import com.example.nexuschat.nexuschat.security.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

@Service
public class TokenBlacklistService {

    private TokenBlacklistRepository tokenBlacklistRepository;
    private JwtService jwtService; 

    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository, JwtService jwtService){

        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.jwtService = jwtService;

    }

    public void blacklistToken(String token) {
        
        try {
            String jti = jwtService.extractJti(token);
            Instant exp = jwtService.extractClaim(token, Claims::getExpiration).toInstant();
            tokenBlacklistRepository.save(new TokenBlacklist(jti, exp));
        }catch (ExpiredJwtException ex) {
   
            throw new RuntimeException("Token inválido: ya expiró y no se agregó a blacklist", ex);
        }  
    }

}
