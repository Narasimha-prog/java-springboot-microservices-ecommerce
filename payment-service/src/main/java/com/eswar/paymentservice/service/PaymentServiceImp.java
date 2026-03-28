package com.eswar.paymentservice.service;

import com.eswar.paymentservice.constatns.PaymentStatus;
import com.eswar.paymentservice.dto.*;
import com.eswar.paymentservice.entity.PaymentEntity;
import com.eswar.paymentservice.kafka.constants.EventStatus;
import com.eswar.paymentservice.kafka.events.OrderCreatedEvent;
import com.eswar.paymentservice.kafka.producer.PaymentEventProducer;
import com.eswar.paymentservice.mapper.IPaymentMapper;
import com.eswar.paymentservice.repository.IPaymentRepository;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements IPaymentService {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.currency:INR}")
    private String currency;

    private final IPaymentRepository paymentRepository;
    private final PaymentEventProducer producer;
    private final RazorpayService razorpayService;
    private final IPaymentMapper mapper;

    // ================= HELPER =================

    private void validateOwnership(PaymentEntity payment, String userId) {
        if (!payment.getUserId().toString().equals(userId)) {
            throw new RuntimeException("Access Denied");
        }
    }

    // ================= KAFKA =================

    @Override
    @Transactional
    public void processPayment(OrderCreatedEvent event) {

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(event.orderId());
        payment.setUserId(event.customerId());
        payment.setAmount(event.totalAmount());
        payment.setStatus(PaymentStatus.INITIATED);

        paymentRepository.save(payment);
    }

    // ================= USER =================

    @Override
    @Transactional
    public PaymentCreateResponse createPayment(UUID orderId, Principal principal) throws RazorpayException {

        PaymentEntity payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        validateOwnership(payment, principal.getName());

        // ✅ Idempotency check
        if (payment.getTransactionId() != null) {
            return new PaymentCreateResponse(
                    payment.getTransactionId(),
                    razorpayKey,
                    currency,
                    payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue()
            );
        }

        String razorpayOrderId = razorpayService.createOrder(payment.getAmount(), currency);

        payment.setTransactionId(razorpayOrderId);

        return new PaymentCreateResponse(
                razorpayOrderId,
                razorpayKey,
                currency,
                payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue()
        );
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(PaymentVerifyRequest request, Principal principal) {

        PaymentEntity payment = paymentRepository
                .findByTransactionId(request.razorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        validateOwnership(payment, principal.getName());

        // ✅ Prevent duplicate verification
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return new PaymentResponse("SUCCESS", "Already verified");
        }

        boolean isValid = razorpayService.verifySignature(
                request.razorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (isValid) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentId(request.razorpayPaymentId());

            producer.sendPaymentStatus(payment.getOrderId(), EventStatus.SUCCESS,"Payment Successful",request.razorpayPaymentId());

            return new PaymentResponse("SUCCESS", "Payment verified successfully");
        } else {
            payment.setStatus(PaymentStatus.FAILED);

            producer.sendPaymentStatus(payment.getOrderId(),EventStatus.FAILED,"Payment Failed or Declined ",null);

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
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return mapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(UUID paymentId, PaymentStatus status) {

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Cannot modify completed payment");
        }

        payment.setStatus(status);

        return mapper.toResponse(payment);
    }
}