package com.example.nexuschat.nexuschat.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.nexuschat.nexuschat.DTO.LoginRequest;
import com.example.nexuschat.nexuschat.DTO.SignupRequest;
import com.example.nexuschat.nexuschat.security.AuthService;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {

        System.out.println(loginRequest.getCorreo());
        System.out.println(loginRequest.getPassword());
        String token = authService
                .authenticateAndGenerateToken(
                        loginRequest.getCorreo(),
                        loginRequest.getPassword());

        System.out.println(token);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> registrarUsuario(@RequestBody SignupRequest SignupRequest) {
        String jwt = authService.registerAndAuthenticateUser(SignupRequest);
        System.out.println(jwt);
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);

            return ResponseEntity.ok("Logeo exitoso, token invalidado");

        }
        return ResponseEntity.badRequest().body("Token invalido");
    }

}
