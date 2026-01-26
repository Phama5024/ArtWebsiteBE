package com.example.be.service.post;

import com.example.be.dto.post.PostListDTO;
import com.example.be.dto.post.PostResponseDTO;
import com.example.be.entity.Post;
import com.example.be.entity.User;
import com.example.be.repository.post.PostRepository;
import com.example.be.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;


    private User requireUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Post requireMyPost(Long postId, Long userId) {
        return postRepository.findByIdAndDeletedAtIsNullAndUser_Id(postId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết hoặc không có quyền"));
    }

    private PostResponseDTO toDto(Post p) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setAuthorName(p.getAuthorName());
        dto.setContent(p.getContent());
        dto.setCoverImage(p.getCoverImage());
        dto.setStatus(p.getStatus());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        if (p.getUser() != null) {
            dto.setUserId(p.getUser().getId());
            dto.setFullName(p.getUser().getFullName());
        }
        return dto;
    }

    public Page<PostResponseDTO> listMine(String email, String keyword, int page, int size) {
        User u = requireUserByEmail(email);

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        return postRepository.searchMine(u.getId(), kw, pageable).map(this::toDto);
    }

    public PostResponseDTO getMine(String email, Long postId) {
        User u = requireUserByEmail(email);
        Post p = requireMyPost(postId, u.getId());
        return toDto(p);
    }

    @Transactional
    public PostResponseDTO createMine(String email, String title, String content, MultipartFile cover) throws Exception {
        Post p = createForUser(email, title, content, cover);
        return toDto(p);
    }

    @Transactional
    public PostResponseDTO updateMine(String email, Long postId, String title, String content, MultipartFile cover) throws Exception {
        User u = requireUserByEmail(email);
        Post p = requireMyPost(postId, u.getId());

        if (title != null && !title.isBlank()) p.setTitle(title.trim());
        if (content != null) p.setContent(content);

        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageService.savePostCover(cover);
            p.setCoverImage(coverUrl);
        }

        p.setAuthorName(u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getEmail());

        return toDto(postRepository.save(p));
    }

    @Transactional
    public void softDeleteMine(String email, Long postId) {
        User u = requireUserByEmail(email);
        Post p = requireMyPost(postId, u.getId());
        p.setDeletedAt(LocalDateTime.now());
        postRepository.save(p);
    }

    @Transactional
    public Post create(String title, String authorName, String content, MultipartFile cover) throws Exception {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title bắt buộc");
        if (authorName == null || authorName.isBlank()) throw new IllegalArgumentException("author_name bắt buộc");

        String coverUrl = fileStorageService.savePostCover(cover);

        Post p = new Post();
        p.setTitle(title.trim());
        p.setAuthorName(authorName.trim());
        p.setContent(content);
        p.setCoverImage(coverUrl);

        return postRepository.save(p);
    }

    @Transactional
    public Post createForUser(String email, String title, String content, MultipartFile cover) throws Exception {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title bắt buộc");

        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String coverUrl = fileStorageService.savePostCover(cover);

        Post p = new Post();
        p.setTitle(title.trim());
        p.setAuthorName(u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getEmail());
        p.setContent(content);
        p.setCoverImage(coverUrl);
        p.setUser(u);

        p.setStatus("draft");

        return postRepository.save(p);
    }


    public Page<PostListDTO> list(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        String kw = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();
        return postRepository.search(kw, pageable);
    }


    public Post get(Long id) {
        Post p = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));
        if (p.getDeletedAt() != null) throw new IllegalArgumentException("Bài viết đã bị xoá");
        return p;
    }

    @Transactional
    public Post update(Long id, String title, String authorName, String content, MultipartFile cover) throws Exception {
        Post p = get(id);

        if (title != null && !title.isBlank()) p.setTitle(title.trim());
        if (authorName != null && !authorName.isBlank()) p.setAuthorName(authorName.trim());
        if (content != null) p.setContent(content);

        if (cover != null && !cover.isEmpty()) {
            String coverUrl = fileStorageService.savePostCover(cover);
            p.setCoverImage(coverUrl);
        }
        return postRepository.save(p);
    }

    @Transactional
    public void softDelete(Long id) {
        Post p = get(id);
        p.setDeletedAt(LocalDateTime.now());
        postRepository.save(p);
    }

    public PostResponseDTO getPublicDetail(Long id) {
        Post p = postRepository.findDetailWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));
        return toDto(p);
    }

    public PostResponseDTO getAdminDetail(Long id) {
        Post p = postRepository.findDetailWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));
        return toDto(p);
    }

}
