package com.example.be.repository.product;

import com.example.be.entity.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {

    @Modifying
    @Query("""
        UPDATE ProductMedia m
        SET m.isPrimary = false
        WHERE m.product.id = :productId
    """)
    void resetPrimary(@Param("productId") Long productId);
}
