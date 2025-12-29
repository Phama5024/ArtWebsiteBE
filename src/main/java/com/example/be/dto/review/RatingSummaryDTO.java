package com.example.be.dto.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatingSummaryDTO {

    private Double averageRating;
    private Integer totalReviews;

    private Integer fiveStar;
    private Integer fourStar;
    private Integer threeStar;
    private Integer twoStar;
    private Integer oneStar;
}
