package com.aneesh.suraksha.users.component;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class RefreshTokenGenerator {
    public String generateToken() {
        String prefix = "rf_";
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        String refreshToken = prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return refreshToken;
    }
}
