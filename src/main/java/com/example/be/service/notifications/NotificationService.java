package com.example.be.service.notifications;

import com.example.be.entity.Notification;
import com.example.be.enums.NotificationType;
import com.example.be.repository.notifications.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;

    @Transactional
    public Notification create(Long recipientId, Long actorId,
                               NotificationType type,
                               String title, String body,
                               String url, String dataJson) {

        if (actorId != null && actorId.equals(recipientId)) return null;

        Notification n = Notification.builder()
                .recipientId(recipientId)
                .actorId(actorId)
                .type(type)
                .title(title)
                .body(body)
                .url(url)
                .data(dataJson)
                .read(false)
                .build();

        return repo.save(n);
    }

    public long unreadCount(Long recipientId) {
        return repo.countByRecipientIdAndRead(recipientId, false);
    }

    @Transactional
    public void markRead(Long recipientId, Long notificationId) {
        int updated = repo.markRead(notificationId, recipientId);
        if (updated == 0) throw new RuntimeException("Notification not found");
    }

    @Transactional
    public int markAllRead(Long recipientId) {
        return repo.markAllRead(recipientId);
    }
}
