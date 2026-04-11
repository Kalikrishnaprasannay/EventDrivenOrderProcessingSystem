package com.eventdriven.payment.service;

import com.eventdriven.payment.event.OrderCreatedEvent;
import com.eventdriven.payment.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_TOPIC = "payment-topic";
    private static final String IDEMPOTENCY_PREFIX = "payment:processed:";

    public void processPayment(OrderCreatedEvent event) {
        String orderId = event.getOrderId();
        String idempotencyKey = IDEMPOTENCY_PREFIX + orderId;

        // Idempotency Check using Redis SETNX (setIfAbsent)
        Boolean isProcessed = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "PROCESSING", Duration.ofHours(24));

        if (Boolean.FALSE.equals(isProcessed)) {
            log.warn("Duplicate processing attempt detected for Order ID: {}. Skipping.", orderId);
            return;
        }

        try {
            log.info("Processing payment for Order ID: {} with amount: {}", orderId, event.getAmount());

            // Simulate failure for specific amounts (for Testing Retry/DLQ)
            if (event.getAmount() != null && event.getAmount().doubleValue() == 999.99) {
                log.error("Simulated payment processing failure for Order ID: {}", orderId);
                throw new RuntimeException("Simulated payment failure (e.g. insufficient funds, external API down)");
            }

            // Simulate processing delay
            Thread.sleep(100);

            PaymentProcessedEvent processedEvent = PaymentProcessedEvent.builder()
                    .orderId(orderId)
                    .customerId(event.getCustomerId())
                    .amount(event.getAmount())
                    .paymentStatus("SUCCESS")
                    .build();

            // Publish success event
            kafkaTemplate.send(PAYMENT_TOPIC, orderId, processedEvent);
            log.info("Payment processed successfully for Order ID: {}. Published event to payment-topic", orderId);

            // Mark as SUCCESS in Redis
            redisTemplate.opsForValue().set(idempotencyKey, "SUCCESS", Duration.ofHours(24));

        } catch (Exception e) {
            log.error("Payment processing failed for Order ID: {}. Error: {}", orderId, e.getMessage());
            // Remove the processing lock so retries can try again
            redisTemplate.delete(idempotencyKey);
            throw new RuntimeException(e); // Throwing will trigger the Kafka error handler (Retries + DLQ)
        }
    }
}
