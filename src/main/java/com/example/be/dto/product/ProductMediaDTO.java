package com.example.be.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductMediaDTO {

    private Long id;
    private String imageUrl;
    private Boolean isPrimary;
}
