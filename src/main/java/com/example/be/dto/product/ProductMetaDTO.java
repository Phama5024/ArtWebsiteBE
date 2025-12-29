package com.example.be.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductMetaDTO {
    private String fileFormat;
    private String resolution;
    private String aspectRatio;
    private String fileSize;
    private String author;
    private String style;
    private String origin;
    private String characterName;
    private String extraInfo;
}
