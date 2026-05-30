package com.srinu.chatbot_service.rest;

import com.srinu.chatbot_service.dto.ChatRequest;
import com.srinu.chatbot_service.dto.ChatResponse;
import com.srinu.chatbot_service.service.IChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final IChatBotService chatBotService;

    @PostMapping("/query")
    public ResponseEntity<ChatResponse> handleUserQuery(@RequestBody ChatRequest request) {
        String aiAnswer = chatBotService.chatWithKnowledgeBase(request.message());
        return ResponseEntity.ofNullable(new ChatResponse(aiAnswer));
    }
}
