package com.example.be.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequestDTO(
        @NotNull String contentType,
        @NotBlank String content,
        String metadata,
        Long replyToMessageId
) {}
