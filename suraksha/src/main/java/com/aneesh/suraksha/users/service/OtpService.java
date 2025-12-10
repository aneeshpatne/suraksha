package com.aneesh.suraksha.users.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private String generateOtp() {
        int randomNumber = 1000 + secureRandom.nextInt(9000);
        return Integer.toString(randomNumber);
    }

    public void OtpFlow() {
        String otp = generateOtp();
        String key = "otp:login:" + 1234;
        stringRedisTemplate.opsForValue().set(key, otp, 5, TimeUnit.MINUTES);
    }
}
