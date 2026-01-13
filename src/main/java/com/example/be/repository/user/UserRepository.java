package com.example.be.repository.user;

import com.example.be.dto.user.AdminUserListRowDTO;
import com.example.be.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles"})
    @Query("""
        SELECT u FROM User u
        WHERE (:q IS NULL OR :q = '' OR
              LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR
              LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR
              LOWER(u.phone) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:status IS NULL OR :status = '' OR u.status = :status)
        """)
    Page<User> adminFindUsers(@Param("q") String q,
                              @Param("status") String status,
                              Pageable pageable);

    @Query("""
    SELECT
      u.id as id,
      u.email as email,
      u.fullName as fullName,
      u.phone as phone,
      u.address as address,

      u.gender as gender,
      u.dateOfBirth as dateOfBirth,
      u.avatarUrl as avatarUrl,

      u.isVerified as isVerified,
      u.status as status,
      u.createdAt as createdAt,

      COALESCE(COUNT(o.id), 0) as ordersCount,
      COALESCE(SUM(o.totalAmount), 0) as totalSpent
    FROM User u
    LEFT JOIN Order o
      ON o.user.id = u.id
      AND o.status = com.example.be.enums.OrderStatus.COMPLETED
      AND o.deleted = false
    WHERE (:q IS NULL OR :q = '' OR
          LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR
          LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR
          LOWER(u.phone) LIKE LOWER(CONCAT('%', :q, '%')))
      AND (:status IS NULL OR :status = '' OR u.status = :status)
    GROUP BY
      u.id, u.email, u.fullName, u.phone, u.address,
      u.gender, u.dateOfBirth, u.avatarUrl,
      u.isVerified, u.status, u.createdAt
    """)
    Page<AdminUserListRowDTO> adminFindUsersWithStats(@Param("q") String q,
                                                      @Param("status") String status,
                                                      Pageable pageable);

}
