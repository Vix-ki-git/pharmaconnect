//package com.cts.mfrp.pc.service;
//
//import kong.unirest.HttpResponse;
//import kong.unirest.Unirest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    @Value("${mailgun.api.key}")
//    private String apiKey;
//
//    @Value("${mailgun.domain}")
//    private String domain;
//
//    @Value("${mailgun.base.url}")
//    private String baseUrl;
//
//    public void sendResetEmail(String toEmail, String token) {
//        // This is the link the user will click in their email
//        String resetLink = "http://localhost:3000/reset-password?token=" + token;
//
//        HttpResponse<String> response = Unirest.post(baseUrl + domain + "/messages")
//                .basicAuth("api", apiKey)
//                .field("from", "Smart Pharma Support <mailgun@" + domain + ">")
//                .field("to", toEmail)
//                .field("subject", "Password Reset - Smart Pharma Connect")
//                .field("text", "Hello,\n\nYou requested a password reset. Please click the link below to set a new password:\n"
//                        + resetLink + "\n\nThis link expires in 15 minutes.")
//                .asString();
//
//        if (response.getStatus() != 200) {
//            throw new RuntimeException("Mailgun Error: " + response.getBody());
//        }
//    }
//}