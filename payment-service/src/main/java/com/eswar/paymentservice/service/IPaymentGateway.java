package com.eswar.paymentservice.service;

import com.razorpay.RazorpayException;

import java.math.BigDecimal;

public interface IPaymentGateway {

    String createOrder(BigDecimal amount, String currency) throws RazorpayException;

    boolean verifySignature(String orderId, String paymentId, String signature);

    boolean verifyWebhookSignature(String payload, String signature);

    String getGatewayName();
}
