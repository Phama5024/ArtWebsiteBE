package com.example.be.repository.chat;

import com.example.be.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdAndDeletedAtIsNullOrderByIdDesc(Long conversationId, Pageable pageable);
}
