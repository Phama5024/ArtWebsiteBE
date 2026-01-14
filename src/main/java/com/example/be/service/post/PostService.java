package com.example.be.service.post;

import com.example.be.entity.Post;
import com.example.be.repository.post.PostRepository;
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

    public Page<Post> list(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
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
}
