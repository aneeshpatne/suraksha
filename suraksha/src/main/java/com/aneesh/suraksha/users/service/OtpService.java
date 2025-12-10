package com.aneesh.suraksha.users.service;

import com.aneesh.suraksha.config.RabbitMQConfig;
import com.aneesh.suraksha.dto.MailDTO;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(StringRedisTemplate stringRedisTemplate,
            RabbitTemplate rabbitTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    private String generateOtp() {
        int randomNumber = 1000 + secureRandom.nextInt(9000);
        return Integer.toString(randomNumber);
    }

    public void OtpFlow() {
        String otp = generateOtp();
        String key = "otp:login:" + 1234;
        stringRedisTemplate.opsForValue().set(key, otp, 5, TimeUnit.MINUTES);

        String emailBody = generateEmailBody(otp);
        MailDTO mailDTO = new MailDTO("aneeshpatne@gmail.com",
                "Suraksha Verification Code", emailBody);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY, mailDTO);
    }

    private String generateEmailBody(String otp) {
        String template = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verification Code</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0a0a0a;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background-color: #0a0a0a;">
                        <tr>
                            <td align="center" style="padding: 60px 20px;">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width: 420px; background-color: #111111; border-radius: 16px; border: 1px solid #222222;">
                                    <tr>
                                        <td style="padding: 48px 40px; text-align: center;">
                                            <div style="margin-bottom: 32px;">
                                                <svg width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M24 4L6 12V22C6 33.1 13.7 43.5 24 46C34.3 43.5 42 33.1 42 22V12L24 4Z" fill="url(#shield_grad)" stroke="#333333" stroke-width="1"/>
                                                    <path d="M20 24L23 27L28 20" stroke="#ffffff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                                                    <defs>
                                                        <linearGradient id="shield_grad" x1="6" y1="4" x2="42" y2="46" gradientUnits="userSpaceOnUse">
                                                            <stop offset="0%%" stop-color="#222222"/>
                                                            <stop offset="100%%" stop-color="#0a0a0a"/>
                                                        </linearGradient>
                                                    </defs>
                                                </svg>
                                            </div>
                                            <h1 style="margin: 0 0 8px 0; font-size: 20px; font-weight: 600; color: #ffffff; letter-spacing: -0.5px;">
                                                Verification Code
                                            </h1>
                                            <p style="margin: 0 0 40px 0; font-size: 14px; color: #666666; line-height: 1.5;">
                                                Enter this code to continue
                                            </p>
                                            <div style="background-color: #0a0a0a; border: 1px solid #333333; border-radius: 12px; padding: 24px 32px; margin-bottom: 40px;">
                                                <span style="font-size: 42px; font-weight: 700; color: #ffffff; letter-spacing: 12px; font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;">%s</span>
                                            </div>
                                            <p style="margin: 0; font-size: 13px; color: #555555;">
                                                Expires in <span style="color: #888888; font-weight: 500;">5 minutes</span>
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width: 420px; margin-top: 32px;">
                                    <tr>
                                        <td style="text-align: center;">
                                            <p style="margin: 0 0 8px 0; font-size: 13px; color: #444444;">
                                                Didn't request this? <span style="color: #666666;">Secure your account now.</span>
                                            </p>
                                            <p style="margin: 0; font-size: 12px; color: #333333;">
                                                &copy; 2025 Suraksha
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;
        return String.format(template, otp);
    }
}
