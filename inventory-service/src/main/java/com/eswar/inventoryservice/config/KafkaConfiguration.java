package com.eswar.inventoryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfiguration {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        // This part handles the "Configuration" (Where to send failures)
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);

        // This part handles the "Retry" (How many times to try)
        FixedBackOff backOff = new FixedBackOff(1000L, 2);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // 🔹 CRITICAL: This tells Spring to commit the offset AFTER
        // the message is successfully moved to the DLQ.
        handler.setAckAfterHandle(true);

        return handler;
    }
}
