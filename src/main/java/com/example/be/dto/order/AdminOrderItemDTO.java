package com.example.be.dto.order;

import java.math.BigDecimal;

public record AdminOrderItemDTO(
        Long productId,
        String productName,
        String thumbnailUrl,
        String fileFormat,
        Integer quantity,
        BigDecimal unitPrice
) {}
