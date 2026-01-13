package com.example.be.controller.checkout;

import com.example.be.dto.checkout.QrInfoDTO;
import com.example.be.service.checkout.PaymentFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentFlowController {

    private final PaymentFlowService paymentFlowService;

    @GetMapping("/{orderId}/qr")
    public QrInfoDTO qr(Authentication authentication, @PathVariable Long orderId) {
        String email = authentication.getName();
        return paymentFlowService.getQrInfo(email, orderId);
    }

    @PostMapping("/{orderId}/confirm")
    public void confirm(Authentication authentication, @PathVariable Long orderId) {
        String email = authentication.getName();
        paymentFlowService.confirmPaid(email, orderId);
    }
}
