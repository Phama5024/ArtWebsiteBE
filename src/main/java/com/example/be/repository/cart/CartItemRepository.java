package com.example.be.repository.cart;

import com.example.be.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    void deleteByCartIdAndProductId(Long cartId, Long productId);

    @Query("""
    select distinct ci
    from CartItem ci
    join fetch ci.product p
    left join fetch p.mediaList
    where ci.cart.id = :cartId
""")
    List<CartItem> findAllByCartIdFetchProductAndMedia(@Param("cartId") Long cartId);


    @Modifying
    @Query("delete from CartItem ci where ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);
}
