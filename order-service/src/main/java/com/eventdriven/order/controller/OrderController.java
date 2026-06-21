package com.eventdriven.order.controller;

import com.eventdriven.order.dto.OrderRequest;
import com.eventdriven.order.dto.OrderResponse;
import com.eventdriven.order.mapper.OrderMapper;
import com.eventdriven.order.model.Order;
import com.eventdriven.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "Endpoints for order creation and retrieval")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        Order order = orderService.placeOrder(request);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by orderId")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return orderService.findByOrderId(orderId)
                .map(orderMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all orders")
    public ResponseEntity<List<OrderResponse>> listOrders() {
        List<OrderResponse> response = orderService.listOrders().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
