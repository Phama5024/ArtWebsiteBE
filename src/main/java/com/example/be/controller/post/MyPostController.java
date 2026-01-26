package com.example.be.controller.post;

import com.example.be.dto.post.PostResponseDTO;
import com.example.be.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me/posts")
@RequiredArgsConstructor
public class MyPostController {

    private final PostService postService;

    @PostMapping(consumes = {"multipart/form-data"})
    public PostResponseDTO create(
            Authentication auth,
            @RequestParam("title") String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestPart(value = "cover", required = false) MultipartFile cover
    ) throws Exception {
        return postService.createMine(auth.getName(), title, content, cover);
    }

    @GetMapping
    public Page<PostResponseDTO> list(
            Authentication auth,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.listMine(auth.getName(), keyword, page, size);
    }

    @GetMapping("/{id}")
    public PostResponseDTO detail(Authentication auth, @PathVariable Long id) {
        return postService.getMine(auth.getName(), id);
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public PostResponseDTO update(
            Authentication auth,
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestPart(value = "cover", required = false) MultipartFile cover
    ) throws Exception {
        return postService.updateMine(auth.getName(), id, title, content, cover);
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication auth, @PathVariable Long id) {
        postService.softDeleteMine(auth.getName(), id);
    }
}
