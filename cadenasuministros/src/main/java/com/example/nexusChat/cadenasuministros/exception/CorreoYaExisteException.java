package com.example.nexusChat.cadenasuministros.exception;

public class CorreoYaExisteException extends RuntimeException{

    public CorreoYaExisteException(String message){
        super(message);
    }
}
