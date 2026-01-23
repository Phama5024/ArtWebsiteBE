package com.example.be.controller.checkout;

import com.example.be.service.checkout.paypal.PaypalFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/paypal")
@RequiredArgsConstructor
public class PaypalController {

    private final PaypalFlowService paypalFlowService;

    @GetMapping("/{orderId}/init")
    public Map<String, String> init(Authentication auth, @PathVariable Long orderId) {
        String email = auth.getName();
        String approveUrl = paypalFlowService.init(email, orderId);
        return Map.of("approveUrl", approveUrl);
    }

    @PostMapping("/{orderId}/capture")
    public void capture(Authentication auth, @PathVariable Long orderId) {
        String email = auth.getName();
        paypalFlowService.capture(email, orderId);
    }
}
