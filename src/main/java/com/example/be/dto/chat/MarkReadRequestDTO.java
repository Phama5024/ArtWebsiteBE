package com.example.be.dto.chat;

import jakarta.validation.constraints.NotNull;

public record MarkReadRequestDTO(@NotNull Long lastReadMessageId) {}
