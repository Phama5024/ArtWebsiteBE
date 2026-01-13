package com.example.be.controller.checkout;

import com.example.be.dto.checkout.CheckoutRequestDTO;
import com.example.be.dto.checkout.OrderSummaryDTO;
import com.example.be.service.checkout.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/checkout")
    public OrderSummaryDTO checkout(Authentication authentication,
                                    @Valid @RequestBody CheckoutRequestDTO req) {
        String email = authentication.getName();
        return checkoutService.checkoutFromCart(email, req);
    }

    @GetMapping("/{orderId}")
    public OrderSummaryDTO summary(Authentication authentication,
                                   @PathVariable Long orderId) {
        String email = authentication.getName();
        return checkoutService.getOrderSummary(email, orderId);
    }
}
