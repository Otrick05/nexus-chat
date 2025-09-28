package com.example.nexuschat.nexuschat.model;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {


    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String jti;
    @Column(nullable = false)
    private Instant expiryDate;
    
    public TokenBlacklist(){}
    public TokenBlacklist(String jti, Instant expiration){
        this.jti = jti;
        this.expiryDate = expiration;
    }

}
