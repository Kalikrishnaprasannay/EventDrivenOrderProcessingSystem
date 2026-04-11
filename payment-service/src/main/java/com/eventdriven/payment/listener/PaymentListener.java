package com.eventdriven.payment.listener;

import com.eventdriven.payment.event.OrderCreatedEvent;
import com.eventdriven.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-topic", groupId = "payment-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for Order ID: {}", event.getOrderId());
        paymentService.processPayment(event);
    }
    
    @KafkaListener(topics = "order-topic.DLT", groupId = "payment-dlq-group")
    public void handleOrderCreatedDeadLetter(OrderCreatedEvent event) {
        log.error("ALERT: Dead Letter Queue processing for Order ID: {}. Requires manual intervention.", event.getOrderId());
    }
}
