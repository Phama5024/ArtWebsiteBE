package com.example.be.service.product;

import com.example.be.dto.product.ProductDetailResponseDTO;
import com.example.be.dto.product.ProductRequestDTO;
import com.example.be.dto.product.ProductResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    ProductResponseDTO create(ProductRequestDTO request);
    ProductResponseDTO update(Long id, ProductRequestDTO request);
    List<ProductResponseDTO> getAll();
    ProductResponseDTO getById(Long id);
    ProductDetailResponseDTO getProductDetail(Long id);
    Page<ProductResponseDTO> getAllPaged(
            int page,
            int size,
            String sort,
            String category
    );
    void softDeleteMany(List<Long> ids);
    void lockProducts(List<Long> ids);
    void unlockProducts(List<Long> ids);

}
