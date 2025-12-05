package com.example.nexuschat.nexuschat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nexuschat.nexuschat.model.Chat;

@Repository
public interface ChatRestRepository extends JpaRepository<Chat, Long>{


}
