package com.example.be.service.checkout;


import com.example.be.dto.checkout.CheckoutRequestDTO;
import com.example.be.dto.checkout.OrderSummaryDTO;

public interface CheckoutService {
    OrderSummaryDTO checkoutFromCart(String email, CheckoutRequestDTO req);
    OrderSummaryDTO getOrderSummary(String email, Long orderId);
    OrderSummaryDTO checkoutFromCommission(String email, Long commissionRequestId, CheckoutRequestDTO req);

}
