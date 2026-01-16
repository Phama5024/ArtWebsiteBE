package com.example.be.dto.commission;


import com.example.be.enums.CommissionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommissionRequestResponseDTO {
    private Long id;
    private String title;
    private String description;
    private CommissionStatus status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<CommissionItemResponseDTO> items;
    private String contactMethod;
    private String contactValue;

}
