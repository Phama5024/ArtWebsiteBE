package com.example.be.dto.notifications;

import java.time.LocalDateTime;

public record NotificationDTO(
        Long id,
        String type,
        String title,
        String body,
        String url,
        String data,
        boolean read,
        LocalDateTime createdAt
) {}
