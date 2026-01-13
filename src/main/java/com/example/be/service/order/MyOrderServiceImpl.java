package com.example.be.service.order;

import com.example.be.dto.order.MyOrderDTO;
import com.example.be.dto.order.MyOrderItemDTO;
import com.example.be.entity.Order;
import com.example.be.entity.Payment;
import com.example.be.enums.OrderStatus;
import com.example.be.repository.order.OrderItemRepository;
import com.example.be.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyOrderServiceImpl implements MyOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Page<MyOrderDTO> myOrders(String email, OrderStatus status, Pageable pageable) {
        Page<Order> page = orderRepository.myPagedFetch(email, status, pageable);

        List<MyOrderDTO> dtos = page.getContent().stream().map(o -> {
            List<MyOrderItemDTO> items = orderItemRepository.findMyItems(o.getId());
            return toDto(o, items);
        }).toList();

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public MyOrderDTO myOrderDetail(String email, Long orderId) {
        Order order = orderRepository.findOwnedOrderFetch(orderId, email)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<MyOrderItemDTO> items = orderItemRepository.findMyItems(orderId);
        return toDto(order, items);
    }

    private MyOrderDTO toDto(Order o, List<MyOrderItemDTO> items) {
        Payment p = o.getPayment();

        return new MyOrderDTO(
                o.getId(),
                "#AA" + String.format("%08d", o.getId()),
                o.getCreatedAt(),
                o.getTotalAmount(),
                o.getStatus(),
                p != null ? p.getPaymentMethod() : null,
                p != null ? p.getPaymentStatus() : null,
                p != null ? p.getPaidAt() : null,
                items
        );
    }
}
