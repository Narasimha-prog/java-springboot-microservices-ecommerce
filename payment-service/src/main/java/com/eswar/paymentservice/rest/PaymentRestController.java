package com.eswar.paymentservice.rest;

import com.eswar.paymentservice.constatns.PaymentStatus;
import com.eswar.paymentservice.dto.PageResponse;
import com.eswar.paymentservice.dto.PaymentCreateResponse;
import com.eswar.paymentservice.dto.PaymentResponse;
import com.eswar.paymentservice.dto.PaymentVerifyRequest;
import com.eswar.paymentservice.service.IPaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentRestController {

    private final IPaymentService paymentService;

    // ✅ USER → Create payment (Razorpay order)
    @PostMapping("/create/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentCreateResponse> createPayment(
            @PathVariable UUID orderId,
            Principal principal) throws RazorpayException {

        return ResponseEntity.ok(paymentService.createPayment(orderId, principal));
    }

    // ✅ USER → Verify payment
    @PostMapping("/verify")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @RequestBody PaymentVerifyRequest request,
            Principal principal) {

        return ResponseEntity.ok(paymentService.verifyPayment(request, principal));
    }

    // ✅ USER → Get my payments
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PageResponse<PaymentResponse>> getMyPayments(
            @ParameterObject Pageable pageable,
            Principal principal) {

        return ResponseEntity.ok(paymentService.getMyPayments(principal, pageable));
    }

    // ================= ADMIN =================

    // ✅ ADMIN → Get all payments
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<PaymentResponse>> getAllPayments(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    // ✅ ADMIN → Get payment by ID
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    // ✅ ADMIN → Update status manually
    @PutMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> updateStatus(
            @PathVariable UUID paymentId,
            @RequestParam PaymentStatus status) {

        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }
}
