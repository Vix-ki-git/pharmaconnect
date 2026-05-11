package com.cts.mfrp.pc.controller;

import com.cts.mfrp.pc.dto.ChatRequest;
import com.cts.mfrp.pc.dto.ChatResponse;
import com.cts.mfrp.pc.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> ask(@Valid @RequestBody ChatRequest request) {
        String reply = chatbotService.ask(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(reply));
    }
}
