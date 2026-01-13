package com.example.be.service.order;

import com.example.be.dto.order.MyOrderDTO;
import com.example.be.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MyOrderService {
    Page<MyOrderDTO> myOrders(String email, OrderStatus status, Pageable pageable);
    MyOrderDTO myOrderDetail(String email, Long orderId);
}
