package com.eventdriven.notification.listener;

import com.eventdriven.notification.event.PaymentProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationListener {

    @KafkaListener(topics = "payment-topic", groupId = "notification-group")
    public void handlePaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("==========================================================");
        log.info("NOTIFICATION DISPATCHED");
        log.info("Order ID: {}", event.getOrderId());
        log.info("Customer ID: {}", event.getCustomerId());
        log.info("Amount: {}", event.getAmount());
        log.info("Payment Status: {}", event.getPaymentStatus());
        log.info("==========================================================");
    }
}
