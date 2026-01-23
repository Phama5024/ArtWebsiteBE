package com.example.be.service.review;

import com.example.be.dto.review.RatingSummaryDTO;
import com.example.be.dto.review.ReviewDTO;
import com.example.be.dto.review.ReviewUpsertRequestDTO;
import com.example.be.entity.Review;
import com.example.be.entity.ReviewMedia;
import com.example.be.repository.product.ProductRepository;
import com.example.be.repository.review.ReviewRepository;
import com.example.be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public RatingSummaryDTO getRatingSummary(Long productId) {
        var row = reviewRepository.getRatingSummaryRow(productId);

        long total = row == null || row.getTotal() == null ? 0L : row.getTotal();
        if (total == 0) {
            return RatingSummaryDTO.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .fiveStar(0)
                    .fourStar(0)
                    .threeStar(0)
                    .twoStar(0)
                    .oneStar(0)
                    .build();
        }

        double avg = row.getAvgRating() == null ? 0.0 : row.getAvgRating();

        return RatingSummaryDTO.builder()
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .totalReviews(Math.toIntExact(total))
                .fiveStar(Math.toIntExact(nz(row.getFiveStar())))
                .fourStar(Math.toIntExact(nz(row.getFourStar())))
                .threeStar(Math.toIntExact(nz(row.getThreeStar())))
                .twoStar(Math.toIntExact(nz(row.getTwoStar())))
                .oneStar(Math.toIntExact(nz(row.getOneStar())))
                .build();
    }

    private static long nz(Long v) { return v == null ? 0L : v; }

    @Override
    public List<ReviewDTO> getReviewsByProduct(Long productId) {
        return reviewRepository.findPreviewByProductId(productId)
                .stream()
                .limit(5)
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public ReviewDTO upsertMyReview(Long productId, String email, ReviewUpsertRequestDTO req) {
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be 1..5");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Review review = reviewRepository.findByUser_IdAndProduct_Id(user.getId(), productId)
                .orElseGet(() -> {
                    var r = new Review();
                    r.setUser(user);
                    r.setProduct(product);
                    return r;
                });

        review.setRating(req.getRating());
        review.setComment(req.getComment());

        if (req.getImages() != null) {
            review.getMediaList().clear();

            for (String url : req.getImages()) {
                if (url == null || url.isBlank()) continue;

                ReviewMedia m = new ReviewMedia();
                m.setReview(review);
                m.setImageUrl(url);

                review.getMediaList().add(m);
            }
        }


        var saved = reviewRepository.save(review);
        var reloaded = reviewRepository.findMyReview(user.getId(), productId).orElse(saved);
        return toDTO(reloaded);
    }

    @Transactional
    @Override
    public void deleteMyReview(Long productId, String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        reviewRepository.findByUser_IdAndProduct_Id(user.getId(), productId)
                .ifPresent(reviewRepository::delete);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ReviewDTO> getMyReview(Long productId, String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return reviewRepository.findMyReview(user.getId(), productId)
                .map(this::toDTO);
    }

    private ReviewDTO toDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .userName(review.getUser().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .images(
                        review.getMediaList() == null
                                ? List.of()
                                : review.getMediaList().stream().map(ReviewMedia::getImageUrl).toList()
                )
                .build();
    }
}
