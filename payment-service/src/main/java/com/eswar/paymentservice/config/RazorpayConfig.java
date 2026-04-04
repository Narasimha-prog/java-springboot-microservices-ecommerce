package com.eswar.paymentservice.config;

import com.eswar.paymentservice.test.FakePaymentGenerator;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {
    @Value("${razorpay.key}")
    private String key;

    @Value("${razorpay.secret}")
    private String secret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(key, secret);
    }

    @Bean
    CommandLineRunner runner(FakePaymentGenerator generator) {
        return args -> {
            var fake = generator.generate();
            System.out.println("signature for payment: "+fake.getWebSignature());
            System.out.println("OrderId: " + fake.getOrderId());
            System.out.println("PaymentId: " + fake.getPaymentId());
            System.out.println("Signature: " + fake.getSignature());
        };
    }
}
