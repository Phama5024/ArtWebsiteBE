package com.example.be.service.review;

import com.example.be.dto.review.RatingSummaryDTO;
import com.example.be.dto.review.ReviewDTO;

import java.util.List;

public interface ReviewService {

    RatingSummaryDTO getRatingSummary(Long productId);

    List<ReviewDTO> getReviewsByProduct(Long productId);
}
