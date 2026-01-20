package com.example.be.repository.notifications;

import com.example.be.entity.Notification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndRead(Long recipientId, boolean read, Pageable pageable);

    long countByRecipientIdAndRead(Long recipientId, boolean read);

    @Modifying
    @Transactional
    @Query("""
        update Notification n
        set n.read = true, n.readAt = CURRENT_TIMESTAMP
        where n.recipientId = :recipientId and n.read = false
    """)
    int markAllRead(@Param("recipientId") Long recipientId);

    @Modifying
    @Transactional
    @Query("""
        update Notification n
        set n.read = true, n.readAt = CURRENT_TIMESTAMP
        where n.id = :id and n.recipientId = :recipientId
    """)
    int markRead(@Param("id") Long id, @Param("recipientId") Long recipientId);
}
