package com.aneesh.suraksha.users.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import com.aneesh.suraksha.users.dto.TokenSubject;
import com.aneesh.suraksha.users.model.UserEntity;

@Service
public class ForgotPasswordService {

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public ForgotPasswordService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    private String GenerateToken() {
        byte[] random = new byte[64];
        SECURE_RANDOM.nextBytes(random);
        return Base64.getUrlEncoder().encodeToString(random);
    }

    public String ResetPassword(UserEntity user) {
        String token = GenerateToken();
        if (user == null) {
            return token;
        }
        TokenSubject tokenSubject = TokenSubject.fromUser(user);
        String payload = objectMapper.writeValueAsString(tokenSubject);
        stringRedisTemplate.opsForValue().set("reset:" + token, payload);
        return token;

    }
}
