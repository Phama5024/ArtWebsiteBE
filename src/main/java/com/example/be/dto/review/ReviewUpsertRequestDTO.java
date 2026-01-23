package com.example.be.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReviewUpsertRequestDTO {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;

    private List<String> images;
}
