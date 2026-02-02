package com.example.be.dto.commission;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommissionDeliverableResponseDTO {
    private Long id;
    private String fileUrl;
    private String originalName;
    private String contentType;
    private Long size;
    private LocalDateTime createdAt;
}
