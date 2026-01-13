package com.example.be.dto.order;

import com.example.be.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyOrderRowDTO(
        Long id,
        String orderCode,
        LocalDateTime createdAt,
        BigDecimal totalAmount,
        OrderStatus status,
        String paymentMethod,
        String paymentStatus
) {}
