package com.eswar.paymentservice.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RazorpayService {

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.secret}")
    private String secret;

    public String createOrder(BigDecimal amount,String currency) throws RazorpayException {

        JSONObject options = new JSONObject();
        options.put("amount", amount.multiply(BigDecimal.valueOf(100))); // paise
        options.put("currency", currency);
        options.put("receipt", "order_rcptid_" + System.currentTimeMillis());

        Order order = razorpayClient.orders.create(options);

        return order.get("id");
    }

    // ✅ Verify Signature (CRITICAL)
    public boolean verifySignature(String orderId, String paymentId, String signature) {

        try {
            String payload = orderId + "|" + paymentId;

            String generatedSignature = hmacSHA256(payload, secret);

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            throw new RuntimeException("Error verifying payment signature", e);
        }
    }

    // 🔐 HMAC SHA256
    private String hmacSHA256(String data, String secret) {

        try {
            //create engine with alg
            Mac mac = Mac.getInstance("HmacSHA256");

            //to fit in that engine
            SecretKeySpec secretKey =
                    new SecretKeySpec(secret.getBytes(), "HmacSHA256");

            //load that key in that engine
            mac.init(secretKey);

            //get finger prints (key)
            byte[] rawHmac = mac.doFinal(data.getBytes());

            return bytesToHex(rawHmac);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }
        return hex.toString();
    }
}