package com.example.be.controller.checkout;

import com.example.be.service.checkout.paypal.PaypalFlowService;
import com.example.be.service.checkout.paypal.PaypalHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/paypal")
@RequiredArgsConstructor
public class PaypalWebhookController {

    private final PaypalHttpClient paypalHttpClient;
    private final PaypalFlowService paypalFlowService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader("PAYPAL-AUTH-ALGO") String authAlgo,
            @RequestHeader("PAYPAL-CERT-URL") String certUrl,
            @RequestHeader("PAYPAL-TRANSMISSION-ID") String transmissionId,
            @RequestHeader("PAYPAL-TRANSMISSION-SIG") String transmissionSig,
            @RequestHeader("PAYPAL-TRANSMISSION-TIME") String transmissionTime,
            @RequestBody String body
    ) {
        String token = paypalHttpClient.getAccessToken();
        boolean ok = paypalHttpClient.verifyWebhookSignature(
                token, authAlgo, certUrl, transmissionId, transmissionSig, transmissionTime, body
        );
        if (!ok) return ResponseEntity.badRequest().build();

        paypalFlowService.handleWebhook(body);
        return ResponseEntity.ok().build();
    }
}
