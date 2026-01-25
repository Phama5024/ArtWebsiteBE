package com.example.be.controller.chat;

import com.example.be.dto.chat.ChatMessageDTO;
import com.example.be.dto.chat.SendMessageRequestDTO;
import com.example.be.dto.chat.*;
import com.example.be.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(@Payload WsSendMessageRequestDTO payload, Principal principal) {
        String email = principal.getName();
        Long senderId = chatService.getUserIdByEmailOrThrow(email);

        ChatMessageDTO saved = chatService.sendMessage(
                senderId,
                payload.conversationId(),
                new SendMessageRequestDTO(
                        payload.contentType(),
                        payload.content(),
                        payload.metadata(),
                        payload.replyToMessageId()
                )
        );

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + payload.conversationId(),
                new WsEventDTO("MESSAGE_CREATED", saved)
        );

        var convUpdate = new WsEventDTO(
                "CONVERSATION_UPDATED",
                Map.of(
                        "conversationId", payload.conversationId(),
                        "lastMessageId", saved.id(),
                        "lastMessageAt", saved.createdAt(),
                        "senderId", senderId
                )
        );

        messagingTemplate.convertAndSendToUser(email, "/queue/conversations", convUpdate);

        for (String recipientEmail : chatService.getOtherParticipantEmails(payload.conversationId(), senderId)) {
            messagingTemplate.convertAndSendToUser(recipientEmail, "/queue/conversations", convUpdate);
        }
    }

    @MessageMapping("/chat.read")
    public void markRead(@Payload WsMarkReadRequestDTO payload, Principal principal) {
        String email = principal.getName();
        Long userId = chatService.getUserIdByEmailOrThrow(email);

        chatService.markRead(userId, payload.conversationId(), payload.lastReadMessageId());

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + payload.conversationId(),
                new WsEventDTO("READ_UPDATED", Map.of(
                        "conversationId", payload.conversationId(),
                        "userId", userId,
                        "lastReadMessageId", payload.lastReadMessageId()
                ))
        );
    }
}
