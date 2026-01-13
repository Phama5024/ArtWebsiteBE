package com.example.be.dto.order;

import com.example.be.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderRowDTO(
        Long id,
        String orderCode,
        String invoiceCode,
        String paymentMethod,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt
) {}
