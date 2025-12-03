package com.aneesh.suraksha.users.component;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyGenerator {

    public String generateAPIKey() {
        String prefix = "suraksha_apiKey_";
        byte[] RandomBytes = new byte[40];
        new SecureRandom().nextBytes(RandomBytes);
        String RandomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(RandomBytes);
        return prefix + RandomPart;

    }

}
