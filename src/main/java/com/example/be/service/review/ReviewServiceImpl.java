package com.example.be.service.review;

import com.example.be.dto.review.RatingSummaryDTO;
import com.example.be.dto.review.ReviewDTO;
import com.example.be.entity.ReviewMedia;
import com.example.be.repository.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public RatingSummaryDTO getRatingSummary(Long productId) {

        long total = reviewRepository.countByProductId(productId);

        if (total == 0) {
            return RatingSummaryDTO.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .build();
        }

        long five = reviewRepository.countByProductIdAndRating(productId, 5);
        long four = reviewRepository.countByProductIdAndRating(productId, 4);
        long three = reviewRepository.countByProductIdAndRating(productId, 3);
        long two = reviewRepository.countByProductIdAndRating(productId, 2);
        long one = reviewRepository.countByProductIdAndRating(productId, 1);

        double avg = (
                five * 5 +
                        four * 4 +
                        three * 3 +
                        two * 2 +
                        one
        ) / (double) total;

        return RatingSummaryDTO.builder()
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalReviews((int) total)
                .fiveStar((int) five)
                .fourStar((int) four)
                .threeStar((int) three)
                .twoStar((int) two)
                .oneStar((int) one)
                .build();
    }

    @Override
    public List<ReviewDTO> getReviewsByProduct(Long productId) {

        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .limit(5) // preview
                .map(review -> ReviewDTO.builder()
                        .id(review.getId())
                        .userName(review.getUser().getFullName())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .images(
                                review.getMediaList()
                                        .stream()
                                        .map(ReviewMedia::getImageUrl)
                                        .toList()
                        )
                        .build()
                )
                .toList();
    }

}
