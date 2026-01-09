package com.aneesh.suraksha.users.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import com.aneesh.suraksha.users.dto.TokenSubject;

@Service
public class TwofactorService {

    private final OtpService otpService;

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate stringRedisTemplate;

    public TwofactorService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper, OtpService otpService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.otpService = otpService;
    }

    public Boolean Generate(TokenSubject tokenSubject) {
        String otp = otpService.

    }

}
