package com.eswar.paymentservice.validation;

import com.eswar.paymentservice.dto.PaymentVerifyRequest;
import com.eswar.paymentservice.exception.BusinessException;
import com.eswar.paymentservice.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentValidator {

    public void validateCreatePayment(UUID orderId) {
        if (orderId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    public void validateVerifyPayment(PaymentVerifyRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (isBlank(request.razorpayOrderId()) || !request.razorpayOrderId().startsWith("order_")){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (isBlank(request.razorpayPaymentId() )|| request.razorpayPaymentId().startsWith("pay_")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (isBlank(request.razorpaySignature())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    public void validateWebhook(String payload, String signature) {
        if (isBlank(payload)) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_PAYLOAD);
        }

        if (isBlank(signature)) {
            throw new BusinessException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}