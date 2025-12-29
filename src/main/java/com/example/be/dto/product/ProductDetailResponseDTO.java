package com.example.be.dto.product;

import com.example.be.dto.review.RatingSummaryDTO;
import com.example.be.dto.review.ReviewDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class ProductDetailResponseDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    private Set<String> categories;

    private List<ProductMediaDTO> images;

    private ProductMetaDTO meta;

    private RatingSummaryDTO rating;

    private List<ReviewDTO> reviews;
}
