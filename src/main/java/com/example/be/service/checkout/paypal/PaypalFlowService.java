package com.example.be.service.checkout.paypal;

import com.example.be.entity.Order;
import com.example.be.entity.Payment;
import com.example.be.enums.NotificationType;
import com.example.be.enums.OrderStatus;
import com.example.be.repository.order.OrderRepository;
import com.example.be.repository.payment.PaymentRepository;
import com.example.be.service.cart.CartService;
import com.example.be.service.notifications.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaypalFlowService {

    private final PaypalHttpClient paypalHttpClient;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    private final CartService cartService;
    private final NotificationService notificationService;

    @Value("${paypal.return-url}") private String returnUrl;
    @Value("${paypal.cancel-url}") private String cancelUrl;

    @Value("${paypal.currency:USD}") private String currency;

    private static final BigDecimal VND_PER_USD = new BigDecimal("26000");
    public String init(String email, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (o.getUser() == null || o.getUser().getEmail() == null
                || !o.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Forbidden");
        }

        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        p.setPaymentMethod("PAYPAL");
        p.setPaymentStatus("PENDING");

        String token = paypalHttpClient.getAccessToken();

        String invoiceId = p.getTransactionId();

        BigDecimal vndAmount = o.getTotalAmount();
        if (vndAmount == null || vndAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid order total amount");
        }

        BigDecimal usdAmount = vndAmount
                .divide(VND_PER_USD, 2, RoundingMode.HALF_UP);

        if (usdAmount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new RuntimeException("Amount too small for PayPal after conversion");
        }

        String amount = usdAmount.toPlainString();

        JsonNode json = paypalHttpClient.createOrder(
                token,
                invoiceId,
                "USD",
                amount,
                returnUrl,
                cancelUrl
        );

        String paypalOrderId = json.get("id").asText();
        p.setProviderRef(paypalOrderId);
        p.setProviderPayload(json.toString());
        paymentRepository.save(p);

        String approveUrl = null;
        for (JsonNode link : json.get("links")) {
            if ("approve".equalsIgnoreCase(link.get("rel").asText())) {
                approveUrl = link.get("href").asText();
                break;
            }
        }

        if (approveUrl == null) throw new RuntimeException("Approve link not found");
        return approveUrl;
    }

    public void capture(String email, Long orderId) {
        Order o = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (!o.getUser().getEmail().equalsIgnoreCase(email)) throw new RuntimeException("Forbidden");

        Payment p = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Payment not found"));
        if ("PAID".equalsIgnoreCase(p.getPaymentStatus())) return;

        if (p.getProviderRef() == null) throw new RuntimeException("PayPal order id missing");

        String token = paypalHttpClient.getAccessToken();
        JsonNode res = paypalHttpClient.captureOrder(token, p.getProviderRef());

        p.setProviderPayload(res.toString());
        markPaid(p);
    }

    public void handleWebhook(String bodyJson) {
        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(bodyJson);

            String invoiceId = null;
            JsonNode pu = root.at("/resource/purchase_units");
            if (pu.isArray() && pu.size() > 0 && pu.get(0).get("invoice_id") != null) {
                invoiceId = pu.get(0).get("invoice_id").asText();
            }

            Payment p;
            if (invoiceId != null && !invoiceId.isBlank()) {
                p = paymentRepository.findByTransactionId(invoiceId)
                        .orElseThrow(() -> new RuntimeException("Payment not found"));
            } else {
                String paypalOrderId = root.at("/resource/supplementary_data/related_ids/order_id").asText(null);
                if (paypalOrderId == null || paypalOrderId.isBlank())
                    throw new RuntimeException("Cannot map PayPal webhook");
                p = paymentRepository.findByProviderRef(paypalOrderId)
                        .orElseThrow(() -> new RuntimeException("Payment not found"));
            }

            if ("PAID".equalsIgnoreCase(p.getPaymentStatus())) return;

            p.setProviderPayload(bodyJson);
            markPaid(p);

        } catch (Exception e) {
            throw new RuntimeException(e);
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
}
