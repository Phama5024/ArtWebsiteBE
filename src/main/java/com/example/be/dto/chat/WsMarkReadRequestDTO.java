package com.example.be.dto.chat;

public record WsMarkReadRequestDTO(
        Long conversationId,
        Long lastReadMessageId
) {}
