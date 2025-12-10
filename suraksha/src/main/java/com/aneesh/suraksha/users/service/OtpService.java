package com.aneesh.suraksha.users.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JavaMailSender javaMailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.properties.mail.from}")
    private String fromEmail;

    public OtpService(StringRedisTemplate stringRedisTemplate, JavaMailSender javaMailSender) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.javaMailSender = javaMailSender;
    }

    private String generateOtp() {
        int randomNumber = 1000 + secureRandom.nextInt(9000);
        return Integer.toString(randomNumber);
    }

    public void OtpFlow() {
        String otp = generateOtp();
        String key = "otp:login:" + 1234;
        stringRedisTemplate.opsForValue().set(key, otp, 5, TimeUnit.MINUTES);
        try {
            sendEmail("aneeshpatne@gmail.com", "Suraksha Verification Code", generateEmailBody(otp));
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private void sendEmail(String to, String subject, String body) throws jakarta.mail.MessagingException {
        jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
        org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true); // true indicates HTML
        javaMailSender.send(message);
    }

    private String generateEmailBody(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Suraksha Verification</title>
                    <style>
                        body {
                            font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
                            background-color: #f4f4f7;
                            margin: 0;
                            padding: 0;
                            -webkit-text-size-adjust: none;
                            color: #51545E;
                        }
                        .email-wrapper {
                            width: 100%;
                            background-color: #f4f4f7;
                            padding: 40px 0;
                        }
                        .email-content {
                            max-width: 500px;
                            margin: 0 auto;
                            background-color: #ffffff;
                            border-radius: 8px;
                            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05); /* Soft premium shadow */
                            overflow: hidden;
                        }
                        .email-header {
                            background-color: #000000; /* Sleek Dark Header */
                            padding: 30px;
                            text-align: center;
                        }
                        .email-header h1 {
                            color: #ffffff;
                            margin: 0;
                            font-size: 24px;
                            font-weight: 600;
                            letter-spacing: 1px;
                            text-transform: uppercase;
                        }
                        .email-body {
                            padding: 40px 40px;
                            text-align: left;
                        }
                        .email-body p {
                            font-size: 16px;
                            line-height: 1.6;
                            color: #333333;
                            margin-bottom: 20px;
                        }
                        .otp-container {
                            margin: 30px 0;
                            text-align: center;
                        }
                        .otp-code {
                            font-size: 36px;
                            font-weight: 700;
                            color: #000000;
                            letter-spacing: 8px;
                            padding: 15px 30px;
                            background-color: #f8f9fa;
                            border: 1px solid #e9ecef;
                            border-radius: 6px;
                            display: inline-block;
                        }
                        .email-footer {
                            text-align: center;
                            padding: 20px;
                            font-size: 12px;
                            color: #6b6e76;
                            background-color: #f4f4f7;
                        }
                        .email-footer p {
                            margin: 5px 0;
                        }
                        .highlight {
                            color: #000000;
                            font-weight: 600;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-wrapper">
                        <div class="email-content">
                            <div class="email-header">
                                <h1>Suraksha</h1>
                            </div>
                            <div class="email-body">
                                <p>Hello,</p>
                                <p>We received a request to access your account. To confirm your identity, please use the verification code below:</p>

                                <div class="otp-container">
                                    <span class="otp-code">"""
                + otp
                + """
                                            </span>
                                        </div>

                                        <p>This code will expire in <strong>5 minutes</strong>. If you did not request this code, reset your passwords immediately.</p>

                                        <p style="margin-top: 30px; font-size: 14px; color: #666;">
                                            Best regards,<br>
                                            The Suraksha Security Team
                                        </p>
                                    </div>
                                </div>
                                <div class="email-footer">
                                    <p>&copy; 2025 Suraksha. All rights reserved.</p>
                                    <p>Secure. Private. Trusted.</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """;
    }
}
