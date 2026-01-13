package com.example.be.dto.order;

import com.example.be.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderDetailDTO(
        Long id,
        String orderCode,
        LocalDateTime createdAt,
        BigDecimal totalAmount,
        OrderStatus status,
        String receiverName,
        String receiverPhone,
        String shippingAddress,

        String invoiceCode,
        String paymentMethod,
        String paymentStatus,
        LocalDateTime paidAt,

        List<AdminOrderItemDTO> items
) {}
