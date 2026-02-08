package com.example.be.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminRevenueStatsDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
}
