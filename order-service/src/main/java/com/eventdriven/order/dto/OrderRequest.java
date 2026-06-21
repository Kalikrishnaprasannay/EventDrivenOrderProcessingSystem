package com.eventdriven.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {

    @Schema(description = "Unique customer identifier", example = "USER_123")
    @NotBlank(message = "customerId is required")
    private String customerId;

    @Schema(description = "Order total amount", example = "150.00")
    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;
}
