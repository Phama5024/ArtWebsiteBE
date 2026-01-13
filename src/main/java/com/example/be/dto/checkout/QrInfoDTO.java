package com.example.be.dto.checkout;


import java.math.BigDecimal;

public record QrInfoDTO(
        Long orderId,
        String orderCode,
        String transactionId,
        BigDecimal amount,
        String qrValue
) {}