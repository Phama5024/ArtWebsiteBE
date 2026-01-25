package com.example.be.dto.chat;

public record WsEventDTO(
        String type,
        Object data
) {}
