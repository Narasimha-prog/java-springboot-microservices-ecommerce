package com.eswar.paymentservice.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class FakePaymentGenerator {

    @Value("${razorpay.secret}")
    public  String secret;

    @Value("${razorpay.webhook.secret}") // For Webhook logic
    private String webhookSecret;

    public FakePayment generate() {

        String orderId = "order_SZX3h6WRDE09tU";
        String paymentId = "pay_" + UUID.randomUUID();
        String payload= """
                {
                  "account_id" : "acc_REd778sHXikpGt",
                  "contains" : [ "payment" ],
                  "created_at" : 1775331186,
                  "entity" : "event",
                  "event" : "payment.authorized",
                  "payload" : {
                    "payment" : {
                      "entity" : {
                        "acquirer_data" : {
                          "auth_code" : null
                        },
                        "amount" : 100,
                        "amount_refunded" : 0,
                        "amount_transferred" : 0,
                        "bank" : null,
                        "captured" : false,
                        "card" : {
                          "emi" : false,
                          "entity" : "card",
                          "id" : "card_SZX4EcjMdMpaP0",
                          "iin" : "999999",
                          "international" : false,
                          "issuer" : "DCBL",
                          "last4" : "1007",
                          "name" : "",
                          "network" : "Visa",
                          "sub_type" : "consumer",
                          "type" : "debit"
                        },
                        "card_id" : "card_SZX4EcjMdMpaP0",
                        "contact" : "+917878787878",
                        "created_at" : 1775331180,
                        "currency" : "INR",
                        "description" : "#SZX3EZPxnNnCIa",
                        "email" : "void@razorpay.com",
                        "entity" : "payment",
                        "error_code" : null,
                        "error_description" : null,
                        "error_reason" : null,
                        "error_source" : null,
                        "error_step" : null,
                        "fee" : 0,
                        "id" : "pay_SZX4EcjMdMpaP0",
                        "international" : false,
                        "invoice_id" : null,
                        "method" : "card",
                        "notes" : [ ],
                        "order_id" : "order_SZX3h6WRDE09tU",
                        "refund_status" : null,
                        "status" : "authorized",
                        "tax" : 0,
                        "vpa" : null,
                        "wallet" : null
                      }
                    }
                  }
                }
                
                """;
        String signature = generateSignature(orderId, paymentId);
        String websignature=generateSignature(payload,webhookSecret);
        return new FakePayment(orderId, paymentId, signature,websignature);
    }

    private String generateSignature(String orderId, String paymentId) {
        try {
            String payload = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(key);

            byte[] raw = mac.doFinal(payload.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : raw) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) hex.append('0');
                hex.append(s);
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // --- NEW METHOD FOR WEBHOOK ---
    // Pass the content of your dummy.txt here
    public String generateWebhookSignature(String rawJsonBody) {
       return  hmacSHA256(rawJsonBody, webhookSecret);
    }

    // REUSABLE HMAC METHOD
    private String hmacSHA256(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(key);
            byte[] raw = mac.doFinal(data.getBytes());

            // Using modern HexFormat if you're on Java 17+
            // return java.util.HexFormat.of().formatHex(raw);

            StringBuilder hex = new StringBuilder();
            for (byte b : raw) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) hex.append('0');
                hex.append(s);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }
    @Data
    @AllArgsConstructor
    public static class FakePayment {
        private String orderId;
        private String paymentId;
        private String signature;
        private String webSignature;
    }
}