package com.aneesh.suraksha.users.service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aneesh.suraksha.config.AppSecretConfig;
import com.aneesh.suraksha.users.model.UserEntity;

import io.jsonwebtoken.Jwts;

@Service
public class JwtService {

    private final AppSecretConfig appSecretConfig;

    public JwtService(AppSecretConfig appSecretConfig) {
        this.appSecretConfig = appSecretConfig;
    }

    private PrivateKey getSigningKey() {
        try {
            // Sanitize the PEM key - remove headers, footers, and whitespace
            String privateKeyPEM = appSecretConfig.getRsaPrivateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\\n", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA private key", e);
        }
    }

    public String generateToken(UserEntity userEntity) {
        return generateToken(Map.of(), userEntity);
    }

    public String generateToken(Map<String, Object> extraClaims, UserEntity userEntity) {
        long now = System.currentTimeMillis();
        long expirationMs = 1 * 60 * 1000; // 15 min

        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("userId", userEntity.getId());
        claims.put("mailId", userEntity.getMailId());
        claims.put("organisationId", userEntity.getOrganisations().getId());

        return Jwts.builder()
                .claims(claims)
                .subject(userEntity.getMailId())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey(), Jwts.SIG.RS256)
                .compact();
    }
}
