package com.eswar.paymentservice.kafka.service;

import com.eswar.paymentservice.entity.EventEntity;
import com.eswar.paymentservice.kafka.constants.EventStatus;
import com.eswar.paymentservice.kafka.constants.EventType;
import com.eswar.paymentservice.kafka.events.OrderStatusEvent;
import com.eswar.paymentservice.kafka.producer.PaymentEventProducer;
import com.eswar.paymentservice.repository.IEventRepository;
import com.eswar.paymentservice.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaService {

  private final PaymentEventProducer paymentEventProducer;

    public void sendOrderStatusEvent(EventEntity entity,EventStatus status, String message,String paymentRef) {



        OrderStatusEvent statusEvent = new OrderStatusEvent(
                entity.getEventId(),
                entity.getOrderId(),      // orderId
                EventType.PAYMENT,       // type INVENTORY
                status,                    // SUCCESS or FAILED
                message   ,                 // optional message
                paymentRef
        );

        paymentEventProducer.sendPaymentStatus(statusEvent);

        log.info("Sent OrderStatusEvent for order {} with status {}", entity.getOrderId(), status);
    }
}
