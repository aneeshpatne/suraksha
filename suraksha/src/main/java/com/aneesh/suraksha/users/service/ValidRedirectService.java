package com.aneesh.suraksha.users.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ValidRedirectService {

    private final StringRedisTemplate stringRedisTemplate;

    public ValidRedirectService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean validate(String organisationId, String redirect) {
        Boolean isValid = stringRedisTemplate.opsForSet().isMember("org:redirect:" + organisationId, redirect);
        return isValid;
    }

}
