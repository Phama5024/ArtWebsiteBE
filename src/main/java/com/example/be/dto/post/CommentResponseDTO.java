package com.example.be.dto.post;

import java.time.LocalDateTime;

public record CommentResponseDTO(
        Long id,
        Long postId,
        String content,
        LocalDateTime createdAt,
        Long userId,
        String userFullName,
        String userAvatarUrl
) {}
