package com.aneesh.suraksha.users.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.dto.MailDto;
import com.aneesh.suraksha.users.dto.MagicLinkTokenPayload;
import com.aneesh.suraksha.users.dto.TokenSubject;
import com.aneesh.suraksha.users.dto.RequestMetadata;
import com.aneesh.suraksha.users.dto.CreateRefreshTokenRequest;
import com.aneesh.suraksha.users.dto.MagicLinkResult;
import com.aneesh.suraksha.users.model.UserEntity;
import tools.jackson.databind.ObjectMapper;

@Service
public class MagicUrlService {

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;
    public static final SecureRandom secureRandom = new SecureRandom();
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final MailSenderService mailSenderService;

    public MagicUrlService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper,
            JwtService jwtService, RefreshTokenService refreshTokenService, MailSenderService mailSenderService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.mailSenderService = mailSenderService;
    }

    private String generateRandomMagicBytes() {
        String prefix = "magic_";
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return prefix + randomPart;
    }

    public void SendMagicUrl(UserEntity user) {
        try {
            String magicUrl = generateRandomMagicBytes();
            String key = "magic:" + magicUrl;
            TokenSubject payload = new TokenSubject(user.getId(), user.getMailId(), user.getOrganisations().getId());
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.opsForValue().set(key, json, Duration.ofMinutes(10));

            String fullMagicLink = "http://localhost:8080/api/v1/verify-magic-url?token=" + magicUrl;
            String emailBody = generateEmailBody(fullMagicLink);

            MailDto mailDto = new MailDto("aneeshpatne@gmail.com",
                    "Suraksha Magic Sign In", emailBody);

            mailSenderService.send(mailDto);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate magic URL", e);
        }
    }

    public MagicLinkResult verifySendMagicUrl(String token, RequestMetadata metaData) {
        try {
            String json = stringRedisTemplate.opsForValue().get("magic:" + token);
            if (json == null) {
                return new MagicLinkResult(false, null, null, null);
            }
            TokenSubject data = objectMapper.readValue(json, TokenSubject.class);
            String jwt = jwtService.generateToken(data);
            stringRedisTemplate.delete("magic:" + token);
            String refresh_token = refreshTokenService
                    .generate(new CreateRefreshTokenRequest(data, metaData.ip(), metaData.userAgent()));
            return new MagicLinkResult(true, data.userId(), jwt, refresh_token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify magic URL", e);
        }
    }

    private String generateEmailBody(String magicLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="color-scheme" content="light dark">
                    <meta name="supported-color-schemes" content="light dark">
                    <title>Magic Sign In</title>
                    <style>
                        :root {
                            color-scheme: light dark;
                            supported-color-schemes: light dark;
                        }
                        @media (prefers-color-scheme: dark) {
                            .body-bg { background-color: #09090b !important; }
                            .content-box { background-color: #18181b !important; border-color: #27272a !important; }
                            .logo-light { display: none !important; }
                            .logo-dark { display: block !important; }
                            .text-primary { color: #fafafa !important; }
                            .text-secondary { color: #a1a1aa !important; }
                            .btn-primary { background-color: #fafafa !important; color: #18181b !important; }
                            .code-box { background-color: #09090b !important; border-color: #27272a !important; }
                            .link-text { color: #a1a1aa !important; }
                            .footer-text { color: #71717a !important; }
                        }
                    </style>
                </head>
                <body class="body-bg" style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f4f4f5;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" class="body-bg" style="background-color: #f4f4f5; width: 100%%;">
                        <tr>
                            <td align="center" style="padding: 40px 20px;">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" class="content-box" style="max-width: 400px; background-color: #ffffff; border-radius: 16px; border: 1px solid #e4e4e7; margin: 0 auto;">
                                    <tr>
                                        <td style="padding: 40px 32px; text-align: center;">
                                            <img src="https://raw.githubusercontent.com/aneeshpatne/suraksha/refs/heads/main/assets/logo_light_mode.png" class="logo-light" alt="Suraksha" width="48" height="48" style="margin: 0 auto 24px auto; width: 48px; height: 48px; border-radius: 12px; display: block; border: 0;">
                                            <img src="https://raw.githubusercontent.com/aneeshpatne/suraksha/refs/heads/main/assets/logo_dark_mode.png" class="logo-dark" alt="Suraksha" width="48" height="48" style="display: none; margin: 0 auto 24px auto; width: 48px; height: 48px; border-radius: 12px; border: 0;">
                                            <h1 class="text-primary" style="margin: 0 0 8px 0; font-size: 20px; font-weight: 600; color: #18181b; letter-spacing: -0.5px;">
                                                Magic Sign In
                                            </h1>
                                            <p class="text-secondary" style="margin: 0 0 32px 0; font-size: 14px; color: #71717a; line-height: 1.5;">
                                                Click the button below to sign in directly.
                                            </p>

                                            <!-- Button -->
                                            <div style="margin-bottom: 32px;">
                                                <a href="%s" class="btn-primary" style="display: inline-block; background-color: #18181b; color: #ffffff; padding: 12px 32px; border-radius: 12px; text-decoration: none; font-weight: 500; font-size: 14px; transition: all 0.2s;">
                                                    Sign In Now
                                                </a>
                                            </div>

                                            <p class="text-secondary" style="margin: 0 0 12px 0; font-size: 13px; color: #71717a;">
                                                Or copy and paste this link:
                                            </p>

                                            <!-- Raw Link -->
                                            <div class="code-box" style="background-color: #fafafa; border: 1px solid #e4e4e7; border-radius: 8px; padding: 12px; word-break: break-all;">
                                                <a href="%s" class="link-text" style="font-size: 11px; color: #52525b; text-decoration: none; line-height: 1.4; display: block;">
                                                    %s
                                                </a>
                                            </div>

                                            <p class="text-secondary" style="margin: 24px 0 0 0; font-size: 13px; color: #71717a;">
                                                Link expires in <span style="font-weight: 500;">10 minutes</span>
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width: 400px; margin-top: 24px;">
                                    <tr>
                                        <td style="text-align: center;">
                                            <p class="footer-text" style="margin: 0; font-size: 12px; color: #a1a1aa;">
                                                &copy; 2025 Suraksha Security
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(magicLink, magicLink, magicLink);
    }
}
