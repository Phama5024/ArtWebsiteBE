package com.example.be.dto.commission;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionCharacterDTO {
    private Integer characterIndex;
    private String poseScope;
    private BigDecimal extraPrice;
}
