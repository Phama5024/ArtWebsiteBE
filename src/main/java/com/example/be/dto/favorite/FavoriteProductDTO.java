package com.example.be.dto.favorite;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
public class FavoriteProductDTO {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String primaryImageUrl;
    private Double ratingAvg;
    private Long ratingCount;
}
