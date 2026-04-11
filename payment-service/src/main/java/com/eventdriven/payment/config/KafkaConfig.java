package com.eventdriven.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder.name("payment-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderDlqTopic() {
        return TopicBuilder.name("order-topic.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Configures a DefaultErrorHandler with a FixedBackOff (3 attempts, 2 seconds apart).
     * If all attempts fail, the message is sent to a Dead Letter Topic (.DLT).
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        FixedBackOff backOff = new FixedBackOff(2000L, 2); // 2 seconds backoff, max 2 retries (total 3 attempts)
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
