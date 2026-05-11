package com.cts.mfrp.pc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

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
              * Keep replies under 180 words. Use short paragraphs or bullet points.
              * Always end medication-related answers with one short line:
                "This is general information, not medical advice — please consult a
                pharmacist or doctor."
            """;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    public String ask(String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return "The chatbot is not configured yet. An administrator needs to set the "
                    + "GROQ_API_KEY environment variable. In the meantime, please use "
                    + "the Search page to find medicines at nearby pharmacies.";
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user",   "content", userMessage)
                ),
                "temperature", 0.4,
                "max_tokens", 512
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(GROQ_URL, entity, Map.class);
            return extractReply(response);
        } catch (RestClientException e) {
            return "The assistant is temporarily unavailable. Please try again in a moment.";
        } catch (Exception e) {
            return "Sorry, something went wrong. Please try again.";
        }
    }

    @SuppressWarnings("unchecked")
    private String extractReply(Map<String, Object> response) {
        if (response == null) return "Sorry, I couldn't generate a reply. Please try rephrasing.";

        Object choicesObj = response.get("choices");
        if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
            Object first = choices.get(0);
            if (first instanceof Map<?, ?> firstMap) {
                Object message = firstMap.get("message");
                if (message instanceof Map<?, ?> messageMap) {
                    Object content = messageMap.get("content");
                    if (content instanceof String s && !s.isBlank()) {
                        return s.trim();
                    }
                }
            }
        }

        return "Sorry, I couldn't generate a reply. Please try rephrasing.";
    }
}
