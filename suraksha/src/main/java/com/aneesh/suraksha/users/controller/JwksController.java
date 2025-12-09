package com.aneesh.suraksha.users.controller;

import com.aneesh.suraksha.config.AppSecretConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class JwksController {

    private final AppSecretConfig appSecretConfig;

    public JwksController(AppSecretConfig appSecretConfig) {
        this.appSecretConfig = appSecretConfig;
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        try {
            String publicKeyPEM = appSecretConfig.getRsaPublicKey()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

            // Convert modulus and exponent to Base64URL
            String n = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(publicKey.getModulus().toByteArray());
            String e = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(publicKey.getPublicExponent().toByteArray());

            Map<String, Object> jwk = new HashMap<>();
            jwk.put("kty", "RSA");
            jwk.put("use", "sig");
            jwk.put("alg", "RS256");
            jwk.put("n", n);
            jwk.put("e", e);

            Map<String, Object> response = new HashMap<>();
            response.put("keys", List.of(jwk));

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate JWKS");
            errorResponse.put("message", ex.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
