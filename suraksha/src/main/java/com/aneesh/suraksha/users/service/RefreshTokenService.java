package com.aneesh.suraksha.users.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.aneesh.suraksha.config.AppSecretConfig;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final AppSecretConfig appSecretConfig;

    public RefreshTokenService(AppSecretConfig appSecretConfig) {
        this.appSecretConfig = appSecretConfig;
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String IssueRefreshToken() {
        String prefix = "rf_";
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().encodeToString(randomBytes);
        return prefix + randomPart;
    }

    private byte[] getSecretBytes() {
        return Base64.getDecoder().decode(appSecretConfig.getRefreshSecretKey());
    }

    private String hashToken(String token) {
        try {
            byte[] secretBytes = getSecretBytes();
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA256");
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token");
        }
    }

}
