package com.eswar.paymentservice.kafka.service;

import com.eswar.paymentservice.entity.EventEntity;
import com.eswar.paymentservice.kafka.constants.EventStatus;
import com.eswar.paymentservice.kafka.constants.EventType;
import com.eswar.paymentservice.kafka.events.OrderCreatedEvent;
import com.eswar.paymentservice.repository.IEventRepository;
import com.eswar.paymentservice.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaService {

    private final IEventRepository eventRepository;
    private final IPaymentService paymentService;

    @Transactional
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {

        UUID eventId = event.eventId();
        UUID traceId = event.traceId();

        // 1️⃣ Check if the event exists
        EventEntity entity = eventRepository.findById(eventId).orElse(null);

        if (entity != null) {
            // ✅ Event exists → add new traceId for retry
            if (entity.getTraceIds() == null) {
                entity.setTraceIds(new HashSet<>());
            }
            entity.getTraceIds().add(traceId);
            log.info("Retry detected for event {}. Added new traceId {}", eventId, traceId);
        } else {
            // ✅ First attempt → create new EventEntity
            entity = EventEntity.builder()
                    .eventId(eventId)
                    .traceIds(new HashSet<>(Set.of(traceId))) // add first traceId
                    .orderId(event.orderId())
                    .eventType(EventType.PAYMENT)
                    .status(EventStatus.RECEIVED)
                    .payload(event.toString())
                    .build();
            log.info("New event {} received with traceId {}", eventId, traceId);
        }

        eventRepository.save(entity);

        try {
            // 2️⃣ Business logic
            paymentService.handleOrderCreatedEvent(event);

            // 3️⃣ Mark success
            entity.setStatus(EventStatus.PROCESSED);

        } catch (Exception ex) {
            // 4️⃣ Mark failed
            entity.setStatus(EventStatus.FAILED);
            entity.setErrorMessage(ex.getMessage());
        }

        eventRepository.save(entity); // persist updates
    }
}
