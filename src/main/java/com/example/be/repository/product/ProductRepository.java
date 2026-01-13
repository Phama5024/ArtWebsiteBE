package com.example.be.repository.product;

import com.example.be.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {
            "categories",
            "mediaList",
            "meta"
    })
    Optional<Product> findDetailById(Long id);


    @Query("""
        SELECT p
        FROM Product p
        WHERE EXISTS (
            SELECT c FROM p.categories c
            WHERE c.name = :category
        )
    """)
    Page<Product> findByProductCategory(
        @Param("category") String category,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("""
    UPDATE Product p
    SET p.deleted = true
    WHERE p.id IN :ids
""")
    void softDeleteMany(@Param("ids") List<Long> ids);

    @Query("""
    SELECT p FROM Product p
    WHERE p.deleted = false
""")
    Page<Product> findAllActive(Pageable pageable);

    @EntityGraph(attributePaths = {"categories", "mediaList"})
    @Query("""
        SELECT p FROM Product p
        WHERE p.deleted = false
    """)
    Page<Product> findAllActiveWithMedia(Pageable pageable);

    @EntityGraph(attributePaths = {"categories", "mediaList"})
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.deleted = false
          AND EXISTS (
              SELECT c FROM p.categories c
              WHERE c.name = :category
          )
    """)
    Page<Product> findByProductCategoryWithMedia(
            @Param("category") String category,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("""
    UPDATE Product p
    SET p.status = 'LOCKED'
    WHERE p.id IN :ids
""")
    void lockMany(@Param("ids") List<Long> ids);


    @Modifying
    @Transactional
    @Query("""
    UPDATE Product p
    SET p.status = 'ACTIVE'
    WHERE p.id IN :ids
""")
    void unlockMany(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"categories", "mediaList"})
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.deleted = false
          AND EXISTS (
              SELECT c FROM p.categories c
              WHERE c.name = :category
          )
    """)
    List<Product> findTopActiveByCategoryWithMedia(
            @Param("category") String category,
            Pageable pageable
    );

}
