
package com.cts.mfrp.pc.service;

// These are the correct imports for version 7.0.0
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class EmailService {

    @Value("${brevo.api.key}") // 🛠️ Best practice: Store this in application.properties
    private String apiKey;

    public void sendResetPasswordEmail(String recipientEmail, String token) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

        // 1. Set Sender (Must be your verified Brevo email)
        sendSmtpEmail.setSender(new SendSmtpEmailSender()
                .name("Smart Pharma Connect")
                .email("revathigogu3012@gmail.com"));

        // 2. Set Recipient
        sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(recipientEmail)));

        // 3. Set Subject
        sendSmtpEmail.setSubject("Reset Your Password - Smart Pharma Connect");

        // 4. Set Content (This link will point to your Angular app)
        String resetLink = "http://localhost:4200/reset-password?token=" + token;
        String htmlContent = "<html><body>" +
                "<h3>Password Reset Request</h3>" +
                "To reset your password, please click the link below:<br>" +
                "<a href='" + resetLink + "'>Reset Password Now</a><br><br>" +
                "This link will expire in 15 minutes." +
                "</body></html>";

        sendSmtpEmail.setHtmlContent(htmlContent);

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ SUCCESS: Brevo email sent to " + recipientEmail);
        } catch (Exception e) {
            System.err.println("❌ ERROR: Failed to send email via Brevo: " + e.getMessage());
        }
    }
}
