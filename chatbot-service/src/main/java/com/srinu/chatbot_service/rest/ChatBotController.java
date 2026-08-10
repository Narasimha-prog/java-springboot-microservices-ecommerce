package com.srinu.chatbot_service.rest;

import com.srinu.chatbot_service.dto.ChatRequest;
import com.srinu.chatbot_service.dto.ChatResponse;
import com.srinu.chatbot_service.service.IChatBotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Tag(name = "chatbot", description = "API for managing chatbot service in the e-commerce platform")
public class ChatBotController {

    private final IChatBotService chatBotService;

    @PostMapping("/query")
    @Operation(summary = "to ask queries,through post request ..",description = "to ask queries")
    public ResponseEntity<ChatResponse> handleUserQuery(@RequestBody ChatRequest request) {
        String aiAnswer = chatBotService.chatWithKnowledgeBase(request.message());
        return ResponseEntity.ofNullable(new ChatResponse(aiAnswer));
    }
}
