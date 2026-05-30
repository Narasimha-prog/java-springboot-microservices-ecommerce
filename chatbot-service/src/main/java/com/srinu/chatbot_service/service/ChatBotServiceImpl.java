package com.srinu.chatbot_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.srinu.chatbot_service.entity.ProductEventEntity;
import com.srinu.chatbot_service.kafka.constants.EventStatus;
import com.srinu.chatbot_service.kafka.constants.EventType;
import com.srinu.chatbot_service.kafka.event.ProductCreatedEvent;
import com.srinu.chatbot_service.repository.ChromaVectorRepository;
import com.srinu.chatbot_service.repository.IProductEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotServiceImpl implements IChatBotService {

    private final ObjectMapper objectMapper;
    private final IProductEventRepository eventRepository;
    private final ChromaVectorRepository chromaVectorRepository;
    private final ChatModel chatModel;
    // Inject your ChromaDB vector interaction layer here when ready:
    // private final IChromaVectorRepository chromaVectorRepository;

    @Autowired
    @Lazy
    private ChatBotServiceImpl self; // Lazy self-proxy to enable internal method @Transactional aspects

    @Override
    @Transactional
    public void handleProductCreatedEvent(@NonNull ProductCreatedEvent event) {
        log.info("Beginning orchestrator pipeline for product sync event: {}", event.eventId());

        // Step A: Record the Receipt (Always Commits to PostgreSQL via REQUIRES_NEW)
        ProductEventEntity eventEntity = self.recordReceipt(event);

        // Step B: Skip if already processed (Idempotency check)
        if (eventEntity.getStatus() == EventStatus.PROCESSED) {
            log.info("Event {} has already been synced to vector engine. Skipping business logic execution.", event.eventId());
            return;
        }

        try {
            // Step C: Risky ChromaDB Vector Ingestion
            self.processChatBotVectorSync(event, eventEntity);
        } catch (Exception ex) {
            log.error("Execution exception encountered during ChromaDB sync for event: {}", event.eventId(), ex);
            // Step D: Record Failure state permanently to disk (Always Commits)
            self.recordFailure(eventEntity, ex.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProductEventEntity recordReceipt(ProductCreatedEvent event) {

        log.info("Checking transaction history ledger logs for event ID: {}", event.eventId());

        return eventRepository.findById(event.eventId())
                .map(existing -> {
                    log.info("Retry attempt detected for event: {}. Appending fresh tracking token.", event.eventId());
                    UUID localAttemptId = UUID.randomUUID();
                    existing.getTraceIds().add(localAttemptId);
                    return eventRepository.save(existing);
                })
                .orElseGet(() -> {
                    log.info("First delivery validation for event: {}. Staging text backup payload.", event.eventId());

                    String jsonPayload;
                    try {
                        jsonPayload = objectMapper.writeValueAsString(event);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to serialize event payload fallback for id: {}", event.eventId());
                        jsonPayload = "{}";
                    }

                    ProductEventEntity newEntity = ProductEventEntity.builder()
                            .eventId(event.eventId())
                            .productId(event.productId())
                            .eventType(EventType.CHATBOT_PRODUCT_SYNC)
                            .status(EventStatus.RECEIVED)
                            .payload(jsonPayload)
                            .traceIds(new HashSet<>(Set.of(event.traceId() != null ? event.traceId() : UUID.randomUUID())))
                            .build();

                    return eventRepository.save(newEntity);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processChatBotVectorSync(
            ProductCreatedEvent event,
            ProductEventEntity entity
    ) {

        log.info(
                "Streaming context payloads into vector store for product ID: {}",
                event.productId()
        );

        chromaVectorRepository.indexProductKnowledge(event);

        entity.setStatus(EventStatus.PROCESSED);

        eventRepository.save(entity);

        log.info(
                "Vector database generation committed successfully for event ID: {}",
                event.eventId()
        );
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(ProductEventEntity entity, String error) {
        log.info("Writing isolated failure audit context details for event record entry: {}", entity.getEventId());

        entity.setStatus(EventStatus.FAILED);
        entity.setErrorMessage(error);
        eventRepository.save(entity);

        log.info("Failure operational flag successfully written to disk for database tracking.");
    }

    @Override
    public String chatWithKnowledgeBase(String userMessage) {
        log.info("Received user chat query: {}", userMessage);

        try {
            // 🚀 Call the clean repository method wrapper we just created
            List<Document> similarDocuments = chromaVectorRepository.searchSimilarDocuments(userMessage, 3);

            // Extract and concatenate text contents
            String vectorContext = similarDocuments.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n"));

            log.info("Retrieved {} relevant context blocks from vector store.", similarDocuments.size());

            // Build a structured prompt augmenting the user message with the retrieved context
            String systemPromptText = """
                    You are an advanced E-Commerce Assistant. Use the following context details from our product catalog 
                    to accurately answer the user's request. If the answer cannot be found in the context, politely 
                    inform the user that you don't have that product detail right now.
                    
                    CONTEXT DATA:
                    %s
                    
                    USER QUESTION:
                    %s
                    
                    ANSWER:
                    """.formatted(vectorContext, userMessage);

            Prompt prompt = new Prompt(systemPromptText);
            return Objects.requireNonNull(chatModel.call(prompt).getResult()).getOutput().getText();

        } catch (Exception e) {
            log.error("Failed to execute RAG retrieval flow for user message", e);
            return "I'm having trouble connecting to my knowledge base right now. Please try again shortly!";
        }
    }

}