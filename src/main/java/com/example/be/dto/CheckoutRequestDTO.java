package com.example.be.dto;

import lombok.Data;

@Data
public class CheckoutRequestDTO {

    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;
    private String paymentMethod;
}
