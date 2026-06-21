package com.eventdriven.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderResponse {

    @Schema(description = "System-generated order identifier", example = "f3b9d1ec-44f5-4bdd-9e9f-2fda5f2b1a3e")
    private String orderId;

    @Schema(description = "Customer identifier", example = "USER_123")
    private String customerId;

    @Schema(description = "Total order amount", example = "150.00")
    private BigDecimal amount;

    @Schema(description = "Order processing status", example = "PENDING")
    private String status;
}
