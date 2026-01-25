package com.example.be.repository.chat;

import com.example.be.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByTypeAndDirectUser1IdAndDirectUser2Id(
            Conversation.ConversationType type,
            Long directUser1Id,
            Long directUser2Id
    );

    Optional<Conversation> findByTypeAndContextTypeAndContextId(
            Conversation.ConversationType type,
            Conversation.ContextType contextType,
            Long contextId
    );


    @Query(
            value = """
      SELECT * FROM conversations
      WHERE id IN (:ids)
      ORDER BY (last_message_at IS NULL) ASC, last_message_at DESC, id DESC
    """,
            nativeQuery = true
    )
    List<Conversation> findByIdInOrderByLastMessageAtDesc(List<Long> ids);


    @Query(value = """
    SELECT
      c.id AS id,
      c.type AS type,
      c.title AS title,
      c.context_id AS contextId,
      c.context_type AS contextType,

      c.last_message_id AS lastMessageId,
      c.last_message_at AS lastMessageAt,

      CASE
        WHEN lm.content_type = 'TEXT' THEN lm.content
        WHEN lm.content_type = 'IMAGE' THEN '[Image]'
        WHEN lm.content_type = 'FILE' THEN '[File]'
        WHEN lm.content_type = 'ORDER_CARD' THEN '[Order]'
        WHEN lm.content_type = 'COMMISSION_CARD' THEN '[Commission]'
        ELSE '[Message]'
      END AS lastMessagePreview,

      ou.id AS otherUserId,
      ou.full_name AS otherFullName,
      ou.avatar_url AS otherAvatarUrl,

      (
        SELECT COUNT(1)
        FROM messages m
        WHERE m.conversation_id = c.id
          AND m.deleted_at IS NULL
          AND m.sender_type = 'USER'
          AND m.sender_user_id <> :meId
          AND m.id > COALESCE(cp.last_read_message_id, 0)
      ) AS unreadCount

    FROM conversations c
    JOIN conversation_participants cp
      ON cp.conversation_id = c.id
     AND cp.participant_type = 'USER'
     AND cp.user_id = :meId

    LEFT JOIN messages lm
      ON lm.id = c.last_message_id

    LEFT JOIN users ou
      ON ou.id = CASE
        WHEN c.direct_user1_id = :meId THEN c.direct_user2_id
        ELSE c.direct_user1_id
      END

    WHERE c.type <> 'BOT'
    ORDER BY c.last_message_at DESC, c.id DESC
""", nativeQuery = true)
    List<ChatConversationOverviewRow> findMyConversationOverviews(@Param("meId") Long meId);


}
