package com.example.be.controller.post;

import com.example.be.entity.Post;
import com.example.be.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PublicPostController {

    private final PostService postService;

    @GetMapping
    public Page<Post> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.list(keyword, page, size);
    }

    @GetMapping("/{id}")
    public Post detail(@PathVariable Long id) {
        return postService.get(id);
    }
}
