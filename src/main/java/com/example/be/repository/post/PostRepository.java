package com.example.be.repository.post;

import com.example.be.entity.Post;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
      SELECT p FROM Post p
      WHERE p.deletedAt IS NULL
        AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.authorName LIKE %:keyword%)
      """)
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);
}
