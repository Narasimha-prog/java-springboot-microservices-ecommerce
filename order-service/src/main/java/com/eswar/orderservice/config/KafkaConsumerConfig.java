package com.eswar.orderservice.config;

import com.eswar.orderservice.kafka.event.OrderStatusEvent;
import com.eswar.orderservice.kafka.event.StockRejectedEvent;
import com.eswar.orderservice.kafka.event.StockReservedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;


import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, OrderStatusEvent> orderStatusConsumerFactory() {



        return new DefaultKafkaConsumerFactory<>(
                commonProps(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(
                        new JacksonJsonDeserializer<>(OrderStatusEvent.class, false)
                )
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderStatusEvent> orderListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderStatusEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderStatusConsumerFactory());
        return factory;
    }

    private Map<String, Object> commonProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}
