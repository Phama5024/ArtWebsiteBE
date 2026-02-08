package com.example.be.service.checkout;

import com.example.be.dto.checkout.QrInfoDTO;
import com.example.be.entity.CommissionRequest;
import com.example.be.entity.Order;
import com.example.be.entity.Payment;
import com.example.be.enums.CommissionStatus;
import com.example.be.enums.NotificationType;
import com.example.be.enums.OrderStatus;
import com.example.be.repository.commission.CommissionRequestRepository;
import com.example.be.repository.order.OrderRepository;
import com.example.be.repository.payment.PaymentRepository;
import com.example.be.service.cart.CartService;
import com.example.be.service.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentFlowServiceImpl implements PaymentFlowService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CommissionRequestRepository commissionRequestRepository; // ✅ add
    private final CartService cartService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public QrInfoDTO getQrInfo(String email, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (o.getUser() == null || o.getUser().getEmail() == null
                || !o.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Forbidden");
        }

        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // (optional) ensure method exists for QR flow
        if (p.getPaymentMethod() == null || p.getPaymentMethod().isBlank()) {
            p.setPaymentMethod("BANK_QR");
            if (p.getPaymentStatus() == null || p.getPaymentStatus().isBlank()) {
                p.setPaymentStatus("PENDING");
            }
            paymentRepository.save(p);
        }

        String orderCode = "#AA" + String.format("%08d", o.getId());

        String qrValue = "BANK://pay?amount=" + o.getTotalAmount() + "&ref=" + p.getTransactionId();

        return new QrInfoDTO(
                o.getId(),
                orderCode,
                p.getTransactionId(),
                o.getTotalAmount(),
                qrValue
        );
    }

    @Override
    public void confirmPaid(String email, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (o.getUser() == null || o.getUser().getEmail() == null
                || !o.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Forbidden");
        }

        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("PAID".equalsIgnoreCase(p.getPaymentStatus())) {
            return;
        }

        p.setPaymentMethod(p.getPaymentMethod() == null ? "BANK_QR" : p.getPaymentMethod());
        p.setPaymentStatus("PAID");
        p.setPaidAt(LocalDateTime.now());

        o.setStatus(OrderStatus.COMPLETED);

        if (o.getCommissionRequest() != null) {
            CommissionRequest cr = o.getCommissionRequest();
            cr.setStatus(CommissionStatus.PAID);
            commissionRequestRepository.save(cr);
        }

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
                """
                {"orderId":%d,"paymentId":%d,"status":"PAID"}
                """.formatted(o.getId(), p.getId())
        );

        notificationService.create(
                recipientId,
                null,
                NotificationType.ORDER_STATUS_CHANGED,
                "Đơn hàng cập nhật",
                "Đơn " + o.getId() + " chuyển sang: " + o.getStatus().name(),
                "/orders/" + o.getId(),
                """
                {"orderId":%d,"status":"%s"}
                """.formatted(o.getId(), o.getStatus().name())
        );

        if (o.getCommissionRequest() == null) {
            cartService.clearCart(email);
        }
    }
}
