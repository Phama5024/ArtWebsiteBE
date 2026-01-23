package com.example.be.controller.review;

import com.example.be.dto.review.RatingSummaryDTO;
import com.example.be.dto.review.ReviewDTO;
import com.example.be.dto.review.ReviewUpsertRequestDTO;
import com.example.be.service.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/summary")
    public ResponseEntity<RatingSummaryDTO> getRatingSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getRatingSummary(productId));
    }

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getPreviewReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    @GetMapping("/me")
    public ResponseEntity<ReviewDTO> getMyReview(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return reviewService.getMyReview(productId, email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> upsertMyReview(
            @PathVariable Long productId,
            Authentication authentication,
            @Valid @RequestBody ReviewUpsertRequestDTO req
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(reviewService.upsertMyReview(productId, email, req));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyReview(
            @PathVariable Long productId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        reviewService.deleteMyReview(productId, email);
        return ResponseEntity.noContent().build();
    }
}
