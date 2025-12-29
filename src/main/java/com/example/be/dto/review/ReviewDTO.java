package com.example.be.dto.review;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewDTO {

    private Long id;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    private List<String> images;
}
