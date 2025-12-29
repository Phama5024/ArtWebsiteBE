package com.example.be.dto.product;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class DeleteProductsRequestDTO {
    private List<Long> ids;
}