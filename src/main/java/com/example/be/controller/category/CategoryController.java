package com.example.be.controller.category;

import com.example.be.dto.category.CategoryResponseDTO;
import com.example.be.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDTO> getAll() {
        return categoryService.getAll();
    }
}
