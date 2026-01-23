package com.example.be.controller.checkout;

import com.example.be.service.checkout.vnpay.VnpayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class VnpayController {

    private final VnpayPaymentService vnpayPaymentService;

    private static String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (first.startsWith("::ffff:")) return first.substring(7);
            return first;
        }

        String realIp = req.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            if (realIp.startsWith("::ffff:")) return realIp.substring(7);
            return realIp.trim();
        }

        String ip = req.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) return "127.0.0.1";
        if (ip != null && ip.startsWith("::ffff:")) return ip.substring(7);
        return ip;
    }

    @GetMapping("/{orderId}/init")
    public Map<String, String> init(Authentication auth,
                                    @PathVariable Long orderId,
                                    HttpServletRequest request) throws UnsupportedEncodingException {
        String email = auth.getName();
        String ip = getClientIp(request);
        String paymentUrl = vnpayPaymentService.init(email, orderId, ip);
        return Map.of("paymentUrl", paymentUrl);
    }

    @GetMapping("/return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> params) {
        log.info("VNPay return callback: {}", params);

        try {
            vnpayPaymentService.handleCallback(params);

            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");

            if ("00".equals(responseCode)) {
                log.info("Payment success: {}", txnRef);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:5173/payment/success?txnRef=" + txnRef))
                        .build();
            } else {
                log.warn("Payment failed: {}, code: {}", txnRef, responseCode);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:5173/payment/failed?txnRef=" + txnRef + "&code=" + responseCode))
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in return callback", e);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173/payment/error"))
                    .build();
        }
    }


    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(@RequestParam Map<String, String> params) {
        log.info("VNPay IPN callback: {}", params);

        Map<String, String> response = new HashMap<>();

        try {
            vnpayPaymentService.handleCallback(params);

            String responseCode = params.get("vnp_ResponseCode");

            if ("00".equals(responseCode)) {
                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            } else {
                response.put("RspCode", "01");
                response.put("Message", "Transaction failed");
            }
        } catch (Exception e) {
            log.error("Error in IPN callback", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
        }

        return ResponseEntity.ok(response);
    }
}