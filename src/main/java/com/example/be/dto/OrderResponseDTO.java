package com.example.be.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {

    private Long orderId;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;

    private List<OrderItemDTO> items;
}
