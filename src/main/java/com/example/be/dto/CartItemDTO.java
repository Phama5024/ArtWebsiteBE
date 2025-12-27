package com.example.be.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
