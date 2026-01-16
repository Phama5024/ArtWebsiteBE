package com.example.be.dto.commission;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CommissionItemDTO {
    private String style;
    private BigDecimal basePrice;
    private List<CommissionCharacterDTO> characters;
}
