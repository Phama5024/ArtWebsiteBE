package com.example.be.dto.chat;

import java.time.LocalDateTime;

public record ChatConversationDTO(
        Long id,
        String type,
        String title,
        Long contextId,
        String contextType,
        Long lastMessageId,
        LocalDateTime lastMessageAt,
        Long directUser1Id,
        Long directUser2Id
) {}
