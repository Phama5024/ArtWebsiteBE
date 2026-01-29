package com.example.be.service.checkout;

import com.example.be.dto.cart.CartDTO;
import com.example.be.dto.cart.CartItemDTO;
import com.example.be.dto.checkout.*;
import com.example.be.entity.*;
import com.example.be.enums.OrderStatus;
import com.example.be.repository.order.OrderItemRepository;
import com.example.be.repository.order.OrderRepository;
import com.example.be.repository.payment.PaymentRepository;
import com.example.be.repository.product.ProductRepository;
import com.example.be.repository.user.UserRepository;
import com.example.be.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public OrderSummaryDTO checkoutFromCart(String email, CheckoutRequestDTO req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CartDTO cart = cartService.getMyCart(email);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setReceiverName(req.receiverName());
        order.setReceiverPhone(req.receiverPhone());
        order.setShippingAddress(req.shippingAddress());
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (CartItemDTO it : cart.getItems()) {
            Long productId = it.getProductId();
            int qty = it.getQuantity() == null ? 0 : it.getQuantity();

            Product p = productRepository.findDetailById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            if (Boolean.TRUE.equals(p.getDeleted())) throw new RuntimeException("Product not available");
            if (p.getStatus() != null && "OUT_OF_STOCK".equalsIgnoreCase(p.getStatus().trim())) {
                throw new RuntimeException("Product out of stock");
            }

            BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(qty);
            oi.setPrice(price);
            oi.setProductNameSnapshot(p.getName());

            if (p.getMeta() != null) {
                oi.setFileFormatSnapshot(p.getMeta().getFileFormat());

                oi.setDownloadPath(p.getMeta().getDownloadPath());
                oi.setDownloadName(p.getMeta().getDownloadName());
            }

            String thumb = null;
            if (p.getMediaList() != null) {
                thumb = p.getMediaList().stream()
                        .filter(pm -> Boolean.TRUE.equals(pm.getIsPrimary()))
                        .map(ProductMedia::getImageUrl)
                        .findFirst()
                        .orElse(null);
            }
            oi.setThumbnailUrlSnapshot(thumb);

            if (p.getMeta() != null) {
                oi.setDownloadPath(p.getMeta().getDownloadPath());
                oi.setDownloadName(p.getMeta().getDownloadName());
            }

            order.getItems().add(oi);
            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        for (OrderItem oi : savedOrder.getItems()) {
            orderItemRepository.save(oi);
        }

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentMethod(req.paymentMethod() == null ? "BANK_QR" : req.paymentMethod().toUpperCase());
        payment.setPaymentStatus("PENDING");
        payment.setTransactionId(generateInvoiceCode(savedOrder.getId()));
        payment.setPaidAt(null);

        paymentRepository.save(payment);
        savedOrder.setPayment(payment);

        return toSummary(savedOrder, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryDTO getOrderSummary(String email, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!o.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Forbidden");
        }

        Payment p = paymentRepository.findByOrderId(orderId).orElse(null);
        return toSummary(o, p);
    }

    private OrderSummaryDTO toSummary(Order o, Payment p) {
        return new OrderSummaryDTO(
                o.getId(),
                buildOrderCode(o.getId()),
                o.getTotalAmount(),
                o.getStatus(),
                p == null ? null : new PaymentSummaryDTO(
                        p.getPaymentMethod(),
                        p.getPaymentStatus(),
                        p.getTransactionId(),
                        p.getPaidAt()
                ),
                o.getCreatedAt()
        );
    }

    private String buildOrderCode(Long id) {
        return "#AA" + String.format("%08d", id);
    }

    private String generateInvoiceCode(Long orderId) {
        return "HD" + String.format("%06d", orderId);
    }
}
