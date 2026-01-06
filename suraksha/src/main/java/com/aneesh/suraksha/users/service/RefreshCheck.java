package com.aneesh.suraksha.users.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.dto.RefreshCheckCheckResponse;
import com.aneesh.suraksha.users.dto.TokenSubject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RefreshCheck {

    private final HashingService hashingService;

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    public RefreshCheck(StringRedisTemplate stringRedisTemplate, HashingService hashingService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.hashingService = hashingService;
        this.objectMapper = new ObjectMapper();
    }

    public RefreshCheckCheckResponse Check(String token) {
        try {
            String hashedToken = hashingService.sha256(token);
            String key = "rf_" + hashedToken;
            String value = stringRedisTemplate.opsForValue().get(key);

            if (value == null) {
                return new RefreshCheckCheckResponse(false, null);
            }

            // Parse the metadata JSON to extract TokenSubject fields
            Map<String, Object> metadata = objectMapper.readValue(value,
                    new TypeReference<Map<String, Object>>() {
                    });

            TokenSubject subject = new TokenSubject(
                    UUID.fromString((String) metadata.get("userId")),
                    (String) metadata.get("mailId"),
                    (String) metadata.get("organisationId"));

            return new RefreshCheckCheckResponse(true, subject);
        } catch (Exception e) {
            return new RefreshCheckCheckResponse(false, null);
        }
    }

}
