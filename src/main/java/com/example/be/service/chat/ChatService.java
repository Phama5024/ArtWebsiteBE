package com.example.be.service.chat;

import com.example.be.dto.chat.*;
import com.example.be.entity.User;
import com.example.be.entity.*;
import com.example.be.enums.NotificationType;
import com.example.be.repository.chat.*;
import com.example.be.repository.user.UserRepository;
import com.example.be.service.notifications.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.be.dto.chat.ChatConversationOverviewDTO;
import com.example.be.repository.chat.ChatConversationOverviewRow;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepo;

    private final ConversationRepository conversationRepo;
    private final ConversationParticipantRepository participantRepo;
    private final MessageRepository messageRepo;


    private final NotificationService notificationService;

    public Long getUserIdByEmailOrThrow(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private ConversationParticipant.ParticipantRole mapRole(User user) {
        Set<String> roleNames = (user.getRoles() == null)
                ? Set.of()
                : user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());

        if (roleNames.contains("ADMIN")) return ConversationParticipant.ParticipantRole.ADMIN;
        if (roleNames.contains("SELLER")) return ConversationParticipant.ParticipantRole.SELLER;
        return ConversationParticipant.ParticipantRole.USER;
    }

    private void assertMember(Long conversationId, Long userId) {
        boolean ok = participantRepo.existsByConversationIdAndParticipantTypeAndUserId(
                conversationId,
                ConversationParticipant.ParticipantType.USER,
                userId
        );
        if (!ok) throw new RuntimeException("You are not a participant of this conversation");
    }

    @Transactional
    public ChatConversationDTO getOrCreateDirectConversation(Long meId, Long otherUserId) {
        if (Objects.equals(meId, otherUserId)) {
            throw new RuntimeException("Cannot create direct conversation with yourself");
        }

        long u1 = Math.min(meId, otherUserId);
        long u2 = Math.max(meId, otherUserId);

        Optional<Conversation> existing = conversationRepo.findByTypeAndDirectUser1IdAndDirectUser2Id(
                Conversation.ConversationType.DIRECT, u1, u2
        );

        Conversation c = existing.orElseGet(() -> {
            Conversation created = conversationRepo.save(
                    Conversation.builder()
                            .type(Conversation.ConversationType.DIRECT)
                            .directUser1Id(u1)
                            .directUser2Id(u2)
                            .build()
            );

            User me = userRepo.findById(meId).orElseThrow(() -> new RuntimeException("User not found"));
            User other = userRepo.findById(otherUserId).orElseThrow(() -> new RuntimeException("User not found"));

            participantRepo.save(ConversationParticipant.builder()
                    .conversationId(created.getId())
                    .participantType(ConversationParticipant.ParticipantType.USER)
                    .userId(meId)
                    .role(mapRole(me))
                    .joinedAt(LocalDateTime.now())
                    .build());

            participantRepo.save(ConversationParticipant.builder()
                    .conversationId(created.getId())
                    .participantType(ConversationParticipant.ParticipantType.USER)
                    .userId(otherUserId)
                    .role(mapRole(other))
                    .joinedAt(LocalDateTime.now())
                    .build());

            return created;
        });

        return new ChatConversationDTO(
                c.getId(),
                c.getType().name(),
                c.getTitle(),
                c.getContextId(),
                c.getContextType() == null ? null : c.getContextType().name(),
                c.getLastMessageId(),
                c.getLastMessageAt(),
                c.getDirectUser1Id(),
                c.getDirectUser2Id()
        );
    }

    @Transactional(readOnly = true)
    public List<ChatConversationDTO> listMyConversations(Long userId) {
        List<ConversationParticipant> parts = participantRepo.findByUserIdAndParticipantType(
                userId, ConversationParticipant.ParticipantType.USER
        );
        List<Long> ids = parts.stream().map(ConversationParticipant::getConversationId).distinct().toList();
        if (ids.isEmpty()) return List.of();

        List<Conversation> conversations = conversationRepo.findByIdInOrderByLastMessageAtDesc(ids);
        return conversations.stream().map(c -> new ChatConversationDTO(
                c.getId(),
                c.getType().name(),
                c.getTitle(),
                c.getContextId(),
                c.getContextType() == null ? null : c.getContextType().name(),
                c.getLastMessageId(),
                c.getLastMessageAt(),
                c.getDirectUser1Id(),
                c.getDirectUser2Id()
        )).toList();
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getMessages(Long userId, Long conversationId, int page, int size) {
        assertMember(conversationId, userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Message> p = messageRepo.findByConversationIdAndDeletedAtIsNullOrderByIdDesc(conversationId, pageable);

        return p.map(m -> new ChatMessageDTO(
                m.getId(),
                m.getConversationId(),
                m.getSenderType().name(),
                m.getSenderUserId(),
                m.getContentType().name(),
                m.getContent(),
                m.getMetadata(),
                m.getReplyToMessageId(),
                m.getCreatedAt()
        ));
    }

    @Transactional
    public ChatMessageDTO sendMessage(Long userId, Long conversationId, SendMessageRequestDTO req) {
        assertMember(conversationId, userId);

        Message.ContentType ct;
        try {
            ct = Message.ContentType.valueOf(req.contentType());
        } catch (Exception e) {
            throw new RuntimeException("Invalid contentType");
        }

        Message m = messageRepo.save(Message.builder()
                .conversationId(conversationId)
                .senderType(Message.SenderType.USER)
                .senderUserId(userId)
                .contentType(ct)
                .content(req.content())
                .metadata(req.metadata())
                .replyToMessageId(req.replyToMessageId())
                .build());

        Conversation c = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        c.setLastMessageId(m.getId());
        c.setLastMessageAt(m.getCreatedAt());
        conversationRepo.save(c);

        User sender = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String senderName = (sender.getFullName() != null && !sender.getFullName().isBlank())
                ? sender.getFullName()
                : sender.getEmail();

        List<ConversationParticipant> participants = participantRepo.findByConversationId(conversationId);
        for (ConversationParticipant p : participants) {
            if (p.getParticipantType() != ConversationParticipant.ParticipantType.USER) continue;
            if (Objects.equals(p.getUserId(), userId)) continue;

            String url = "/messages/" + conversationId;

            String title = "Bạn có tin nhắn mới từ " + senderName;
            String body = null;

            String safeSenderName = senderName.replace("\"", "\\\"");
            String dataJson = """
        {"conversationId": %d, "messageId": %d, "senderId": %d, "senderName": "%s"}
    """.formatted(conversationId, m.getId(), userId, safeSenderName);

            notificationService.create(
                    p.getUserId(),
                    userId,
                    NotificationType.CHAT_MESSAGE,
                    title,
                    body,
                    url,
                    dataJson
            );
        }


        return new ChatMessageDTO(
                m.getId(),
                m.getConversationId(),
                m.getSenderType().name(),
                m.getSenderUserId(),
                m.getContentType().name(),
                m.getContent(),
                m.getMetadata(),
                m.getReplyToMessageId(),
                m.getCreatedAt()
        );
    }

    @Transactional
    public void markRead(Long userId, Long conversationId, Long lastReadMessageId) {
        assertMember(conversationId, userId);

        int updated = participantRepo.updateReadState(
                conversationId,
                userId,
                lastReadMessageId,
                LocalDateTime.now()
        );

        if (updated == 0) {
            throw new RuntimeException("Participant not found");
        }
    }

    @Transactional(readOnly = true)
    public List<String> getOtherParticipantEmails(Long conversationId, Long excludeUserId) {
        List<ConversationParticipant> participants = participantRepo.findByConversationId(conversationId);

        List<Long> userIds = participants.stream()
                .filter(p -> p.getParticipantType() == ConversationParticipant.ParticipantType.USER)
                .map(ConversationParticipant::getUserId)
                .filter(Objects::nonNull)
                .filter(id -> !id.equals(excludeUserId))
                .distinct()
                .toList();

        if (userIds.isEmpty()) return List.of();

        return userRepo.findAllById(userIds).stream()
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatConversationOverviewDTO> listMyConversationOverviews(Long userId) {
        List<ChatConversationOverviewRow> rows = conversationRepo.findMyConversationOverviews(userId);

        return rows.stream().map(r -> new ChatConversationOverviewDTO(
                r.getId(),
                r.getType(),
                r.getTitle(),
                r.getContextId(),
                r.getContextType(),

                r.getLastMessageId(),
                r.getLastMessageAt(),
                trimPreview(r.getLastMessagePreview()),

                r.getOtherUserId(),
                r.getOtherFullName(),
                r.getOtherAvatarUrl(),

                r.getUnreadCount() == null ? 0 : r.getUnreadCount()
        )).toList();
    }

    private String trimPreview(String s) {
        if (s == null) return null;
        String t = s.strip();
        if (t.length() <= 80) return t;
        return t.substring(0, 80) + "...";
    }

}
