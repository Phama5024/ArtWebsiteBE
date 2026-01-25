package com.example.be.dto.chat;

import java.time.LocalDateTime;

public record ChatConversationOverviewDTO(
        Long id,
        String type,
        String title,
        Long contextId,
        String contextType,

        Long lastMessageId,
        LocalDateTime lastMessageAt,
        String lastMessagePreview,

        Long otherUserId,
        String otherFullName,
        String otherAvatarUrl,

        long unreadCount
) {}
