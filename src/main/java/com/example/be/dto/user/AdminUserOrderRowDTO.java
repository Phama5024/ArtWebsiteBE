package com.example.be.dto.user;

import com.example.be.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminUserOrderRowDTO(
        Long orderId,
        String orderCode,
        LocalDateTime createdAt,
        BigDecimal totalAmount,
        OrderStatus status
) {}
