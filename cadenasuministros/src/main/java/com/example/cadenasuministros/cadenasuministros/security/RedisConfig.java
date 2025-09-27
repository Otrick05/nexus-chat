package com.example.cadenasuministros.cadenasuministros.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory rcf){

        RedisTemplate<String,String> template = new RedisTemplate<>();
        template.setConnectionFactory(rcf);
        
        StringRedisSerializer s = new StringRedisSerializer();
        template.setKeySerializer(s);
        template.setValueSerializer(s);
        template.setHashKeySerializer(s);
        template.setHashValueSerializer(s);
        return template;
    }
}
