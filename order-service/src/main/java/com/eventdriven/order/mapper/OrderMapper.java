package com.eventdriven.order.mapper;

import com.eventdriven.order.dto.OrderResponse;
import com.eventdriven.order.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .amount(order.getAmount())
                .status(order.getStatus())
                .build();
    }
}
