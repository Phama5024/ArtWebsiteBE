package com.example.be.service.checkout;

import com.example.be.dto.checkout.QrInfoDTO;
import com.example.be.entity.Order;
import com.example.be.entity.Payment;
import com.example.be.enums.OrderStatus;
import com.example.be.repository.order.OrderRepository;
import com.example.be.repository.payment.PaymentRepository;
import com.example.be.service.cart.CartService;
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
    private final CartService cartService;

    @Override
    @Transactional(readOnly = true)
    public QrInfoDTO getQrInfo(String email, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!o.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Forbidden");
        }

        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

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

        if (!o.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Forbidden");
        }

        Payment p = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        p.setPaymentStatus("PAID");
        p.setPaidAt(LocalDateTime.now());
        o.setStatus(OrderStatus.COMPLETED);

        paymentRepository.save(p);
        orderRepository.save(o);

        cartService.clearCart(email);
    }
}
