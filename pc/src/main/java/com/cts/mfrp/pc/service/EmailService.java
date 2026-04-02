package com.cts.mfrp.pc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import java.util.Collections;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    public void sendResetPasswordEmail(String toEmail, String token) {
        System.out.println("DEBUG: Preparing reset email for: " + toEmail);

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("ERROR: Brevo API Key is missing in properties!");
            return;
        }
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);
        String resetLink = "http://localhost:4200/reset-password?token=" + token;

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(new SendSmtpEmailSender()
                .name("Smart Pharma Connect")
                .email("revathigogu3012@gmail.com")); // Verified Brevo Sender

        sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(toEmail)));
        sendSmtpEmail.setSubject("Password Reset Request - Smart Pharma Connect");

        sendSmtpEmail.setHtmlContent("<html><body style='font-family: Arial, sans-serif;'>"
                + "<h2>Reset Your Password</h2>"
                + "<p>We received a request to reset your password for Smart Pharma Connect.</p>"
                + "<div style='margin: 20px 0;'><a href='" + resetLink + "' "
                + "style='background-color: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>"
                + "Reset My Password</a></div>"
                + "<p>This link will expire in <b>15 minutes</b>.</p>"
                + "<p>If you didn't request this, you can safely ignore this email.</p>"
                + "</body></html>");

        // Replace your existing try-catch in EmailService.java with this:
        try {
            System.out.println("DEBUG: Attempting to send via Brevo API...");
            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("✅ SUCCESS: Brevo email sent!");
        } catch (sendinblue.ApiException e) {
            System.err.println("❌ BREVO ERROR CODE: " + e.getCode());
            System.err.println("❌ BREVO ERROR BODY: " + e.getResponseBody()); // THIS IS THE KEY
        } catch (Exception e) {
            System.err.println("❌ GENERAL ERROR: " + e.getMessage());
        }
    }
}