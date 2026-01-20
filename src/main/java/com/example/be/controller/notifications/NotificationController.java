package com.example.be.controller.notifications;

import com.example.be.dto.notifications.NotificationDTO;
import com.example.be.entity.Notification;
import com.example.be.repository.notifications.NotificationRepository;
import com.example.be.repository.user.UserRepository;
import com.example.be.service.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository repo;
    private final NotificationService service;
    private final UserRepository userRepo;

    private Long currentUserId() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @GetMapping
    public Page<NotificationDTO> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean unread
    ) {
        Long userId = currentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> p = (unread == null)
                ? repo.findByRecipientId(userId, pageable)
                : repo.findByRecipientIdAndRead(userId, !unread, pageable);

        return p.map(n -> new NotificationDTO(
                n.getId(),
                n.getType().name(),
                n.getTitle(),
                n.getBody(),
                n.getUrl(),
                n.getData(),
                n.isRead(),
                n.getCreatedAt()
        ));
    }

    @GetMapping("/unread-count")
    public long unreadCount() {
        return service.unreadCount(currentUserId());
    }

    @PostMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        service.markRead(currentUserId(), id);
    }

    @PostMapping("/read-all")
    public int markAllRead() {
        return service.markAllRead(currentUserId());
    }
}
