package com.eswar.productservice.kafka.sheduler;


import com.eswar.productservice.constatnts.EventStatus;
import com.eswar.productservice.entity.ProductCreatedEventEntity;
import com.eswar.productservice.kafka.event.ProductCreatedEvent;
import com.eswar.productservice.kafka.producer.ProductEventProducer;
import com.eswar.productservice.repository.IProductCreatedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxMessageRelay {
   private final ObjectMapper objectMapper;
    private final IProductCreatedEventRepository outboxRepository;
    private final ProductEventProducer productEventProducer;

    // Runs automatically 2 seconds after the last run completes
    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void relayPendingMessages() {
        List<ProductCreatedEventEntity> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(EventStatus.RECEIVED);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox event(s) to relay to Kafka...", pendingEvents.size());

        for (ProductCreatedEventEntity event : pendingEvents) {
            try {
                // Send payload directly to Kafka using partition keys

                ProductCreatedEvent eventObject = objectMapper.readValue(event.getPayload(), ProductCreatedEvent.class);
                productEventProducer.sendProductCreatedEvent(eventObject);

                // Mark as processed upon network confirmation
                event.setStatus(EventStatus.PROCESSED);
                outboxRepository.save(event);
                log.info("Successfully relayed outbox event ID: {} to Kafka topic: {}", event.getId(), event.getTopic());
            } catch (Exception e) {
                log.error("Kafka push failure for Outbox Entry: {}. Stopping batch execution loop to preserve ordering. Retrying next cycle...", event.getId(), e);
                // Break out of loop so we don't try sending later events out of sequence
                break;
            }
        }
    }
}
