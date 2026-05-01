package com.sitamahalakshmi.notification_service.service;

import com.sitamahalakshmi.notification_service.grpc.client.GrpcUserServiceClient;
import com.sitamahalakshmi.notification_service.kafka.events.OrderCreatedEvent;
import com.sitamahalakshmi.notification_service.kafka.events.OrderStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationServiceImp implements INotificationService{

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;


    private final GrpcUserServiceClient userServiceClient;


    @Override
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {

    }

    @Override
    public void handleOrderStatusEvent(OrderStatusEvent event) {

    }
}
