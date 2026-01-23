package com.example.be.repository.favorite;

import com.example.be.entity.Favorite;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    Optional<Favorite> findByUser_IdAndProduct_Id(Long userId, Long productId);

    @Modifying
    @Query("delete from Favorite f where f.user.id = :userId and f.product.id = :productId")
    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);


    @Query(value = """
    SELECT
      p.id AS productId,
      p.name AS name,
      p.price AS price,
      pm.image_url AS primaryImageUrl,
      COALESCE(rs.avg_rating, 0) AS ratingAvg,
      COALESCE(rs.total, 0) AS ratingCount,
      f.created_at AS favoritedAt
    FROM favorites f
    JOIN products p ON p.id = f.product_id
    LEFT JOIN product_media pm ON pm.product_id = p.id AND pm.is_primary = 1
    LEFT JOIN (
      SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS total
      FROM reviews
      GROUP BY product_id
    ) rs ON rs.product_id = p.id
    WHERE f.user_id = :userId
    ORDER BY f.created_at DESC
  """, nativeQuery = true)
    List<FavoriteRow> findMyFavorites(@Param("userId") Long userId);

    interface FavoriteRow {
        Long getProductId();
        String getName();
        java.math.BigDecimal getPrice();
        String getPrimaryImageUrl();
        Double getRatingAvg();
        Long getRatingCount();
        java.sql.Timestamp getFavoritedAt();
    }
}
