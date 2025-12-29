package com.example.be.controller.product;

import com.example.be.dto.product.DeleteProductsRequestDTO;
import com.example.be.dto.product.ProductDetailResponseDTO;
import com.example.be.dto.product.ProductRequestDTO;
import com.example.be.dto.product.ProductResponseDTO;
import com.example.be.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProductController {

    private final ProductService productService;
    @PostMapping
    public ProductResponseDTO create(@RequestBody ProductRequestDTO request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponseDTO update(
            @PathVariable Long id,
            @RequestBody ProductRequestDTO request
    ) {
        return productService.update(id, request);
    }

    @GetMapping
    public List<ProductResponseDTO> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ProductResponseDTO getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/{id}/detail")
    public ProductDetailResponseDTO getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }


    @GetMapping("/paged")
    public Page<ProductResponseDTO> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(required = false) String category
    ) {
        return productService.getAllPaged(page, size, sort, category);
    }

    @DeleteMapping("/batch")
    public void softDelete(@RequestBody DeleteProductsRequestDTO request) {
        productService.softDeleteMany(request.getIds());
    }

    @PutMapping("/lock")
    public void lockProducts(@RequestBody DeleteProductsRequestDTO request) {
        productService.lockProducts(request.getIds());
    }

    @PutMapping("/unlock")
    public void unlockProducts(@RequestBody DeleteProductsRequestDTO request) {
        productService.unlockProducts(request.getIds());
    }


}