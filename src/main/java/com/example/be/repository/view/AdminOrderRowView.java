package com.example.be.repository.view;

import com.example.be.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AdminOrderRowView {
    Long getId();
    String getTransactionId();
    String getPaymentMethod();
    BigDecimal getTotalAmount();
    OrderStatus getStatus();
    LocalDateTime getCreatedAt();
}
