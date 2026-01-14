package com.example.be.repository.post;

import com.example.be.dto.post.CommentResponseDTO;
import com.example.be.entity.PostComment;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("""
        SELECT new com.example.be.dto.post.CommentResponseDTO(
            c.id,
            c.postId,
            c.content,
            c.createdAt,
            u.id,
            u.fullName,
            u.avatarUrl
        )
        FROM PostComment c
        JOIN User u ON u.id = c.userId
        WHERE c.postId = :postId
          AND c.deletedAt IS NULL
        ORDER BY c.createdAt DESC, c.id DESC
    """)
    Page<CommentResponseDTO> findComments(@Param("postId") Long postId, Pageable pageable);
}
