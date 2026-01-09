package com.aneesh.suraksha.users.service;

import java.util.concurrent.TimeUnit;
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
        String otp = otpService.generateOtp();
        try {
            stringRedisTemplate.opsForValue()
                    .set("2fa:" + tokenSubject.organisationId() + ":" + tokenSubject.mailId() + ":" + otp,
                            objectMapper.writeValueAsString(tokenSubject), 2, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate 2FA token");
        }
    }

}
