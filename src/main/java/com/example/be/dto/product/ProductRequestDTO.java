package com.example.be.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class ProductRequestDTO {

    private String name;
    private BigDecimal price;
    private String description;
    private String status;
    private List<Long> categoryIds;

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
