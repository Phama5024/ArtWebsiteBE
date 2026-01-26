package com.example.be.repository.post;

import com.example.be.dto.post.PostListDTO;
import com.example.be.entity.Post;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            SELECT new com.example.be.dto.post.PostListDTO(
                p.id, p.title, p.authorName, p.content, p.coverImage, p.status, p.createdAt,
                u.id, u.fullName
            )
            FROM Post p
            LEFT JOIN p.user u
            WHERE p.deletedAt IS NULL
              AND (:keyword IS NULL OR :keyword = ''
                   OR p.title LIKE %:keyword%
                   OR p.authorName LIKE %:keyword%)
            """)
    Page<PostListDTO> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.deletedAt IS NULL
              AND p.user.id = :userId
              AND (:keyword IS NULL OR p.title LIKE %:keyword% OR p.authorName LIKE %:keyword%)
            """)
    Page<Post> searchMine(@Param("userId") Long userId,
                          @Param("keyword") String keyword,
                          Pageable pageable);

    Optional<Post> findByIdAndDeletedAtIsNullAndUser_Id(Long id, Long userId);

    @Query("""
              SELECT p FROM Post p
              LEFT JOIN FETCH p.user u
              WHERE p.deletedAt IS NULL AND p.id = :id
            """)
    Optional<Post> findDetailWithUser(@Param("id") Long id);
}
