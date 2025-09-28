package com.example.nexuschat.nexuschat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nexuschat.nexuschat.model.TokenBlacklist;

public interface TokenBlacklistRepository  extends JpaRepository<TokenBlacklist,Long>{

    Optional<TokenBlacklist> findByJti(String jti);
    boolean existsByJti(String jti);
    
    
} 
