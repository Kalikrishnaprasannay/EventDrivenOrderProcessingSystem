package com.eventdriven.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentProcessedEvent {
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String paymentStatus;
}
