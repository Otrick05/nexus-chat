package com.example.cadenasuministros.cadenasuministros.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.jsonwebtoken.ExpiredJwtException;

@ControllerAdvice
public class GlobalExceptionHandler {

    
    // 🟢 Caso: Correo duplicado en el registro
    @ExceptionHandler(CorreoYaExisteException.class)
    public ResponseEntity<Map<String, Object>> handleCorreoDuplicado(CorreoYaExisteException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 🟠 Caso: Credenciales inválidas (login fallido)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleCredencialesInvalidas(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // 🔴 Caso: Token expirado
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpirado(ExpiredJwtException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "El token ha expirado. Por favor vuelve a iniciar sesión.");
    }

    // 🔴 Caso genérico: cualquier otro RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    // 📌 Método utilitario para construir las respuestas JSON
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", mensaje);
        return new ResponseEntity<>(body, status);
    }
}
