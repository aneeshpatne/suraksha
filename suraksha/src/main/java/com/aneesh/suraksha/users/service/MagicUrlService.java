package com.aneesh.suraksha.users.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.dto.MailDTO;
import com.aneesh.suraksha.users.dto.MagicURLDTO;
import com.aneesh.suraksha.users.model.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MagicUrlService {
    public static final SecureRandom secureRandom = new SecureRandom();
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public MagicUrlService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper,
            RabbitTemplate rabbitTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    private String generateRandomMagicBytes() {
        String prefix = "magic_";
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().encodeToString(randomBytes);
        return prefix + randomPart;
    }

    public void SendMagicUrl(UserEntity user) {
        try {
            String magicUrl = generateRandomMagicBytes();
            String key = "magic:" + magicUrl;
            MagicURLDTO payload = new MagicURLDTO(user.getId(), System.currentTimeMillis());
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.opsForValue().set(key, json, Duration.ofMinutes(10));
            MailDTO mailDTO = new MailDTO("aneeshpatne@gmail.com",
                    "Suraksha Magic URL " + emailBody);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate magic URL", e);
        }

    }
}
