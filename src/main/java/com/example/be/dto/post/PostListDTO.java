package com.example.be.dto.post;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PostListDTO {
    private Long id;
    private String title;
    private String authorName;
    private String content;
    private String coverImage;
    private String status;
    private LocalDateTime createdAt;

    private Long userId;
    private String fullName;
}
