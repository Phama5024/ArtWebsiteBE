package com.example.be.service.category;

import com.example.be.dto.category.CategoryResponseDTO;
import java.util.List;

public interface CategoryService {
    List<CategoryResponseDTO> getAll();
}
