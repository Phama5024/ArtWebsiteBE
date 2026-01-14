package com.example.be.controller.post;

import com.example.be.dto.post.CommentResponseDTO;
import com.example.be.entity.PostComment;
import com.example.be.repository.post.PostCommentRepository;
import com.example.be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentRepository commentRepo;
    private final UserRepository userRepo;

    public record CreateCommentReq(String content) {}

    // Public: xem danh sách comment
    @GetMapping("/{postId}/comments")
    public Page<CommentResponseDTO> list(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepo.findComments(postId, pageable);
    }

    // Authenticated: thêm comment
    @PostMapping("/{postId}/comments")
    public CommentResponseDTO create(
            @PathVariable Long postId,
            @RequestBody CreateCommentReq req
    ) {
        if (req.content() == null || req.content().isBlank()) {
            throw new IllegalArgumentException("Nội dung comment bắt buộc");
        }

        String email = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        PostComment c = new PostComment();
        c.setPostId(postId);
        c.setUserId(user.getId());
        c.setContent(req.content().trim());

        PostComment saved = commentRepo.save(c);

        return new CommentResponseDTO(
                saved.getId(),
                saved.getPostId(),
                saved.getContent(),
                saved.getCreatedAt(),
                user.getId(),
                user.getFullName(),
                user.getAvatarUrl()
        );
    }
}
