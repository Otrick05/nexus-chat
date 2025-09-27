package com.example.cadenasuministros.cadenasuministros.exception;

public class CorreoYaExisteException extends RuntimeException{

    public CorreoYaExisteException(String message){
        super(message);
    }
}
