package com.aneesh.suraksha.users.service;

import java.security.MessageDigest;

import org.springframework.stereotype.Service;

@Service
public class HashingService {

    /**
     * Hashes a string using SHA-256 algorithm
     * 
     * @param input The string to hash
     * @return The hexadecimal representation of the hash
     * @throws RuntimeException if hashing fails
     */
    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash)
                hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash using SHA-256", e);
        }
    }
}
