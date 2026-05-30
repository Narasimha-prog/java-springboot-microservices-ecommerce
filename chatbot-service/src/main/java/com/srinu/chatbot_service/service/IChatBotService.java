package com.srinu.chatbot_service.service;

import com.srinu.chatbot_service.kafka.event.ProductCreatedEvent;
import org.jspecify.annotations.NonNull;

public interface IChatBotService {
    void handleProductCreatedEvent(@NonNull ProductCreatedEvent event);

    String chatWithKnowledgeBase(String userMessage);
}
