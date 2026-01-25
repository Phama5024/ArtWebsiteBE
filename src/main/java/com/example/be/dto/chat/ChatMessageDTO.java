package com.example.be.dto.chat;

import java.time.LocalDateTime;

public record ChatMessageDTO(
        Long id,
        Long conversationId,
        String senderType,
        Long senderUserId,
        String contentType,
        String content,
        String metadata,
        Long replyToMessageId,
        LocalDateTime createdAt
) {}
