package com.aneesh.suraksha.users.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    public RefreshTokenService(AppSecretConfig appSecretConfig,
            com.aneesh.suraksha.users.model.RefreshTokenRepository refreshTokenRepository,
            StringRedisTemplate stringRedisTemplate) {
        this.appSecretConfig = appSecretConfig;
        this.refreshTokenRepository = refreshTokenRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String IssueRefreshToken() {
        String prefix = "rf_";
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().encodeToString(randomBytes);
        return prefix + randomPart;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash)
                hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String generate(CreateRefreshTokenRequest request) {
        try {
            String token = IssueRefreshToken();
            String hashedToken = hashToken(token);
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
