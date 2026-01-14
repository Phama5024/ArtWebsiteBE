package com.example.be.dto.post;

import java.time.LocalDateTime;

public record PostResponseDTO(
        Long id,
        String title,
        String authorName,
        String content,
        String coverImage,
        String status,
        LocalDateTime createdAt
) {}