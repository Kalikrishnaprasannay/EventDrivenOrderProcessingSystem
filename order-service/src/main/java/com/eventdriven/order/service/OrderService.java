package com.eventdriven.order.service;

import com.eventdriven.order.dto.OrderRequest;
import com.eventdriven.order.event.OrderCreatedEvent;
import com.eventdriven.order.model.Order;
import com.eventdriven.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_TOPIC = "order-topic";

    @Transactional
    public Order placeOrder(OrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .status("PENDING")
                .build();

        order.generateOrderId();
        orderRepository.save(order);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .amount(order.getAmount())
                .status(order.getStatus())
                .build();

        // Publish to Kafka
        kafkaTemplate.send(ORDER_TOPIC, order.getOrderId(), event);
        log.info("Published OrderCreatedEvent for Order ID: {}", order.getOrderId());

        return order;
    }
}
