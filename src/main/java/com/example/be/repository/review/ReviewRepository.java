package com.example.be.repository.review;

import com.example.be.entity.Review;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query(value = """
    SELECT
      COALESCE(AVG(rating), 0) AS avgRating,
      COUNT(*) AS total,
      SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END) AS fiveStar,
      SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END) AS fourStar,
      SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END) AS threeStar,
      SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) AS twoStar,
      SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) AS oneStar
    FROM reviews
    WHERE product_id = :productId
    """, nativeQuery = true)
    RatingSummaryRow getRatingSummaryRow(@Param("productId") Long productId);

    interface RatingSummaryRow {
        Double getAvgRating();
        Long getTotal();
        Long getFiveStar();
        Long getFourStar();
        Long getThreeStar();
        Long getTwoStar();
        Long getOneStar();
    }

    @Query("""
    select distinct r
    from Review r
    join fetch r.user u
    left join fetch r.mediaList m
    where r.product.id = :productId
    order by r.createdAt desc
    """)
    List<Review> findPreviewByProductId(@Param("productId") Long productId);

    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);

    @Query("""
    select distinct r
    from Review r
    join fetch r.user u
    left join fetch r.mediaList m
    where r.product.id = :productId and r.user.id = :userId
    """)
    Optional<Review> findMyReview(@Param("userId") Long userId, @Param("productId") Long productId);
}
