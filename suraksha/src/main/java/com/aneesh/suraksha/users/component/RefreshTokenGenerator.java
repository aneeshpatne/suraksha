package com.aneesh.suraksha.users.component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.aneesh.suraksha.config.AppSecretConfig;

@Component
public class RefreshTokenGenerator {

    private final AppSecretConfig appSecretConfig;

    public RefreshTokenGenerator(AppSecretConfig appSecretConfig) {
        this.appSecretConfig = appSecretConfig;
    }

    public String generateToken() {
        String prefix = "rf_";
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        String refreshToken = prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return refreshToken;
    }

    private byte[] getSecretBytes() {
        return Base64.getDecoder().decode(appSecretConfig.getRefreshSecretKey());
    }

    public String hashToken(String token) {
        try {
            byte[] secretBytes = getSecretBytes();
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA256");
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (java.security.NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            throw new IllegalStateException("Failed to hash refresh token", e);
        }

    }
}
