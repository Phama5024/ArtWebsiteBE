package com.example.be.repository.order;

import com.example.be.entity.Order;
import com.example.be.enums.OrderStatus;
import com.example.be.repository.view.AdminOrderRowView;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        select
          o.id as id,
          p.transactionId as transactionId,
          p.paymentMethod as paymentMethod,
          o.totalAmount as totalAmount,
          o.status as status,
          o.createdAt as createdAt
        from Order o
        left join o.payment p
        where o.deleted = false
          and (:status is null or o.status = :status)
          and (
              :keyword is null
              or cast(o.id as string) like concat('%', :keyword, '%')
              or lower(o.receiverPhone) like lower(concat('%', :keyword, '%'))
              or lower(o.receiverName) like lower(concat('%', :keyword, '%'))
              or lower(p.transactionId) like lower(concat('%', :keyword, '%'))
          )
        """)
    Page<AdminOrderRowView> adminPaged(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Modifying
    @Query("update Order o set o.deleted = true, o.deletedAt = current_timestamp where o.id in :ids")
    int softDeleteMany(@Param("ids") List<Long> ids);

    Page<Order> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    @Query("""
        select o
        from Order o
        left join fetch o.payment p
        join fetch o.user u
        where o.deleted = false
          and lower(u.email) = lower(:email)
          and (:status is null or o.status = :status)
        """)
    Page<Order> myPagedFetch(
            @Param("email") String email,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("""
        select o
        from Order o
        join fetch o.user u
        left join fetch o.payment p
        where o.id = :orderId
          and o.deleted = false
          and lower(u.email) = lower(:email)
        """)
    Optional<Order> findOwnedOrderFetch(
            @Param("orderId") Long orderId,
            @Param("email") String email
    );
}
