package com.example.be.controller.chat;

import com.example.be.dto.chat.*;
import com.example.be.service.chat.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    private Long currentUserId() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.getUserIdByEmailOrThrow(email);
    }

    @PostMapping("/direct/{otherUserId}")
    public ChatConversationDTO getOrCreateDirect(@PathVariable Long otherUserId) {
        return chatService.getOrCreateDirectConversation(currentUserId(), otherUserId);
    }

    @GetMapping("/conversations")
    public List<ChatConversationOverviewDTO> listMyConversations() {
        return chatService.listMyConversationOverviews(currentUserId());
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public Page<ChatMessageDTO> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return chatService.getMessages(currentUserId(), conversationId, page, size);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ChatMessageDTO sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequestDTO request
    ) {
        return chatService.sendMessage(currentUserId(), conversationId, request);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public void markRead(
            @PathVariable Long conversationId,
            @Valid @RequestBody MarkReadRequestDTO request
    ) {
        chatService.markRead(currentUserId(), conversationId, request.lastReadMessageId());
    }
}
