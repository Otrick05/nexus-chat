package com.example.nexuschat.nexuschat.DTO;

import lombok.Getter;

@Getter
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";

    public AuthResponse (String accessToken){
        this.accessToken = accessToken;
    }
}
