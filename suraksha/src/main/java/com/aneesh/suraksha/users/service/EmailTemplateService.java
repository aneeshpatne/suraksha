package com.aneesh.suraksha.users.service;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    public String generateOtpEmail(String otp) {
        return """
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="color-scheme" content="light dark">
                    <meta name="supported-color-schemes" content="light dark">
                    <title>Suraksha OTP</title>
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

                        /* Client-specific resets */
                        body { margin: 0; padding: 0; width: 100%% !important; -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%; }
                        table { border-spacing: 0; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
                        img { border: 0; line-height: 100%%; outline: none; text-decoration: none; -ms-interpolation-mode: bicubic; }

                        /* Light Mode Variables (Default) */
                        :root {
                            color-scheme: light dark;
                            supported-color-schemes: light dark;
                        }

                        /* Base Styles (Light Mode) */
                        .body-bg { background-color: #f4f4f5; }
                        .card-bg { background-color: #ffffff; border: 1px solid #e4e4e7; }
                        .text-main { color: #18181b; }
                        .text-muted { color: #71717a; }
                        .otp-box-bg { background-color: #f4f4f5; border: 1px solid #e4e4e7; }
                        .footer-border { border-top: 1px solid #e4e4e7; }

                        /* Logo Default Visibility */
                        .light-logo { display: block; }
                        .dark-logo { display: none; }

                        /* Dark Mode Overrides */
                        @media (prefers-color-scheme: dark) {
                            .body-bg { background-color: #000000 !important; }
                            .card-bg { background-color: #111113 !important; border: 1px solid rgba(255, 255, 255, 0.08) !important; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06) !important; }
                            .text-main { color: #ffffff !important; }
                            .text-muted { color: #a1a1aa !important; }
                            .otp-box-bg { background-color: #18181b !important; border: 1px solid #27272a !important; }
                            .footer-border { border-top: 1px solid rgba(255, 255, 255, 0.06) !important; }

                            /* Logo Toggle */
                            .light-logo { display: none !important; }
                            .dark-logo { display: block !important; }
                        }
                    </style>
                </head>
                <body class="body-bg" style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; -webkit-font-smoothing: antialiased;">
                    <!-- Wrapper -->
                    <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td class="body-bg" align="center" style="padding: 40px 20px;">
                                <!-- Login Card -->
                                <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0" style="max-width: 380px; width: 100%%;">
                                    <tr>
                                        <td class="card-bg" style="border-radius: 16px; overflow: hidden; box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);">
                                            <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0">
                                                <!-- Main Content -->
                                                <tr>
                                                    <td style="padding: 40px 32px; text-align: center;">
                                                        <!-- Brand Logo -->
                                                        <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0">
                                                            <tr>
                                                                <td align="center" style="padding-bottom: 24px;">
                                                                    <span class="text-main" style="font-weight: 700; font-size: 18px; text-decoration: none; display: inline-block;">
                                                                        <!-- Table for vertical alignment compatibility -->
                                                                        <table role="presentation" border="0" cellspacing="0" cellpadding="0" style="display: inline-table;">
                                                                            <tr>
                                                                                <td style="padding-right: 10px;">
                                                                                    <!-- Light Mode Logo -->
                                                                                    <img src="https://raw.githubusercontent.com/aneeshpatne/suraksha/main/assets/logo_light_mode.png" alt="" width="24" height="24" class="light-logo" style="border-radius: 50%%;" />
                                                                                    <!-- Dark Mode Logo -->
                                                                                    <img src="https://raw.githubusercontent.com/aneeshpatne/suraksha/main/assets/logo_dark_mode.png" alt="" width="24" height="24" class="dark-logo" style="border-radius: 50%%;" />
                                                                                </td>
                                                                                <td class="text-main" style="font-size: 18px; font-weight: 700;">
                                                                                     Suraksha
                                                                                </td>
                                                                            </tr>
                                                                        </table>
                                                                    </span>
                                                                </td>
                                                            </tr>
                                                        </table>

                                                        <!-- Header -->
                                                        <h1 class="text-main" style="margin: 0 0 8px; font-size: 20px; font-weight: 700; letter-spacing: -0.025em;">Authentication Required</h1>
                                                        <p class="text-muted" style="margin: 0 0 24px; font-size: 14px; line-height: 1.5;">Use the following One-Time Password to complete your login.</p>

                                                        <!-- OTP Box -->
                                                        <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0">
                                                            <tr>
                                                                <td class="otp-box-bg" align="center" style="padding: 16px; border-radius: 12px;">
                                                                    <span style="font-family: 'Monaco', 'Menlo', monospace; font-size: 32px; font-weight: 700; color: #6c47ff; letter-spacing: 4px;">%s</span>
                                                                </td>
                                                            </tr>
                                                        </table>

                                                        <!-- Spacer -->
                                                        <div style="height: 24px; line-height: 24px; font-size: 24px;">&nbsp;</div>

                                                        <p class="text-muted" style="margin: 0; font-size: 12px; line-height: 1.5;">This code will expire in 2 minutes.<br>If you didn't request this, please ignore this email.</p>
                                                    </td>
                                                </tr>
                                                <!-- Footer -->
                                                <tr>
                                                    <td class="footer-border" style="padding: 24px 32px; text-align: center;">
                                                        <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0">
                                                            <tr>
                                                                <td align="center" class="text-muted" style="font-size: 12px;">
                                                                    <span style="vertical-align: middle;">Secured by</span>
                                                                    <span class="text-muted" style="vertical-align: middle; font-weight: 600; color: #71717a;">Suraksha</span>
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(otp);
    }
}
