package com.eventdriven.order.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderRequest {
    private String customerId;
    private BigDecimal amount;
}
