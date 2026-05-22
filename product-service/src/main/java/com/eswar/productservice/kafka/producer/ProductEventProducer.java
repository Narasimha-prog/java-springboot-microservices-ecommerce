package com.eswar.productservice.kafka.producer;

import com.eswar.productservice.kafka.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String PRODUCT_TOPIC = "product-created";

    public void sendProductCreatedEvent(ProductCreatedEvent event) {
        // Generate a fresh unique traceId for this network send attempt
        UUID newTraceId = UUID.randomUUID();

        ProductCreatedEvent finalizedEvent = new ProductCreatedEvent(
                event.eventId(),      // Keeps core event correlation id
                newTraceId,           // Unique attempt tracker
                event.productId(),
                event.sku(),
                event.name(),
                event.description(),
                event.price(),
                event.categoryName()
        );

        log.info("Sending product-created event for SKU: {} with TraceID: {}", finalizedEvent.sku(), newTraceId);

        // Routing using the Product ID as the Kafka Partition Key to maintain ordering guarantees
        kafkaTemplate.send(PRODUCT_TOPIC, finalizedEvent.productId().toString(), finalizedEvent);
    }
}
