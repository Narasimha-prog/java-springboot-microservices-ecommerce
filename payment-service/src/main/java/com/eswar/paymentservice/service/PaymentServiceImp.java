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
import com.eswar.paymentservice.kafka.service.PaymentKafkaService;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
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
    private final PaymentKafkaService paymentKafkaService;


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
            self.processPaymentInitialization(event, eventEntity);
            log.info("Successfully updated order {} to status {}", event.orderId(),eventEntity.getStatus());
        } catch (Exception ex) {
            // Step D: Record Failure (Independent Transaction)
            self.recordEventFailure(eventEntity, ex.getMessage());
            log.warn("Error while handle event from orderCreated-event: ",ex);
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
    public WebhookEventEntity recordWebhookReceipt(UUID orderId, String eventId, String payload, JSONObject json) {
        String currentType = json.getString("event");

        // 1. Still do the check (to save time if it's already committed)
        Optional<WebhookEventEntity> existing = webHookEventRepository.findByEventIdAndEventType(eventId, currentType);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. Try to save, but be prepared for a race condition failure
        try {
            WebhookEventEntity entity = new WebhookEventEntity();
            entity.setOrderId(orderId);
            entity.setEventId(eventId);
            entity.setPayload(payload);
            entity.setEventType(currentType);
            entity.setResourceId(eventId);
            entity.setStatus(WebhookStatus.RECEIVED);

            return webHookEventRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            // 3. If we hit a duplicate key error, someone else just saved it.
            // Fetch that one and return it instead of crashing.
            return webHookEventRepository.findByEventIdAndEventType(eventId, currentType)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED,"Concurrency error: Event vanished"));
        }
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processWebhookBusinessLogic(JSONObject eventJson, WebhookEventEntity webhookEvent) {
        // Extract Order ID from payload
        String razorpayOrderId = eventJson.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getString("order_id");

        // 1. Find the Business Entity
        PaymentEntity payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 2. 🔥 THE BRIDGE: Find the original Kafka Event ID from our Event Ledger
        EventEntity originalEvent = eventRepository.findFirstByOrderIdAndEventTypeOrderByCreatedAtDesc(
                        payment.getOrderId(), EventType.PAYMENT)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        String eventType = eventJson.getString("event");

        if ("payment.authorized".equals(eventType)) {
            // Just update internal status to PENDING/AUTHORIZED
            payment.setStatus(PaymentStatus.PENDING);
            webhookEvent.setStatus(WebhookStatus.PROCESSING);
            log.info("Payment authorized for order {}. Waiting for capture.", razorpayOrderId);
        }
        else if ("payment.captured".equals(eventType)) {
            // This is the trigger for Kafka and Order Success
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                log.info("Payment already marked as SUCCESS. Skipping Kafka.");
                return;
            }
            payment.setStatus(PaymentStatus.SUCCESS);
            webhookEvent.setStatus(WebhookStatus.PROCESSED);
            paymentKafkaService.sendOrderStatusEvent(originalEvent, EventStatus.PROCESSED, "Payment is success", payment.getRazorpayPaymentId());
        }
        else if ("payment.failed".equals(eventType)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentKafkaService.sendOrderStatusEvent(originalEvent, EventStatus.FAILED, "Payment is failed", null);
        }

        // 3. Update Statuses
        paymentRepository.save(payment);
        webhookEvent.setProcessedAt(Instant.now());
        webHookEventRepository.save(webhookEvent);
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
                    p.setCurrency("INR");
                    return p;
                });

        // Update status and link to the current event
        payment.setStatus(PaymentStatus.INITIATED);
        paymentRepository.save(payment);

        // Mark Event as Processed
        entity.setStatus(EventStatus.PROCESSED);
        eventRepository.save(entity);
    }

    // ================= USER =================

    @Override
    @Transactional
    //to get order id for frontend
    public PaymentCreateResponse createPayment(UUID orderId, Principal principal) throws RazorpayException {
        //validate UUID basic
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
        // 1. Validate & Verify (No DB state yet)
        validator.validateWebhook(payload, signature);
        if (!razorpayService.verifyWebhookSignature(payload, signature)) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }

        JSONObject eventJson = new JSONObject(payload);

        // FIX 1: Extract the Payment ID to use as your reference instead of a missing root ID
        String razorpayPaymentId = eventJson.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getString("id"); // This is "pay_SgwxwW4T7a3pIj"

// FIX 2: Correct way to get the Order ID (already matches your current nested logic)
        String razorpayOrderId = eventJson.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getString("order_id"); // This is "order_SgwxUPKIr8EnjP"

        PaymentEntity payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 2. Step A: Record Webhook Receipt (Independent Transaction)
        WebhookEventEntity webhookEvent = self.recordWebhookReceipt(payment.getOrderId(),razorpayPaymentId, payload, eventJson);

        // 3. Step B: Skip if already done
        if (webhookEvent.getStatus() == WebhookStatus.PROCESSED) {
            log.info("Webhook {} already processed. Skipping.", razorpayPaymentId);
            return;
        }

        try {
            // 4. Step C: Apply Business Logic & Notify Kafka (Independent Transaction)
            self.processWebhookBusinessLogic(eventJson, webhookEvent);
        } catch (Exception ex) {
            // 5. Step D: Record failure
            eventRepository.findFirstByOrderIdAndEventTypeOrderByCreatedAtDesc(
                            webhookEvent.getOrderId(), EventType.PAYMENT)
                    .ifPresent(kafkaEvent -> self.recordEventFailure(kafkaEvent, ex.getMessage()));
            self.recordWebhookFailure(webhookEvent, ex.getMessage());
            throw ex; // Re-throw to trigger Razorpay retry
        }


    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEventFailure(EventEntity entity, String error) {
        log.error("❌ Recording failure for Event ID: {}. Error: {}", entity.getEventId(), error);

        // 1. Update the 'Communication Ledger'
        entity.setStatus(EventStatus.FAILED);
        entity.setErrorMessage(error);

        // 2. Commit the failure status to the DB
        eventRepository.save(entity);

        log.info("Failure state successfully persisted for Event {}.", entity.getEventId());
    }



    // Recorder for Webhook Ledger
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWebhookFailure(WebhookEventEntity webhookEvent, String error) {
        log.error("❌ Recording failure for WebhookEntity Event ID: {}. Error: {}", webhookEvent.getEventId(), error);
        webhookEvent.setStatus(WebhookStatus.FAILED);
        webhookEvent.setErrorMessage(error);
        webHookEventRepository.save(webhookEvent);
    }
}