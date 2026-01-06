package com.aneesh.suraksha.users.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {

    private final StringRedisTemplate stringRedisTemplate;
    private final HashingService hashingService;

    public LogoutService(StringRedisTemplate stringRedisTemplate, HashingService hashingService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.hashingService = hashingService;
    }

    public Boolean logout(String rawToken) {
        String hashedToken = hashingService.sha256(rawToken);
        String val = stringRedisTemplate.opsForValue().getAndDelete(hashedToken);
        return val != null;
    }
}
