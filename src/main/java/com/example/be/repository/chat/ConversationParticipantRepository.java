package com.example.be.repository.chat;

import com.example.be.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    Optional<ConversationParticipant> findByConversationIdAndParticipantTypeAndUserId(
            Long conversationId,
            ConversationParticipant.ParticipantType participantType,
            Long userId
    );

    List<ConversationParticipant> findByUserIdAndParticipantType(Long userId, ConversationParticipant.ParticipantType type);

    List<ConversationParticipant> findByConversationId(Long conversationId);

    boolean existsByConversationIdAndParticipantTypeAndUserId(
            Long conversationId,
            ConversationParticipant.ParticipantType participantType,
            Long userId
    );

    @Modifying
    @Transactional
    @Query("""
        update ConversationParticipant p
        set p.lastReadMessageId = :lastReadMessageId,
            p.lastReadAt = :lastReadAt
        where p.conversationId = :conversationId
          and p.participantType = 'USER'
          and p.userId = :userId
    """)
    int updateReadState(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId,
            @Param("lastReadMessageId") Long lastReadMessageId,
            @Param("lastReadAt") LocalDateTime lastReadAt
    );
}
