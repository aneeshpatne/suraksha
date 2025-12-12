package com.aneesh.suraksha.users.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.config.AppSecretConfig;

@Service
public class ApiKeyService {

    private final OrganisationsRepository organisationsRepository;

    private final AppSecretConfig appSecretConfig;

    public ApiKeyService(AppSecretConfig appSecretConfig, OrganisationsRepository organisationsRepository) {
        this.appSecretConfig = appSecretConfig;
        this.organisationsRepository = organisationsRepository;
    }

    public String generateAPIKey() {
        String prefix = "suraksha_apiKey_";
        byte[] RandomBytes = new byte[40];
        new SecureRandom().nextBytes(RandomBytes);
        String RandomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(RandomBytes);
        return prefix + RandomPart;
    }

    private byte[] getSecretBytes() {
        String secret = appSecretConfig.getAPIKeySecret();
        return Base64.getUrlDecoder().decode(secret);
    }

    public String hashAPIKey(String apiKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(getSecretBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] hmacSecret = mac.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacSecret);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash API key", e);
        }
    }

    public Boolean verifyApiKey(String id, String apiKey) {
        String hashedAPIKey = organisationsRepository.findById(id).map(Organisations::getApiKey).orElse("");
        if (hashedAPIKey.isEmpty()) {
            return false;
        }

        String computedHash = hashAPIKey(apiKey);

        return java.security.MessageDigest.isEqual(
                hashedAPIKey.getBytes(StandardCharsets.UTF_8),
                computedHash.getBytes(StandardCharsets.UTF_8));
    }

}
