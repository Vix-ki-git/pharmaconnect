package com.cts.mfrp.pc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private static final String SYSTEM_PROMPT = """
            You are PharmaConnect Assistant, a helpful guide inside an online pharmacy
            search app. Users are patients/buyers looking up over-the-counter and
            prescription medicines available at nearby Indian pharmacies.

            Stay strictly within these topics:
              * What a medicine is generally used for
              * Common dosage forms (tablet, syrup, etc.) and typical adult dosage ranges
              * Common side effects and well-known interactions
              * Generic equivalents and brand alternatives
              * How to store a medicine
              * General guidance on using the PharmaConnect app (search, reserve)

            Hard rules:
              * Never diagnose a condition. If the user describes symptoms, suggest they
                consult a doctor or pharmacist.
              * Never recommend prescription medicines without prescription. Remind the
                user that a registered medical practitioner must prescribe them.
              * Never give exact pediatric dosages, pregnancy dosages, or dosages for
                people with kidney/liver disease — refer them to a doctor.
              * If asked something off-topic (politics, code help, etc.) politely steer
                back to medicine questions.
              * Aim for 100-250 words. Use short paragraphs or bullet points.
                Be thorough enough to actually answer the question — don't be terse.
              * Always end medication-related answers with one short line:
                "This is general information, not medical advice — please consult a
                pharmacist or doctor."
            """;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    public String ask(String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return "The chatbot is not configured yet. An administrator needs to set the "
                    + "GEMINI_API_KEY environment variable. In the meantime, please use "
                    + "the Search page to find medicines at nearby pharmacies.";
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userMessage))
                )),
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", SYSTEM_PROMPT))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.4,
                        "maxOutputTokens", 1024,
                        "thinkingConfig", Map.of("thinkingBudget", 0)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            return extractReply(response);
        } catch (HttpStatusCodeException e) {
            String status = e.getStatusCode().toString();
            String responseBody = e.getResponseBodyAsString();
            log.error("Gemini call failed: status={} body={}", status, responseBody);
            return "Assistant error (" + status + "): " + truncate(responseBody);
        } catch (RestClientException e) {
            log.error("Gemini call network error", e);
            return "The assistant is temporarily unavailable: " + e.getMessage();
        } catch (Exception e) {
            log.error("Gemini call unexpected error", e);
            return "Sorry, something went wrong: " + e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractReply(Map<String, Object> response) {
        if (response == null) return "Sorry, I couldn't generate a reply. Please try rephrasing.";

        Object candidatesObj = response.get("candidates");
        if (candidatesObj instanceof List<?> candidates && !candidates.isEmpty()) {
            Object first = candidates.get(0);
            if (first instanceof Map<?, ?> firstMap) {
                Object content = firstMap.get("content");
                if (content instanceof Map<?, ?> contentMap) {
                    Object parts = contentMap.get("parts");
                    if (parts instanceof List<?> partsList && !partsList.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Object p : partsList) {
                            if (p instanceof Map<?, ?> partMap) {
                                Object text = partMap.get("text");
                                if (text instanceof String s && !s.isBlank()) {
                                    if (sb.length() > 0) sb.append("\n\n");
                                    sb.append(s.trim());
                                }
                            }
                        }
                        if (sb.length() > 0) return sb.toString();
                    }
                }
            }
        }

        Object feedback = response.get("promptFeedback");
        if (feedback instanceof Map<?, ?> feedbackMap && feedbackMap.get("blockReason") != null) {
            return "I can't answer that question. Try asking about a specific medicine — "
                    + "its use, dosage form, side effects, or generics.";
        }

        return "Sorry, I couldn't generate a reply. Please try rephrasing.";
    }

    private String truncate(String s) {
        if (s == null) return "(no body)";
        return s.length() > 400 ? s.substring(0, 400) + "..." : s;
    }
}
