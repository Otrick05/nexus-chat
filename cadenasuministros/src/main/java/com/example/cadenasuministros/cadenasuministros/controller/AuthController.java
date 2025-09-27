package com.example.cadenasuministros.cadenasuministros.controller;



import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.cadenasuministros.cadenasuministros.DTO.LoginRequest;
import com.example.cadenasuministros.cadenasuministros.DTO.SignupRequest;
import com.example.cadenasuministros.cadenasuministros.security.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.example.cadenasuministros.cadenasuministros.service.SessionStoreService;
import com.example.cadenasuministros.cadenasuministros.service.TokenBlacklistService;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    
    private AuthService authService;
    private TokenBlacklistService tokenBlacklistService;
    private SessionStoreService sessionStoreService;

    public AuthController(AuthService authService, TokenBlacklistService tokenBlacklistService, SessionStoreService sessionStoreService){
        this.authService = authService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.sessionStoreService = sessionStoreService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {

        String token = authService
            .authenticateAndGenerateToken(
                loginRequest.getCorreo(),
                loginRequest.getPassword()
            );

        return ResponseEntity.ok(token);
    }
    
    @PostMapping("/signup")
    public ResponseEntity <String> registrarUsuario (@RequestBody SignupRequest SignupRequest) {
        String jwt = authService.registerAndAuthenticateUser(SignupRequest);
    
        return new ResponseEntity<String>(jwt, HttpStatus.CREATED);
    }
    


    @PostMapping("/logout")
    public ResponseEntity <String> logout(@RequestHeader("Authorization") String authHeader ) {
       
        if (authHeader!=null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            sessionStoreService.invalidar(token);

            return ResponseEntity.ok("Logeo exitoso, token invalidado");
            
        }
        return ResponseEntity.badRequest().body("Token invalido") ;
    }
    

}
