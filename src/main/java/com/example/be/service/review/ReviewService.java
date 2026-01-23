package com.example.be.service.review;

import com.example.be.dto.review.RatingSummaryDTO;
import com.example.be.dto.review.ReviewDTO;
import com.example.be.dto.review.ReviewUpsertRequestDTO;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    RatingSummaryDTO getRatingSummary(Long productId);
    List<ReviewDTO> getReviewsByProduct(Long productId);

    ReviewDTO upsertMyReview(Long productId, String email, ReviewUpsertRequestDTO req);
    void deleteMyReview(Long productId, String email);
    Optional<ReviewDTO> getMyReview(Long productId, String email);
}
