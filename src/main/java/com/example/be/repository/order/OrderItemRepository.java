package com.example.be.repository.order;

import com.example.be.dto.order.AdminOrderItemDTO;
import com.example.be.dto.order.MyOrderItemDTO;
import com.example.be.entity.OrderItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        select new com.example.be.dto.order.AdminOrderItemDTO(
            p.id,
            p.name,
            pm.imageUrl,
            meta.fileFormat,
            oi.quantity,
            oi.price
        )
        from OrderItem oi
        join oi.product p
        left join p.meta meta
        left join p.mediaList pm with pm.isPrimary = true
        where oi.order.id = :orderId
        """)
    List<AdminOrderItemDTO> findAdminItems(@Param("orderId") Long orderId);

    @Query("""
        select new com.example.be.dto.order.MyOrderItemDTO(
            oi.id,
            p.id,
            p.name,
            pm.imageUrl,
            meta.fileFormat,
            oi.quantity,
            oi.price,
            oi.downloadName
        )
        from OrderItem oi
        join oi.product p
        left join p.meta meta
        left join p.mediaList pm with pm.isPrimary = true
        where oi.order.id = :orderId
        """)
    List<MyOrderItemDTO> findMyItems(@Param("orderId") Long orderId);

    Optional<OrderItem> findByIdAndOrderId(Long id, Long orderId);
}
