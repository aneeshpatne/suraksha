package com.aneesh.suraksha.users.service;

import java.nio.charset.StandardCharsets;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.aneesh.suraksha.config.AppSecretConfig;
import com.aneesh.suraksha.users.dto.CreateRefreshTokenRequest;
import com.aneesh.suraksha.users.model.RefreshToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    private final AppSecretConfig appSecretConfig;

    private final com.aneesh.suraksha.users.model.RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper;

    private final HashingService hashingService;

    public RefreshTokenService(AppSecretConfig appSecretConfig,
            com.aneesh.suraksha.users.model.RefreshTokenRepository refreshTokenRepository,
            StringRedisTemplate stringRedisTemplate,
            HashingService hashingService) {
        this.appSecretConfig = appSecretConfig;
        this.refreshTokenRepository = refreshTokenRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
        this.hashingService = hashingService;
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String IssueRefreshToken() {
        String prefix = "rf_";
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().encodeToString(randomBytes);
        return prefix + randomPart;
    }

    public String generate(CreateRefreshTokenRequest request) {
        try {
            String token = IssueRefreshToken();
            String hashedToken = hashingService.sha256(token);
            String key = "rf_" + hashedToken;

            // Create metadata map
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("userId", request.user().getId());
            metadata.put("ip", request.ip());
            metadata.put("userAgent", request.userAgent());

            // Serialize to JSON
            String metadataJson = objectMapper.writeValueAsString(metadata);

            stringRedisTemplate.opsForValue().set(key, metadataJson, 30, TimeUnit.MINUTES);

            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate refresh token", e);
        }
    }

}
