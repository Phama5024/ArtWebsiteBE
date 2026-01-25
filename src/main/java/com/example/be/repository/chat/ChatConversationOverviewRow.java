package com.example.be.repository.chat;

import java.time.LocalDateTime;

public interface ChatConversationOverviewRow {
    Long getId();
    String getType();
    String getTitle();
    Long getContextId();
    String getContextType();

    Long getLastMessageId();
    LocalDateTime getLastMessageAt();
    String getLastMessagePreview();

    Long getOtherUserId();
    String getOtherFullName();
    String getOtherAvatarUrl();

    Long getUnreadCount();
}
