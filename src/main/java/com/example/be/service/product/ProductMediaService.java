package com.example.be.service.product;

import com.example.be.dto.product.ProductMediaDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProductMediaService {
    ProductMediaDTO uploadProductImage(Long productId, MultipartFile file, boolean isPrimary);
}