package com.example.be.controller.post;

import com.example.be.entity.Post;
import com.example.be.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final PostService postService;

    @PostMapping(consumes = {"multipart/form-data"})
    public Post create(
            @RequestParam("title") String title,
            @RequestParam("author_name") String authorName,
            @RequestParam(value = "content", required = false) String content,
            @RequestPart(value = "cover", required = false) MultipartFile cover
    ) throws Exception {
        return postService.create(title, authorName, content, cover);
    }

    @GetMapping
    public Page<Post> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.list(keyword, page, size);
    }

    @GetMapping("/{id}")
    public Post detail(@PathVariable Long id) {
        return postService.get(id);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public Post update(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author_name", required = false) String authorName,
            @RequestParam(value = "content", required = false) String content,
            @RequestPart(value = "cover", required = false) MultipartFile cover
    ) throws Exception {
        return postService.update(id, title, authorName, content, cover);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        postService.softDelete(id);
    }
}
