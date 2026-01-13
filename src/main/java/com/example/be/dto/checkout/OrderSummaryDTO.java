package com.example.be.dto.checkout;

import com.example.be.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDTO(
        Long orderId,
        String orderCode,
        BigDecimal totalAmount,
        OrderStatus status,
        PaymentSummaryDTO payment,
        LocalDateTime createdAt
) {}
