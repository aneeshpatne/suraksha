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
                "Suraksha Verification Code " + otp, emailBody);

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
                    <meta name="color-scheme" content="light dark">
                    <meta name="supported-color-schemes" content="light dark">
                    <title>Verification Code</title>
                    <style>
                        :root {
                            color-scheme: light dark;
                            supported-color-schemes: light dark;
                        }
                        @media (prefers-color-scheme: dark) {
                            .body-bg { background-color: #09090b !important; }
                            .content-box { background-color: #18181b !important; border-color: #27272a !important; }
                            .text-primary { color: #fafafa !important; }
                            .text-secondary { color: #a1a1aa !important; }
                            .code-box { background-color: #09090b !important; border-color: #27272a !important; }
                            .code-text { color: #fafafa !important; }
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
                                            <div style="margin-bottom: 24px;">
                                                <img src="https://ui-avatars.com/api/?name=S&background=18181b&color=fff&size=64&rounded=true&bold=true" alt="Suraksha" width="48" height="48" style="display: inline-block; border-radius: 12px;">
                                            </div>
                                            <h1 class="text-primary" style="margin: 0 0 8px 0; font-size: 20px; font-weight: 600; color: #18181b; letter-spacing: -0.5px;">
                                                Verification Code
                                            </h1>
                                            <p class="text-secondary" style="margin: 0 0 32px 0; font-size: 14px; color: #71717a; line-height: 1.5;">
                                                Enter this code to verify your identity.
                                            </p>
                                            <div class="code-box" style="background-color: #fafafa; border: 1px solid #e4e4e7; border-radius: 12px; padding: 20px; margin-bottom: 32px;">
                                                <span class="code-text" style="font-size: 32px; font-weight: 700; color: #18181b; letter-spacing: 8px; font-family: monospace;">%s</span>
                                            </div>
                                            <p class="text-secondary" style="margin: 0; font-size: 13px; color: #71717a;">
                                                Expires in <span style="font-weight: 500;">5 minutes</span>
                                            </p>
                                            <p class="text-secondary" style="margin: 24px 0 0 0; font-size: 13px; color: #71717a;">
                                                Didn't request this? <span style="text-decoration: underline; cursor: pointer;">Secure your account.</span>
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
                """;
        return String.format(template, otp);
    }
}
