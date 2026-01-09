package com.aneesh.suraksha.users.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import com.aneesh.suraksha.dto.MailDto;
import com.aneesh.suraksha.users.dto.TokenSubject;
import com.aneesh.suraksha.users.dto.TwoFactorAuthData;

@Service
public class TwofactorService {

    private final OtpService otpService;

    private final ObjectMapper objectMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MailSenderService mailSenderService;

    public TwofactorService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper, OtpService otpService,
            MailSenderService mailSenderService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.otpService = otpService;
        this.mailSenderService = mailSenderService;
    }

    private String RandomKeyGenerator() {
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }

    public String Generate(TokenSubject tokenSubject) {
        String otp = otpService.generateOtp();
        String key = RandomKeyGenerator();
        try {
            TwoFactorAuthData data = new TwoFactorAuthData(tokenSubject, otp);
            stringRedisTemplate.opsForValue()
                    .set("2fa:" + key,
                            objectMapper.writeValueAsString(data), 2, TimeUnit.MINUTES);

            MailDto mailDto = new MailDto(tokenSubject.mailId(), "Suraksha OTP", "Your OTP is: " + otp);
            mailSenderService.send(mailDto);

            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate 2FA token", e);
        }
    }

}
