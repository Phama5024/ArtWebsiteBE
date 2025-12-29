package com.example.be.repository.product;

import com.example.be.entity.ProductMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductMetaRepository extends JpaRepository<ProductMeta, Long> {

    Optional<ProductMeta> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
