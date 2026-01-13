package com.example.be.dto.order;

import java.math.BigDecimal;

public record MyOrderItemDTO(
        Long itemId,
        Long productId,
        String productName,
        String thumbnailUrl,
        String fileFormat,
        Integer quantity,
        BigDecimal unitPrice,
        String downloadName
) {}
