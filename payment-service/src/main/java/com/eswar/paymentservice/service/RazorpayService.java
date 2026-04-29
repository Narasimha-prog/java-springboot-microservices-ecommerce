package com.eswar.paymentservice.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("dev")
public class RazorpayService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.secret}")
    private String secret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    /**
     * Step 1: Create an order in Razorpay systems.
     */
    public String createOrder(BigDecimal amount, String currency) throws RazorpayException {
        JSONObject options = new JSONObject();
        // Razorpay expects amount in subunits (e.g., Paise for INR)
        options.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
        options.put("currency", currency);
        options.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = razorpayClient.orders.create(options);
        return order.get("id");
    }

    /**
     * Step 2: Verify the payment signature returned by the frontend Checkout UI.
     */
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String generatedSignature = generateHmacSha256(payload, secret);

            // Use MessageDigest.isEqual for constant-time comparison (prevents timing attacks)
            return MessageDigest.isEqual(
                    generatedSignature.getBytes(),
                    signature.getBytes()
            );
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    /**
     * Step 3: Verify Webhook authenticity.
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        String generated = generateHmacSha256(payload, webhookSecret);
        return MessageDigest.isEqual(generated.getBytes(), signature.getBytes());
    }

    private String generateHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }
}