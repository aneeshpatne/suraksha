package com.aneesh.suraksha.users.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aneesh.suraksha.config.AppSecretConfig;
import com.aneesh.suraksha.users.model.UserEntity;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final AppSecretConfig appSecretConfig;

    public JwtService(AppSecretConfig appSecretConfig) {
        this.appSecretConfig = appSecretConfig;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(appSecretConfig.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserEntity userEntity) {
        return generateToken(Map.of(), userEntity);
    }

    public String generateToken(Map<String, Object> extraClaims, UserEntity userEntity) {
        long now = System.currentTimeMillis();
        long expirationMs = 15 * 60 * 1000; // 15 min

        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("userId", userEntity.getId());
        claims.put("mailId", userEntity.getMailId());
        claims.put("organisationId", userEntity.getOrganisations().getId());

        return Jwts.builder()
                .claims(claims)
                .subject(userEntity.getMailId())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }
}
