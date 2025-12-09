package com.aneesh.suraksha.users.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String IssueRefreshToken() {
        String prefix = "rf_";
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().encodeToString(randomBytes);
        return prefix + randomPart;
    }
}
