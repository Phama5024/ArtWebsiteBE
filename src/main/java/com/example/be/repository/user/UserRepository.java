package com.example.be.repository.user;

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
}
