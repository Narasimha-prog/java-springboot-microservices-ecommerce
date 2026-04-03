package com.eswar.paymentservice.service;

import com.eswar.paymentservice.constatns.PaymentStatus;
import com.eswar.paymentservice.constatns.WebhookStatus;
import com.eswar.paymentservice.dto.*;
import com.eswar.paymentservice.entity.EventEntity;
import com.eswar.paymentservice.entity.PaymentEntity;
import com.eswar.paymentservice.entity.WebhookEventEntity;
import com.eswar.paymentservice.exception.BusinessException;
import com.eswar.paymentservice.exception.ErrorCode;
import com.eswar.paymentservice.kafka.constants.EventStatus;
import com.eswar.paymentservice.kafka.constants.EventType;
import com.eswar.paymentservice.kafka.events.OrderCreatedEvent;
import com.eswar.paymentservice.kafka.events.OrderStatusEvent;
import com.eswar.paymentservice.kafka.producer.PaymentEventProducer;
import com.eswar.paymentservice.mapper.IPaymentMapper;
import com.eswar.paymentservice.repository.IEventRepository;
import com.eswar.paymentservice.repository.IPaymentRepository;
import com.eswar.paymentservice.repository.IWebHookEventRepository;
import com.eswar.paymentservice.validation.PaymentValidator;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImp implements IPaymentService {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.currency:INR}")
    private String currency;

    private final PaymentValidator validator;
    private final IPaymentRepository paymentRepository;
    private final PaymentEventProducer producer;
    private final RazorpayService razorpayService;
    private final IPaymentMapper mapper;
    private final IWebHookEventRepository webHookEventRepository;
    private final IEventRepository eventRepository;


    @Autowired
    @Lazy
    private PaymentServiceImp self;

    // ================= HELPER =================

    private void validateOwnership(PaymentEntity payment, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        String userId = principal.getName();

        if (!payment.getUserId().toString().equals(userId)) {
            log.warn("Unauthorized access: user {} → payment {}",
                    userId, payment.getId());
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }
    }

    // ================= KAFKA =================
    @Override
    @Transactional
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {

        // Step A: Record Receipt (Independent Transaction)
        // This ensures we have a record even if the rest fails
        EventEntity eventEntity = self.recordEventReceipt(event);

        // Step B: Idempotency Check
        if (eventEntity.getStatus() == EventStatus.PROCESSED) {
            log.info("Event {} already processed. Skipping.", event.eventId());
            return;
        }

        try {
            // Step C: Business Logic (Independent Transaction)
            self.applyStatusUpdate(event, eventEntity);
            log.info("Successfully updated order {} to status {}", event.orderId(), event.status());
        } catch (Exception ex) {
            // Step D: Record Failure (Independent Transaction)
            self.recordEventFailure(eventEntity, ex.getMessage());
            // We rethrow to trigger Kafka retry if necessary
            throw ex;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventEntity recordEventReceipt(OrderCreatedEvent event) {
        return eventRepository.findById(event.eventId())
                .map(existing -> {
                    log.info("Retry detected for event {}. Adding traceId {}.",
                            event.eventId(), event.traceId());
                    existing.getTraceIds().add(event.traceId());
                    return eventRepository.save(existing);
                })
                .orElseGet(() -> {
                    EventEntity newEntity = EventEntity.builder()
                            .eventId(event.eventId())
                            .orderId(event.orderId())
                            .eventType(EventType.PAYMENT)
                            .status(EventStatus.RECEIVED)
                            .payload(event.toString())
                            .traceIds(new HashSet<>(Set.of(event.traceId())))
                            .build();
                    return eventRepository.save(newEntity);
                });
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPaymentInitialization(OrderCreatedEvent event, EventEntity entity) {

        // Find or Create Payment for this Order
        PaymentEntity payment = paymentRepository.findByOrderId(event.orderId())
                .orElseGet(() -> {
                    PaymentEntity p = new PaymentEntity();
                    p.setOrderId(event.orderId());
                    p.setUserId(event.customerId());
                    p.setAmount(event.totalAmount());
                    return p;
                });

        // Update status and link to the current event
        payment.setStatus(PaymentStatus.INITIATED);
        paymentRepository.save(payment);

        // Mark Event as Processed
        entity.setStatus(EventStatus.PROCESSED);
        eventRepository.save(entity);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEventFailure(EventEntity entity, String error) {
        entity.setStatus(EventStatus.FAILED);
        entity.setErrorMessage(error);
        eventRepository.save(entity);
    }
    // ================= USER =================

    @Override
    @Transactional
    //to get order id for frontend
    public PaymentCreateResponse createPayment(UUID orderId, Principal principal) throws RazorpayException {
        validator.validateCreatePayment(orderId);
        //find payment entity after created by order event
        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("Payment not found for order {}", orderId);
                    return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        //find owner not allow eg: admin  & others to modify
        validateOwnership(payment, principal);

        // Idempotency check
        if (payment.getRazorpayOrderId() != null) {
            return new PaymentCreateResponse(
                    payment.getRazorpayOrderId(),
                    razorpayKey,
                    currency,
                    payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue()
            );
        }

        //get order id from razorpay
        String razorpayOrderId;
        try {
             razorpayOrderId = razorpayService.createOrder(payment.getAmount(), currency);
        } catch (RazorpayException ex) {
            log.error("Razorpay service failed for order {}", orderId, ex);
            throw new BusinessException(ErrorCode.PAYMENT_SERVICE_UNAVAILABLE);
        }

        //update transaction using razor order id
        payment.setRazorpayOrderId(razorpayOrderId);

        return new PaymentCreateResponse(
                razorpayOrderId,
                razorpayKey,
                currency,
                payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue()
        );
    }

    @Override
    @Transactional()
    //verify payment details from frontend
    public PaymentResponse verifyPayment(PaymentVerifyRequest request, Principal principal) {

         validator.validateVerifyPayment(request);
        //get payment entity using transaction : order id from razorpay
        PaymentEntity payment = paymentRepository
                .findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        validateOwnership(payment, principal);


        // Prevent duplicate verification
        if (payment.getStatus() == PaymentStatus.SUCCESS
                || payment.getStatus() == PaymentStatus.PENDING) {

            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_VERIFIED);
        }

        //verify signature
        boolean isValid = razorpayService.verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        //sending event and update status
        if (isValid) {
            payment.setStatus(PaymentStatus.PENDING);
            payment.setRazorpayPaymentId(request.razorpayPaymentId());
            paymentRepository.save(payment);
            return new PaymentResponse("SUCCESS", "Payment verified is pending from webhook");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return new PaymentResponse("FAILED", "Invalid payment signature");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getMyPayments(Principal principal, Pageable pageable) {


        UUID userId = UUID.fromString(principal.getName());

        var page = paymentRepository.findByUserId(userId, pageable);

        return new PageResponse<>(
                page.getContent().stream().map(mapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    // ================= ADMIN =================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getAllPayments(Pageable pageable) {

        var page = paymentRepository.findAll(pageable);

        return new PageResponse<>(
                page.getContent().stream().map(mapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->  new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,"Payment is not found with: "+paymentId));

        return mapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatus status) {

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,"Payment is not found with: "+paymentId));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        payment.setStatus(status);

        return mapper.toResponse(payment);
    }


    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {

        validator.validateWebhook(payload, signature);

        boolean isValid = razorpayService.verifyWebhookSignature(payload, signature);

        if (!isValid) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }

        JSONObject event;

        try {
            event = new JSONObject(payload);
        } catch (Exception e) {
            log.error("Invalid webhook payload: {}", payload, e);
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_PAYLOAD);
        }

        String eventId = event.getString("id");
        String eventType = event.getString("event");

        // ✅ Duplicate check
        if (webHookEventRepository.findByEventId(eventId).isPresent()) {
            log.info("Duplicate webhook received: {}", eventId);
            return;
        }

        JSONObject paymentJson;

        try {
            paymentJson = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");
        } catch (Exception e) {
            log.error("Malformed webhook structure: {}", payload, e);
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_PAYLOAD);
        }

        String razorpayOrderId = paymentJson.getString("order_id");
        String paymentId = paymentJson.getString("id");

        // ✅ Save webhook event
        WebhookEventEntity webhookEvent = new WebhookEventEntity();
        webhookEvent.setEventId(eventId);
        webhookEvent.setEventType(eventType);
        webhookEvent.setPayload(payload);
        webhookEvent.setResourceId(razorpayOrderId);

        webHookEventRepository.save(webhookEvent);

        try {

            webhookEvent.setStatus(WebhookStatus.PROCESSING);

            PaymentEntity payment = paymentRepository
                    .findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

            if (payment.getStatus() == PaymentStatus.SUCCESS ||
                    payment.getStatus() == PaymentStatus.FAILED) {
                webhookEvent.setStatus(WebhookStatus.PROCESSED);
                return;
            }

            if ("payment.captured".equals(eventType)) {

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(paymentId);
                if (payment.getLastEventId() == null) {
                    log.error("Missing eventId for order {}", payment.getOrderId());
                    throw new BusinessException(ErrorCode.INVALID_REQUEST);
                }
                producer.sendPaymentStatus(
                        payment.getLastEventId(),
                        payment.getOrderId(),
                        EventStatus.PROCESSED,
                        "Payment is successfully",
                        paymentId
                );

            } else if ("payment.failed".equals(eventType)) {

                payment.setStatus(PaymentStatus.FAILED);

                if (payment.getLastEventId() == null) {
                    log.error("Missing eventId for order {}", payment.getOrderId());
                    throw new BusinessException(ErrorCode.INVALID_REQUEST);
                }
                producer.sendPaymentStatus(
                        payment.getLastEventId(),
                        payment.getOrderId(),
                        EventStatus.FAILED,
                        "Payment Failed",
                        null
                );
            }

            paymentRepository.save(payment);

            webhookEvent.setStatus(WebhookStatus.PROCESSED);
            webhookEvent.setProcessedAt(Instant.now());

        } catch (Exception e) {

            log.error("Webhook processing failed: {}", eventId, e);

            webhookEvent.setStatus(WebhookStatus.FAILED);
            webhookEvent.setErrorMessage(e.getMessage());
        }

        webHookEventRepository.save(webhookEvent);
    }
}