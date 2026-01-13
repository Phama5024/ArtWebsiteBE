package com.example.be.dto.checkout;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequestDTO(
        @NotBlank String receiverName,
        @NotBlank String receiverPhone,
        @NotBlank String shippingAddress,
        String paymentMethod
) {}
