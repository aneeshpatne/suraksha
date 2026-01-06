package com.aneesh.suraksha.users.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshCheck {

    private final HashingService hashingService;

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshCheck(StringRedisTemplate stringRedisTemplate, HashingService hashingService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.hashingService = hashingService;
    }

    public String Check(String token) {
        String hashedToken = hashingService.sha256(token);
        String key = "rf_" + hashedToken;
        String value = stringRedisTemplate.opsForValue().get(key);
    }

}
