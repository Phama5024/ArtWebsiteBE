package com.example.be.dto.order;

import com.example.be.enums.OrderStatus;

public record AdminOrderUpdateRequestDTO(
        OrderStatus status,
        String receiverName,
        String receiverPhone,
        String shippingAddress
) {}