package com.example.be.service.stats;

import com.example.be.dto.stats.AdminRevenueStatsDTO;
import com.example.be.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final OrderRepository orderRepository;

    public AdminRevenueStatsDTO getRevenueStats(LocalDateTime from, LocalDateTime to) {
        BigDecimal totalRevenue = orderRepository.sumRevenueCompletedInRange(from, to);
        Long totalOrders = orderRepository.countCompletedOrdersInRange(from, to);
        return new AdminRevenueStatsDTO(totalRevenue, totalOrders);
    }
}
