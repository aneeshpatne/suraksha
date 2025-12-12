package com.aneesh.suraksha.users.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.aneesh.suraksha.config.AppSecretConfig;
import com.aneesh.suraksha.users.dto.RefreshTokenServiceRequest;
import com.aneesh.suraksha.users.model.RefreshToken;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final AppSecretConfig appSecretConfig;

    private final com.aneesh.suraksha.users.model.RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(AppSecretConfig appSecretConfig,
            com.aneesh.suraksha.users.model.RefreshTokenRepository refreshTokenRepository) {
        this.appSecretConfig = appSecretConfig;
        this.refreshTokenRepository = refreshTokenRepository;
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

    public String generate(RefreshTokenServiceRequest request) {
        String token = IssueRefreshToken();
        String hashedToken = hashToken(token);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(request.user());
        refreshToken.setToken(hashedToken);
        refreshToken.setIp(request.ip());
        refreshToken.setUserAgent(request.userAgent());
        refreshToken.setRevoked(false);
        // Set expires at to 7 days from now
        refreshToken.setExpiresAt(new java.sql.Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7));

        refreshToken = refreshTokenRepository.save(refreshToken);

        return token;
    }

}
