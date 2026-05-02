package com.sitamahalakshmi.notification_service.service;

import com.sitamahalakshmi.notification_service.grpc.client.GrpcUserServiceClient;
import com.sitamahalakshmi.notification_service.kafka.constatnts.EventStatus;
import com.sitamahalakshmi.notification_service.kafka.events.OrderCreatedEvent;
import com.sitamahalakshmi.notification_service.kafka.events.OrderStatusEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationServiceImp implements INotificationService{

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;


    private final GrpcUserServiceClient userServiceClient;


    @Override
    @Async
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("📧 Handling Order Created for ID: {}", event.orderId());

        // 1. Fetch User details via gRPC
        var user = userServiceClient.getUserById(event.customerId().toString());

        // 2. Prepare Context
        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("orderId", event.orderId());
        context.setVariable("items", event.items());
        context.setVariable("totalAmount", event.totalAmount());

        // 3. Process and Send
        String html = templateEngine.process("order-received", context);
        sendMail(user.getEmail(), "Order Received: #" + event.orderId(), html);
    }

    @Override
    @Async
    public void handleOrderStatusEvent(OrderStatusEvent event) {
        log.info("🔄 Handling Status Update: {} - {}", event.eventType(), event.status());

        // 1. Fetch user (Assuming customerId is available or fetched via orderId)
        var user = userServiceClient.getUserById(event.customerId().toString());

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("orderId", event.orderId());
        context.setVariable("message", event.message());

        String templateName;
        String subject;

        // 2. Logic to pick template based on EventType and Status
        switch (event.eventType()) {
            case PAYMENT -> {
                if (event.status() == EventStatus.PROCESSED) {
                    templateName = "order-confirmed";
                    subject = "Payment Successful! Order #" + event.orderId();
                } else {
                    templateName = "payment-failed";
                    subject = "Action Required: Payment Failed for #" + event.orderId();
                }
            }
            case INVENTORY -> {
                if (event.status() == EventStatus.FAILED) {
                    templateName = "inventory-failed";
                    subject = "Update regarding your Order #" + event.orderId();
                } else {
                    return; // Don't notify user for internal inventory success
                }
            }
            default -> {
                log.warn("Unknown EventType: {}", event.eventType());
                return;
            }
        }

        // 3. Process and Send
        String html = templateEngine.process(templateName, context);
        sendMail(user.getEmail(), subject, html);
    }

    private void sendMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}