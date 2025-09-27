package com.example.nexusChat.cadenasuministros.service;

import java.time.Duration;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.example.nexusChat.cadenasuministros.security.JwtService;

import io.jsonwebtoken.ExpiredJwtException;


@Service
public class SessionStoreService {

    private static final String KEY_PREFIX = "ValidJwts:last-jti:";
    
    private final RedisTemplate<String,String> redis;
    private final JwtService jwtService;

    public SessionStoreService(RedisTemplate<String, String> redis, JwtService jwtService){
        this.redis = redis;
        this.jwtService = jwtService;
    }

    public void registrar(String subject, String jti, Duration ttl){
        
        ValueOperations<String,String> ops = redis.opsForValue();
        ops.set(KEY_PREFIX + subject, jti, ttl);
    }

    public String obtener(String subject){
        
        return redis.opsForValue().get(KEY_PREFIX+subject);
    }
    
    
    public void invalidar (String token){
       
        try {
            String subject = jwtService.extractUsername(token);
            redis.delete(KEY_PREFIX+subject);
        }catch (ExpiredJwtException ex) {
   
            throw new RuntimeException("Token inválido: ya expiró y no se agregó a blacklist", ex);
        }  
        
    }
}
