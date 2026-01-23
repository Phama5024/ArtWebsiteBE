package com.example.be.service.checkout.vnpay;

import com.example.be.entity.Order;
import com.example.be.entity.Payment;
import com.example.be.enums.NotificationType;
import com.example.be.enums.OrderStatus;
import com.example.be.repository.order.OrderRepository;
import com.example.be.repository.payment.PaymentRepository;
import com.example.be.service.cart.CartService;
import com.example.be.service.notifications.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class VnpayPaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final VnpaySigner signer;

    private final CartService cartService;
    private final NotificationService notificationService;

    @Value("${vnpay.tmn-code}") private String tmnCode;
    @Value("${vnpay.hash-secret}") private String hashSecret;
    @Value("${vnpay.pay-url}") private String payUrl;
    @Value("${vnpay.return-url}") private String returnUrl;
    @Value("${vnpay.version}") private String version;


    private static String vnpEncode(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    public String init(String email, Long orderId, String clientIp) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!o.getUser().getEmail().equalsIgnoreCase(email))
            throw new RuntimeException("Forbidden");

        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        p.setPaymentMethod("VNPAY");
        if (p.getPaymentStatus() == null) p.setPaymentStatus("PENDING");
        paymentRepository.save(p);

        long amount = o.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        var zone = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
        var now = java.time.ZonedDateTime.now(zone);
        String createDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String expireDate = now.plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String ip = (clientIp == null || clientIp.isBlank()) ? "0.0.0.0" : clientIp;

        String txnRef = p.getTransactionId();
        Map<String, String> vnp = new TreeMap<>();
        vnp.put("vnp_Version", version);
        vnp.put("vnp_Command", "pay");
        vnp.put("vnp_TmnCode", tmnCode);
        vnp.put("vnp_Amount", String.valueOf(amount));
        vnp.put("vnp_CurrCode", "VND");
        vnp.put("vnp_TxnRef", txnRef);
        vnp.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        vnp.put("vnp_OrderType", "other");
        vnp.put("vnp_Locale", "vn");
        vnp.put("vnp_ReturnUrl", returnUrl);
        vnp.put("vnp_IpAddr", ip);
        vnp.put("vnp_CreateDate", createDate);
        vnp.put("vnp_ExpireDate", expireDate);


        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (var e : vnp.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (v == null || v.isBlank()) continue;

            if (hashData.length() > 0) { hashData.append('&'); query.append('&'); }

            String encK = vnpEncode(k);
            String encV = vnpEncode(v);

            hashData.append(encK).append('=').append(encV);
            query.append(encK).append('=').append(encV);
        }

        String secureHash = signer.hmacSha512Hex(hashSecret.trim(), hashData.toString());
        String url = payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
        return url;
    }

    public void handleCallback(Map<String, String> params) {
        if (!signer.verify(params, hashSecret)) throw new RuntimeException("Invalid VNPay signature");

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String vnpTransactionNo = params.get("vnp_TransactionNo");

        Payment p = paymentRepository.findByTransactionId(txnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("PAID".equalsIgnoreCase(p.getPaymentStatus())) return;

        p.setProviderRef(vnpTransactionNo);
        p.setProviderPayload(toJsonSafe(params));

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            markPaid(p);
        } else {
            p.setPaymentStatus("FAILED");
            paymentRepository.save(p);
        }
    }

    private void markPaid(Payment p) {
        p.setPaymentStatus("PAID");
        p.setPaidAt(LocalDateTime.now());

        Order o = p.getOrder();
        o.setStatus(OrderStatus.COMPLETED);

        paymentRepository.save(p);
        orderRepository.save(o);

        Long recipientId = o.getUser().getId();
        notificationService.create(
                recipientId,
                null,
                NotificationType.PAYMENT_PAID,
                "Thanh toán thành công",
                "Đơn " + o.getId() + " đã thanh toán thành công.",
                "/orders/" + o.getId(),
                "{\"orderId\":" + o.getId() + ",\"paymentId\":" + p.getId() + ",\"status\":\"PAID\"}"
        );

        cartService.clearCart(o.getUser().getEmail());
    }

    private String toJsonSafe(Map<String, String> params) {
        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (Exception e) {
            return null;
        }
    }
}
